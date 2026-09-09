package app.veylo.ui

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.veylo.domain.model.AssetAvailability
import app.veylo.domain.model.CropMode
import app.veylo.domain.model.WallpaperProject
import app.veylo.domain.model.WallpaperType
import app.veylo.ui.components.MediaPreview
import app.veylo.ui.components.MediaThumbnail
import app.veylo.wallpaper.service.VeyloWallpaperService

private object Route {
    const val HOME = "home"
    const val CREATE = "create"
    const val LIBRARY = "library"
    const val SETTINGS = "settings"
    const val EDITOR = "editor"
    const val DETAIL = "detail/{projectId}"
    fun detail(projectId: Long) = "detail/$projectId"
}

@Composable
fun VeyloApp(viewModel: VeyloViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    Scaffold(
        bottomBar = { VeyloNavigationBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Route.HOME) {
                HomeScreen(
                    projects = projects,
                    onCreate = { navController.navigate(Route.CREATE) },
                    onProjectSelected = { navController.navigate(Route.detail(it)) },
                )
            }
            composable(Route.CREATE) {
                CreateScreen(
                    onMediaSelected = { uri, type, name ->
                        viewModel.beginDraft(uri, type, name)
                        navController.navigate(Route.EDITOR)
                    },
                )
            }
            composable(Route.LIBRARY) {
                LibraryScreen(projects = projects, onProjectSelected = { navController.navigate(Route.detail(it)) })
            }
            composable(Route.SETTINGS) { SettingsScreen() }
            composable(Route.EDITOR) {
                val draft by viewModel.draft.collectAsStateWithLifecycle()
                if (draft == null) {
                    EmptyState(title = "선택한 미디어가 없습니다", actionLabel = "Create로 이동") {
                        navController.navigate(Route.CREATE)
                    }
                } else {
                    EditorScreen(
                        draft = draft!!,
                        onDraftChange = viewModel::updateDraft,
                        onSave = { viewModel.saveDraft { navController.navigate(Route.detail(it)) } },
                    )
                }
            }
            composable(Route.DETAIL) { entry ->
                val id = entry.arguments?.getString("projectId")?.toLongOrNull()
                val project = projects.firstOrNull { it.id == id }
                if (project == null) {
                    EmptyState(title = "Wallpaper를 찾을 수 없습니다", actionLabel = "라이브러리") {
                        navController.navigate(Route.LIBRARY)
                    }
                } else {
                    DetailScreen(
                        project = project,
                        availability = viewModel.assetAvailability.collectAsStateWithLifecycle().value,
                        onCheckAsset = { viewModel.checkAsset(project) },
                        onApply = {
                            viewModel.applyWallpaper(project.id) {
                                openWallpaperPreview(context, project.id)
                            }
                        },
                        onDelete = { viewModel.deleteProject(project.id) { navController.navigate(Route.LIBRARY) } },
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryScreen(projects: List<WallpaperProject>, onProjectSelected: (Long) -> Unit) {
    if (projects.isEmpty()) {
        EmptyState(title = "My Wallpapers가 비어 있습니다", actionLabel = "새 Wallpaper 만들기") { }
        return
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(20.dp))
        Text("My Wallpapers", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            contentPadding = PaddingValues(bottom = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(projects, key = { it.id }) { project ->
                ProjectCard(project = project, onClick = { onProjectSelected(project.id) })
            }
        }
    }
}

@Composable
private fun ProjectCard(project: WallpaperProject, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column {
            MediaThumbnail(project.thumbnailUri ?: project.sourceUri, Modifier.fillMaxWidth().aspectRatio(0.75f))
            Column(Modifier.padding(12.dp)) {
                Text(project.name, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
                Text(project.type.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ProjectRow(project: WallpaperProject, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            MediaThumbnail(project.thumbnailUri ?: project.sourceUri, Modifier.size(72.dp).clip(MaterialTheme.shapes.medium))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(project.name, fontWeight = FontWeight.SemiBold)
                Text(project.type.name.lowercase().replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DetailScreen(
    project: WallpaperProject,
    availability: AssetAvailability?,
    onCheckAsset: () -> Unit,
    onApply: () -> Unit,
    onDelete: () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(project.id) { onCheckAsset() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            MediaPreview(project.sourceUri, project.type, Modifier.fillMaxWidth().aspectRatio(9f / 16f).clip(MaterialTheme.shapes.extraLarge))
        }
        item {
            Text(project.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(project.type.name.lowercase().replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (availability == AssetAvailability.Missing) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text("원본 미디어에 접근할 수 없습니다. 파일을 다시 선택해 교체할 수 있도록 향후 업데이트에서 지원합니다.", Modifier.padding(16.dp))
                }
            }
        }
        item {
            Button(onClick = onApply, modifier = Modifier.fillMaxWidth(), enabled = availability != AssetAvailability.Missing) {
                Text("Apply Wallpaper")
            }
        }
        item { OutlinedButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) { Text("Delete") } }
    }
}

@Composable
private fun SettingsScreen() {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Text("Veylo는 기본적으로 원본 미디어를 기기에서 직접 참조합니다.")
        Spacer(Modifier.height(20.dp))
        HorizontalDivider()
        Spacer(Modifier.height(20.dp))
        Text("추가 설정", style = MaterialTheme.typography.titleMedium)
        Text("FPS, 품질, 배터리 절약, 저장 공간 설정은 추후 추가됩니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyState(title: String, actionLabel: String, onAction: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onAction) { Text(actionLabel) }
    }
}

private fun openWallpaperPreview(context: android.content.Context, projectId: Long) {
    val component = ComponentName(context, VeyloWallpaperService::class.java)
    context.startActivity(
        Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, component)
            putExtra("app.veylo.PROJECT_ID", projectId)
        },
    )
}

@Composable
private fun VeyloNavigationBar(navController: NavHostController) {
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination?.route
    NavigationBar {
        listOf(
            Route.HOME to Triple("Home", Icons.Default.Home, Route.HOME),
            Route.CREATE to Triple("Create", Icons.Default.Add, Route.CREATE),
            Route.LIBRARY to Triple("My Wallpapers", Icons.Default.Collections, Route.LIBRARY),
            Route.SETTINGS to Triple("Settings", Icons.Default.Settings, Route.SETTINGS),
        ).forEach { (route, item) ->
            NavigationBarItem(
                selected = destination == route,
                onClick = { navController.navigate(route) { launchSingleTop = true } },
                icon = { Icon(item.second, contentDescription = item.first) },
                label = { Text(item.first) },
            )
        }
    }
}

@Composable
private fun HomeScreen(
    projects: List<WallpaperProject>,
    onCreate: () -> Unit,
    onProjectSelected: (Long) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Veylo", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            Text("Make your screen alive.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Column(Modifier.padding(20.dp)) {
                    Text("나만의 화면을 만들어보세요", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("이미지와 동영상을 선택해 미리보고 Live Wallpaper로 적용할 수 있습니다.")
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onCreate) { Text("Wallpaper 만들기") }
                }
            }
        }
        item { Text("최근 Wallpaper", style = MaterialTheme.typography.titleLarge) }
        if (projects.isEmpty()) {
            item { EmptyState(title = "아직 저장한 Wallpaper가 없습니다", actionLabel = "Create") { onCreate() } }
        } else {
            items(projects.take(5).size) { index ->
                ProjectRow(project = projects[index], onClick = { onProjectSelected(projects[index].id) })
            }
        }
    }
}

@Composable
private fun CreateScreen(onMediaSelected: (String, WallpaperType, String) -> Unit) {
    val context = LocalContext.current
    fun resolveName(uri: Uri): String {
        return context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getString(0) else null
        } ?: "New wallpaper"
    }
    fun launchSelected(uri: Uri?, type: WallpaperType) {
        uri ?: return
        runCatching {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        onMediaSelected(uri.toString(), type, resolveName(uri).substringBeforeLast('.'))
    }
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { launchSelected(it, WallpaperType.IMAGE) }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { launchSelected(it, WallpaperType.VIDEO) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Create", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("기기의 미디어를 안전하게 참조해 Wallpaper를 만듭니다.")
        Spacer(Modifier.height(32.dp))
        Button(onClick = { imagePicker.launch(arrayOf("image/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Create from Image")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { videoPicker.launch(arrayOf("video/*")) }, modifier = Modifier.fillMaxWidth()) {
            Text("Create from Video")
        }
    }
}

@Composable
private fun EditorScreen(draft: WallpaperDraft, onDraftChange: ((WallpaperDraft) -> WallpaperDraft) -> Unit, onSave: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Preview", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            MediaPreview(draft.uri, draft.type, Modifier.fillMaxWidth().aspectRatio(9f / 16f).clip(MaterialTheme.shapes.extraLarge))
        }
        item {
            Text("이름", style = MaterialTheme.typography.titleMedium)
            androidx.compose.material3.OutlinedTextField(
                value = draft.name,
                onValueChange = { value -> onDraftChange { it.copy(name = value) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            Text("밝기 ${"%.0f".format(draft.brightness * 100)}%")
            Slider(value = draft.brightness, onValueChange = { value -> onDraftChange { it.copy(brightness = value) } }, valueRange = 0.4f..1.4f)
        }
        if (draft.type == WallpaperType.VIDEO) {
            item {
                Text("재생 속도 ${"%.2f".format(draft.playbackSpeed)}x")
                Slider(value = draft.playbackSpeed, onValueChange = { value -> onDraftChange { it.copy(playbackSpeed = value) } }, valueRange = 0.5f..1.5f)
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CropMode.entries.forEach { mode ->
                    OutlinedButton(onClick = { onDraftChange { it.copy(cropMode = mode) } }) {
                        Text(if (mode == CropMode.CENTER_CROP) "Center Crop" else "Fit")
                    }
                }
            }
        }
        item { Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) { Text("Save Wallpaper") } }
    }
}
