package app.veylo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = VeyloLilac,
    secondary = VeyloMint,
    surface = VeyloInk,
)
private val LightColors = lightColorScheme(
    primary = VeyloPurple,
    secondary = VeyloTeal,
)

@Composable
fun VeyloTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = VeyloTypography,
        content = content,
    )
}
