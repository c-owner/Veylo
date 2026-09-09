package app.veylo.domain.model

data class WallpaperProject(
    val id: Long = 0,
    val name: String,
    val type: WallpaperType,
    val sourceUri: String,
    val sourceType: SourceType = SourceType.LOCAL_URI,
    val thumbnailUri: String? = null,
    val cropMode: CropMode = CropMode.CENTER_CROP,
    val brightness: Float = 1f,
    val playbackSpeed: Float = 1f,
    val loopMode: LoopMode = LoopMode.REPEAT,
    val createdAt: Long,
    val updatedAt: Long,
    val isFavorite: Boolean = false,
)

enum class WallpaperType { IMAGE, VIDEO }
enum class SourceType { LOCAL_URI, APP_ASSET, APP_COPY }
enum class CropMode { CENTER_CROP, FIT }
enum class LoopMode { REPEAT }

sealed interface AssetAvailability {
    data object Available : AssetAvailability
    data object Missing : AssetAvailability
}
