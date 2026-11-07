package vn.savify.karaoke

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.SurfaceView
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
class MainActivity : Activity(), IframeHost {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var core: Core? = null
    private var coreJobs = ArrayList<Job>()

    private lateinit var root: FrameLayout
    private lateinit var surface: SurfaceView
    private lateinit var wall: ImageView
    private lateinit var fx: FxView
    private var qrBig = false
    private var web: WebView? = null
    private var webFailed = false
    private lateinit var idle: TextView
    private lateinit var strip: FrameLayout
    private lateinit var tickerText: TextView
    private lateinit var qrBtn: ImageView
    private lateinit var qrWrap: FrameLayout
    private lateinit var qrMsg: TextView
    private lateinit var overlay: LinearLayout
    private lateinit var bigQr: ImageView
    private lateinit var bigText: TextView
    private lateinit var bigCap: TextView
    private lateinit var bClose: Button
    private lateinit var react: ReactionView
    private lateinit var msgView: MessageView
    private lateinit var volBox: LinearLayout
    private lateinit var volText: TextView
    private lateinit var volBar: android.widget.ProgressBar
    private val volHide = Runnable { volBox.visibility = View.GONE }
    private var wallShown = false

    private var anim: ValueAnimator? = null
    private var tickerShown: String? = null
    /** true khi dong chu da chay xong 1 luot va dang an cho 30 giay truoc khi chay lai */
    private var tickerHold = false
    private var tickerResume: Runnable? = null
    private var lastUi: UiState = UiState()
    private var lastQrUrl: String? = null
    private var overlayUntil = 0L
    private var vw = 0
    private var vh = 0
    private var par = 1f
    private var pageReady = false
    private var pendingJs: String? = null
    private var surfaceAttached = false
    private var inSession = false
    private var pulse: ValueAnimator? = null
    private var qrScale = 1f

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun vi() = core?.settings?.tvLang != "en"

    private val videoListener = object : Player.Listener {
        override fun onVideoSizeChanged(videoSize: VideoSize) {
            vw = videoSize.width
            vh = videoSize.height
            par = if (videoSize.pixelWidthHeightRatio > 0) videoSize.pixelWidthHeightRatio else 1f
            fitSurface()
        }
    }

    // ------------------------------------------------------------------ lifecycle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        buildUi()
        setContentView(root)
        hideSystemBars()

        CrashLog.read(this)?.let { showCrash(it) }

        try {
            val i = Intent(this, KaraokeService::class.java)
            if (Build.VERSION.SDK_INT >= 26) startForegroundService(i) else startService(i)
        } catch (e: Throwable) {
            CrashLog.write(this, "START_SERVICE", e)
            CrashLog.read(this)?.let { showCrash(it) }
        }
        scope.launch {
            delay(5000)
            if (core == null) CrashLog.read(this@MainActivity)?.let { showCrash(it) }
        }

