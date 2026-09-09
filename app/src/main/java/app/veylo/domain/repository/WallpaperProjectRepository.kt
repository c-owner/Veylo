package app.veylo.domain.repository

import app.veylo.domain.model.AssetAvailability
import app.veylo.domain.model.WallpaperProject
import kotlinx.coroutines.flow.Flow

interface WallpaperProjectRepository {
    fun observeProjects(): Flow<List<WallpaperProject>>
    suspend fun getProject(id: Long): WallpaperProject?
    suspend fun saveProject(project: WallpaperProject): Long
    suspend fun deleteProject(id: Long)
    suspend fun checkAvailability(project: WallpaperProject): AssetAvailability
}
