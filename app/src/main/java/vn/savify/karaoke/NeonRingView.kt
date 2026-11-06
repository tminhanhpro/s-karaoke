package vn.savify.karaoke

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SweepGradient
import android.view.View

/**
 * Dai sang NEON lien mach chay sat vien trang cua ma QR.
 *
 * - Mot dai duy nhat (khong con bong den roi rac), mau doi lien tuc theo cau vong (gradient khep kin
 *   nen khong co diem noi), cac mau "chay" vong quanh QR.
 * - Duong ve nam ngay tren mep QR (qrBtn de len tren nen nua trong bi che) => dai sang dinh sat vien trang.
 * - Hieu ung phat sang (glow) bang nhieu lop net ve chong len nhau, khong dung BlurMaskFilter
 *   nen chay muot tren Android cu / TV box yeu.
 */
class NeonRingView(ctx: Context) : View(ctx) {

    private val d = resources.displayMetrics.density

    /** Khoang cach tu mep view den mep QR (phai bang neonPad trong MainActivity). */
    private val edge = 14f * d

    private class Layer(val widthDp: Float, val alpha: Int, val sat: Float, val paint: Paint = Paint(Paint.ANTI_ALIAS_FLAG))

    // Tu ngoai vao trong: 3 lop sang mo, 1 lop dai mau chinh, 1 loi sang (trang hon) o giua.
    private val layers = arrayOf(
        Layer(28f, 38, 0.95f),
        Layer(20f, 70, 0.95f),
        Layer(14f, 125, 0.95f),
        Layer(10f, 255, 0.90f),
        Layer(4.5f, 235, 0.30f)
    )

    private val rect = RectF()
    private val matrix = Matrix()
    private var cx = 0f
    private var cy = 0f
    private var ready = false

    init {
        for (l in layers) {
            l.paint.style = Paint.Style.STROKE
            l.paint.strokeWidth = l.widthDp * d
            l.paint.strokeJoin = Paint.Join.ROUND
        }
    }

    override fun onSizeChanged(w: Int, h: Int, ow: Int, oh: Int) {
        super.onSizeChanged(w, h, ow, oh)
        if (w <= 0 || h <= 0) return
        rect.set(edge, edge, w - edge, h - edge)
        cx = w / 2f
        cy = h / 2f
        for (l in layers) l.paint.shader = SweepGradient(cx, cy, rainbow(l.sat, l.alpha), null)
        ready = true
    }

    /** Day mau cau vong khep kin (mau dau = mau cuoi) => doi mau lien mach, khong co duong noi. */
    private fun rainbow(sat: Float, alpha: Int): IntArray {
        val steps = 24
        val hsv = FloatArray(3)
        return IntArray(steps + 1) { i ->
            hsv[0] = (i % steps) * 360f / steps
            hsv[1] = sat
            hsv[2] = 1f
            Color.HSVToColor(alpha, hsv)
        }
    }

    override fun onDraw(c: Canvas) {
        if (!ready) return
        // 1 vong quay het 4 giay: mau chay lien tuc quanh vien QR
        val t = (System.nanoTime() / 1_000_000L % 4000L).toFloat() / 4000f
        matrix.setRotate(t * 360f, cx, cy)
        for (l in layers) {
            val sh = l.paint.shader
            if (sh != null) {
                sh.setLocalMatrix(matrix)
                c.drawRect(rect, l.paint)
            }
        }
        if (isShown) postInvalidateOnAnimation()
    }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (isVisible) postInvalidateOnAnimation()
    }
}
