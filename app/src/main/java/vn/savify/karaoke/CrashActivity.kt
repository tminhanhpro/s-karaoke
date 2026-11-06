package vn.savify.karaoke

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Man hinh bao loi TOAN MAN HINH, chay o tien trinh rieng (":crash") nen van o lai
 * du app chinh da sap. Chi tat khi bam "Dong" hoac "Chay lai". Co the cuon bang phim len/xuong.
 */
@Suppress("DEPRECATION")
class CrashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

        val dm = resources.displayMetrics
        val pad = (dm.widthPixels * 0.03f).toInt()
        val all = CrashLog.read(this)
        val text = if (all == null) "(Không có nội dung lỗi / No error text)" else {
            val idx = ArrayList<Int>()
            var k = all.indexOf("[")
            while (k >= 0) { if (k == 0 || all[k - 1] == '\n') idx.add(k); k = all.indexOf("[", k + 1) }
            if (idx.size >= 2) all.substring(idx[idx.size - 2]) else all
        }

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.parseColor("#1A0000"))
        root.setPadding(pad, pad, pad, pad)

        val title = TextView(this)
        title.text = "LỖI / ERROR – chụp ảnh màn hình này gửi cho người hỗ trợ"
        title.setTextColor(Color.YELLOW)
        title.setTextSize(22f)
        title.typeface = Typeface.DEFAULT_BOLD
        root.addView(title, LinearLayout.LayoutParams(-1, -2))

        val ver = TextView(this)
        ver.text = "Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT}) | " +
            "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL} | App " + try { packageManager.getPackageInfo(packageName, 0).versionName } catch (e: Throwable) { "?" }
        ver.setTextColor(Color.LTGRAY)
        ver.setTextSize(16f)
        ver.setPadding(0, 6, 0, 6)
        root.addView(ver, LinearLayout.LayoutParams(-1, -2))

        val sv = ScrollView(this)
        sv.isFocusable = true
        sv.isFocusableInTouchMode = true
        sv.isScrollbarFadingEnabled = false
        sv.setBackgroundColor(Color.BLACK)
        val tv = TextView(this)
        tv.text = text
        tv.setTextColor(Color.WHITE)
        tv.setTextSize(16f)
        tv.typeface = Typeface.MONOSPACE
        tv.setPadding(12, 12, 12, 12)
        sv.addView(tv, LinearLayout.LayoutParams(-1, -2))
        root.addView(sv, LinearLayout.LayoutParams(-1, 0, 1f))

        val row = LinearLayout(this)
        row.orientation = LinearLayout.HORIZONTAL
        row.gravity = Gravity.CENTER
        row.setPadding(0, 10, 0, 0)
        val bRetry = Button(this)
        bRetry.text = "Chạy lại / Retry"
        bRetry.setOnClickListener {
            CrashLog.clear(this)
            val i = Intent(this, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(i)
            finish()
        }
        val bClose = Button(this)
        bClose.text = "Đóng / Close"
        bClose.setOnClickListener {
            CrashLog.clear(this)
            finishAndRemoveTask()
        }
        row.addView(bRetry)
        row.addView(bClose)
        root.addView(row, LinearLayout.LayoutParams(-1, -2))

        setContentView(root)
        bRetry.requestFocus()
    }

    // Phim len/xuong tren remote luon cuon duoc noi dung loi
    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent): Boolean {
        val sv = (window.decorView.findViewById<View>(android.R.id.content) as? android.view.ViewGroup)
            ?.let { findScroll(it) }
        if (sv != null) {
            when (keyCode) {
                android.view.KeyEvent.KEYCODE_DPAD_UP -> { sv.smoothScrollBy(0, -200); return true }
                android.view.KeyEvent.KEYCODE_DPAD_DOWN -> { sv.smoothScrollBy(0, 200); return true }
                android.view.KeyEvent.KEYCODE_PAGE_UP -> { sv.smoothScrollBy(0, -600); return true }
                android.view.KeyEvent.KEYCODE_PAGE_DOWN -> { sv.smoothScrollBy(0, 600); return true }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun findScroll(v: View): ScrollView? {
        if (v is ScrollView) return v
        if (v is android.view.ViewGroup) for (i in 0 until v.childCount) findScroll(v.getChildAt(i))?.let { return it }
        return null
    }

    override fun onBackPressed() {
        // khong cho thoat nham bang nut Back; dung nut Dong
    }
}
