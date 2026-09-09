package app.veylo.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import app.veylo.domain.model.WallpaperProject
import app.veylo.domain.model.WallpaperType
import java.io.File

internal class MediaThumbnailStore(private val context: Context) {
    fun createVideoThumbnail(project: WallpaperProject): String? {
        if (project.type != WallpaperType.VIDEO) return project.sourceUri
        return runCatching {
            val thumbnails = File(context.cacheDir, "wallpaper_thumbnails").apply { mkdirs() }
            val output = File(thumbnails, "${project.createdAt}.jpg")
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, android.net.Uri.parse(project.sourceUri))
                val frame = requireNotNull(retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC))
                output.outputStream().use { stream -> frame.compress(Bitmap.CompressFormat.JPEG, 82, stream) }
                frame.recycle()
            }
            output.absolutePath
        }.getOrNull()
    }
}
