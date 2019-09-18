package com.franckrj.respawnirc.utils

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.Excludes
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.AppGlideModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.Source
import okio.buffer
import java.io.IOException
import java.io.InputStream
import java.util.WeakHashMap

/* Basé sur https://medium.com/@mr.johnnyne/how-to-use-glide-v4-load-image-with-progress-update-eb02671dac18. */
@Excludes(OkHttpLibraryGlideModule::class)
@GlideModule
class CustomAppGlideModule : AppGlideModule() {
    companion object {
        fun forget(url: String) {
            DispatchingProgressListener.forget(url)
        }

        fun expect(url: String, listener: UIonProgressListener) {
            DispatchingProgressListener.expect(url, listener)
        }
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        val client = OkHttpClient.Builder()
            .addNetworkInterceptor(object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    val request = chain.request()
                    val response = chain.proceed(request)
                    val listener = DispatchingProgressListener()
                    return response.newBuilder()
                        .body(OkHttpProgressResponseBody(request.url, response.body, listener))
                        .build()
                }
            })
            .build()

        registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
    }

    private class DispatchingProgressListener : ResponseProgressListener {
        companion object {
            private val LISTENERS = WeakHashMap<String, UIonProgressListener>()

            fun forget(url: String) {
                LISTENERS.remove(url)
            }

            fun expect(url: String, listener: UIonProgressListener) {
                LISTENERS[url] = listener
            }
        }

        override fun update(url: HttpUrl, bytesRead: Long, contentLength: Long) {
            val key: String = url.toString()
            val listener: UIonProgressListener? = LISTENERS[key]

            if (listener != null) {
                if (contentLength <= bytesRead) {
                    forget(key)
                }

                //todo utiliser le scope de la vue ?
                GlobalScope.launch(Dispatchers.Main) {
                    listener.onProgress(bytesRead, contentLength)
                }
            }
        }
    }

    private class OkHttpProgressResponseBody constructor(
        private val url: HttpUrl,
        private val responseBody: ResponseBody?,
        private val progressListener: ResponseProgressListener
    ) : ResponseBody() {
        private var bufferedSource: BufferedSource? = null

        override fun contentType(): MediaType? {
            return responseBody?.contentType()
        }

        override fun contentLength(): Long {
            return responseBody?.contentLength() ?: -1
        }

        override fun source(): BufferedSource {
            var currentBufferedSource: BufferedSource? = bufferedSource

            if (currentBufferedSource == null) {
                /* Création d'un faux buffer si jamais le responseBody est nul, peut-être pas une bonne idée. */
                currentBufferedSource = source(responseBody?.source() ?: Buffer()).buffer()
            }

            return currentBufferedSource
        }

        private fun source(source: Source): Source {
            return object : ForwardingSource(source) {
                var totalBytesRead = 0L

                @Throws(IOException::class)
                override fun read(sink: Buffer, byteCount: Long): Long {
                    val bytesRead: Long = super.read(sink, byteCount)
                    val fullLength: Long = contentLength()

                    /* bytesRead vaut -1 quand la source est vide. */
                    if (bytesRead == -1L) {
                        totalBytesRead = fullLength
                    } else {
                        totalBytesRead += bytesRead
                    }

                    progressListener.update(url, totalBytesRead, fullLength)

                    return bytesRead
                }
            }
        }
    }

    private interface ResponseProgressListener {
        fun update(url: HttpUrl, bytesRead: Long, contentLength: Long)
    }

    interface UIonProgressListener {
        fun onProgress(bytesRead: Long, expectedLength: Long)
    }
}
