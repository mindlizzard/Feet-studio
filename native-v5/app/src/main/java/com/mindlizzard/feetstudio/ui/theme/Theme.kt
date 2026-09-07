package com.mindlizzard.feetstudio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD946EF),
    secondary = Color(0xFF818CF8),
    background = Color(0xFF090B0F),
    surface = Color(0xFF111318),
    surfaceVariant = Color(0xFF181B22),
    onPrimary = Color.White,
    onBackground = Color(0xFFF4F4F5),
    onSurface = Color(0xFFF4F4F5)
)

@Composable
fun FeetStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
