package app.veylo.data.repository

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.wallpaperDataStore by preferencesDataStore(name = "wallpaper_preferences")
private val activeWallpaperIdKey = longPreferencesKey("active_wallpaper_id")

class WallpaperPreferences(private val context: Context) {
    val activeWallpaperId: Flow<Long?> = context.wallpaperDataStore.data.map { it[activeWallpaperIdKey] }

    suspend fun setActiveWallpaperId(id: Long) {
        context.wallpaperDataStore.edit { preferences -> preferences[activeWallpaperIdKey] = id }
    }
}
