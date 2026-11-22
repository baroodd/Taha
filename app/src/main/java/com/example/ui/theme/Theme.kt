package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = BlueprintNavy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD9E6F7),
    onPrimaryContainer = BlueprintNavy,
    secondary = CopperGold,
    onSecondary = Color.White,
    secondaryContainer = CopperGoldLight,
    onSecondaryContainer = Color(0xFF5A2B0A),
    tertiary = ArchitecturalTeal,
    onTertiary = Color.White,
    tertiaryContainer = ArchitecturalTealContainer,
    onTertiaryContainer = Color(0xFF063D38),
    background = DraftingPaper,
    onBackground = BlueprintInk,
    surface = DraftingSurface,
    onSurface = BlueprintInk,
    surfaceVariant = DraftingSurfaceVariant,
    onSurfaceVariant = BlueprintMuted,
    outline = BlueprintOutline,
    outlineVariant = Color(0xFFE2E8F0),
    error = ErrorCrimson,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = NightBluePrimary,
    onPrimary = Color(0xFF08203D),
    primaryContainer = BlueprintNavyLight,
    onPrimaryContainer = Color(0xFFD9E6F7),
    secondary = NightCopperSecondary,
    onSecondary = Color(0xFF3F1D06),
    secondaryContainer = Color(0xFF5F3010),
    onSecondaryContainer = CopperGoldLight,
    tertiary = NightTealTertiary,
    onTertiary = Color(0xFF03332F),
    tertiaryContainer = Color(0xFF0A4D47),
    onTertiaryContainer = ArchitecturalTealContainer,
    background = NightBlueprintBg,
    onBackground = NightTextPrimary,
    surface = NightBlueprintSurface,
    onSurface = NightTextPrimary,
    surfaceVariant = NightBlueprintSurfaceVariant,
    onSurfaceVariant = NightTextMuted,
    outline = NightOutline,
    outlineVariant = Color(0xFF223249),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun BunyanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
