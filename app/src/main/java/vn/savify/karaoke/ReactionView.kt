package vn.savify.karaoke

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/** Danh sach ma hieu ung hop le (phai khop voi REACTIONS trong app.js cua remote). */
object ReactIds {
    val ALL: Set<String> = setOf(
        "heart", "clap", "party", "fire", "star", "laugh", "love", "mic",
        "rose", "firework", "like", "beer", "crown", "dance", "balloon", "cry"
    )
}

/**
 * Lop hieu ung cam xuc phu len tren video: tha tim, vo tay, phao giay, phao hoa...
 * Chi ve khi co hat; het hieu ung thi tu dung vong lap ve (khong ton CPU).
 */
class ReactionView(ctx: Context) : View(ctx) {

    private class Part(
        var kind: Int,            // 0 emoji, 1 giay confetti, 3 tia lua, 5 vong song
        var emoji: String? = null,
        var x: Float = 0f, var y: Float = 0f,
        var vx: Float = 0f, var vy: Float = 0f,
        var g: Float = 0f, var drag: Float = 0f,
        var size: Float = 0f, var size2: Float = 0f,
        var rot: Float = 0f, var vrot: Float = 0f,
        var life: Float = 2f, var delay: Float = 0f,
        var env: Int = 0,         // 0: mo dan cuoi, 1: pop, 2: khong doi, 3: phong ra roi mo dan
        var color: Int = Color.WHITE,
        var sway: Float = 0f, var swayS: Float = 0f, var phase: Float = 0f,
        var flip: Boolean = false, var flipS: Float = 0f,
        var wobA: Float = 0f, var wobS: Float = 0f,
        var pulse: Float = 0f,
        var hopH: Float = 0f, var hopS: Float = 0f,
        var mt: Float = 0f, var sx: Float = 0f, var sy: Float = 0f, var tx: Float = 0f, var ty: Float = 0f, var ease: Int = 0,
        var end: Int = 0          // 1: no phao hoa, 2: va cham (cung ly)
    ) {
        var age = 0f
        var px = 0f
        var py = 0f
    }

    private val d = resources.displayMetrics.density
    private val parts = ArrayList<Part>()
    private val pending = ArrayList<Part>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val rectF = RectF()
    private val cache = HashMap<String, Bitmap>()
    private val maxParts = 450
    private var last = 0L
    private var running = false

    private val confettiColors = intArrayOf(
        0xFFFF4D8D.toInt(), 0xFFFFD43B.toInt(), 0xFF29D3FF.toInt(), 0xFFFF9F1C.toInt(),
        0xFF5CE65C.toInt(), 0xFFB36BFF.toInt(), 0xFFFF4D4D.toInt(), 0xFFFFFFFF.toInt()
    )
    private val fireColors = intArrayOf(
        0xFFFF5C8A.toInt(), 0xFFFFD43B.toInt(), 0xFF4DD2FF.toInt(), 0xFF7CFF6B.toInt(),
        0xFFFF9F1C.toInt(), 0xFFC77DFF.toInt(), 0xFFFFFFFF.toInt()
    )

    init {
        isFocusable = false
        isClickable = false
        setWillNotDraw(false)
    }

    // ------------------------------------------------------------------ helpers

    private fun vw() = if (width > 0) width.toFloat() else 1920f
    private fun vh() = if (height > 0) height.toFloat() else 1080f
    private fun rnd(a: Float, b: Float) = a + Random.nextFloat() * (b - a)
    private fun big() = min(vw(), vh()) * 0.42f

