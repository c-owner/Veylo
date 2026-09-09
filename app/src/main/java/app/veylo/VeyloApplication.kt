package app.veylo

import android.app.Application
import app.veylo.data.local.VeyloDatabase
import app.veylo.data.repository.DefaultWallpaperProjectRepository
import app.veylo.data.repository.WallpaperPreferences
import app.veylo.domain.repository.WallpaperProjectRepository

class VeyloApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(application: Application) {
    private val database = VeyloDatabase.create(application)
    val wallpaperRepository: WallpaperProjectRepository = DefaultWallpaperProjectRepository(
        context = application,
        projectDao = database.wallpaperProjectDao(),
    )
    val wallpaperPreferences = WallpaperPreferences(application)
}
