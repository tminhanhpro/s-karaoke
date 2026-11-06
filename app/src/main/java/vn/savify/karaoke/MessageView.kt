package vn.savify.karaoke

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * Tin nhan chu chay gui tu dien thoai: hien thanh mot the mau (ten nguoi gui + noi dung)
 * chay tu phai sang trai tren man hinh. 3 lan chay (lane) de nhieu tin khong de len nhau.
 * Moi tin duoc ve san vao Bitmap mot lan, nen khung hinh rat nhe. Het tin thi tu dung vong lap ve.
 */
class MessageView(ctx: Context) : View(ctx) {

    private class Msg(val bmp: Bitmap, val hue: Int) {
        var x = 0f
        var lane = 0
        var age = 0f
        var started = false
    }

    private val d = resources.displayMetrics.density
    private val active = ArrayList<Msg>()
    private val pending = ArrayList<Msg>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private val laneTail = FloatArray(3) { -1e9f }   // toa do mep phai cua tin cuoi cung o moi lan
    private val laneY = floatArrayOf(0.20f, 0.31f, 0.42f)
    private var last = 0L
    private var running = false
    private var nextHue = 0

    // cap mau (nen, vien) cho the tin nhan
    private val palette = arrayOf(
        intArrayOf(0xE6E91E63.toInt(), 0xFFFFB3CF.toInt()),
        intArrayOf(0xE6039BE5.toInt(), 0xFFB3E5FC.toInt()),
        intArrayOf(0xE67E57C2.toInt(), 0xFFD1C4E9.toInt()),
        intArrayOf(0xE62E9E4F.toInt(), 0xFFC8F5D4.toInt()),
        intArrayOf(0xE6F57C00.toInt(), 0xFFFFE0B2.toInt())
    )

    init {
        isFocusable = false
        isClickable = false
        setWillNotDraw(false)
    }

    private fun vw() = if (width > 0) width.toFloat() else 1920f
    private fun vh() = if (height > 0) height.toFloat() else 1080f

    private fun render(sender: String, text: String): Bitmap {
        val unit = min(vw(), vh())
        val msgSize = unit * 0.062f
        val nameSize = msgSize * 0.5f
        val padX = msgSize * 0.8f
        val padY = msgSize * 0.38f
        val pal = palette[nextHue % palette.size]

        val pm = Paint(Paint.ANTI_ALIAS_FLAG)
        pm.textSize = msgSize
        pm.typeface = Typeface.DEFAULT_BOLD
        val pn = Paint(Paint.ANTI_ALIAS_FLAG)
        pn.textSize = nameSize
        pn.typeface = Typeface.DEFAULT_BOLD

        val name = sender.trim()
        val mw = pm.measureText(text)
        val nw = if (name.isEmpty()) 0f else pn.measureText(name)
        val w = (max(mw, nw) + padX * 2f).toInt().coerceAtMost((vw() * 3f).toInt())
        val nameH = if (name.isEmpty()) 0f else nameSize * 1.25f
        val h = (padY * 2f + nameH + msgSize * 1.2f).toInt()

        val b = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        val rr = h / 2f
        val rect = RectF(3f, 3f, w - 3f, h - 3f)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG)
        bg.color = pal[0]
        c.drawRoundRect(rect, rr, rr, bg)
        bg.style = Paint.Style.STROKE
        bg.strokeWidth = max(3f, d * 2f)
        bg.color = pal[1]
        c.drawRoundRect(rect, rr, rr, bg)

        pn.color = pal[1]
        pm.color = 0xFFFFFFFF.toInt()
        pm.setShadowLayer(msgSize * 0.06f, 0f, msgSize * 0.04f, 0x99000000.toInt())
        var y = padY
        if (name.isNotEmpty()) {
            y += nameSize
            c.drawText(name, padX, y, pn)
            y += nameSize * 0.25f
        }
        y += msgSize * 0.95f
        c.drawText(text, padX, y, pm)
        return b
    }

    /** Them mot tin moi (goi tren luong chinh). */
    fun show(sender: String, text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        val m = Msg(render(sender, t), nextHue)
        nextHue++
        if (pending.size >= 8) pending.removeAt(0).bmp.recycle()
        pending.add(m)
        if (!running) {
            running = true
            last = 0L
            postInvalidateOnAnimation()
        }
    }

    override fun onDetachedFromWindow() {
        running = false
        super.onDetachedFromWindow()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if ((active.isNotEmpty() || pending.isNotEmpty()) && !running) {
            running = true
            last = 0L
            postInvalidateOnAnimation()
        }
    }

    override fun onDraw(c: Canvas) {
        val now = System.nanoTime()
        val dt = if (last == 0L) 0.016f else ((now - last) / 1e9f).coerceAtMost(0.05f)
        last = now
        val w = vw()
        val h = vh()
        val speed = w * 0.17f   // px/giay: ~11 giay de chay het mot man hinh 1920px

        // dua tin cho vao lan nao da trong
        if (pending.isNotEmpty()) {
            for (lane in 0 until 3) {
                if (pending.isEmpty()) break
                if (laneTail[lane] < w * 0.88f) {
                    val m = pending.removeAt(0)
                    m.lane = lane
                    m.x = w + 10f
                    laneTail[lane] = m.x + m.bmp.width
                    active.add(m)
                }
            }
        }

        for (i in 0 until 3) laneTail[i] = -1e9f
        var wi = 0
        for (ri in 0 until active.size) {
            val m = active[ri]
            m.x -= speed * dt
            if (m.x + m.bmp.width < -20f) {
                m.bmp.recycle()
                continue
            }
            laneTail[m.lane] = max(laneTail[m.lane], m.x + m.bmp.width)
            active[wi++] = m
            val cy = h * laneY[m.lane]
            c.drawBitmap(m.bmp, m.x, cy - m.bmp.height / 2f, paint)
        }
        while (active.size > wi) active.removeAt(active.size - 1)

        if (active.isNotEmpty() || pending.isNotEmpty()) postInvalidateOnAnimation()
        else { running = false; last = 0L }
    }
}