    private fun bmp(e: String): Bitmap = cache.getOrPut(e) {
        val s = 192
        val b = Bitmap.createBitmap(s, s, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.textSize = s * 0.78f
        p.textAlign = Paint.Align.CENTER
        val fm = p.fontMetrics
        c.drawText(e, s / 2f, s / 2f - (fm.ascent + fm.descent) / 2f, p)
        b
    }

    private fun add(p: Part) {
        if (parts.size + pending.size < maxParts) pending.add(p)
    }

    private fun flush() {
        if (pending.isNotEmpty()) {
            parts.addAll(pending)
            pending.clear()
        }
        if (parts.isNotEmpty() && !running) {
            running = true
            last = 0L
            postInvalidateOnAnimation()
        }
    }

    private fun ease(kind: Int, u: Float): Float = when (kind) {
        1 -> { // nay (bounce)
            var t = u
            if (t < 1f / 2.75f) 7.5625f * t * t
            else if (t < 2f / 2.75f) { t -= 1.5f / 2.75f; 7.5625f * t * t + 0.75f }
            else if (t < 2.5f / 2.75f) { t -= 2.25f / 2.75f; 7.5625f * t * t + 0.9375f }
            else { t -= 2.625f / 2.75f; 7.5625f * t * t + 0.984375f }
        }
        2 -> { // vot qua roi ve (back)
            val c1 = 1.70158f
            val c3 = c1 + 1f
            1f + c3 * (u - 1f).pow(3) + c1 * (u - 1f).pow(2)
        }
        else -> 1f - (1f - u) * (1f - u)
    }

    // ------------------------------------------------------------------ builders

    /** Mot emoji bay tu do (roi, bay len, no ra...). Don vi: px, px/s. */
    private fun emo(
        e: String, x: Float, y: Float, size: Float,
        vx: Float = 0f, vy: Float = 0f, g: Float = 0f, drag: Float = 0f,
        life: Float = 2f, delay: Float = 0f, env: Int = 0,
        sway: Float = 0f, rotSpeed: Float = 0f, rot: Float = 0f
    ): Part {
        val p = Part(0, emoji = e, x = x, y = y, vx = vx, vy = vy, g = g, drag = drag, size = size,
            life = life, delay = delay, env = env, sway = sway, swayS = rnd(0.8f, 2.2f), phase = rnd(0f, 6.28f),
            rot = rot, vrot = rotSpeed)
        add(p)
        return p
    }

    /** Mua emoji roi tu tren xuong. */
    private fun rain(list: Array<String>, count: Int, dpMin: Float, dpMax: Float, spread: Float, vMin: Float = 230f, vMax: Float = 420f) {
        repeat(count) {
            val sz = rnd(dpMin, dpMax) * d
            val vy = rnd(vMin, vMax) * d
            val life = (vh() + sz * 2f) / vy
            emo(list.random(), rnd(0f, vw()), -sz, sz, vy = vy, life = life, delay = rnd(0f, spread), env = 2,
                sway = rnd(10f, 40f) * d, rotSpeed = rnd(-40f, 40f))
        }
    }

    /** Emoji bay tu duoi len. */
    private fun rise(list: Array<String>, count: Int, dpMin: Float, dpMax: Float, spread: Float, vMin: Float = 200f, vMax: Float = 380f) {
        repeat(count) {
            val sz = rnd(dpMin, dpMax) * d
            val vy = -rnd(vMin, vMax) * d
            val life = (vh() + sz * 2f) / -vy
            emo(list.random(), rnd(0f, vw()), vh() + sz, sz, vy = vy, life = life, delay = rnd(0f, spread), env = 2,
                sway = rnd(12f, 50f) * d, rotSpeed = rnd(-25f, 25f))
        }
    }

    /** Emoji khong lo o giua man hinh, nay len (pop) roi mo dan. */
    private fun pop(e: String, scale: Float = 1f, life: Float = 2.6f, wobble: Float = 6f, pulse: Float = 0f, delay: Float = 0f, cy: Float = 0.5f): Part {
        val sz = big() * scale
        val p = Part(0, emoji = e, x = vw() / 2f, y = vh() * cy, size = sz, life = life, delay = delay, env = 1,
            wobA = wobble, wobS = 7f, pulse = pulse)
        add(p)
        return p
    }

    /** Vong song lan toa ra phia sau emoji khong lo. */
    private fun ring(color: Int, delay: Float = 0f) {
        val p = Part(5, x = vw() / 2f, y = vh() / 2f, size = big() * 0.35f, size2 = big() * 1.25f, life = 0.9f, delay = delay, color = color)
        add(p)
    }

    /** No ban ra xung quanh mot diem. */
    private fun burst(list: Array<String>, x: Float, y: Float, count: Int, dpMin: Float, dpMax: Float, vMin: Float = 350f, vMax: Float = 950f, delay: Float = 0f, gdp: Float = 380f) {
        repeat(count) {
            val a = rnd(0f, 6.2832f)
            val v = rnd(vMin, vMax) * d
            val sz = rnd(dpMin, dpMax) * d
            emo(list.random(), x, y, sz, vx = cos(a) * v, vy = sin(a) * v, g = gdp * d, drag = 1.7f,
                life = rnd(1.4f, 2.3f), delay = delay, env = 3, rotSpeed = rnd(-240f, 240f))
        }
    }

    /** Giay (confetti) + dai ruy bang ban ra tu mot goc duoi man hinh. */
    private fun cannon(fromLeft: Boolean, count: Int, delay: Float) {
        val ox = if (fromLeft) vw() * 0.03f else vw() * 0.97f
        val oy = vh() * 1.02f
        repeat(count) {
            val deg = rnd(52f, 82f)
            val a = Math.toRadians(deg.toDouble()).toFloat()
            val v = rnd(1000f, 2300f) * d
            val dir = if (fromLeft) 1f else -1f
            val streamer = Random.nextInt(100) < 22
            val p = Part(1,
                x = ox, y = oy,
                vx = cos(a) * v * dir, vy = -sin(a) * v,
                g = 430f * d, drag = 2.2f,
                size = if (streamer) rnd(4f, 6f) * d else rnd(8f, 16f) * d,
                size2 = if (streamer) rnd(26f, 44f) * d else rnd(5f, 10f) * d,
                rot = rnd(0f, 360f), vrot = rnd(-420f, 420f),
                life = rnd(3.2f, 4.8f), delay = delay + rnd(0f, 0.25f), env = 0,
                color = confettiColors.random(), sway = rnd(10f, 30f) * d, swayS = rnd(1.5f, 4f), phase = rnd(0f, 6.28f),
                flipS = rnd(6f, 14f))
            add(p)
        }
    }

    /** Phao hoa: ten lua bay len roi no thanh nhieu tia sang. */
    private fun rocket(delay: Float) {
        val sx = rnd(vw() * 0.15f, vw() * 0.85f)
        val ty = rnd(vh() * 0.18f, vh() * 0.5f)
        val p = Part(3, x = sx, y = vh(), size = 3.5f * d, life = rnd(0.75f, 1.05f), delay = delay, env = 2,
            color = Color.WHITE, mt = 1f, sx = sx, sy = vh(), tx = sx + rnd(-60f, 60f) * d, ty = ty, ease = 0, end = 1)
        p.mt = p.life
        add(p)
    }

    private fun explode(x: Float, y: Float, c1: Int) {
        val c2 = fireColors.random()
        val n = 44
        for (i in 0 until n) {
            val a = (2 * PI * i / n).toFloat() + rnd(-0.08f, 0.08f)
            val ring2 = i % 2 == 0
            val v = (if (ring2) rnd(420f, 560f) else rnd(230f, 330f)) * d
            add(Part(3, x = x, y = y, vx = cos(a) * v, vy = sin(a) * v, g = 170f * d, drag = 1.6f,
                size = rnd(2.8f, 4.6f) * d, life = rnd(1.2f, 1.9f), env = 0, color = if (ring2) c1 else c2))
        }
        repeat(8) {
            val a = rnd(0f, 6.28f)
            val v = rnd(60f, 220f) * d
            add(Part(3, x = x, y = y, vx = cos(a) * v, vy = sin(a) * v, g = 120f * d, drag = 1.2f,
                size = rnd(2f, 3.4f) * d, life = rnd(1.4f, 2.2f), env = 0, color = Color.WHITE))
        }
    }

    /** Cung ly cham nhau: nuoc/sao bung ra va giay no. */
    private fun impact(x: Float, y: Float) {
        burst(arrayOf("✨", "💦", "⭐"), x, y, 16, 34f, 70f, 300f, 800f)
        repeat(26) {
            val a = rnd(0f, 6.28f)
            val v = rnd(300f, 900f) * d
            add(Part(1, x = x, y = y, vx = cos(a) * v, vy = sin(a) * v - 250f * d, g = 430f * d, drag = 2f,
                size = rnd(8f, 14f) * d, size2 = rnd(5f, 9f) * d, rot = rnd(0f, 360f), vrot = rnd(-400f, 400f),
                life = rnd(1.8f, 2.8f), env = 0, color = confettiColors.random(), flipS = rnd(6f, 14f)))
        }
    }

    // ------------------------------------------------------------------ public

    fun fire(id: String) {
        val w = vw()
        val h = vh()
        val big = big()
        when (id) {
            "heart" -> {
                rain(arrayOf("❤️", "💖", "💗", "💕", "💝"), 18, 70f, 150f, 2.0f)
                rain(arrayOf("❤️", "💗"), 16, 28f, 56f, 2.4f, 300f, 520f)
            }
            "clap" -> {
                ring(0xFFFFE066.toInt())
                pop("👏", 1f, 2.8f, wobble = 5f, pulse = 0.10f)
                burst(arrayOf("✨", "⭐", "👏"), w / 2, h / 2, 16, 30f, 70f, delay = 0.1f)
                cannon(true, 22, 0.25f); cannon(false, 22, 0.25f)
            }
            "party" -> {
                cannon(true, 70, 0f); cannon(false, 70, 0f)
                cannon(true, 40, 0.7f); cannon(false, 40, 0.7f)
                burst(arrayOf("🎉", "🎊"), w * 0.05f, h * 0.95f, 5, 60f, 110f, 900f, 1500f, 0f, 500f)
                burst(arrayOf("🎉", "🎊"), w * 0.95f, h * 0.95f, 5, 60f, 110f, 900f, 1500f, 0f, 500f)
            }
            "fire" -> {
                rise(arrayOf("🔥", "🔥", "🔥", "🧡"), 26, 70f, 170f, 1.8f, 260f, 520f)
            }
            "star" -> {
                ring(0xFFFFF3B0.toInt())
                pop("⭐", 0.9f, 2.4f, wobble = 12f)
                burst(arrayOf("⭐", "🌟", "✨"), w / 2, h / 2, 34, 36f, 96f, 450f, 1100f, 0.05f, 300f)
            }
            "laugh" -> {
                pop("😂", 1f, 2.6f, wobble = 9f, pulse = 0.05f)
                burst(arrayOf("😂", "🤣"), w / 2, h / 2, 14, 50f, 100f, 500f, 1000f, 0.1f, 450f)
                rain(arrayOf("😂", "🤣"), 10, 50f, 100f, 1.6f)
            }
            "love" -> {
                ring(0xFFFF8FB5.toInt())
                pop("😍", 0.95f, 2.6f, wobble = 6f, pulse = 0.04f)
                rise(arrayOf("❤️", "😍", "💘", "💖"), 22, 60f, 130f, 1.8f)
            }
            "mic" -> {
                pop("🎤", 0.85f, 2.6f, wobble = 14f)
                repeat(22) {
                    val a = Math.toRadians(rnd(205f, 335f).toDouble()).toFloat()
                    val v = rnd(450f, 1000f) * d
                    emo(listOf("🎵", "🎶", "🎼").random(), w / 2, h * 0.5f, rnd(48f, 100f) * d,
                        vx = cos(a) * v, vy = sin(a) * v, g = 260f * d, drag = 1.2f, life = rnd(1.8f, 2.8f),
                        delay = rnd(0.1f, 1.0f), env = 3, rotSpeed = rnd(-90f, 90f))
                }
            }
            "rose" -> {
                rain(arrayOf("🌹", "🌹", "🌸", "💮"), 28, 60f, 130f, 2.2f, 190f, 340f)
            }
            "firework" -> {
                for (i in 0 until 6) rocket(i * 0.45f)
            }
            "like" -> {
                ring(0xFF7CC8FF.toInt())
                pop("👍", 1f, 2.6f, wobble = 8f, pulse = 0.06f)
                burst(arrayOf("✨", "💯", "👍"), w / 2, h / 2, 18, 36f, 80f, delay = 0.1f)
                cannon(true, 26, 0.2f); cannon(false, 26, 0.2f)
            }
            "beer" -> {
                val sz = big * 0.7f
                val cy = h * 0.5f
                val gap = sz * 0.42f
                val l = emo("🍺", -sz, cy, sz, life = 2.6f, env = 2)
                l.mt = 0.55f; l.sx = -sz; l.sy = cy; l.tx = w / 2 - gap; l.ty = cy; l.ease = 2; l.rot = 14f; l.end = 2
                val r = emo("🍺", w + sz, cy, sz, life = 2.6f, env = 2)
                r.mt = 0.55f; r.sx = w + sz; r.sy = cy; r.tx = w / 2 + gap; r.ty = cy; r.ease = 2; r.rot = -14f; r.flip = true
                // lam mo dan o cuoi bang env 0 (ghi de env 2)
                l.env = 0; r.env = 0
            }
            "crown" -> {
                val sz = big * 0.95f
                val c = emo("👑", w / 2, -sz, sz, life = 3.0f, env = 0)
                c.mt = 0.9f; c.sx = w / 2; c.sy = -sz; c.tx = w / 2; c.ty = h * 0.5f; c.ease = 1
                ring(0xFFFFD43B.toInt(), 0.7f)
                rain(arrayOf("⭐", "✨", "🌟"), 20, 40f, 90f, 2.0f, 200f, 360f)
            }
            "dance" -> {
                val list = arrayOf("💃", "🕺")
                for (i in 0 until 5) {
                    val sz = rnd(90f, 140f) * d
                    val fromLeft = i % 2 == 0
                    val speed = rnd(w * 0.18f, w * 0.28f)
                    val p = emo(list[i % 2], if (fromLeft) -sz else w + sz, h - sz * 0.7f, sz,
                        vx = if (fromLeft) speed else -speed, life = (w + sz * 2f) / speed, delay = i * 0.35f, env = 2)
                    p.hopH = rnd(0.35f, 0.6f) * sz; p.hopS = rnd(6f, 9f)
                    p.wobA = 12f; p.wobS = rnd(6f, 9f)
                }
                rise(arrayOf("🎶", "🎵", "✨"), 18, 40f, 90f, 2.0f, 180f, 320f)
            }
            "balloon" -> {
                rise(arrayOf("🎈"), 16, 90f, 170f, 2.0f, 150f, 300f)
                rise(arrayOf("🎈", "🎀"), 6, 50f, 90f, 2.4f, 180f, 340f)
            }
            "cry" -> {
                pop("😭", 0.95f, 2.8f, wobble = 5f, pulse = 0.03f)
                rain(arrayOf("💧"), 28, 34f, 62f, 2.0f, 260f, 460f)
            }
        }
        flush()
    }

    // ------------------------------------------------------------------ lifecycle

    override fun onDetachedFromWindow() {
        running = false
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parts.isNotEmpty() && !running) {
            running = true
            last = 0L
            postInvalidateOnAnimation()
        }
    }