        scope.launch {
            AppRuntime.core.collect { c ->
                detachCore()
                if (c != null) attachCore(c)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        core?.let {
            attachSurface(it)
            it.controller.attachIframeHost(this)
            it.controller.tvForeground()
        }
    }

    override fun onStop() {
        core?.let {
            it.controller.tvBackground()
            it.controller.attachIframeHost(null)
            detachSurface(it)
        }
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
        web?.onResume()
    }

    override fun onPause() {
        web?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        detachCore()
        scope.cancel()
        anim?.cancel()
        pulse?.cancel()
        try {
            web?.destroy()
        } catch (e: Exception) {
        }
        super.onDestroy()
    }

    private fun hideSystemBars() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    // ------------------------------------------------------------------ attach / detach core

    private fun attachSurface(c: Core) {
        if (surfaceAttached) return
        c.controller.player.setVideoSurfaceView(surface)
        c.controller.player.addListener(videoListener)
        surfaceAttached = true
    }

    private fun detachSurface(c: Core) {
        if (!surfaceAttached) return
        c.controller.player.removeListener(videoListener)
        c.controller.player.clearVideoSurfaceView(surface)
        surfaceAttached = false
    }

    private fun attachCore(c: Core) {
        core = c
        attachSurface(c)
        c.controller.attachIframeHost(this)
        coreJobs.add(scope.launch { c.controller.ui.collect { renderUi(it) } })
        coreJobs.add(scope.launch { c.controller.reactions.collect { react.fire(it) } })
        coreJobs.add(scope.launch { c.controller.messages.collect { msgView.show(it.first, it.second) } })
        coreJobs.add(scope.launch { c.controller.volumeEvents.collect { showVolume(it) } })
        coreJobs.add(scope.launch {
            while (true) {
                updateQr(c)
                updateTicker(c)
                delay(1000)
            }
        })
    }

    private fun detachCore() {
        coreJobs.forEach { it.cancel() }
        coreJobs.clear()
        core?.let {
            try {
                detachSurface(it)
                it.controller.attachIframeHost(null)
            } catch (e: Exception) {
            }
        }
        core = null
    }

    // ------------------------------------------------------------------ UI construction

    private fun buildUi() {
        root = FrameLayout(this)
        root.setBackgroundColor(Color.BLACK)
        root.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ -> fitSurface() }

        // Wallpaper (icon chinh cua app) tran man hinh + hieu ung roi, nam duoi video
        wall = ImageView(this)
        wall.scaleType = ImageView.ScaleType.CENTER_CROP
        wall.setImageResource(R.drawable.wallpaper)
        root.addView(wall, FrameLayout.LayoutParams(-1, -1))
        fx = FxView(this)
        root.addView(fx, FrameLayout.LayoutParams(-1, -1))

        surface = SurfaceView(this)
        surface.visibility = View.INVISIBLE
        root.addView(surface, FrameLayout.LayoutParams(-1, -1, Gravity.CENTER))

        idle = TextView(this)
        idle.setTextColor(Color.WHITE)
        idle.textSize = 40f
        idle.typeface = Typeface.DEFAULT_BOLD
        idle.gravity = Gravity.CENTER
        idle.setShadowLayer(8f, 0f, 3f, Color.BLACK)
        val ibg = GradientDrawable()
        ibg.setColor(0xD9E91E63.toInt())
        ibg.cornerRadius = dp(36).toFloat()
        idle.background = ibg
        idle.setPadding(dp(36), dp(14), dp(36), dp(14))
        idle.visibility = View.GONE
        val ilp = FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
        ilp.bottomMargin = (resources.displayMetrics.heightPixels * 0.07f).toInt()
        root.addView(idle, ilp)

        // Hieu ung cam xuc (tha tim, phao giay...) tu remote: phu len tren video, duoi cac nut/QR
        react = ReactionView(this)
        root.addView(react, FrameLayout.LayoutParams(-1, -1))
        // Tin nhan chu chay tu dien thoai: nam tren hieu ung, duoi cac nut/QR
        msgView = MessageView(this)
        root.addView(msgView, FrameLayout.LayoutParams(-1, -1))
        // Thanh am luong: hien o giua duoi khi bam tang/giam am luong tu remote
        volBox = LinearLayout(this)
        volBox.orientation = LinearLayout.VERTICAL
        volBox.gravity = Gravity.CENTER_HORIZONTAL
        val vbg = GradientDrawable()
        vbg.setColor(0xB3000000.toInt())
        vbg.cornerRadius = dp(16).toFloat()
        volBox.background = vbg
        volBox.setPadding(dp(32), dp(16), dp(32), dp(20))
        volText = TextView(this)
        volText.setTextColor(Color.WHITE)
        volText.textSize = 28f
        volText.typeface = Typeface.DEFAULT_BOLD
        volBar = android.widget.ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
        volBar.max = 100
        volBar.progressTintList = android.content.res.ColorStateList.valueOf(0xFFFFB300.toInt())
        volBar.progressBackgroundTintList = android.content.res.ColorStateList.valueOf(0x66FFFFFF)
        volBox.addView(volText, LinearLayout.LayoutParams(-2, -2))
        val vbl = LinearLayout.LayoutParams(dp(360), dp(10))
        vbl.topMargin = dp(10)
        volBox.addView(volBar, vbl)
        volBox.visibility = View.GONE
        val vlp = FrameLayout.LayoutParams(-2, -2, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
        vlp.bottomMargin = (resources.displayMetrics.heightPixels * 0.15f).toInt()
        root.addView(volBox, vlp)

        val dm = resources.displayMetrics
        val safeX = (dm.widthPixels * 0.05f).toInt()
        val safeY = (dm.heightPixels * 0.05f).toInt()
        val qrFull = dp(190)
        val neonPad = dp(14)
        val qrWrapSize = qrFull + 2 * neonPad

        // Next-up ticker
        strip = FrameLayout(this)
        strip.clipChildren = true
        strip.isFocusable = false
        val bg = GradientDrawable()
        bg.setColor(0x8C000000.toInt())
        val r = dp(12).toFloat()
        bg.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, r, r, r, r)
        strip.background = bg
        val slp = FrameLayout.LayoutParams(-1, dp(40), Gravity.TOP or Gravity.START)
        slp.leftMargin = safeX
        slp.topMargin = safeY
        slp.rightMargin = safeX + dp(330)
        strip.visibility = View.GONE
        tickerText = TextView(this)
        tickerText.setTextColor(Color.WHITE)
        tickerText.textSize = 22f
        tickerText.typeface = Typeface.DEFAULT_BOLD
        tickerText.setSingleLine(true)
        tickerText.gravity = Gravity.CENTER_VERTICAL
        tickerText.setShadowLayer(1f, 1f, 1f, Color.BLACK)
        strip.addView(tickerText, FrameLayout.LayoutParams(-2, -1))
        root.addView(strip, slp)

        // QR button
        qrBtn = ImageView(this)
        qrBtn.scaleType = ImageView.ScaleType.FIT_XY
        qrBtn.setBackgroundColor(Color.WHITE)
        qrBtn.isFocusable = true
        qrBtn.isClickable = true
        qrBtn.setOnClickListener { toggleOverlay() }
        qrBtn.setOnFocusChangeListener { _, _ -> applyQrScale() }
        // Khung chua: dai sang neon lien mach (sat vien trang) + QR co vien trang (de len tren)
        qrWrap = FrameLayout(this)
        qrWrap.clipChildren = false
        qrWrap.pivotX = qrWrapSize.toFloat()
        qrWrap.pivotY = 0f
        qrWrap.addView(NeonRingView(this), FrameLayout.LayoutParams(-1, -1))
        qrWrap.addView(qrBtn, FrameLayout.LayoutParams(qrFull, qrFull, Gravity.CENTER))
        val qlp = FrameLayout.LayoutParams(qrWrapSize, qrWrapSize, Gravity.TOP or Gravity.END)
        qlp.topMargin = safeY
        qlp.rightMargin = safeX
        qrWrap.visibility = View.GONE
        root.addView(qrWrap, qlp)

        qrMsg = TextView(this)
        qrMsg.setTextColor(Color.WHITE)
        qrMsg.textSize = 18f
        qrMsg.setBackgroundColor(0x99000000.toInt())
        qrMsg.setPadding(dp(8), dp(6), dp(8), dp(6))
        qrMsg.visibility = View.GONE
        val mlp = FrameLayout.LayoutParams(-2, -2, Gravity.TOP or Gravity.END)
        mlp.topMargin = safeY
        mlp.rightMargin = safeX
        root.addView(qrMsg, mlp)

        // enlarged QR overlay
        overlay = LinearLayout(this)
        overlay.orientation = LinearLayout.VERTICAL
        overlay.gravity = Gravity.CENTER
        overlay.setBackgroundColor(0xE6000000.toInt())
        overlay.visibility = View.GONE
        overlay.isClickable = true
        bigCap = TextView(this)
        bigCap.setTextColor(Color.WHITE)
        bigCap.textSize = 30f
        bigCap.typeface = Typeface.DEFAULT_BOLD
        bigCap.gravity = Gravity.CENTER
        bigCap.setPadding(0, 0, 0, dp(14))
        overlay.addView(bigCap, LinearLayout.LayoutParams(-2, -2))
        bigQr = ImageView(this)
        val bbg = GradientDrawable()
        bbg.setColor(Color.WHITE)
        bbg.cornerRadius = dp(12).toFloat()
        bigQr.background = bbg
        bigQr.setPadding(dp(12), dp(12), dp(12), dp(12))
        overlay.addView(bigQr, LinearLayout.LayoutParams(dp(300), dp(300)))
        bigText = TextView(this)
        bigText.setTextColor(Color.WHITE)
        bigText.textSize = 22f
        bigText.gravity = Gravity.CENTER
        bigText.setPadding(0, dp(12), 0, dp(12))
        overlay.addView(bigText, LinearLayout.LayoutParams(-2, -2))
        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        val bSettings = Button(this)
        bSettings.text = "Cài đặt / Settings"
        bSettings.setOnClickListener { showSettings() }
        val bReset = Button(this)
        bReset.text = "Đặt lại ghép nối / Reset pairing"
        bReset.setOnClickListener { confirmReset() }
        bClose = Button(this)
        bClose.text = "Đóng / Close"
        bClose.setOnClickListener { hideOverlay() }
        row.addView(bSettings)
        row.addView(bReset)
        row.addView(bClose)
        overlay.addView(row, LinearLayout.LayoutParams(-2, -2))
        root.addView(overlay, FrameLayout.LayoutParams(-1, -1))
    }

