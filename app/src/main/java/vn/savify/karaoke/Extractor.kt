package vn.savify.karaoke

import okhttp3.OkHttpClient
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.InfoItem
import org.schabi.newpipe.extractor.ListExtractor
import org.schabi.newpipe.extractor.MediaFormat
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.search.SearchExtractor
import org.schabi.newpipe.extractor.stream.DeliveryMethod
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType
import org.schabi.newpipe.extractor.stream.VideoStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import okhttp3.Request as OkRequest

/** HTTP client used by NewPipe Extractor. */
class NpDownloader : Downloader() {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    override fun execute(request: Request): Response {
        val method = request.httpMethod()
        val url = request.url()
        val data = request.dataToSend()
        val body = if (data != null) data.toRequestBody()
        else if (method == "POST" || method == "PUT") ByteArray(0).toRequestBody()
        else null

        val rb = OkRequest.Builder().method(method, body).url(url)
            .addHeader("User-Agent", Util.UA)
        for ((name, values) in request.headers()) {
            rb.removeHeader(name)
            for (v in values) rb.addHeader(name, v)
        }
        val resp = client.newCall(rb.build()).execute()
        if (resp.code == 429) {
            resp.close()
            throw ReCaptchaException("reCaptcha Challenge requested", url)
        }
        val text = resp.body?.string()
        val finalUrl = resp.request.url.toString()
        return Response(resp.code, resp.message, resp.headers.toMultimap(), text, finalUrl)
    }
}

/** Phuong an du phong khi NewPipe loi: doc trang ket qua YouTube va oEmbed bang OkHttp (khong dung NewPipe/Jsoup). */
object YtScrape {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
    private val cache = ConcurrentHashMap<String, Pair<Long, List<SearchHit>>>()

    private fun get(url: String): String {
        val rq = OkRequest.Builder().url(url)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36")
            .header("Accept-Language", "vi-VN,vi;q=0.9,en;q=0.6")
            .header("Cookie", "CONSENT=YES+1; SOCS=CAI")
            .build()
        client.newCall(rq).execute().use { r ->
            if (!r.isSuccessful) throw java.io.IOException("HTTP " + r.code)
            return r.body?.string() ?: throw java.io.IOException("empty body")
        }
    }

    private fun parseDur(t: String?): Int {
        if (t == null) return -1
        var total = 0
        for (part in t.split(':')) total = total * 60 + (part.trim().toIntOrNull() ?: return -1)
        return total
    }

    private fun walk(o: Any?, out: ArrayList<SearchHit>, seen: HashSet<String>) {
        if (out.size >= 30) return
        when (o) {
            is org.json.JSONObject -> {
                val it = o.keys()
                while (it.hasNext()) {
                    val k = it.next()
                    val v = o.opt(k)
                    if (k == "videoRenderer" && v is org.json.JSONObject) {
                        val id = v.optString("videoId")
                        val dur = parseDur(v.optJSONObject("lengthText")?.optString("simpleText"))
                        if (Util.ID_RE.matches(id) && dur > 60 && seen.add(id)) {
                            val title = v.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: ""
                            val ch = v.optJSONObject("ownerText")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: ""
                            out.add(SearchHit(id, title, ch, dur))
                        }
                    } else walk(v, out, seen)
                }
            }
            is org.json.JSONArray -> for (i in 0 until o.length()) walk(o.opt(i), out, seen)
        }
    }

    /** Tim bang API noi bo cua YouTube (InnerTube) - on dinh hon viec doc HTML. */
    private fun innertube(q: String): List<SearchHit> {
        val ver = "2.20260105.01.00"
        val body = org.json.JSONObject()
            .put("context", org.json.JSONObject().put("client", org.json.JSONObject()
                .put("clientName", "WEB").put("clientVersion", ver).put("hl", "vi").put("gl", "VN")))
            .put("query", q)
            .put("params", "EgIQAQ==")
        val rq = OkRequest.Builder().url("https://www.youtube.com/youtubei/v1/search?prettyPrint=false")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36")
            .header("Origin", "https://www.youtube.com")
            .header("X-YouTube-Client-Name", "1")
            .header("X-YouTube-Client-Version", ver)
            .header("Cookie", "CONSENT=YES+1; SOCS=CAI")
            .build()
        client.newCall(rq).execute().use { r ->
            if (!r.isSuccessful) throw java.io.IOException("HTTP " + r.code)
            val root = org.json.JSONObject(r.body?.string() ?: throw java.io.IOException("empty body"))
            val out = ArrayList<SearchHit>()
            walk(root, out, HashSet())
            return out
        }
    }