    // ------------------------------------------------------------------ draw

    override fun onDraw(c: Canvas) {
        val now = System.nanoTime()
        val dt = if (last == 0L) 0.016f else ((now - last) / 1e9f).coerceAtMost(0.05f)
        last = now

        var wi = 0
        for (ri in 0 until parts.size) {
            val p = parts[ri]
            p.age += dt
            val a = p.age - p.delay
            if (a < 0f) { parts[wi++] = p; continue }
            if (a >= p.life) {
                when (p.end) {
                    1 -> explode(p.x, p.y, fireColors.random())
                    2 -> impact(p.x, p.y)
                }
                continue
            }
            p.px = p.x; p.py = p.y
            if (p.mt > 0f) {
                val e = ease(p.ease, min(1f, a / p.mt))
                p.x = p.sx + (p.tx - p.sx) * e
                p.y = p.sy + (p.ty - p.sy) * e
            } else {
                val k = exp(-p.drag * dt)
                p.vx *= k
                p.vy = p.vy * k + p.g * dt
                p.x += p.vx * dt
                p.y += p.vy * dt
            }
            p.rot += p.vrot * dt
            parts[wi++] = p
            drawPart(c, p, a)
        }
        while (parts.size > wi) parts.removeAt(parts.size - 1)
        if (pending.isNotEmpty()) { parts.addAll(pending); pending.clear() }

        if (parts.isNotEmpty() && running) postInvalidateOnAnimation()
        else { running = false; last = 0L }
    }