    /** Creates the WebView only when the fallback player is needed (some TV boxes have no WebView). */
    private fun ensureWeb(): WebView? {
        web?.let { return it }
        if (webFailed) return null
        return try {
            val w = WebView(this)
            w.visibility = View.GONE
            w.setBackgroundColor(Color.BLACK)
            val ws = w.settings
            ws.javaScriptEnabled = true
            ws.domStorageEnabled = true
            ws.mediaPlaybackRequiresUserGesture = false
            ws.allowFileAccess = false
            ws.allowContentAccess = false
            ws.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            w.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                    request.isForMainFrame

                override fun onPageFinished(view: WebView, url: String?) {
                    pageReady = true
                    pendingJs?.let { view.evaluateJavascript(it, null) }
                    pendingJs = null
                }
            }
            w.addJavascriptInterface(Bridge(), "KBridge")
            root.addView(w, 1, FrameLayout.LayoutParams(-1, -1))
            val html = assets.open("player.html").bufferedReader().use { it.readText() }
            w.loadDataWithBaseURL("https://www.youtube.com/", html, "text/html", "utf-8", null)
            web = w
            w
        } catch (e: Throwable) {
            webFailed = true
            CrashLog.write(this, "WEBVIEW", e)
            null
        }
    }

    private fun showCrash(@Suppress("UNUSED_PARAMETER") text: String) {
        CrashLog.showScreen(this)
    }

    private fun fitSurface() {
        val rw = root.width
        val rh = root.height
        if (rw == 0 || rh == 0 || vw == 0 || vh == 0) return
        val ar = vw * par / vh
        var w = rw
        var h = (rw / ar).toInt()
        if (h > rh) {
            h = rh
            w = (rh * ar).toInt()
        }
        val lp = surface.layoutParams as FrameLayout.LayoutParams
        if (lp.width != w || lp.height != h) {
            lp.width = w
            lp.height = h
            lp.gravity = Gravity.CENTER
            surface.layoutParams = lp
        }
    }

    // ------------------------------------------------------------------ rendering

    private fun renderUi(u: UiState) {
        lastUi = u
        val playing = u.state == PState.PLAYING || u.state == PState.PAUSED
        if (playing) inSession = true
        else if (u.state == PState.IDLE || u.state == PState.ERROR) inSession = false
        val showIframe = u.iframe && u.state != PState.IDLE
        // Man hinh cho (wallpaper + chu moi quet ma) chi hien khi CHUA phat bai nao.
        // Chuyen bai khi dang trong phien hat: khong hien wallpaper, giu khung hinh cu.
        val showWall = !showIframe && !playing &&
            (u.state == PState.IDLE || u.state == PState.ERROR || (u.state == PState.RESOLVING && !inSession))
        web?.visibility = if (showIframe) View.VISIBLE else View.GONE
        surface.visibility = if (showIframe || showWall) View.INVISIBLE else View.VISIBLE
        wall.visibility = if (showWall) View.VISIBLE else View.GONE
        fx.visibility = if (showWall) View.VISIBLE else View.GONE
        wallShown = showWall
        setQrSize(showWall)
        val msg: String? = when {
            u.message == "net" -> if (vi()) "Mất kết nối mạng, đang thử lại…" else "Network lost, retrying…"
            showWall -> if (vi()) "Xin mời quét mã để chọn bài" else "Please scan the QR code to pick a song"
            else -> null
        }
        idle.text = msg ?: ""
        idle.visibility = if (msg != null) View.VISIBLE else View.GONE
        setPulse(msg != null && u.message != "net")
        core?.let { updateTicker(it); updateQr(it) }
    }

    /** Hieu ung nhap nhay/phong to nhe de gay chu y cho dong chu moi quet ma. */
    private fun setPulse(on: Boolean) {
        if (on) {
            if (pulse != null) return
            val ev = ArgbEvaluator()
            val a = ValueAnimator.ofFloat(0f, 1f)
            a.duration = 900
            a.repeatMode = ValueAnimator.REVERSE
            a.repeatCount = ValueAnimator.INFINITE
            a.interpolator = AccelerateDecelerateInterpolator()
            a.addUpdateListener {
                val t = it.animatedValue as Float
                val sc = 1f + 0.10f * t
                idle.scaleX = sc
                idle.scaleY = sc
                idle.alpha = 0.8f + 0.2f * t
                idle.setTextColor(ev.evaluate(t, Color.WHITE, 0xFFFFF176.toInt()) as Int)
            }
            pulse = a
            a.start()
        } else {
            pulse?.cancel()
            pulse = null
            idle.scaleX = 1f
            idle.scaleY = 1f
            idle.alpha = 1f
            idle.setTextColor(Color.WHITE)
        }
    }

    /** Cho cho: QR to (de quet); dang phat: QR thu nho bang phep scale (khong doi layout nen khong bi cat). */
    private fun setQrSize(big: Boolean) {
        qrBig = big
        qrScale = if (big) 1f else 0.7f
        applyQrScale()
    }

    private fun applyQrScale() {
        val f = if (qrBtn.hasFocus()) 1.15f else 1f
        qrWrap.scaleX = qrScale * f
        qrWrap.scaleY = qrScale * f
    }

    private fun updateTicker(c: Core) {
        val u = lastUi
        val mode = c.settings.tickerMode
        var show = u.nextText != null && mode != "OFF" &&
            (u.state == PState.PLAYING || u.state == PState.PAUSED)
        if (show && mode == "LAST_30S") {
            val p = c.controller.player
            val dur = p.duration
            show = dur > 0 && (dur - p.currentPosition) <= 30_000
        }
        if (!show) {
            if (strip.visibility != View.GONE) {
                anim?.cancel()
                cancelTickerResume()
                strip.visibility = View.GONE
            }
            tickerShown = null
            tickerHold = false
            return
        }
        val text = (if (vi()) "Tiếp theo: " else "Next: ") + u.nextText
        if (text != tickerShown) {
            tickerShown = text
            tickerHold = false
            strip.visibility = View.VISIBLE
            startTicker(c, text)
        } else if (!tickerHold) {
            strip.visibility = View.VISIBLE
        }
    }

    private fun showVolume(v: Int) {
        volText.text = (if (vi()) "Âm lượng: " else "Volume: ") + v + "%"
        volBar.progress = v
        volBox.visibility = View.VISIBLE
        volBox.removeCallbacks(volHide)
        volBox.postDelayed(volHide, 1800)
    }

    private fun cancelTickerResume() {
        tickerResume?.let { strip.removeCallbacks(it) }
        tickerResume = null
    }

    private fun startTicker(c: Core, text: String) {
        anim?.cancel()
        cancelTickerResume()
        tickerText.text = text
        val w = tickerText.paint.measureText(text).toInt() + dp(24)
        tickerText.layoutParams = FrameLayout.LayoutParams(w, FrameLayout.LayoutParams.MATCH_PARENT)
        val speed = c.settings.tickerSpeed.coerceIn(40, 160) * resources.displayMetrics.density
        fun go() {
            val sw = strip.width.toFloat()
            if (sw <= 0f) {
                strip.postDelayed({ if (tickerShown == text) go() }, 200)
                return
            }
            val end = -(w + sw / 3f)
            val dist = sw - end
            val a = ValueAnimator.ofFloat(sw, end)
            a.duration = (dist / speed * 1000).toLong().coerceAtLeast(1000)
            a.interpolator = LinearInterpolator()
            a.addUpdateListener { tickerText.translationX = it.animatedValue as Float }
            // Chay 1 luot -> an -> sau 30 giay chay lai (lap mai)
            a.addListener(object : android.animation.AnimatorListenerAdapter() {
                private var cancelled = false
                override fun onAnimationCancel(animation: android.animation.Animator) { cancelled = true }
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (cancelled || tickerShown != text) return
                    tickerHold = true
                    strip.visibility = View.GONE
                    val r = Runnable {
                        tickerResume = null
                        if (tickerShown == text) {
                            tickerHold = false
                            strip.visibility = View.VISIBLE
                            go()
                        }
                    }
                    tickerResume = r
                    strip.postDelayed(r, 30_000)
                }
            })
            anim = a
            a.start()
        }
        strip.post { go() }
    }

    /** QR co vien trang (quiet zone 4 o) nam ngay trong anh, moi o la so nguyen pixel -> quet duoc ca khi thu nho. */
    private fun makeQr(text: String, size: Int): Bitmap {
        val hints = mapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to 0,
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M
        )
        val m = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 1, 1, hints)
        val n = m.width
        val quiet = 4
        val total = n + quiet * 2
        val k = maxOf(1, size / total)
        val side = total * k
        val px = IntArray(side * side) { Color.WHITE }
        for (y in 0 until n) for (x in 0 until n) {
            if (m.get(x, y)) {
                val ox = (x + quiet) * k
                val oy = (y + quiet) * k
                for (dy in 0 until k) {
                    val row = (oy + dy) * side + ox
                    for (dx in 0 until k) px[row + dx] = Color.BLACK
                }
            }
        }
        return Bitmap.createBitmap(px, side, side, Bitmap.Config.ARGB_8888)
    }

    private fun updateQr(c: Core) {
        val ip = c.ip.value
        val now = System.currentTimeMillis()
        if (overlayUntil != 0L && now > overlayUntil) hideOverlay()

        bigCap.text = if (vi()) "Mời quét mã QR để thêm bài" else "Scan the QR code to add songs"
        if (ip == null) {
            qrWrap.visibility = View.GONE
            qrMsg.text = if (vi()) "Hãy kết nối Wi-Fi để dùng mã QR" else "Connect to Wi-Fi to use the QR code"
            qrMsg.visibility = View.VISIBLE
            return
        }
        qrMsg.visibility = View.GONE
        val url = "http://$ip:${c.port}/?token=${c.settings.token}"
        if (url != lastQrUrl) {
            lastQrUrl = url
            qrBtn.setImageBitmap(makeQr(url, 600))
            bigQr.setImageBitmap(makeQr(url, 600))
            bigText.text = "http://$ip:${c.port}"
        }
        // Man hinh cho: QR to o goc. Dang phat bai: an QR (bam OK de hien bang QR).
        qrWrap.visibility = if (wallShown) View.VISIBLE else View.GONE
    }

    private fun toggleOverlay() {
        if (overlay.visibility == View.VISIBLE) hideOverlay() else {
            overlay.visibility = View.VISIBLE
            overlayUntil = System.currentTimeMillis() + 15_000
            bClose.requestFocus()
        }
    }

    private fun hideOverlay() {
        overlay.visibility = View.GONE
        overlayUntil = 0L
        core?.let { updateQr(it) }
    }

    // ------------------------------------------------------------------ dialogs

    private fun showSettings() {
        val c = core ?: return
        overlayUntil = System.currentTimeMillis() + 60_000
        val s = c.settings
        val items = arrayOf(
            "Trình phát / Player: ${s.backend}",
            "Độ phân giải tối đa / Max resolution: ${s.maxHeight}p",
            "Dòng chữ Tiếp theo / Next-up ticker: ${s.tickerMode}",
            "Tự phát bài gợi ý / Seed autoplay: ${if (s.seedAutoplay) "ON" else "OFF"}",
            "Ngôn ngữ trên TV / TV language: ${s.tvLang}",
            "Xong / Done"
        )
        AlertDialog.Builder(this)
            .setTitle("Cài đặt / Settings")
            .setItems(items) { d, which ->
                when (which) {
                    0 -> s.backend = when (s.backend) { "AUTO" -> "EXOPLAYER"; "EXOPLAYER" -> "IFRAME"; else -> "AUTO" }
                    1 -> c.controller.setMaxHeight(when (s.maxHeight) { 360 -> 480; 480 -> 720; 720 -> 1080; else -> 360 })
                    2 -> s.tickerMode = when (s.tickerMode) { "ALWAYS" -> "LAST_30S"; "LAST_30S" -> "OFF"; else -> "ALWAYS" }
                    3 -> s.seedAutoplay = !s.seedAutoplay
                    4 -> s.tvLang = if (s.tvLang == "vi") "en" else "vi"
                    else -> {
                        d.dismiss()
                        return@setItems
                    }
                }
                d.dismiss()
                tickerShown = null
                showSettings()
            }
            .show()
    }

    private fun confirmReset() {
        val c = core ?: return
        AlertDialog.Builder(this)
            .setTitle("Đặt lại ghép nối? / Reset pairing?")
            .setMessage("Tất cả điện thoại đã kết nối sẽ bị ngắt. / All phones will be disconnected.")
            .setPositiveButton("Đặt lại / Reset") { _, _ ->
                c.settings.token = Util.newToken()
                updateQr(c)
            }
            .setNegativeButton("Hủy / Cancel", null)
            .show()
    }

    private fun confirmExit() {
        val d = AlertDialog.Builder(this)
            .setTitle("Thoát Karaoke? / Exit Karaoke?")
            .setPositiveButton("Ở lại / Stay", null)
            .setNegativeButton("Thoát / Exit") { _, _ ->
                stopService(Intent(this, KaraokeService::class.java))
                finishAndRemoveTask()
            }
            .create()
        d.show()
        d.getButton(AlertDialog.BUTTON_POSITIVE)?.requestFocus()
    }

    override fun onBackPressed() {
        if (overlay.visibility == View.VISIBLE) hideOverlay() else confirmExit()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        val c = core
        // Bam OK tren remote TV khi dang phat bai: hien bang ma QR (bam lan nua / Back de dong)
        if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER ||
                keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && overlay.visibility != View.VISIBLE) {
            if (event.repeatCount == 0) toggleOverlay()
            return true
        }
        if (c != null) {
            when (keyCode) {
                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                    if (lastUi.state == PState.PAUSED) c.controller.resume() else c.controller.pause()
                    return true
                }
                KeyEvent.KEYCODE_MEDIA_PLAY -> { c.controller.resume(); return true }
                KeyEvent.KEYCODE_MEDIA_PAUSE -> { c.controller.pause(); return true }
                KeyEvent.KEYCODE_MEDIA_NEXT -> { c.controller.skip(); return true }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    // ------------------------------------------------------------------ IframeHost (Backend B)

    private fun js(code: String) {
        if (!pageReady) {
            pendingJs = code
            return
        }
        web?.evaluateJavascript(code, null)
    }

    override fun load(videoId: String, key: String, startSec: Int) {
        if (!Util.ID_RE.matches(videoId)) return
        val w = ensureWeb()
        if (w == null) {
            core?.controller?.iframeEvent("error", key, 0, 0)
            return
        }
        w.visibility = View.VISIBLE
        js("kLoad('$videoId','${key.filter { it.isLetterOrDigit() || it == '-' }}',$startSec)")
    }

    override fun pause() = js("kPause()")
    override fun resume() = js("kPlay()")
    override fun seek(ms: Long) = js("kSeek(${ms / 1000})")
    override fun setVolume(v: Int) = js("kVol($v)")
    override fun stop() {
        if (pageReady) web?.evaluateJavascript("kStop()", null)
        web?.visibility = View.GONE
    }

    /** Called by player.html (runs on a WebView background thread). */
    inner class Bridge {
        @JavascriptInterface
        fun state(key: String, st: String) {
            core?.controller?.iframeEvent("ready", key, 0, 0)
        }

        @JavascriptInterface
        fun ended(key: String) {
            core?.controller?.iframeEvent("ended", key, 0, 0)
        }

        @JavascriptInterface
        fun error(key: String, code: Int) {
            core?.controller?.iframeEvent("error", key, code.toLong(), 0)
        }

        @JavascriptInterface
        fun pos(key: String, posMs: Double, durMs: Double) {
            core?.controller?.iframeEvent("pos", key, posMs.toLong(), if (durMs.isNaN()) 0L else durMs.toLong())
        }
    }
}
