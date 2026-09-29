package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.sound.LocalSoundPlayer
import ru.lct2026.finedu.productcore.ui.sound.SoundEffect
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Нижняя навигация: Главная / Задания / Магазин / Копилка. */
@Composable
fun FinBottomBar(selected: FinTab, onSelect: (FinTab) -> Unit, modifier: Modifier = Modifier) {
    val sound = LocalSoundPlayer.current
    NavigationBar(
        modifier = modifier.glass(shape = MaterialTheme.shapes.extraLarge, style = GlassStyle.Strong),
        containerColor = Color.Transparent,
        windowInsets = WindowInsets(0)
    ) {
        FinTab.entries.forEach { tab ->
            val label = stringResource(tab.labelRes)
            NavigationBarItem(
                selected = tab == selected,
                onClick = {
                    sound.play(SoundEffect.TAP)
                    onSelect(tab)
                },
                icon = { Icon(painter = painterResource(tab.iconRes), contentDescription = null) },
                label = {
                    // Четыре вкладки на 360dp с крупным системным шрифтом не помещаются: подпись уменьшается,
                    // а не переносится посреди слова. Смысл вкладки дублирует иконка.
                    val style = MaterialTheme.typography.labelMedium
                    Text(
                        text = label,
                        style = style,
                        maxLines = 1,
                        softWrap = false,
                        autoSize = TextAutoSize.StepBased(minFontSize = MIN_LABEL_SIZE, maxFontSize = style.fontSize)
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = FinEduTheme.colors.onGold,
                    indicatorColor = FinEduTheme.colors.gold,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

private val MIN_LABEL_SIZE = 11.sp

private val FinTab.labelRes: Int
    get() = when (this) {
        FinTab.HOME -> R.string.nav_home
        FinTab.QUESTS -> R.string.nav_quests
        FinTab.SHOP -> R.string.nav_shop
        FinTab.SAVINGS -> R.string.nav_savings
    }

private val FinTab.iconRes: Int
    get() = when (this) {
        FinTab.HOME -> R.drawable.ic_nav_home
        FinTab.QUESTS -> R.drawable.ic_nav_quests
        FinTab.SHOP -> R.drawable.ic_nav_shop
        FinTab.SAVINGS -> R.drawable.ic_bag_savings
    }

@Preview
@Composable
private fun FinBottomBarPreview() {
    FinEduPreview {
        FinBottomBar(selected = FinTab.SHOP, onSelect = {}, modifier = Modifier.navigationBarsPadding())
    }
}
