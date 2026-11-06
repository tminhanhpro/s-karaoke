package vn.savify.karaoke

import android.util.Base64
import java.net.URI
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import org.json.JSONObject

object Util {
    val ID_RE = Regex("^[A-Za-z0-9_-]{11}$")
    const val UA = "Mozilla/5.0 (Linux; Android 11; TV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    fun thumb(id: String) = "https://i.ytimg.com/vi/$id/mqdefault.jpg"

    fun newToken(): String {
        val b = ByteArray(16)
        SecureRandom().nextBytes(b)
        return Base64.encodeToString(b, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }

    fun constEq(a: String?, b: String?): Boolean {
        if (a == null || b == null) return false
        return MessageDigest.isEqual(a.toByteArray(), b.toByteArray())
    }

    fun queryParam(q: String?, key: String): String? {
        if (q == null) return null
        for (part in q.split('&')) {
            val i = part.indexOf('=')
            if (i > 0 && part.substring(0, i) == key) {
                return try {
                    java.net.URLDecoder.decode(part.substring(i + 1), "UTF-8")
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

    /** Returns the 11-char video id or null. Host check is exact. */
    fun parseVideoId(input: String): String? {
        val s = input.trim()
        if (ID_RE.matches(s)) return s
        val uri = try {
            URI(if (s.contains("://")) s else "https://$s")
        } catch (e: Exception) {
            return null
        }
        val host = (uri.host ?: return null).lowercase()
        val okHost = host == "youtu.be" || host == "youtube.com" || host.endsWith(".youtube.com")
        if (!okHost) return null
        val segs = (uri.path ?: "").split('/').filter { it.isNotEmpty() }
        val first = segs.firstOrNull() ?: ""
        val cand: String? = when {
            host == "youtu.be" -> segs.firstOrNull()
            first == "watch" -> queryParam(uri.rawQuery, "v")
            first == "shorts" || first == "embed" || first == "live" || first == "v" -> segs.getOrNull(1)
            else -> queryParam(uri.rawQuery, "v")
        }
        return if (cand != null && ID_RE.matches(cand)) cand else null
    }
}

enum class PState { IDLE, RESOLVING, PLAYING, PAUSED, ERROR }

data class QueueItem(
    val itemId: String = UUID.randomUUID().toString(),
    val videoId: String,
    var title: String,
    var channel: String = "",
    var durationSec: Int = 0,
    val addedBy: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    var status: String = "PENDING",
    val source: String = "USER"
) {
    val thumbnailUrl: String get() = Util.thumb(videoId)

    fun toJson(): JSONObject = JSONObject()
        .put("itemId", itemId)
        .put("videoId", videoId)
        .put("title", title)
        .put("channel", channel)
        .put("thumbnailUrl", thumbnailUrl)
        .put("durationSec", durationSec)
        .put("addedBy", addedBy ?: JSONObject.NULL)
        .put("addedAtEpochMs", addedAt)
        .put("status", status)
        .put("source", source)

    companion object {
        fun fromJson(o: JSONObject): QueueItem = QueueItem(
            itemId = o.getString("itemId"),
            videoId = o.getString("videoId"),
            title = o.optString("title"),
            channel = o.optString("channel"),
            durationSec = o.optInt("durationSec"),
            addedBy = if (o.isNull("addedBy")) null else o.getString("addedBy"),
            addedAt = o.optLong("addedAtEpochMs"),
            source = o.optString("source", "USER")
        )
    }
}

class ApiError(val status: Int, val code: String, message: String) : Exception(message)
