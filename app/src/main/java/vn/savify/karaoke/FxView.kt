package vn.savify.karaoke

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Hieu ung roi tren man hinh cho: tim nho, tim to, sao, tuyet. Chi chay khi dang hien. */
class FxView(ctx: Context) : View(ctx) {

    private class P(
        var x: Float, var y: Float, var vy: Float, var sway: Float, var swaySpeed: Float,
        var phase: Float, var size: Float, var kind: Int, var rot: Float, var vrot: Float,
        var alpha: Int, var color: Int
    )

    private val d = resources.displayMetrics.density
    private val ps = ArrayList<P>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val heart = Path()
    private val star = Path()
    private var last = 0L
    private var running = false

    private val heartColors = intArrayOf(0xFFFF4D8D.toInt(), 0xFFFF7AA8.toInt(), 0xFFFF2E63.toInt(), 0xFFFFA6C9.toInt())
    private val starColors = intArrayOf(0xFFFFE066.toInt(), 0xFFFFF3B0.toInt(), 0xFFFFD43B.toInt(), 0xFFB8E0FF.toInt())

    init {
        // trai tim don vi (-1..1)
        heart.moveTo(0f, 0.9f)
        heart.cubicTo(-1.25f, 0.1f, -0.95f, -0.95f, 0f, -0.35f)
        heart.cubicTo(0.95f, -0.95f, 1.25f, 0.1f, 0f, 0.9f)
        heart.close()
        // ngoi sao 5 canh
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) 1f else 0.45f
            val a = Math.PI / 5 * i - Math.PI / 2
            val px = (r * cos(a)).toFloat()
            val py = (r * sin(a)).toFloat()
            if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
        }
        star.close()
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    private fun spawn(initial: Boolean): P {
        val w = if (width > 0) width.toFloat() else 1920f
        val h = if (height > 0) height.toFloat() else 1080f
        val r = Random.nextInt(100)
        val kind: Int
        val size: Float
        val color: Int
        val vy: Float
        when {
            r < 30 -> { kind = 0; size = (7 + Random.nextInt(10)) * d; color = heartColors.random(); vy = (45 + Random.nextInt(50)) * d } // tim nho
            r < 45 -> { kind = 1; size = (22 + Random.nextInt(26)) * d; color = heartColors.random(); vy = (30 + Random.nextInt(35)) * d } // tim to
            r < 62 -> { kind = 2; size = (6 + Random.nextInt(11)) * d; color = starColors.random(); vy = (40 + Random.nextInt(60)) * d }  // sao
            else -> { kind = 3; size = (2 + Random.nextInt(5)) * d; color = Color.WHITE; vy = (30 + Random.nextInt(55)) * d }            // tuyet
        }
        return P(
            x = Random.nextFloat() * w,
            y = if (initial) Random.nextFloat() * h else -size * 2,
            vy = vy,
            sway = (8 + Random.nextInt(30)) * d,
            swaySpeed = 0.6f + Random.nextFloat() * 1.4f,
            phase = Random.nextFloat() * 6.28f,
            size = size, kind = kind,
            rot = Random.nextFloat() * 360f,
            vrot = if (kind == 3) 0f else (Random.nextFloat() - 0.5f) * 70f,
            alpha = if (kind == 3) 150 + Random.nextInt(100) else 170 + Random.nextInt(85),
            color = color
        )
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        if (ps.isEmpty() && w > 0) repeat(55) { ps.add(spawn(true)) }
    }

    override fun onVisibilityChanged(changed: View, visibility: Int) {
        super.onVisibilityChanged(changed, visibility)
        if (visibility == VISIBLE && isAttachedToWindow) start() else running = false
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (visibility == VISIBLE) start()
    }

    override fun onDetachedFromWindow() {
        running = false
        super.onDetachedFromWindow()
    }

    private fun start() {
        if (running) return
        running = true
        last = 0L
        postInvalidateOnAnimation()
    }

    override fun onDraw(c: Canvas) {
        val now = System.nanoTime()
        val dt = if (last == 0L) 0.016f else ((now - last) / 1e9f).coerceAtMost(0.05f)
        last = now
        val h = height.toFloat()
        val t = now / 1e9f
        for (p in ps) {
            p.y += p.vy * dt
            p.rot += p.vrot * dt
            val sx = p.x + sin(t * p.swaySpeed + p.phase) * p.sway
            if (p.y - p.size * 2 > h) {
                val n = spawn(false)
                p.x = n.x; p.y = n.y; p.vy = n.vy; p.sway = n.sway; p.swaySpeed = n.swaySpeed
                p.phase = n.phase; p.size = n.size; p.kind = n.kind; p.rot = n.rot; p.vrot = n.vrot
                p.alpha = n.alpha; p.color = n.color
            }
            paint.color = p.color
            paint.alpha = p.alpha
            when (p.kind) {
                3 -> c.drawCircle(sx, p.y, p.size, paint)
                else -> {
                    c.save()
                    c.translate(sx, p.y)
                    c.rotate(if (p.kind == 2) p.rot else p.rot * 0.15f)
                    c.scale(p.size, p.size)
                    c.drawPath(if (p.kind == 2) star else heart, paint)
                    c.restore()
                }
            }
        }
        if (running) postInvalidateOnAnimation()
    }
}
