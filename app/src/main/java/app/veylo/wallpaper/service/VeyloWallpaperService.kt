package app.veylo.wallpaper.service

import android.net.Uri
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import app.veylo.VeyloApplication
import app.veylo.domain.model.WallpaperProject
import app.veylo.domain.model.WallpaperType
import app.veylo.util.VeyloLog
import app.veylo.wallpaper.renderer.ImageWallpaperRenderer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VeyloWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        VeyloLog.debug("Wallpaper engine created")
        return VeyloEngine()
    }

    inner class VeyloEngine : Engine() {
        private val container = (application as VeyloApplication).appContainer
        private val engineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val imageRenderer = ImageWallpaperRenderer(contentResolver)
        private var player: ExoPlayer? = null
        private var currentHolder: SurfaceHolder? = null
        private var width = 0
        private var height = 0
        private var isVisible = false
        private var renderJob: Job? = null

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            currentHolder = holder
            VeyloLog.debug("Wallpaper surface created")
            reloadIfReady()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            currentHolder = holder
            this.width = width
            this.height = height
            VeyloLog.debug("Wallpaper surface changed: ${width}x${height}")
            reloadIfReady()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            VeyloLog.debug("Wallpaper surface destroyed")
            currentHolder = null
            renderJob?.cancel()
            releasePlayer()
            super.onSurfaceDestroyed(holder)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            VeyloLog.debug("Wallpaper visibility changed: $visible")
            if (visible) reloadIfReady() else player?.pause()
        }

        override fun onDestroy() {
            VeyloLog.debug("Wallpaper engine destroyed")
            renderJob?.cancel()
            releasePlayer()
            engineScope.cancel()
            super.onDestroy()
        }

        private fun reloadIfReady() {
            val holder = currentHolder ?: return
            if (!isVisible || width <= 0 || height <= 0) return
            engineScope.launch {
                val projectId = container.wallpaperPreferences.activeWallpaperId.first()
                val project = if (projectId == null) null else container.wallpaperRepository.getProject(projectId)
                if (project == null) {
                    VeyloLog.debug("No active wallpaper project")
                    releasePlayer()
                    return@launch
                }
                loadProject(holder, project)
            }
        }

        private fun loadProject(holder: SurfaceHolder, project: WallpaperProject) {
            when (project.type) {
                WallpaperType.VIDEO -> startVideo(holder, project)
                WallpaperType.IMAGE -> drawImage(holder, project)
            }
        }

        private fun startVideo(holder: SurfaceHolder, project: WallpaperProject) {
            renderJob?.cancel()
            releasePlayer()
            player = ExoPlayer.Builder(this@VeyloWallpaperService).build().also { exoPlayer ->
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                exoPlayer.volume = 0f
                exoPlayer.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                exoPlayer.playbackParameters = PlaybackParameters(project.playbackSpeed)
                exoPlayer.setVideoSurfaceHolder(holder)
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(project.sourceUri)))
                exoPlayer.prepare()
                exoPlayer.playWhenReady = isVisible
                VeyloLog.debug("Video player initialized")
            }
        }

        private fun drawImage(holder: SurfaceHolder, project: WallpaperProject) {
            releasePlayer()
            renderJob?.cancel()
            renderJob = engineScope.launch {
                runCatching {
                    imageRenderer.draw(
                        holder = holder,
                        uri = Uri.parse(project.sourceUri),
                        width = width,
                        height = height,
                        cropMode = project.cropMode,
                        brightness = project.brightness,
                    )
                }.onFailure { error -> VeyloLog.error("Image wallpaper rendering failed", error) }
            }
        }

        private fun releasePlayer() {
            player?.let { exoPlayer ->
                exoPlayer.setVideoSurfaceHolder(null)
                exoPlayer.release()
                VeyloLog.debug("Video player released")
            }
            player = null
        }
    }
}
