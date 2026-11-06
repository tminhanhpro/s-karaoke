package vn.savify.karaoke

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Process
import java.io.File
import java.util.Date

/** Saves the last error to a file so it can be shown on the TV screen at next launch. */
object CrashLog {
    private const val FILE = "crash.txt"
    @Volatile var appCtx: Context? = null
    private val seen = HashSet<String>()

    /** Ghi loi "mem" (khong lam sap app) de van hien o lan mo sau; moi loi chi ghi 1 lan. */
    fun soft(tag: String, t: Throwable) {
        val c = appCtx ?: return
        val key = tag + t.message
        synchronized(seen) { if (!seen.add(key)) return }
        // Loi "mem" da duoc app tu xu ly (co phuong an du phong) -> ghi file rieng, KHONG hien man hinh loi tren TV
        try {
            val f = File(c.filesDir, "soft.txt")
            if (f.exists() && f.length() > 20_000) f.delete()
            f.appendText("[$tag] ${Date()}\n${describe(t)}\n")
        } catch (e: Exception) {
        }
    }

    fun describe(t: Throwable): String {
        var root: Throwable = t
        var guard = 0
        while (root.cause != null && root.cause !== root && guard++ < 10) root = root.cause!!
        val sb = StringBuilder()
        sb.append(t.javaClass.name).append(": ").append(t.message).append('\n')
        if (root !== t) sb.append("ROOT: ").append(root.javaClass.name).append(": ").append(root.message).append('\n')
        for (f in root.stackTrace.take(14)) sb.append("  at ").append(f.toString()).append('\n')
        return sb.toString()
    }

    fun write(ctx: Context, tag: String, t: Throwable) {
        try {
            val f = File(ctx.filesDir, FILE)
            if (f.exists() && f.length() > 20_000) f.delete()
            f.appendText("[$tag] ${Date()}\n${describe(t)}\n")
        } catch (e: Exception) {
        }
    }

    /** Ghi mot dong thong tin loi tu do (vd: bai khong phat duoc) kem chi tiet. */
    fun writeText(ctx: Context, tag: String, text: String) {
        try {
            val f = File(ctx.filesDir, FILE)
            if (f.exists() && f.length() > 20_000) f.delete()
            f.appendText("[$tag] ${Date()}\n$text\n\n")
        } catch (e: Exception) {
        }
    }

    fun read(ctx: Context): String? {
        return try {
            val f = File(ctx.filesDir, FILE)
            if (f.exists()) f.readText().takeLast(6000) else null
        } catch (e: Exception) {
            null
        }
    }

    fun clear(ctx: Context) {
        try {
            File(ctx.filesDir, FILE).delete()
        } catch (e: Exception) {
        }
    }

    /** Mo man hinh bao loi toan man hinh (chay o tien trinh rieng nen khong bi mat khi app sap). */
    fun showScreen(ctx: Context) {
        try {
            val i = Intent(ctx, CrashActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ctx.startActivity(i)
        } catch (e: Throwable) {
        }
    }

    fun install(app: Application) {
        appCtx = app.applicationContext
        val prev = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { th, e ->
            try {
                write(app, "CRASH ${th.name}", e)
                showScreen(app)
                Thread.sleep(600)
            } catch (x: Throwable) {
                prev?.uncaughtException(th, e)
            }
            Process.killProcess(Process.myPid())
            System.exit(10)
        }
    }
}

class KaraokeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Tien trinh ":crash" chi dung de hien thi loi, khong khoi tao gi them
        if (isCrashProcess()) return
        CrashLog.install(this)
    }

    private fun isCrashProcess(): Boolean = try {
        if (android.os.Build.VERSION.SDK_INT >= 28) Application.getProcessName().endsWith(":crash") else false
    } catch (e: Throwable) {
        false
    }
}
