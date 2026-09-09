package app.veylo.data.repository

import app.veylo.data.local.WallpaperProjectEntity
import app.veylo.domain.model.CropMode
import app.veylo.domain.model.LoopMode
import app.veylo.domain.model.SourceType
import app.veylo.domain.model.WallpaperProject
import app.veylo.domain.model.WallpaperType

internal fun WallpaperProjectEntity.toDomain() = WallpaperProject(
    id = id,
    name = name,
    type = WallpaperType.valueOf(type),
    sourceUri = sourceUri,
    sourceType = SourceType.valueOf(sourceType),
    thumbnailUri = thumbnailUri,
    cropMode = CropMode.valueOf(cropMode),
    brightness = brightness,
    playbackSpeed = playbackSpeed,
    loopMode = LoopMode.valueOf(loopMode),
    createdAt = createdAt,
    updatedAt = updatedAt,
    isFavorite = isFavorite,
)

internal fun WallpaperProject.toEntity() = WallpaperProjectEntity(
    id = id,
    name = name,
    type = type.name,
    sourceUri = sourceUri,
    sourceType = sourceType.name,
    thumbnailUri = thumbnailUri,
    cropMode = cropMode.name,
    brightness = brightness,
    playbackSpeed = playbackSpeed,
    loopMode = loopMode.name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isFavorite = isFavorite,
)