    /** Doc trang ket qua HTML (phuong an cuoi cung). */
    private fun htmlSearch(q: String): List<SearchHit> {
        val url = "https://www.youtube.com/results?search_query=" + java.net.URLEncoder.encode(q, "UTF-8") +
            "&sp=EgIQAQ%3D%3D&hl=vi&gl=VN"
        val html = get(url)
        var key = "ytInitialData = "
        var i = html.indexOf(key)
        if (i < 0) {
            key = "ytInitialData\"] = "
            i = html.indexOf(key)
        }
        if (i < 0) throw java.io.IOException("ytInitialData not found")
        val start = i + key.length
        val end = html.indexOf(";</script>", start).takeIf { it > 0 } ?: throw java.io.IOException("bad page")
        val root = org.json.JSONObject(html.substring(start, end))
        val out = ArrayList<SearchHit>()
        walk(root, out, HashSet())
        return out
    }

    fun search(q: String): List<SearchHit> {
        val c = cache[q]
        if (c != null && System.currentTimeMillis() - c.first < 5 * 60_000) return c.second
        var out: List<SearchHit> = emptyList()
        try {
            out = innertube(q)
        } catch (e: Throwable) {
        }
        if (out.isEmpty()) out = htmlSearch(q)
        cache[q] = Pair(System.currentTimeMillis(), out)
        return out
    }

    /** Tieu de + kenh qua oEmbed (nhe, on dinh). */
    fun oembed(id: String): Pair<String, String>? = try {
        val j = org.json.JSONObject(get("https://www.youtube.com/oembed?format=json&url=" +
            java.net.URLEncoder.encode("https://www.youtube.com/watch?v=$id", "UTF-8")))
        Pair(j.optString("title"), j.optString("author_name"))
    } catch (e: Throwable) {
        null
    }
}

/** Bien LinkageError/NoSuchMethodError (thu vien khong hop Android cu) thanh Exception thuong de app khong sap, loi duoc ghi lai. */
inline fun <T> guard(block: () -> T): T {
    try {
        return block()
    } catch (e: LinkageError) {
        CrashLog.soft("LINKAGE", e)
        throw java.io.IOException("LinkageError: " + e.javaClass.simpleName + ": " + e.message, e)
    }
}

object Np {
    private var done = false

    @Synchronized
    fun init(lang: String = "vi", country: String = "VN") {
        if (done) return
        NewPipe.init(NpDownloader(), Localization(lang, country), ContentCountry(country))
        done = true
    }
}

class UnavailableException(msg: String) : Exception(msg)

/** One way to play a video: a single progressive URL, or video-only + audio-only to merge. */
data class Src(val video: String, val audio: String?)

data class Resolved(
    val sources: List<Src>,
    val expiresAt: Long,
    val title: String,
    val channel: String,
    val durationSec: Int
)

object Resolver {
    private val cache = ConcurrentHashMap<String, Resolved>()
    private val expireRe = Regex("[?&]expire=(\\d+)")

    fun cached(id: String, maxH: Int): Resolved? {
        val key = "$id|$maxH"
        val r = cache[key] ?: return null
        if (System.currentTimeMillis() > r.expiresAt - 60_000) {
            cache.remove(key)
            return null
        }
        return r
    }

    fun invalidate(id: String) {
        cache.keys.removeAll { it.startsWith("$id|") }
    }

    private fun heightOf(s: VideoStream): Int =
        Regex("\\d+").find(s.resolution ?: "")?.value?.toIntOrNull() ?: 0

    /** Blocking. Call from a background thread. */
    fun resolve(videoId: String, maxH: Int): Resolved = guard { resolveImpl(videoId, maxH) }

    private fun resolveImpl(videoId: String, maxH: Int): Resolved {
        cached(videoId, maxH)?.let { return it }
        val info = StreamInfo.getInfo("https://www.youtube.com/watch?v=$videoId")
        val prog = DeliveryMethod.PROGRESSIVE_HTTP

        val muxed = info.videoStreams.filter { it.deliveryMethod == prog && !it.content.isNullOrEmpty() }
        val videoOnly = info.videoOnlyStreams.filter {
            it.deliveryMethod == prog && it.format == MediaFormat.MPEG_4 && !it.content.isNullOrEmpty()
        }
        val audios = info.audioStreams.filter { it.deliveryMethod == prog && !it.content.isNullOrEmpty() }
        val audio = audios.filter { it.format == MediaFormat.M4A }.maxByOrNull { it.averageBitrate }
            ?: audios.maxByOrNull { it.averageBitrate }

        val bestMuxed = muxed.filter { heightOf(it) <= maxH }.maxByOrNull { heightOf(it) }
            ?: muxed.minByOrNull { heightOf(it) }
        val bestVideo = videoOnly.filter { heightOf(it) <= maxH }.maxByOrNull { heightOf(it) }

        val out = ArrayList<Src>()
        if (bestVideo != null && audio != null &&
            (bestMuxed == null || heightOf(bestVideo) > heightOf(bestMuxed))
        ) {
            out.add(Src(bestVideo.content, audio.content))
        }
        if (bestMuxed != null) out.add(Src(bestMuxed.content, null))
        if (out.isEmpty()) throw UnavailableException("No playable stream")

        val exp = expireRe.find(out[0].video)?.groupValues?.get(1)?.toLongOrNull()?.times(1000)
            ?: (System.currentTimeMillis() + 5 * 60_000)
        val r = Resolved(out, exp, info.name ?: "", info.uploaderName ?: "", info.duration.toInt())
        cache["$videoId|$maxH"] = r
        return r
    }

