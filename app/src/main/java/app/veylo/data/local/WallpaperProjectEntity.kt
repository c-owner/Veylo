package app.veylo.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_projects")
data class WallpaperProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    val sourceUri: String,
    val sourceType: String,
    val thumbnailUri: String?,
    val cropMode: String,
    val brightness: Float,
    val playbackSpeed: Float,
    val loopMode: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean,
)
