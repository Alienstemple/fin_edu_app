package ru.lct2026.finedu.productcore.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.dp

// Тема только тёмная: светлой темы в приложении нет.
private val DarkColors = darkColorScheme(
    primary = AccentSelection,
    onPrimary = TextPrimary,
    primaryContainer = ButtonGradientEnd,
    onPrimaryContainer = TextPrimary,
    secondary = BagNeeds,
    onSecondary = TextOnGold,
    tertiary = AccentGold,
    onTertiary = TextOnGold,
    background = BackgroundBottom,
    onBackground = TextPrimary,
    surface = BackgroundTop,
    onSurface = TextPrimary,
    surfaceVariant = GlassTop,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = BackgroundTop,
    surfaceContainerHigh = BackgroundTop,
    outline = GlassStrongBorder,
    outlineVariant = GlassBorder,
    error = AccentError,
    onError = OnAccentError,
    scrim = Scrim
)

// Радиусы макета: 14 — мелкие кнопки, 16–18 — кнопки и поля, 20–22 — карточки и шторки.
private val FinEduShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(22.dp)
)

/**
 * Тема приложения. Цвета и шрифты в UI берём только отсюда: `MaterialTheme.colorScheme.*`,
 * `MaterialTheme.typography.*`, `MaterialTheme.shapes.*` и токены макета `FinEduTheme.colors.*`.
 * Хардкод `Color(0xFF...)` в фичах запрещён.
 */
@Composable
fun FinEduTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalFinEduColors provides DarkFinEduColors) {
        MaterialTheme(
            colorScheme = DarkColors,
            typography = FinEduTypography,
            shapes = FinEduShapes,
            content = content
        )
    }
}

/** Доступ к токенам макета, которых нет в Material `ColorScheme`. */
object FinEduTheme {
    val colors: FinEduColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFinEduColors.current
}