    private fun drawPart(c: Canvas, p: Part, a: Float) {
        val u = a / p.life
        var alpha = 1f
        var sc = 1f
        when (p.env) {
            0 -> if (u > 0.65f) alpha = ((1f - u) / 0.35f).coerceIn(0f, 1f)
            1 -> {
                sc = when {
                    u < 0.14f -> ease(2, u / 0.14f)
                    u > 0.76f -> 1f - ((u - 0.76f) / 0.24f) * 0.4f
                    else -> 1f
                }
                if (u > 0.76f) alpha = ((1f - u) / 0.24f).coerceIn(0f, 1f)
                if (p.pulse > 0f) sc *= 1f + p.pulse * sin(a * 15f)
            }
            3 -> {
                sc = if (u < 0.12f) 0.2f + 0.8f * (u / 0.12f) else 1f
                if (u > 0.6f) alpha = ((1f - u) / 0.4f).coerceIn(0f, 1f)
            }
        }
        val dx = if (p.sway != 0f) sin(a * p.swayS + p.phase) * p.sway else 0f
        val dy = if (p.hopH > 0f) -abs(sin(a * p.hopS)) * p.hopH else 0f
        val rot = p.rot + (if (p.wobA != 0f) sin(a * p.wobS) * p.wobA else 0f)

        when (p.kind) {
            0 -> {
                val b = bmp(p.emoji ?: return)
                val s = p.size * sc / 192f
                paint.alpha = (alpha * 255f).toInt()
                paint.style = Paint.Style.FILL
                c.save()
                c.translate(p.x + dx, p.y + dy)
                c.rotate(rot)
                c.scale(if (p.flip) -s else s, s)
                c.drawBitmap(b, -96f, -96f, paint)
                c.restore()
            }
            1 -> {
                paint.style = Paint.Style.FILL
                paint.color = p.color
                paint.alpha = (alpha * 255f).toInt()
                val f = cos(a * p.flipS + p.phase)
                c.save()
                c.translate(p.x + dx, p.y)
                c.rotate(rot)
                c.scale(1f, if (abs(f) < 0.12f) 0.12f else f)
                rectF.set(-p.size / 2f, -p.size2 / 2f, p.size / 2f, p.size2 / 2f)
                c.drawRect(rectF, paint)
                c.restore()
            }
            3 -> {
                paint.color = p.color
                paint.alpha = (alpha * 255f).toInt()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = p.size
                paint.strokeCap = Paint.Cap.ROUND
                c.drawLine(p.px, p.py, p.x, p.y, paint)
                paint.style = Paint.Style.FILL
                c.drawCircle(p.x, p.y, p.size * 0.7f, paint)
            }
            5 -> {
                val r = p.size + (p.size2 - p.size) * ease(0, u)
                paint.color = p.color
                paint.alpha = ((1f - u) * 200f).toInt().coerceIn(0, 255)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = max(2f, 14f * d * (1f - u))
                c.drawCircle(p.x, p.y, r, paint)
                paint.style = Paint.Style.FILL
            }
        }
    }
}
