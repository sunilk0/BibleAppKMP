package com.sunilbb.bibleappkmp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Burgundy palette — a warm, lightweight wine-red used as the app's primary accent.
private val Burgundy = Color(0xFF7D2935)
private val BurgundyDark = Color(0xFF5A121D)
private val BurgundyLight = Color(0xFFFFB3B8)

private val LightColors = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFDADC),
    onPrimaryContainer = Color(0xFF3B070F),
    secondary = Color(0xFF775656),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDADC),
    onSecondaryContainer = Color(0xFF2C1517),
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF231919),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF231919),
    surfaceVariant = Color(0xFFF4DDDD),
    onSurfaceVariant = Color(0xFF524344),
)

private val DarkColors = darkColorScheme(
    primary = BurgundyLight,
    onPrimary = Color(0xFF5A121D),
    primaryContainer = BurgundyDark,
    onPrimaryContainer = Color(0xFFFFDADC),
    secondary = Color(0xFFE6BDBD),
    onSecondary = Color(0xFF442A2B),
    secondaryContainer = Color(0xFF5D3F40),
    onSecondaryContainer = Color(0xFFFFDADC),
    background = Color(0xFF201A1A),
    onBackground = Color(0xFFECDFDE),
    surface = Color(0xFF201A1A),
    onSurface = Color(0xFFECDFDE),
    surfaceVariant = Color(0xFF524344),
    onSurfaceVariant = Color(0xFFD7C1C1),
)

@Composable
fun BibleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content,
    )
}
