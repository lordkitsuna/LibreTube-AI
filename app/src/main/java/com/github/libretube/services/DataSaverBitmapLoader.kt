package com.github.libretube.services

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.github.libretube.helpers.ImageHelper
import com.github.libretube.util.DataSaverMode
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Session [BitmapLoader] that respects the data saver mode.
 *
 * Media3's default session bitmap loader downloads the artwork over the
 * network when it is not in its own (session) memory cache, bypassing Coil's
 * disk cache. This loader delegates to Coil's [ImageHelper.getImage] instead
 * (which honours the data saver and uses the Coil caches), and — when the data
 * saver is on and the thumbnail is not cached yet — fails the request so the
 * media notification falls back to its default icon.
 */
@UnstableApi
class DataSaverBitmapLoader(private val context: Context) : BitmapLoader {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val httpScheme = "http"

    override fun supportsMimeType(mimeType: String): Boolean = true

    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()
        scope.launch {
            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
            if (bitmap == null) {
                future.setException(IllegalStateException("Failed to decode bitmap"))
            } else {
                future.set(bitmap)
            }
        }
        return future
    }

    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        val future = SettableFuture.create<Bitmap>()

        // don't download the artwork when the data saver is on and it is not
        // cached yet: failing the request makes Media3 keep the default icon
        if (uri.toString().startsWith(httpScheme) &&
            DataSaverMode.isEnabled(context) &&
            !ImageHelper.isCached(uri.toString())
        ) {
            future.setException(IllegalStateException("Artwork not loaded: data saver on, image not cached"))
            return future
        }

        scope.launch {
            val bitmap = ImageHelper.getImage(context, uri)
            if (bitmap == null) {
                future.setException(IllegalStateException("Failed to load bitmap"))
            } else {
                future.set(bitmap)
            }
        }
        return future
    }
}
