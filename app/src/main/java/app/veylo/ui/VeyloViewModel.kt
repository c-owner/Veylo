package app.veylo.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.veylo.VeyloApplication
import app.veylo.domain.model.AssetAvailability
import app.veylo.domain.model.CropMode
import app.veylo.domain.model.WallpaperProject
import app.veylo.domain.model.WallpaperType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class WallpaperDraft(
    val uri: String,
    val type: WallpaperType,
    val name: String,
    val cropMode: CropMode = CropMode.CENTER_CROP,
    val brightness: Float = 1f,
    val playbackSpeed: Float = 1f,
)

class VeyloViewModel(application: Application) : AndroidViewModel(application) {
    private val container = (application as VeyloApplication).appContainer
    private val repository = container.wallpaperRepository

    val projects: StateFlow<List<WallpaperProject>> = repository.observeProjects().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )
    private val _draft = MutableStateFlow<WallpaperDraft?>(null)
    val draft = _draft.asStateFlow()
    private val _assetAvailability = MutableStateFlow<AssetAvailability?>(null)
    val assetAvailability = _assetAvailability.asStateFlow()

    fun beginDraft(uri: String, type: WallpaperType, defaultName: String) {
        _draft.value = WallpaperDraft(uri = uri, type = type, name = defaultName)
    }

    fun updateDraft(transform: (WallpaperDraft) -> WallpaperDraft) {
        _draft.value = _draft.value?.let(transform)
    }

    fun saveDraft(onSaved: (Long) -> Unit) {
        val currentDraft = _draft.value ?: return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val id = repository.saveProject(
                WallpaperProject(
                    name = currentDraft.name.ifBlank { "Untitled wallpaper" },
                    type = currentDraft.type,
                    sourceUri = currentDraft.uri,
                    cropMode = currentDraft.cropMode,
                    brightness = currentDraft.brightness,
                    playbackSpeed = currentDraft.playbackSpeed,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            _draft.value = null
            onSaved(id)
        }
    }

    fun checkAsset(project: WallpaperProject) {
        viewModelScope.launch { _assetAvailability.value = repository.checkAvailability(project) }
    }

    fun applyWallpaper(projectId: Long, onReady: () -> Unit) {
        viewModelScope.launch {
            container.wallpaperPreferences.setActiveWallpaperId(projectId)
            onReady()
        }
    }

    fun deleteProject(projectId: Long, onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            onDeleted()
        }
    }
}
