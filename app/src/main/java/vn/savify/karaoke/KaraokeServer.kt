package vn.savify.karaoke

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

private class St(val code: Int, val desc: String) : NanoHTTPD.Response.IStatus {
    override fun getRequestStatus(): Int = code
    override fun getDescription(): String = "$code $desc"
}

class KaraokeServer(
    port: Int,
    private val ctx: Context,
    private val ctl: Controller,
    private val settings: Settings
) : NanoHTTPD(port) {

    @Volatile
    var lastRemoteSeen = 0L

    private val failures = ConcurrentHashMap<String, ArrayList<Long>>()
    private val idem = ConcurrentHashMap<String, Pair<Long, String>>()
    private val reactTimes = java.util.ArrayDeque<Long>()
    @Volatile private var lastMsgAt = 0L

    private val OK = Response.Status.OK

    override fun serve(session: IHTTPSession): Response {
        val resp = try {
            route(session)
        } catch (e: ApiError) {
            err(e.status, e.code, e.message ?: "")
        } catch (e: Exception) {
            err(500, "INTERNAL", e.message ?: "error")
        }
        resp.addHeader("Cache-Control", "no-store")
        resp.addHeader("X-Content-Type-Options", "nosniff")
        resp.addHeader("Referrer-Policy", "no-referrer")
        resp.addHeader("X-Frame-Options", "DENY")
        resp.addHeader(
            "Content-Security-Policy",
            "default-src 'none'; script-src 'self'; style-src 'self'; " +
                "img-src 'self' https://i.ytimg.com data:; connect-src 'self'; " +
                "manifest-src 'self'; base-uri 'none'; frame-ancestors 'none'; form-action 'none'"
        )
        return resp
    }

    // ------------------------------------------------------------------ helpers

    private fun statusFor(code: Int): Response.IStatus = when (code) {
        200 -> Response.Status.OK
        400 -> Response.Status.BAD_REQUEST
        403 -> Response.Status.FORBIDDEN
        404 -> Response.Status.NOT_FOUND
        else -> St(code, "Error")
    }

    private fun json(o: Any, status: Response.IStatus = Response.Status.OK): Response =
        newFixedLengthResponse(status, "application/json; charset=utf-8", o.toString())

    private fun err(status: Int, code: String, msg: String): Response {
        val o = JSONObject().put("error", JSONObject().put("code", code).put("message", msg))
        val r = json(o, statusFor(status))
        if (status == 429) r.addHeader("Retry-After", "30")
        return r
    }

    private fun mime(name: String): String = when {
        name.endsWith(".html") -> "text/html; charset=utf-8"
        name.endsWith(".js") -> "application/javascript; charset=utf-8"
        name.endsWith(".css") -> "text/css; charset=utf-8"
        name.endsWith(".png") -> "image/png"
        name.endsWith(".json") || name.endsWith(".webmanifest") -> "application/manifest+json; charset=utf-8"
        else -> "application/octet-stream"
    }

    private fun asset(name: String): Response {
        if (name.contains("..")) throw ApiError(404, "NOT_FOUND", "Not found")
        return try {
            newChunkedResponse(Response.Status.OK, mime(name), ctx.assets.open("web/$name"))
        } catch (e: IOException) {
            throw ApiError(404, "NOT_FOUND", "Not found")
        }
    }

    private fun cookieOf(s: IHTTPSession): String? {
        val c = s.headers["cookie"] ?: return null
        for (p in c.split(';')) {
            val t = p.trim()
            if (t.startsWith("kt=")) return t.substring(3)
        }
        return null
    }

    private fun tokenOf(s: IHTTPSession): String? {
        val q = s.parms["token"]
        if (!q.isNullOrEmpty()) return q
        val a = s.headers["authorization"]
        if (a != null && a.startsWith("Bearer ")) return a.substring(7).trim()
        return cookieOf(s)
    }

    private fun blocked(ip: String): Boolean {
        val l = failures[ip] ?: return false
        val now = System.currentTimeMillis()
        synchronized(l) {
            l.removeAll { now - it > 60_000 }
            return l.size >= 10
        }
    }

    private fun noteFailure(ip: String) {
        val l = failures.getOrPut(ip) { ArrayList() }
        synchronized(l) { l.add(System.currentTimeMillis()) }
    }

    private fun setCookie(r: Response) {
        r.addHeader("Set-Cookie", "kt=${settings.token}; HttpOnly; SameSite=Strict; Path=/; Max-Age=2592000")
    }

    private fun body(s: IHTTPSession): JSONObject {
        val ct = (s.headers["content-type"] ?: "").lowercase()
        if (!ct.startsWith("application/json")) {
            throw ApiError(415, "UNSUPPORTED_MEDIA_TYPE", "Use application/json")
        }
        // NanoHTTPD doc body theo US-ASCII neu thieu charset -> tieng Viet thanh dau ?. Ep UTF-8.
        if (!ct.contains("charset")) s.headers["content-type"] = (s.headers["content-type"] ?: "application/json") + "; charset=utf-8"
        val len = s.headers["content-length"]?.toLongOrNull() ?: 0L
        if (len > 8192) throw ApiError(413, "BODY_TOO_LARGE", "Body too large")
        val files = HashMap<String, String>()
        try {
            s.parseBody(files)
        } catch (e: Exception) {
            throw ApiError(400, "INVALID_BODY", "Bad body")
        }
        val txt = files["postData"] ?: ""
        return try {
            JSONObject(if (txt.isBlank()) "{}" else txt)
        } catch (e: Exception) {
            throw ApiError(400, "INVALID_BODY", "Bad JSON")
        }
    }

    private fun snapshot(): JSONObject = ctl.onMain { ctl.snapshot() }

    private fun reply(item: QueueItem? = null): Response {
        val o = JSONObject().put("state", snapshot())
        if (item != null) o.put("item", item.toJson())
        return json(o)
    }

    // ------------------------------------------------------------------ routing

    private fun route(s: IHTTPSession): Response {
        val uri = s.uri
        val method = s.method

        if (uri.startsWith("/static/")) return asset(uri.removePrefix("/static/"))
        if (uri == "/manifest.webmanifest") return asset("manifest.webmanifest")
        if (uri == "/favicon.ico") return asset("icon.png")

        val ip = s.headers["remote-addr"] ?: "?"
        if (blocked(ip)) throw ApiError(429, "RATE_LIMITED", "Too many attempts")
        val ok = Util.constEq(tokenOf(s), settings.token)
        if (!ok) {
            noteFailure(ip)
            if (uri == "/") {
                val page = "<!doctype html><meta charset=utf-8><meta name=viewport content='width=device-width'>" +
                    "<body style='font:20px sans-serif;padding:24px'><h2>Savify Karaoke</h2>" +
                    "<p>Hãy quét mã QR trên TV để kết nối.<br>Scan the QR code on the TV.</p></body>"
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html; charset=utf-8", page)
            }
            throw ApiError(403, "UNAUTHORIZED", "Unauthorized")
        }

        if (uri == "/" && method == Method.GET) {
            val r = asset("index.html")
            setCookie(r)
            return r
        }
        if (!uri.startsWith("/api/")) throw ApiError(404, "NOT_FOUND", "Not found")

        val write = method == Method.POST || method == Method.DELETE
        if (write) {
            val origin = s.headers["origin"]
            if (origin != null && origin != "http://${s.headers["host"]}") {
                throw ApiError(403, "BAD_ORIGIN", "Bad origin")
            }
        }
        val dev: String? = s.headers["x-device"]?.let {
            try {
                java.net.URLDecoder.decode(it, "UTF-8").take(40)
            } catch (e: Exception) {
                null
            }
        }
        if (s.headers["x-remote-heartbeat"] != null || uri == "/api/state") {
            lastRemoteSeen = System.currentTimeMillis()
        }

        val parts = uri.removePrefix("/api/").split('/').filter { it.isNotEmpty() }
        val p0 = parts.getOrNull(0) ?: ""
        val p1 = parts.getOrNull(1)
        val p2 = parts.getOrNull(2)

        when {
            method == Method.GET && p0 == "state" -> return reply()

            method == Method.GET && p0 == "search" -> return search(s)

            method == Method.GET && p0 == "channel-url" -> {
                val id = Util.parseVideoId(s.parms["videoId"] ?: "")
                    ?: throw ApiError(400, "INVALID_URL", "Bad video id")
                return json(JSONObject().put("url", Resolver.channelUrl(id) ?: JSONObject.NULL))
            }

            method == Method.POST && p0 == "resolve-url" -> {
                val b = body(s)
                val id = Util.parseVideoId(b.optString("url", ""))
                    ?: throw ApiError(400, "INVALID_URL", "Paste a valid YouTube video link")
                val m = Resolver.meta(id)
                val o = JSONObject().put("videoId", id)
                    .put("title", if (m != null && m.title.isNotEmpty()) m.title else "YouTube · $id")
                    .put("channel", m?.channel ?: "")
                    .put("thumbnailUrl", Util.thumb(id))
                    .put("durationSec", m?.durationSec ?: 0)
                return json(o)
            }

            method == Method.POST && p0 == "queue" && p1 == null -> {
                val b = body(s)
                val key = s.headers["idempotency-key"]
                if (key != null) {
                    val prev = idem[key]
                    if (prev != null && System.currentTimeMillis() - prev.first < 60_000) {
                        return newFixedLengthResponse(OK, "application/json; charset=utf-8", prev.second)
                    }
                }
                var raw = b.optString("videoId", "")
                if (raw.isEmpty()) raw = b.optString("url", "")
                val id = Util.parseVideoId(raw)
                    ?: throw ApiError(400, "INVALID_URL", "Paste a valid YouTube video link")
                val title = if (b.has("title")) b.optString("title") else null
                val channel = if (b.has("channel")) b.optString("channel") else null
                val dur = b.optInt("durationSec", 0)
                val position = b.optString("position", "end")
                val force = b.optBoolean("force", false)
                val item = ctl.onMain {
                    ctl.noteActor(dev)
                    ctl.add(id, title, channel, dur, b.optString("addedBy", dev ?: ""), position, force)
                }
                val o = JSONObject().put("state", snapshot()).put("item", item.toJson())
                val txt = o.toString()
                if (key != null) {
                    idem[key] = Pair(System.currentTimeMillis(), txt)
                    val now = System.currentTimeMillis()
                    idem.entries.removeAll { now - it.value.first > 60_000 }
                }
                return newFixedLengthResponse(OK, "application/json; charset=utf-8", txt)
            }

            method == Method.POST && p0 == "queue" && p1 == "reorder" -> {
                val b = body(s)
                val itemId = b.optString("itemId")
                val idx = b.optInt("newIndex", 0)
                ctl.onMain { ctl.noteActor(dev); ctl.reorder(itemId, idx) }
                return reply()
            }

            method == Method.DELETE && p0 == "queue" && p1 != null -> {
                ctl.onMain { ctl.noteActor(dev); ctl.remove(p1) }
                return reply()
            }

            method == Method.POST && p0 == "queue" && p1 != null && p2 == "prioritize" -> {
                body(s)
                ctl.onMain { ctl.noteActor(dev); ctl.prioritize(p1) }
                return reply()
            }

            method == Method.POST && p0 == "queue" && p1 != null && p2 == "play" -> {
                body(s)
                ctl.onMain { ctl.noteActor(dev); ctl.playNow(p1) }
                return reply()
            }

            method == Method.POST && p0 == "player" -> {
                val b = body(s)
                var vol: Int? = null
                when (p1) {
                    "skip" -> ctl.onMain { ctl.noteActor(dev); ctl.skip() }
                    "pause" -> ctl.onMain { ctl.noteActor(dev); ctl.pause() }
                    "resume" -> ctl.onMain { ctl.noteActor(dev); ctl.resume() }
                    "restart" -> ctl.onMain { ctl.noteActor(dev); ctl.restart() }
                    "seek" -> ctl.onMain {
                        ctl.noteActor(dev)
                        ctl.seekTo(if (b.has("positionMs")) b.optLong("positionMs") else null, b.optLong("deltaMs", 0L))
                    }
                    "volume" -> vol = ctl.onMain {
                        if (b.has("level")) ctl.setVolumeAbs(b.optInt("level"))
                        else ctl.changeVolume(b.optInt("delta", 0).coerceIn(-100, 100))
                    }
                    else -> throw ApiError(404, "NOT_FOUND", "Not found")
                }
                val o = JSONObject().put("state", snapshot())
                if (vol != null) o.put("level", vol)
                return json(o)
            }

            method == Method.POST && p0 == "react" -> {
                val b = body(s)
                val id = b.optString("id", "")
                if (!ReactIds.ALL.contains(id)) throw ApiError(400, "INVALID_REACTION", "Unknown reaction")
                // gioi han ~12 hieu ung/giay de TV khong bi qua tai khi bam lien tuc
                val now = System.currentTimeMillis()
                var pass = true
                synchronized(reactTimes) {
                    while (reactTimes.isNotEmpty() && now - reactTimes.first() > 1000) reactTimes.removeFirst()
                    if (reactTimes.size >= 12) pass = false else reactTimes.addLast(now)
                }
                if (pass) ctl.react(id)
                return json(JSONObject().put("ok", true).put("dropped", !pass))
            }

            method == Method.POST && p0 == "message" -> {
                val b = body(s)
                // bo ky tu dieu khien, gop khoang trang, toi da 80 ky tu
                val text = b.optString("text", "").replace(Regex("[\\p{Cntrl}\\u200B-\\u200F\\u202A-\\u202E]"), " ")
                    .replace(Regex("\\s+"), " ").trim().take(80)
                if (text.isEmpty()) throw ApiError(400, "EMPTY_MESSAGE", "Empty message")
                val now = System.currentTimeMillis()
                // gioi han: toi da 1 tin / 1,5 giay de khong bi spam
                val pass = synchronized(this) { if (now - lastMsgAt < 1500) false else { lastMsgAt = now; true } }
                if (pass) ctl.message((dev ?: "").trim().take(24), text)
                return json(JSONObject().put("ok", true).put("dropped", !pass))
            }

            method == Method.POST && p0 == "settings" -> {
                val b = body(s)
                if (b.has("maxHeight")) {
                    val h = b.optInt("maxHeight", 720)
                    ctl.onMain { ctl.noteActor(dev); ctl.setMaxHeight(h) }
                }
                return reply()
            }

            method == Method.POST && p0 == "pairing" && p1 == "reset" -> {
                body(s)
                settings.token = Util.newToken()
                val r = json(JSONObject().put("token", settings.token).put("ok", true))
                setCookie(r)
                return r
            }
        }
        throw ApiError(404, "NOT_FOUND", "Not found")
    }

    private fun norm(x: String): String =
        java.text.Normalizer.normalize(x.lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "").replace('\u0111', 'd')

    /** Tim theo kenh: lay toi da ~5 trang ket qua roi chi giu video cua kenh co ten khop. */
    private fun searchByChannel(q: String, chan: String, page: Int, karaoke: Boolean): Pair<List<SearchHit>, Boolean> {
        if (page > 0) return Pair(emptyList(), false)
        val tokens = norm(chan).split(Regex("\\s+")).filter { it.isNotEmpty() }
        val out = ArrayList<SearchHit>()
        var p = 0
        while (p < 5) {
            val r = SearchService.search(q, p, karaoke)
            for (h in r.first) {
                val c = norm(h.channel)
                if (tokens.all { c.contains(it) } && out.none { it.videoId == h.videoId }) out.add(h)
            }
            if (!r.second || out.size >= 20) break
            p++
        }
        return Pair(out, false)
    }

    private fun search(s: IHTTPSession): Response {
        val q = (s.parms["q"] ?: "").trim()
        if (q.isEmpty() || q.length > 200) throw ApiError(400, "INVALID_QUERY", "Query must be 1-200 chars")
        val page = (s.parms["page"] ?: "0").toIntOrNull()?.coerceIn(0, 20) ?: 0
        val karaoke = (s.parms["karaoke"] ?: "1") != "0"
        val chan = (s.parms["channel"] ?: "").trim().take(100)
        val res = try {
            if (chan.isNotEmpty()) searchByChannel(q, chan, page, karaoke) else SearchService.search(q, page, karaoke)
        } catch (e: Throwable) {
            CrashLog.soft("SEARCH", e)
            throw ApiError(502, "UPSTREAM_FAILED", "Search failed: " + (e.message ?: e.javaClass.simpleName).take(160))
        }
        val queued = ctl.onMain { ctl.pendingVideoIds() }
        val arr = JSONArray()
        for (h in res.first) {
            arr.put(
                JSONObject().put("videoId", h.videoId).put("title", h.title).put("channel", h.channel)
                    .put("thumbnailUrl", Util.thumb(h.videoId)).put("durationSec", h.durationSec)
                    .put("alreadyQueued", queued.contains(h.videoId))
            )
        }
        return json(JSONObject().put("results", arr).put("page", page).put("hasMore", res.second))
    }

    companion object {
        fun startOnFreePort(ctx: Context, ctl: Controller, settings: Settings): KaraokeServer {
            for (p in 8080..8099) {
                try {
                    val srv = KaraokeServer(p, ctx, ctl, settings)
                    srv.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                    return srv
                } catch (e: IOException) {
                    // port busy, try next
                }
            }
            throw IOException("No free port in 8080..8099")
        }
    }
}
