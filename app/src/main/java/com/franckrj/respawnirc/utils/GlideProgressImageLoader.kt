package com.franckrj.respawnirc.utils

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.franckrj.respawnirc.R
import com.franckrj.respawnirc.utils.CustomAppGlideModule.UIonProgressListener

/* Basé sur https://medium.com/@mr.johnnyne/how-to-use-glide-v4-load-image-with-progress-update-eb02671dac18. */
class GlideProgressImageLoader(
    private val viewForImage: ImageView,
    private val indeterminateProgressBar: ProgressBar? = null,
    private val determinateProgressBar: ProgressBar? = null,
    private val textForSizeOfImage: TextView? = null
) {
    private var currentUrl: String? = null

    private val imageLoaderRequestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(
            e: GlideException?,
            model: Any?,
            target: Target<Drawable>?,
            isFirstResource: Boolean
        ): Boolean {
            currentUrl?.let { CustomAppGlideModule.forget(it) }
            onFinish()
            return false
        }

        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            currentUrl?.let { CustomAppGlideModule.forget(it) }
            onFinish()
            return false
        }
    }

    fun load(url: String, options: RequestOptions) {
        onStart()

        CustomAppGlideModule.expect(url, object : UIonProgressListener {
            override fun onProgress(bytesRead: Long, expectedLength: Long) {
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
        })

        GlideApp.with(viewForImage.context)
            .load(url)
            .listener(imageLoaderRequestListener)
            .apply(options)
            .into(viewForImage)
    }

    private fun onStart() {
        viewForImage.visibility = View.INVISIBLE
        determinateProgressBar?.visibility = View.GONE
        indeterminateProgressBar?.visibility = View.VISIBLE
        textForSizeOfImage?.visibility = View.GONE
    }

    private fun onFinish() {
        viewForImage.visibility = View.VISIBLE
        determinateProgressBar?.visibility = View.GONE
        indeterminateProgressBar?.visibility = View.GONE
        textForSizeOfImage?.visibility = View.GONE
    }
}
