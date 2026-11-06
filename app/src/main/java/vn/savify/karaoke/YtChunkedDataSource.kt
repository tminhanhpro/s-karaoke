package vn.savify.karaoke

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.TransferListener
import java.io.IOException

/**
 * Tai video YouTube (googlevideo.com) theo tung DOAN NHO bang tham so ?range=a-b.
 *
 * Vi sao can: neu xin ca file bang 1 yeu cau lien tuc, may chu YouTube chi gui nhanh vai giay dau roi
 * "bop" toc do -> bo dem can -> hinh/tieng dung ~10 giay roi moi chay tiep. Xin tung doan 4 MB thi
 * moi doan deu duoc gui o toc do toi da (cach yt-dlp / NewPipe dung), nen phat lien tuc.
 *
 * Voi link khong phai googlevideo thi chuyen thang cho nguon goc (khong chia doan).
 */
class YtChunkedDataSource(
    private val factory: DataSource.Factory,
    private val chunkSize: Long = CHUNK
) : DataSource {

    private val listeners = ArrayList<TransferListener>()
    private var upstream: DataSource? = null
    private var passthrough = false
    private var spec: DataSpec? = null
    private var baseUrl = ""
    private var pos = 0L                              // vi tri byte tiep theo se doc (tuyet doi)
    private var chunkEnd = 0L                         // het doan hien tai (khong gom)
    private var limit = C.LENGTH_UNSET.toLong()       // het du lieu can doc (khong gom), UNSET = chua biet
    private var rn = 0
    private var chunkOpen = false

    override fun addTransferListener(transferListener: TransferListener) {
        listeners.add(transferListener)
        upstream?.addTransferListener(transferListener)
    }

    override fun open(dataSpec: DataSpec): Long {
        val url = dataSpec.uri.toString()
        val host = dataSpec.uri.host ?: ""
        val up = factory.createDataSource()
        for (l in listeners) up.addTransferListener(l)
        upstream = up
        spec = dataSpec

        if (!host.endsWith("googlevideo.com")) {
            passthrough = true
            return up.open(dataSpec)
        }
        passthrough = false
        baseUrl = url.replace(STRIP_RE, "")
        pos = dataSpec.position
        val clen = CLEN_RE.find(url)?.groupValues?.get(1)?.toLongOrNull() ?: C.LENGTH_UNSET.toLong()
        var lim = C.LENGTH_UNSET.toLong()
        if (dataSpec.length != C.LENGTH_UNSET.toLong()) lim = pos + dataSpec.length
        if (clen != C.LENGTH_UNSET.toLong() && (lim == C.LENGTH_UNSET.toLong() || clen < lim)) lim = clen
        limit = lim
        rn = 0
        openChunk()
        return if (limit != C.LENGTH_UNSET.toLong()) limit - pos else C.LENGTH_UNSET.toLong()
    }

    private fun openChunk() {
        val up = upstream ?: throw IOException("closed")
        val s = spec ?: throw IOException("closed")
        var end = pos + chunkSize
        if (limit != C.LENGTH_UNSET.toLong() && end > limit) end = limit
        chunkEnd = end
        rn++
        val sep = if (baseUrl.contains('?')) "&" else "?"
        val u = baseUrl + sep + "range=" + pos + "-" + (end - 1) + "&rn=" + rn
        val ds = s.buildUpon()
            .setUri(Uri.parse(u))
            .setPosition(0)
            .setLength(C.LENGTH_UNSET.toLong())
            .build()
        up.open(ds)
        chunkOpen = true
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        val up = upstream ?: throw IOException("closed")
        if (passthrough) return up.read(buffer, offset, length)
        if (length == 0) return 0
        while (true) {
            if (limit != C.LENGTH_UNSET.toLong() && pos >= limit) return C.RESULT_END_OF_INPUT
            val n = up.read(buffer, offset, length)
            if (n > 0) {
                pos += n
                return n
            }
            if (n == C.RESULT_END_OF_INPUT) {
                if (pos < chunkEnd) {
                    // doan ngan hon yeu cau => het file
                    limit = pos
                    return C.RESULT_END_OF_INPUT
                }
                if (limit != C.LENGTH_UNSET.toLong() && pos >= limit) return C.RESULT_END_OF_INPUT
                try {
                    up.close()
                } catch (e: IOException) {
                }
                chunkOpen = false
                openChunk()
                continue
            }
            return n
        }
    }

    override fun getUri(): Uri? = spec?.uri

    override fun getResponseHeaders(): Map<String, List<String>> =
        upstream?.responseHeaders ?: emptyMap()

    override fun close() {
        val up = upstream
        upstream = null
        spec = null
        chunkOpen = false
        try {
            up?.close()
        } finally {
        }
    }

    companion object {
        const val CHUNK = 4L * 1024 * 1024
        private val STRIP_RE = Regex("[?&](range|rn)=[^&]*")
        private val CLEN_RE = Regex("[?&]clen=(\\d+)")

        fun wrap(upstream: DataSource.Factory): DataSource.Factory =
            DataSource.Factory { YtChunkedDataSource(upstream) }
    }
}
