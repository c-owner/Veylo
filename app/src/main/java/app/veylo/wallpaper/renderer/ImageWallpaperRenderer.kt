package app.veylo.wallpaper.renderer

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.view.SurfaceHolder
import app.veylo.domain.model.CropMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

class ImageWallpaperRenderer(private val contentResolver: ContentResolver) {
    suspend fun draw(
        holder: SurfaceHolder,
        uri: Uri,
        width: Int,
        height: Int,
        cropMode: CropMode,
        brightness: Float,
    ) = withContext(Dispatchers.IO) {
        val bitmap = decodeSampledBitmap(uri, width, height) ?: return@withContext
        try {
            val canvas = holder.lockCanvas() ?: return@withContext
            try {
                val destination = destinationRect(bitmap, width, height, cropMode)
                val colorMatrix = ColorMatrix().apply { setScale(brightness, brightness, brightness, 1f) }
                canvas.drawColor(android.graphics.Color.BLACK)
                canvas.drawBitmap(bitmap, null, destination, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(colorMatrix)
                })
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        } finally {
            bitmap.recycle()
        }
    }

    private fun decodeSampledBitmap(uri: Uri, targetWidth: Int, targetHeight: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sample = calculateSampleSize(bounds.outWidth, bounds.outHeight, targetWidth, targetHeight)
        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }

    private fun calculateSampleSize(width: Int, height: Int, targetWidth: Int, targetHeight: Int): Int {
        var sample = 1
        while (width / (sample * 2) >= targetWidth && height / (sample * 2) >= targetHeight) sample *= 2
        return sample
    }

    private fun destinationRect(bitmap: Bitmap, width: Int, height: Int, cropMode: CropMode): Rect {
        val scale = when (cropMode) {
            CropMode.CENTER_CROP -> max(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
            CropMode.FIT -> minOf(width.toFloat() / bitmap.width, height.toFloat() / bitmap.height)
        }
        val scaledWidth = (bitmap.width * scale).toInt()
        val scaledHeight = (bitmap.height * scale).toInt()
        val left = (width - scaledWidth) / 2
        val top = (height - scaledHeight) / 2
        return Rect(left, top, left + scaledWidth, top + scaledHeight)
    }
}
