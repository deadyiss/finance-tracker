package com.prosperity.tracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette from the "Financial Wellness System" design tokens.
val Primary = Color(0xFF3730A3)
val PrimaryDark = Color(0xFF1F108E)
val Secondary = Color(0xFF059669)   // income / positive
val Tertiary = Color(0xFFE11D48)     // expense / alert
val Neutral = Color(0xFF64748B)
val Mint = Color(0xFF85F8C4)
val OnMintContainer = Color(0xFF00714E)

val AppBackground = Color(0xFFF8F9FF)
val SurfaceWhite = Color(0xFFFFFFFF)
val SurfaceContainer = Color(0xFFE5EEFF)
val SurfaceContainerLow = Color(0xFFEFF4FF)
val OnSurface = Color(0xFF0B1C30)
val OnSurfaceVariant = Color(0xFF64748B)
val OutlineVariant = Color(0xFFE2E8F0)

// Kept names used elsewhere as aliases.
val IncomeGreen = Secondary
val ErrorRed = Tertiary

/** Colors used for chart slices and category accents, by index. */
val ChartPalette = listOf(
    Primary,
    Tertiary,
    Color(0xFF6366F1),
    Mint,
    Color(0xFFF59E0B),
    PrimaryDark,
    Secondary
)

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    onPrimaryContainer = Color.White,
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Mint,
    onSecondaryContainer = OnMintContainer,
    background = AppBackground,
    onBackground = OnSurface,
    surface = AppBackground,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurfaceVariant,
    outlineVariant = OutlineVariant,
    error = Tertiary,
    onError = Color.White
)

@Composable
fun ProsperityTheme(content: @Composable () -> Unit) {
    // Light theme only; the design has no dark variant.
    MaterialTheme(
        colorScheme = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}
