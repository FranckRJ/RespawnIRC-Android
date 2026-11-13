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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.nio.ByteBuffer
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.P) // ImageDecoder, qui sait lire les GIF animés, n'existe pas avant.
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

    fun cancelCurrentLoad() {
        currentLoad?.cancel()
        currentLoad = null
    }

    fun startNewLoad(url: String, @DrawableRes imageForError: Int) {
        cancelCurrentLoad()
        onStart()

        currentLoad = fragment.lifecycleScope.launch {
            val image: Drawable? = try {
                val downloadedImage: ByteArray = ProgressDownloader.download(url) { bytesRead, expectedLength ->
                    if (expectedLength != null) {
                        withContext(Dispatchers.Main) { onProgress(bytesRead, expectedLength) }
                    }
                }

                decodeImage(downloadedImage)
            } catch (e: IOException) {
                null
            }

            if (image == null) {
                viewForImage.setImageResource(imageForError)
            } else {
                viewForImage.setImageDrawable(image)

                if (image is AnimatedImageDrawable) {
                    image.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE // Comme Glide, quel que soit le fichier.
                    image.start()
                }
            }

            onFinish()
        }
    }

    private fun onProgress(bytesRead: Long, expectedLength: Long) {
        if (expectedLength > 0) {
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
    }

    /* L'image est affichée en fitCenter donc elle peut être sous-échantillonnée jusqu'à la taille de l'écran sans perte. */
    private suspend fun decodeImage(downloadedImage: ByteArray): Drawable = withContext(Dispatchers.Default) {
        ImageDecoder.decodeDrawable(ImageDecoder.createSource(ByteBuffer.wrap(downloadedImage))) { decoder, info, _ ->
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
