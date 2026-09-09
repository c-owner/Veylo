package app.veylo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperProjectDao {
    @Query("SELECT * FROM wallpaper_projects ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<WallpaperProjectEntity>>

    @Query("SELECT * FROM wallpaper_projects WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WallpaperProjectEntity?

    @Upsert
    suspend fun upsert(project: WallpaperProjectEntity): Long

    @Delete
    suspend fun delete(project: WallpaperProjectEntity)
}
