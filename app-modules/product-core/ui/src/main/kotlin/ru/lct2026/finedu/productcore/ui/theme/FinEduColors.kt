package ru.lct2026.finedu.productcore.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Токены макета, которых нет в Material `ColorScheme`: цвета мешочков, золото, стекло, градиенты фона и кнопки.
 * Доступ из UI — `FinEduTheme.colors.*`.
 */
@Immutable
data class FinEduColors(
    val needs: Color,
    val wants: Color,
    val savings: Color,
    val gold: Color,
    val goldLight: Color,
    val onGold: Color,
    val selection: Color,
    val buttonGradientStart: Color,
    val buttonGradientEnd: Color,
    val backgroundTop: Color,
    val backgroundBottom: Color,
    val backgroundSpotViolet: Color,
    val backgroundSpotSky: Color,
    val backgroundSpotPink: Color,
    val glassTop: Color,
    val glassBottom: Color,
    val glassBorder: Color,
    val glassStrongTop: Color,
    val glassStrongBottom: Color,
    val glassStrongBorder: Color,
    val glassHighlight: Color
)

internal val DarkFinEduColors = FinEduColors(
    needs = BagNeeds,
    wants = BagWants,
    savings = BagSavings,
    gold = AccentGold,
    goldLight = AccentGoldLight,
    onGold = TextOnGold,
    selection = AccentSelection,
    buttonGradientStart = ButtonGradientStart,
    buttonGradientEnd = ButtonGradientEnd,
    backgroundTop = BackgroundTop,
    backgroundBottom = BackgroundBottom,
    backgroundSpotViolet = BackgroundSpotViolet,
    backgroundSpotSky = BackgroundSpotSky,
    backgroundSpotPink = BackgroundSpotPink,
    glassTop = GlassTop,
    glassBottom = GlassBottom,
    glassBorder = GlassBorder,
    glassStrongTop = GlassStrongTop,
    glassStrongBottom = GlassStrongBottom,
    glassStrongBorder = GlassStrongBorder,
    glassHighlight = GlassHighlight
)

internal val LocalFinEduColors = staticCompositionLocalOf { DarkFinEduColors }
