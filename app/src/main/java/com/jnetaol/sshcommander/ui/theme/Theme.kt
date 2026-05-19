package com.jnetaol.sshcommander.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = SCPrimary,
    onPrimary = Color.Black,
    primaryContainer = SCPrimaryVariant,
    onPrimaryContainer = Color.White,
    secondary = SCSecondary,
    onSecondary = Color.Black,
    tertiary = SCAccent,
    onTertiary = Color.White,
    background = SCBackground,
    onBackground = SCTextPrimary,
    surface = SCSurface,
    onSurface = SCTextPrimary,
    surfaceVariant = SCSurfaceVariant,
    onSurfaceVariant = SCTextSecondary,
    error = SCError,
    onError = Color.White,
    outline = SCTextMuted
)

@Composable
fun SSHCommanderTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography(), content = content)
}
