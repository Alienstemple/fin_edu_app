package ru.lct2026.finedu.productcore.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DzynPurple,
    onPrimary = SurfaceLight,
    primaryContainer = DzynPurpleLight,
    onPrimaryContainer = DzynPurpleDark,
    secondary = HatBlue,
    secondaryContainer = HatBlueLight,
    tertiary = CoinGold,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    error = ErrorColor
)

private val DarkColors = darkColorScheme(
    primary = DzynPurpleLight,
    onPrimary = DzynPurpleDark,
    primaryContainer = DzynPurple,
    onPrimaryContainer = SurfaceLight,
    secondary = HatBlueLight,
    secondaryContainer = HatBlue,
    tertiary = CoinGold,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    error = ErrorColor
)

/**
 * Тема приложения. Цвета и шрифты в UI берём только отсюда: `MaterialTheme.colorScheme.*`,
 * `MaterialTheme.typography.*`. Хардкод `Color(0xFF...)` в фичах запрещён.
 */
@Composable
fun FinEduTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FinEduTypography,
        content = content
    )
}
