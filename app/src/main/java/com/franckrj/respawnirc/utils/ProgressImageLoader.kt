package com.franckrj.respawnirc.utils

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.franckrj.respawnirc.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.ByteBuffer
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.P)
class ProgressImageLoader(
    private val fragment: Fragment,
    private val viewForImage: ImageView,
    private val indeterminateProgressBar: ProgressBar? = null,
    private val determinateProgressBar: ProgressBar? = null,
    private val textForSizeOfImage: TextView? = null
) {
    private var currentLoad: Job? = null

    private fun onStart() {
        determinateProgressBar?.visibility = View.GONE
        indeterminateProgressBar?.visibility = View.VISIBLE
        textForSizeOfImage?.visibility = View.GONE
        viewForImage.visibility = View.INVISIBLE
    }

    private fun onFinish() {
        determinateProgressBar?.visibility = View.GONE
        indeterminateProgressBar?.visibility = View.GONE
        textForSizeOfImage?.visibility = View.GONE
        viewForImage.visibility = View.VISIBLE
    }

    private fun onProgress(bytesRead: Long, expectedLength: Long) {
        if (determinateProgressBar?.visibility == View.GONE) {
            determinateProgressBar.visibility = View.VISIBLE
        }
        if (indeterminateProgressBar?.visibility == View.VISIBLE) {
            indeterminateProgressBar.visibility = View.GONE
        }
        if (textForSizeOfImage?.visibility == View.GONE) {
            var formattedSizeOfFile = expectedLength / 1024.0

            textForSizeOfImage.text = if (formattedSizeOfFile >= 1000) {
                formattedSizeOfFile /= 1024.0
                textForSizeOfImage.context.getString(R.string.megaByteNumber, formattedSizeOfFile)
            } else {
                textForSizeOfImage.context.getString(R.string.kiloByteNumber, formattedSizeOfFile)
            }
            textForSizeOfImage.visibility = View.VISIBLE
        }

        determinateProgressBar?.progress = (100 * bytesRead / expectedLength).toInt()
    }

    fun cancelCurrentLoad() {
        currentLoad?.cancel()
        currentLoad = null
    }

    fun startNewLoad(url: String, @DrawableRes imageForError: Int) {
        cancelCurrentLoad()
        onStart()

        currentLoad = fragment.lifecycleScope.launch {
            val imageDrawable: Drawable? = try {
                decodeImage(downloadImage(url))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                null
            }

            if (imageDrawable == null) {
                viewForImage.setImageResource(imageForError)
            } else {
                viewForImage.setImageDrawable(imageDrawable)
                (imageDrawable as? AnimatedImageDrawable)?.start()
            }

            onFinish()
        }
    }

    private suspend fun downloadImage(url: String): ByteArray = withContext(Dispatchers.IO) {
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        try {
            val expectedLength: Long = connection.contentLengthLong
            val bytesOfImage = ByteArrayOutputStream(if (expectedLength in 1 until Int.MAX_VALUE) expectedLength.toInt() else 32_768)
            val buffer = ByteArray(8_192)
            var totalBytesRead = 0L
            var lastPercent = -1

            connection.inputStream.use { input ->
                while (true) {
                    ensureActive()

                    val bytesRead: Int = input.read(buffer)
                    if (bytesRead == -1) {
                        break
                    }

                    bytesOfImage.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead

                    if (expectedLength > 0) {
                        val percent: Int = (100 * totalBytesRead / expectedLength).toInt()

                        if (percent != lastPercent) {
                            lastPercent = percent
                            withContext(Dispatchers.Main) { onProgress(totalBytesRead, expectedLength) }
                        }
                    }
                }
            }

            bytesOfImage.toByteArray()
        } finally {
            connection.disconnect()
        }
    }

    /* L'image est affichée en fitCenter donc elle peut être sous-échantillonnée jusqu'à la taille de l'écran sans perte. */
    private suspend fun decodeImage(imageData: ByteArray): Drawable = withContext(Dispatchers.Default) {
        ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(imageData))) { decoder, info, _ ->
            val metrics = viewForImage.resources.displayMetrics
            val sampleSize: Int = max(
                info.size.width / max(metrics.widthPixels, 1),
                info.size.height / max(metrics.heightPixels, 1)
            )

            if (sampleSize > 1) {
                decoder.setTargetSampleSize(sampleSize)
            }
        }
    }
}
