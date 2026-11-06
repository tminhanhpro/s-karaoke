package vn.savify.karaoke

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

/** Ban thay the cho cac ham Java moi (Android 13+). Duoc goi tu dong nho CompatFactory luc build. */
object Compat {
    @JvmStatic
    fun encode(s: String, cs: Charset): String = URLEncoder.encode(s, cs.name())

    @JvmStatic
    fun decode(s: String, cs: Charset): String = URLDecoder.decode(s, cs.name())

    @JvmStatic
    fun isBlank(s: String): Boolean = s.all { Character.isWhitespace(it) }

    @JvmStatic
    fun strip(s: String): String = s.trim { Character.isWhitespace(it) }

    @JvmStatic
    fun repeat(s: String, n: Int): String {
        require(n >= 0) { "count is negative: $n" }
        val sb = StringBuilder(s.length * n)
        for (i in 0 until n) sb.append(s)
        return sb.toString()
    }
}
