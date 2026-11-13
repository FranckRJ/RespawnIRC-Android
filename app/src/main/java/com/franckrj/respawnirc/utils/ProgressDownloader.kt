package com.franckrj.respawnirc.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ProgressDownloader {
    suspend fun download(
        url: String,
        onProgress: suspend (bytesRead: Long, expectedLength: Long?) -> Unit
    ): ByteArray = withContext(Dispatchers.IO) {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        try {
            val announcedLength: Int = connection.contentLength // contentLengthLong demande l'API 24.
            val expectedLength: Long? = if (announcedLength == -1) null else announcedLength.toLong()
            val downloadedBytes = ByteArrayOutputStream()
            val buffer = ByteArray(8_192)
            var totalBytesRead = 0L

            connection.inputStream.use { input ->
                while (true) {
                    ensureActive()

                    val bytesRead: Int = input.read(buffer)
                    if (bytesRead == -1) {
                        break
                    }

                    downloadedBytes.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                    onProgress(totalBytesRead, expectedLength)
                }
            }

            downloadedBytes.toByteArray()
        } finally {
            connection.disconnect()
        }
    }
}
