package com.motionforge.editor.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ForgeOrange = Color(0xFFFF6A00)
private val ForgeOrangeDark = Color(0xFFC24E00)
private val ForgeCharcoal = Color(0xFF121212)
private val ForgeSurface = Color(0xFF1E1E1E)
private val ForgeOnSurface = Color(0xFFF2F2F2)

private val DarkColors = darkColorScheme(
    primary = ForgeOrange,
    onPrimary = Color.Black,
    secondary = ForgeOrangeDark,
    background = ForgeCharcoal,
    onBackground = ForgeOnSurface,
    surface = ForgeSurface,
    onSurface = ForgeOnSurface
)

private val LightColors = lightColorScheme(
    primary = ForgeOrange,
    onPrimary = Color.White,
    secondary = ForgeOrangeDark
)

@Composable
fun MotionForgeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