    /** Link kenh cua video (de mo kenh tren dien thoai). */
    fun channelUrl(videoId: String): String? = try {
        StreamInfo.getInfo("https://www.youtube.com/watch?v=$videoId").uploaderUrl?.takeIf { it.isNotEmpty() }
    } catch (e: Throwable) {
        null
    }

    /** Title/channel/duration only (used by "paste link"). Returns null on failure. */
    fun meta(videoId: String): Resolved? = try {
        val info = StreamInfo.getInfo("https://www.youtube.com/watch?v=$videoId")
        Resolved(emptyList(), 0, info.name ?: "", info.uploaderName ?: "", info.duration.toInt())
    } catch (e: Throwable) {
        YtScrape.oembed(videoId)?.let { Resolved(emptyList(), 0, it.first, it.second, 0) }
    }
}

data class SearchHit(val videoId: String, val title: String, val channel: String, val durationSec: Int)

object SearchService {
    private class Sess(val ex: SearchExtractor) {
        val created = System.currentTimeMillis()
        var started = false
        var done = false
        var next: Page? = null
        val pages = ArrayList<List<SearchHit>>()
    }

    private val cache = object : LinkedHashMap<String, Sess>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Sess>?): Boolean = size > 30
    }

    private fun newExtractor(q: String): SearchExtractor {
        val svc = ServiceList.YouTube
        val handler = svc.searchQHFactory.fromQuery(q, listOf("videos"), "")
        return svc.getSearchExtractor(handler)
    }

    private fun toHit(it: InfoItem): SearchHit? {
        if (it !is StreamInfoItem) return null
        if (it.streamType != StreamType.VIDEO_STREAM) return null
        val dur = it.duration.toInt()
        if (dur in 0..60) return null // shorts / unknown
        val id = Util.parseVideoId(it.url ?: return null) ?: return null
        return SearchHit(id, it.name ?: "", it.uploaderName ?: "", dur)
    }

    /** Blocking. Returns (hits for that page, hasMore). */
    fun search(query: String, page: Int, karaoke: Boolean): Pair<List<SearchHit>, Boolean> {
        return try {
            guard { searchImpl(query, page, karaoke) }
        } catch (e: Throwable) {
            // NewPipe loi (YouTube doi giao dien, thu vien khong hop may...) -> dung phuong an du phong
            CrashLog.soft("SEARCH-NEWPIPE", e)
            var q = query.trim()
            val low = q.lowercase()
            if (karaoke && !low.contains("karaoke") && !low.contains("beat")) q += " karaoke"
            val all = YtScrape.search(q)
            val from = page * 10
            val list = if (from < all.size) all.subList(from, minOf(all.size, from + 10)) else emptyList()
            Pair(list, from + 10 < all.size)
        }
    }

    private fun searchImpl(query: String, page: Int, karaoke: Boolean): Pair<List<SearchHit>, Boolean> {
        var q = query.trim()
        val low = q.lowercase()
        if (karaoke && !low.contains("karaoke") && !low.contains("beat")) q += " karaoke"
        val key = q
        val s = synchronized(cache) {
            var e = cache[key]
            if (e == null || System.currentTimeMillis() - e.created > 5 * 60_000) {
                e = Sess(newExtractor(q))
                cache[key] = e
            }
            e
        }
        synchronized(s) {
            while (s.pages.size <= page && !s.done) {
                val p: ListExtractor.InfoItemsPage<InfoItem> = if (!s.started) {
                    s.ex.fetchPage()
                    s.started = true
                    s.ex.initialPage
                } else {
                    s.ex.getPage(s.next!!)
                }
                s.next = p.nextPage
                if (s.next == null) s.done = true
                s.pages.add(p.items.mapNotNull { toHit(it) })
            }
            val list = s.pages.getOrNull(page) ?: emptyList()
            return Pair(list, s.pages.size > page + 1 || !s.done)
        }
    }
}
