package vn.savify.karaoke

import android.content.Context
import android.content.SharedPreferences

class Settings(ctx: Context) {
    val sp: SharedPreferences = ctx.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** AUTO | EXOPLAYER | IFRAME */
    var backend: String
        get() = sp.getString("backend", "AUTO") ?: "AUTO"
        set(v) { sp.edit().putString("backend", v).apply() }

    var maxHeight: Int
        get() = sp.getInt("maxHeight", 720)
        set(v) { sp.edit().putInt("maxHeight", v).apply() }

    /** ALWAYS | LAST_30S | OFF */
    var tickerMode: String
        get() = sp.getString("tickerMode", "ALWAYS") ?: "ALWAYS"
        set(v) { sp.edit().putString("tickerMode", v).apply() }

    var tickerSpeed: Int
        get() = sp.getInt("tickerSpeed", 80)
        set(v) { sp.edit().putInt("tickerSpeed", v).apply() }

    var seedAutoplay: Boolean
        get() = sp.getBoolean("seedAutoplay", true)
        set(v) { sp.edit().putBoolean("seedAutoplay", v).apply() }

    var restoreQueue: Boolean
        get() = sp.getBoolean("restoreQueue", true)
        set(v) { sp.edit().putBoolean("restoreQueue", v).apply() }

    /** vi | en : language of the TV labels */
    var tvLang: String
        get() = sp.getString("tvLang", "vi") ?: "vi"
        set(v) { sp.edit().putString("tvLang", v).apply() }

    var token: String
        get() {
            val t = sp.getString("token", null)
            if (t != null) return t
            val n = Util.newToken()
            sp.edit().putString("token", n).apply()
            return n
        }
        set(v) { sp.edit().putString("token", v).apply() }
}
