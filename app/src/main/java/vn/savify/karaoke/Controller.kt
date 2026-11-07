package vn.savify.karaoke

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit

/** Implemented by the Activity (WebView + YouTube IFrame player, "Backend B"). */
interface IframeHost {
    fun load(videoId: String, key: String, startSec: Int)
    fun pause()
    fun resume()
    fun seek(ms: Long)
    fun setVolume(v: Int)
    fun stop()
}

data class UiState(
    val seq: Int = 0,
    val state: PState = PState.IDLE,
    val nowTitle: String? = null,
    val nextText: String? = null,
    val iframe: Boolean = false,
    val noCurrent: Boolean = true,
    val message: String? = null
)

/**
 * Owns the queue + playback state machine. EVERYTHING here runs on the main thread.
 * HTTP threads must go through [onMain].
 */
class Controller(
    private val app: Context,
    val settings: Settings,
    private val scope: CoroutineScope
) {
    private val handler = Handler(Looper.getMainLooper())
    val ui = MutableStateFlow(UiState())

    /** Hieu ung cam xuc (tha tim, vo tay, phao giay...) tu remote -> TV. An toan goi tu luong HTTP. */
    val reactions = MutableSharedFlow<String>(extraBufferCapacity = 32, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    fun react(id: String) { reactions.tryEmit(id) }

    /** Moi lan doi am luong tu remote -> TV hien thanh am luong (gia tri 0..100). */
    val volumeEvents = MutableSharedFlow<Int>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /** Tin nhan chu chay tu remote -> man hinh TV (nguoi gui, noi dung). */
    val messages = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    fun message(sender: String, text: String) { messages.tryEmit(Pair(sender, text)) }

    val player: ExoPlayer = ExoPlayer.Builder(app)
        .setLoadControl(
            DefaultLoadControl.Builder()
                // dem nhieu hon (30-60 giay) de khong bi dung khi mang chap chon; bat dau/tiep tuc phat som hon
                .setBufferDurationsMs(30_000, 60_000, 2_000, 4_000)
                .setPrioritizeTimeOverSizeThresholds(true)
                .build()
        )
        .build()

    // ---- state ----
    private val queue = ArrayList<QueueItem>()
    private var current: QueueItem? = null
    private var state = PState.IDLE
    private var queueVersion = 0
    private var volume = 100
    private var seq = 0

    private var sources: List<Src> = emptyList()
    private var srcIdx = 0
    private var lastItemId: String? = null
    private var reresolved = false
    private var stallRetries = 0
    private var consecutiveFails = 0
    private var exoFailStreak = 0
    private var halted = false
    private var userPaused = false
    private var autoPaused = false
    private var forceIframeItem: String? = null
    private var fallbackHint = false
    private var netWait = false
    private var lastFailInfo: Throwable? = null
    private var failScreenShown = false

    private var resolveJob: Job? = null
    private var retryJob: Job? = null
    private var prefetchedId: String? = null

    // iframe backend
    var iframeHost: IframeHost? = null
    private var usingIframe = false
    private var iframePos = 0L
    private var iframeDur = 0L

    // seed
    private val seedPool = ArrayList<QueueItem>()
    private var seedFilling = false
    private var lastSeedId: String? = null

    // errors / actors
    private var errorSeq = 0
    private var errCode = ""
    private var errMsg = ""
    private var lastActor: String? = null
    private var lastActorAt = 0L

    // watchdog
    private var lastPos = -1L
    private var lastPosChangeAt = 0L

    private val connectivity = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // ------------------------------------------------------------------ threading

    fun <T> onMain(block: () -> T): T {
        if (Looper.myLooper() == Looper.getMainLooper()) return block()
        val task = FutureTask<T>(Callable { block() })
        handler.post(task)
        try {
            return task.get(8, TimeUnit.SECONDS)
        } catch (e: ExecutionException) {
            throw (e.cause ?: e)
        }
    }

    private val tickRunnable = object : Runnable {
        override fun run() {
            try {
                tick()
            } catch (e: Exception) {
                // never let the watchdog die
            }
            handler.postDelayed(this, 1000)
        }
    }

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (usingIframe) return
                if (playbackState == Player.STATE_READY && state == PState.RESOLVING && current != null) {
                    onStarted()
                } else if (playbackState == Player.STATE_ENDED) {
                    trackEnded()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                handleExoError(error)
            }
        })
    }

    fun start() {
        restore()
        handler.postDelayed(tickRunnable, 1000)
        startNext()
    }

    fun release() {
        handler.removeCallbacksAndMessages(null)
        resolveJob?.cancel()
        retryJob?.cancel()
        try {
            player.release()
        } catch (e: Exception) {
        }
    }

    // ------------------------------------------------------------------ commands

    fun noteActor(by: String?) {
        lastActor = by
        lastActorAt = System.currentTimeMillis()
    }

    fun pendingVideoIds(): Set<String> {
        val s = HashSet<String>()
        queue.forEach { s.add(it.videoId) }
        current?.let { if (it.source == "USER") s.add(it.videoId) }
        return s
    }

    fun add(
        videoId: String, title: String?, channel: String?, durationSec: Int,
        addedBy: String?, position: String, force: Boolean
    ): QueueItem {
        if (queue.size >= 200) throw ApiError(409, "QUEUE_FULL", "Queue is full")
        if (!force && queue.any { it.videoId == videoId }) {
            throw ApiError(409, "DUPLICATE_PENDING", "Already in queue")
        }
        val noTitle = title.isNullOrBlank()
        val item = QueueItem(
            videoId = videoId,
            title = if (noTitle) "YouTube · $videoId" else title!!.take(500),
            channel = (channel ?: "").take(200),
            durationSec = durationSec,
            addedBy = addedBy?.take(40)
        )
        if (position == "next") queue.add(0, item) else queue.add(item)
        val wasHalted = halted
        if (wasHalted) {
            halted = false
            consecutiveFails = 0
        }
        bump()
        if (current == null || wasHalted) startNext()
        if (noTitle) {
            scope.launch {
                val m = withContext(Dispatchers.IO) { Resolver.meta(videoId) }
                if (m != null && m.title.isNotEmpty()) {
                    item.title = m.title
                    item.channel = m.channel
                    item.durationSec = m.durationSec
                    bump()
                }
            }
        }
        return item
    }

    fun remove(itemId: String) {
        val cur = current
        if (cur != null && cur.itemId == itemId) {
            skip()
            return
        }
        val i = queue.indexOfFirst { it.itemId == itemId }
        if (i < 0) throw ApiError(404, "ITEM_NOT_FOUND", "Item not found")
        queue.removeAt(i)
        bump()
    }

    fun prioritize(itemId: String) {
        val i = queue.indexOfFirst { it.itemId == itemId }
        if (i < 0) throw ApiError(404, "ITEM_NOT_FOUND", "Item not found")
        val it = queue.removeAt(i)
        queue.add(0, it)
        bump()
    }

    fun reorder(itemId: String, newIndex: Int) {
        val i = queue.indexOfFirst { it.itemId == itemId }
        if (i < 0) throw ApiError(404, "ITEM_NOT_FOUND", "Item not found")
        val it = queue.removeAt(i)
        queue.add(newIndex.coerceIn(0, queue.size), it)
        bump()
    }

    fun playNow(itemId: String) {
        val i = queue.indexOfFirst { it.itemId == itemId }
        if (i < 0) throw ApiError(404, "ITEM_NOT_FOUND", "Item not found")
        val item = queue.removeAt(i)
        current?.status = "DONE"
        halted = false
        consecutiveFails = 0
        queueVersion++
        persist()
        playItem(item)
    }

    fun skip() {
        halted = false
        consecutiveFails = 0
        startNext()
    }

    fun pause() {
        userPaused = true
        autoPaused = false
        applyPause()
    }

    fun resume() {
        userPaused = false
        autoPaused = false
        applyResume()
    }

    fun restart() {
        if (usingIframe) iframeHost?.seek(0) else player.seekTo(0)
    }

    /** Doi chat luong video toi da (360/480/720/1080). Ap dung ngay cho bai dang phat: tai lai tu vi tri hien tai. */
    fun setMaxHeight(h: Int) {
        val v = when {
            h <= 360 -> 360
            h <= 480 -> 480
            h <= 720 -> 720
            else -> 1080
        }
        if (v == settings.maxHeight) return
        settings.maxHeight = v
        prefetchedId = null
        val cur = current
        if (cur != null && !usingIframe && (state == PState.PLAYING || state == PState.PAUSED)) {
            playItem(cur, player.currentPosition)
        } else {
            publish()
        }
        prefetchHead()
    }

    fun setVolumeAbs(level: Int): Int {
        volume = level.coerceIn(0, 100)
        player.volume = volume / 100f
        iframeHost?.setVolume(volume)
        publish()
        volumeEvents.tryEmit(volume)
        return volume
    }

    /** Tua: positionMs = vi tri tuyet doi, hoac deltaMs = tua tuong doi (am = lui). */
    fun seekTo(positionMs: Long?, deltaMs: Long) {
        if (current == null || (state != PState.PLAYING && state != PState.PAUSED)) return
        val pos = if (usingIframe) iframePos else player.currentPosition
        var dur = if (usingIframe) iframeDur else player.duration
        if (dur == C.TIME_UNSET || dur <= 0) dur = (current?.durationSec ?: 0) * 1000L
        var target = (positionMs ?: (pos + deltaMs)).coerceAtLeast(0L)
        if (dur > 0) target = target.coerceAtMost(maxOf(0L, dur - 1500L))
        if (usingIframe) iframeHost?.seek(target) else player.seekTo(target)
        lastPosChangeAt = System.currentTimeMillis()
    }

    fun changeVolume(delta: Int): Int = setVolumeAbs(volume + delta)

    // TV lifecycle ------------------------------------------------------

    fun tvBackground() {
        if (state == PState.PLAYING) {
            autoPaused = true
            applyPause()
        }
    }

    fun tvForeground() {
        if (autoPaused && !userPaused) {
            autoPaused = false
            applyResume()
        }
    }

    fun attachIframeHost(h: IframeHost?) {
        iframeHost = h
        val cur = current
        if (h != null && usingIframe && cur != null && state == PState.RESOLVING) {
            h.load(cur.videoId, cur.itemId, 0)
            h.setVolume(volume)
        }
    }

    // ------------------------------------------------------------------ internals

    private fun applyPause() {
        if (state == PState.PLAYING) {
            if (usingIframe) iframeHost?.pause() else player.pause()
            state = PState.PAUSED
            publish()
        } else if (state == PState.RESOLVING) {
            publish() // remembered through userPaused / autoPaused flags
        }
    }

    private fun applyResume() {
        if (state == PState.PAUSED) {
            if (usingIframe) iframeHost?.resume() else player.play()
            state = PState.PLAYING
            publish()
        }
    }

    private fun bump() {
        queueVersion++
        persist()
        publish()
        prefetchHead()
    }

    /** Lay san link bai ke tiep ngay khi no len dau hang doi, de bam chuyen bai la phat lien. */
    private fun prefetchHead() {
        val next = queue.firstOrNull() ?: return
        if (prefetchedId == next.videoId || settings.backend == "IFRAME") return
        prefetchedId = next.videoId
        val id = next.videoId
        scope.launch(Dispatchers.IO) {
            try {
                Resolver.resolve(id, settings.maxHeight)
            } catch (e: Exception) {
                if (prefetchedId == id) prefetchedId = null
            }
        }
    }

    private fun publish() {
        seq++
        val next = queue.firstOrNull()
        val label = next?.let { it.title + (if (it.addedBy != null) " · ${it.addedBy}" else "") }
        val msg = when {
            netWait -> "net"
            else -> null
        }
        ui.value = UiState(seq, state, current?.title, label, usingIframe, current == null, msg)
    }

    private fun emitError(code: String, msg: String) {
        errorSeq++
        errCode = code
        errMsg = msg
    }

    private fun isOnline(): Boolean {
        val n = connectivity.activeNetwork ?: return false
        val c = connectivity.getNetworkCapabilities(n) ?: return false
        return c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun startNext() {
        resolveJob?.cancel()
        retryJob?.cancel()
        current?.status = "DONE"
        val next = if (queue.isNotEmpty()) queue.removeAt(0) else seedNext()
        if (next == null) {
            current = null
            state = PState.IDLE
            usingIframe = false
            player.stop()
            iframeHost?.stop()
            bump()
            return
        }
        queueVersion++
        persist()
        playItem(next)
        prefetchHead()
    }

    private fun seedNext(): QueueItem? {
        if (!settings.seedAutoplay) return null
        if (seedPool.isEmpty()) {
            fillSeed()
            return null
        }
        val it = seedPool.removeAt(0)
        lastSeedId = it.videoId
        return it
    }

    private fun fillSeed() {
        if (seedFilling) return
        seedFilling = true
        scope.launch {
            var got: List<QueueItem> = emptyList()
            try {
                got = withContext(Dispatchers.IO) { loadSeed() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
            }
            seedFilling = false
            if (got.isNotEmpty()) {
                seedPool.addAll(got)
                if (current == null && queue.isEmpty()) startNext()
            } else {
                delay(10_000)
                if (current == null && queue.isEmpty()) startNext()
            }
        }
    }

    private fun loadSeed(): List<QueueItem> {
        val out = ArrayList<QueueItem>()
        val txt = app.assets.open("seed.json").bufferedReader().use { it.readText() }
        val root = JSONObject(txt)
        val vids = root.optJSONArray("videos")
        if (vids != null) {
            for (i in 0 until vids.length()) {
                val o = vids.getJSONObject(i)
                val id = o.optString("videoId")
                if (Util.ID_RE.matches(id)) {
                    out.add(QueueItem(videoId = id, title = o.optString("title", "YouTube · $id"), source = "SEED"))
                }
            }
        }
        val qs = root.optJSONArray("queries")
        if (qs != null) {
            val list = ArrayList<String>()
            for (i in 0 until qs.length()) list.add(qs.getString(i))
            list.shuffle()
            for (q in list.take(2)) {
                try {
                    val (hits, _) = SearchService.search(q, 0, false)
                    for (h in hits) {
                        if (h.durationSec in 120..720 && h.videoId != lastSeedId) {
                            out.add(
                                QueueItem(
                                    videoId = h.videoId, title = h.title, channel = h.channel,
                                    durationSec = h.durationSec, source = "SEED"
                                )
                            )
                        }
                    }
                } catch (e: Throwable) {
                }
            }
        }
        out.shuffle()
        return out
    }

    private fun playItem(item: QueueItem, startPos: Long = 0L) {
        resolveJob?.cancel()
        retryJob?.cancel()
        current = item
        item.status = "RESOLVING"
        state = PState.RESOLVING
        netWait = false
        sources = emptyList()
        srcIdx = 0
        if (lastItemId != item.itemId) {
            lastItemId = item.itemId
            reresolved = false
            stallRetries = 0
        }
        lastPos = -1
        publish()

        val useIframe = settings.backend == "IFRAME" ||
            (settings.backend == "AUTO" && forceIframeItem == item.itemId)
        if (useIframe) {
            playIframe(item, startPos)
            return
        }
        usingIframe = false
        iframeHost?.stop()
        resolveJob = scope.launch {
            try {
                val r = resolve(item)
                sources = r.sources
                srcIdx = 0
                if (item.title.startsWith("YouTube · ") && r.title.isNotEmpty()) {
                    item.title = r.title
                    item.channel = r.channel
                    item.durationSec = r.durationSec
                }
                startExo(item, startPos)
            } catch (e: UnavailableException) {
                lastFailInfo = e
                failCurrent(item, "UNAVAILABLE")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                lastFailInfo = e
                if (!isOnline()) waitNetwork(item, startPos) else failCurrent(item, "PLAYBACK_FAILED")
            }
        }
    }

    private suspend fun resolve(item: QueueItem): Resolved {
        Resolver.cached(item.videoId, settings.maxHeight)?.let { return it }
        var attempt = 0
        while (true) {
            try {
                val r = withTimeoutOrNull(15_000) {
                    withContext(Dispatchers.IO) { Resolver.resolve(item.videoId, settings.maxHeight) }
                }
                if (r != null) return r
                throw java.io.IOException("timeout")
            } catch (e: UnavailableException) {
                throw e
            } catch (e: org.schabi.newpipe.extractor.exceptions.ContentNotAvailableException) {
                throw UnavailableException(e.message ?: "unavailable")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                attempt++
                if (attempt >= 3) throw e
                delay(1000L shl (attempt - 1))
            }
        }
    }

    private fun startExo(item: QueueItem, startPos: Long) {
        if (current?.itemId != item.itemId) return
        val s = sources.getOrNull(srcIdx) ?: run {
            failCurrent(item, "PLAYBACK_FAILED")
            return
        }
        val dsf = DefaultHttpDataSource.Factory()
            .setUserAgent(Util.UA)
            .setConnectTimeoutMs(10_000)
            .setReadTimeoutMs(10_000)
            .setAllowCrossProtocolRedirects(true)
        // Tai theo tung doan 4 MB (range=) de YouTube khong bop toc do -> het dung ~10 giay sau vai giay dau
        val pf = ProgressiveMediaSource.Factory(YtChunkedDataSource.wrap(dsf))
        val src = if (s.audio != null) {
            MergingMediaSource(
                pf.createMediaSource(MediaItem.fromUri(s.video)),
                pf.createMediaSource(MediaItem.fromUri(s.audio))
            )
        } else {
            pf.createMediaSource(MediaItem.fromUri(s.video))
        }
        usingIframe = false
        player.volume = volume / 100f
        player.setMediaSource(src, startPos)
        player.prepare()
        player.playWhenReady = !(userPaused || autoPaused)
    }

    private fun onStarted() {
        val item = current ?: return
        item.status = "PLAYING"
        state = if (userPaused || autoPaused) PState.PAUSED else PState.PLAYING
        consecutiveFails = 0
        exoFailStreak = 0
        failScreenShown = false
        lastPosChangeAt = System.currentTimeMillis()
        publish()
    }

    private fun trackEnded() {
        consecutiveFails = 0
        startNext()
    }

    private fun handleExoError(e: PlaybackException) {
        lastFailInfo = e
        val item = current ?: return
        if (usingIframe) return
        val pos = player.currentPosition
        val net = e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ||
            e.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT
        if (net && !isOnline()) {
            player.stop()
            waitNetwork(item, pos)
            return
        }
        if (srcIdx + 1 < sources.size) {
            srcIdx++
            startExo(item, if (pos > 5000) pos else 0L)
            return
        }
        if (!reresolved) {
            reresolved = true
            Resolver.invalidate(item.videoId)
            playItem(item, if (pos > 5000) pos else 0L)
            return
        }
        failCurrent(item, "PLAYBACK_FAILED")
    }

    private fun waitNetwork(item: QueueItem, pos: Long) {
        netWait = true
        state = PState.RESOLVING
        publish()
        retryJob?.cancel()
        retryJob = scope.launch {
            while (!isOnline()) delay(3000)
            delay(1500)
            if (current?.itemId == item.itemId) playItem(item, pos)
        }
    }

    private fun failCurrent(item: QueueItem, code: String) {
        if (current?.itemId != item.itemId) return
        item.status = "UNAVAILABLE"
        if (code != "UNAVAILABLE" && settings.backend == "AUTO" && !usingIframe) {
            exoFailStreak++
            if (exoFailStreak >= 3 && forceIframeItem != item.itemId) {
                forceIframeItem = item.itemId
                fallbackHint = true
                playItem(item)
                return
            }
        }
        consecutiveFails++
        if (consecutiveFails >= 3 && !failScreenShown) {
            // Nhieu bai lien tiep khong phat duoc (thuong do may khac/ mang): hien bang loi TOAN MAN HINH de chup lai
            failScreenShown = true
            val info = StringBuilder()
            info.append("Khong phat duoc ").append(consecutiveFails).append(" bai lien tiep\n")
            info.append("Ma loi: ").append(code).append(" | Bai: ").append(item.title).append(" (").append(item.videoId).append(")\n")
            info.append("Trinh phat: ").append(settings.backend).append(if (usingIframe) " (iframe)" else " (exoplayer)")
            info.append(" | Mang: ").append(if (isOnline()) "co" else "KHONG").append('\n')
            lastFailInfo?.let { info.append(CrashLog.describe(it)) }
            CrashLog.writeText(app, "PLAYBACK", info.toString())
            CrashLog.showScreen(app)
        }
        emitError(code, item.title)
        player.stop()
        iframeHost?.stop()
        state = PState.ERROR
        if (consecutiveFails >= 5) {
            halted = true
            publish()
            return
        }
        publish()
        scope.launch {
            delay(1500)
            if (current?.itemId == item.itemId && state == PState.ERROR && !halted) startNext()
        }
    }

    // ------------------------------------------------------------------ iframe backend

    private fun playIframe(item: QueueItem, startPos: Long) {
        usingIframe = true
        player.stop()
        state = PState.RESOLVING
        publish()
        val h = iframeHost
        if (h != null) {
            h.load(item.videoId, item.itemId, (startPos / 1000).toInt())
            h.setVolume(volume)
        }
        // if no host yet, attachIframeHost() will issue the load
        retryJob?.cancel()
        retryJob = scope.launch {
            delay(25_000) // never started -> treat as failure
            if (current?.itemId == item.itemId && state == PState.RESOLVING && usingIframe) {
                failCurrent(item, "PLAYBACK_FAILED")
            }
        }
    }

    /** Called from the JS bridge thread. kind: ready | ended | error | pos */
    fun iframeEvent(kind: String, key: String, a: Long, b: Long) {
        handler.post {
            val item = current
            if (item == null || item.itemId != key || !usingIframe) return@post
            when (kind) {
                "ready" -> {
                    if (state == PState.RESOLVING) {
                        retryJob?.cancel()
                        item.status = "PLAYING"
                        consecutiveFails = 0
                        failScreenShown = false
                        state = PState.PLAYING
                        if (userPaused || autoPaused) {
                            iframeHost?.pause()
                            state = PState.PAUSED
                        }
                        publish()
                    }
                }
                "ended" -> trackEnded()
                "error" -> {
                    // 2 invalid id, 5 html5 error, 100 not found, 101/150 embedding disabled
                    failCurrent(item, "UNAVAILABLE")
                }
                "pos" -> {
                    iframePos = a
                    iframeDur = b
                }
            }
        }
    }

    // ------------------------------------------------------------------ watchdog / prefetch

    private fun tick() {
        if (usingIframe) return
        val item = current ?: return
        val now = System.currentTimeMillis()
        val pos = player.currentPosition
        val dur = player.duration

        if (state == PState.PLAYING) {
            if (pos != lastPos) {
                lastPos = pos
                lastPosChangeAt = now
            } else if (now - lastPosChangeAt > 15_000 && isOnline()) {
                lastPosChangeAt = now
                if (stallRetries < 2) {
                    stallRetries++
                    Resolver.invalidate(item.videoId)
                    playItem(item, pos)
                } else {
                    failCurrent(item, "PLAYBACK_FAILED")
                }
                return
            }
            // prefetch the next item at 70%
            if (dur != C.TIME_UNSET && dur > 0 && pos * 100 / dur >= 70) {
                val next = queue.firstOrNull()
                if (next != null && prefetchedId != next.videoId) {
                    prefetchedId = next.videoId
                    val id = next.videoId
                    scope.launch(Dispatchers.IO) {
                        try {
                            Resolver.resolve(id, settings.maxHeight)
                        } catch (e: Exception) {
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------ snapshot / persistence

    fun snapshot(): JSONObject {
        val o = JSONObject()
        o.put("version", 1)
        o.put("state", state.name)
        o.put("backend", if (usingIframe) "iframe" else "exoplayer")
        val cur = current
        o.put("nowPlaying", cur?.toJson() ?: JSONObject.NULL)
        val pos: Long
        var dur: Long
        if (usingIframe) {
            pos = iframePos
            dur = iframeDur
        } else {
            pos = if (state == PState.PLAYING || state == PState.PAUSED) player.currentPosition else 0L
            dur = player.duration
            if (dur == C.TIME_UNSET || dur <= 0) dur = (cur?.durationSec ?: 0) * 1000L
        }
        o.put("positionMs", pos)
        o.put("durationMs", dur)
        val arr = JSONArray()
        queue.forEach { arr.put(it.toJson()) }
        o.put("queue", arr)
        o.put("nextUp", queue.firstOrNull()?.toJson() ?: JSONObject.NULL)
        o.put("volume", volume)
        o.put("queueVersion", queueVersion)
        o.put("fallbackHint", fallbackHint && usingIframe)
        o.put("netWait", netWait)
        o.put("halted", halted)
        o.put("error", JSONObject().put("seq", errorSeq).put("code", errCode).put("message", errMsg))
        o.put(
            "lastChange",
            JSONObject().put("by", lastActor ?: JSONObject.NULL)
                .put("agoMs", System.currentTimeMillis() - lastActorAt)
        )
        o.put(
            "settings",
            JSONObject().put("backend", settings.backend).put("maxHeight", settings.maxHeight)
                .put("tickerMode", settings.tickerMode).put("seedAutoplay", settings.seedAutoplay)
        )
        return o
    }

    private fun persist() {
        if (!settings.restoreQueue) return
        try {
            val arr = JSONArray()
            current?.let { if (it.source == "USER") arr.put(it.toJson()) }
            queue.forEach { arr.put(it.toJson()) }
            settings.sp.edit().putString("queue", arr.toString()).apply()
        } catch (e: Exception) {
        }
    }

    private fun restore() {
        if (!settings.restoreQueue) return
        try {
            val txt = settings.sp.getString("queue", null) ?: return
            val arr = JSONArray(txt)
            for (i in 0 until arr.length()) {
                val it = QueueItem.fromJson(arr.getJSONObject(i))
                queue.add(it)
            }
        } catch (e: Exception) {
            queue.clear()
        }
    }
}
