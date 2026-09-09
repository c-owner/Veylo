package app.veylo.data.repository

import android.content.Context
import android.net.Uri
import app.veylo.data.local.WallpaperProjectDao
import app.veylo.domain.model.AssetAvailability
import app.veylo.domain.model.WallpaperProject
import app.veylo.domain.repository.WallpaperProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DefaultWallpaperProjectRepository(
    private val context: Context,
    private val projectDao: WallpaperProjectDao,
) : WallpaperProjectRepository {
    override fun observeProjects(): Flow<List<WallpaperProject>> = projectDao.observeAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getProject(id: Long): WallpaperProject? = projectDao.getById(id)?.toDomain()

    override suspend fun saveProject(project: WallpaperProject): Long {
        val thumbnailUri = project.thumbnailUri ?: withContext(Dispatchers.IO) {
            MediaThumbnailStore(context).createVideoThumbnail(project)
        }
        return projectDao.upsert(project.copy(thumbnailUri = thumbnailUri).toEntity())
    }

    override suspend fun deleteProject(id: Long) {
        val project = projectDao.getById(id) ?: return
        projectDao.delete(project)
    }

    override suspend fun checkAvailability(project: WallpaperProject): AssetAvailability = runCatching {
        context.contentResolver.openAssetFileDescriptor(Uri.parse(project.sourceUri), "r")?.use { }
            ?: error("Media asset is unavailable")
    }.fold(
        onSuccess = { AssetAvailability.Available },
        onFailure = { AssetAvailability.Missing },
    )
}
