package app.veylo.data.repository

import app.veylo.domain.model.CropMode
import app.veylo.domain.model.LoopMode
import app.veylo.domain.model.SourceType
import app.veylo.domain.model.WallpaperProject
import app.veylo.domain.model.WallpaperType
import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperProjectMapperTest {
    @Test
    fun `entity mapping preserves wallpaper project settings`() {
        val project = WallpaperProject(
            id = 12,
            name = "Aurora",
            type = WallpaperType.VIDEO,
            sourceUri = "content://media/video/12",
            sourceType = SourceType.LOCAL_URI,
            thumbnailUri = "/cache/aurora.jpg",
            cropMode = CropMode.CENTER_CROP,
            brightness = 1.2f,
            playbackSpeed = 0.8f,
            loopMode = LoopMode.REPEAT,
            createdAt = 10,
            updatedAt = 20,
            isFavorite = true,
        )

        assertEquals(project, project.toEntity().toDomain())
    }
}
