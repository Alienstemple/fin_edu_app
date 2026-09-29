package ru.lct2026.finedu.feature.parent.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.parent.ui.BonusReason
import ru.lct2026.finedu.feature.parent.ui.BonusUiState
import ru.lct2026.finedu.feature.parent.ui.R
import ru.lct2026.finedu.productcore.domain.model.AgeMode
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.AmountStepper
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.SoundSettings
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.sound.LocalSoundPlayer
import ru.lct2026.finedu.productcore.ui.sound.SoundEffect
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Вкладка «Настройки»: бонус, возрастной режим, звук, спокойный режим, крупный шрифт, сброс и удаление. */
@Composable
internal fun SettingsTab(
    bonus: BonusUiState,
    settings: Settings,
    onBonusMinusClick: () -> Unit,
    onBonusPlusClick: () -> Unit,
    onBonusReasonSelect: (BonusReason) -> Unit,
    onGrantBonusClick: (String) -> Unit,
    onAgeModeSelect: (AgeMode) -> Unit,
    onCalmModeToggle: () -> Unit,
    onLargeFontToggle: () -> Unit,
    onVolumeChange: (Int) -> Unit,
    onMusicToggle: () -> Unit,
    onResetClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        BonusCard(
            bonus = bonus,
            onMinusClick = onBonusMinusClick,
            onPlusClick = onBonusPlusClick,
            onReasonSelect = onBonusReasonSelect,
            onGrantClick = onGrantBonusClick
        )
        ParentCard {
            SectionTitle(stringResource(R.string.parent_age_title))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AgeMode.entries.forEach { mode ->
                    OptionChip(
                        text = stringResource(mode.labelRes),
                        isSelected = settings.ageMode == mode,
                        onClick = { onAgeModeSelect(mode) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            SecondaryText(stringResource(settings.ageMode.hintRes))
        }
        ParentCard {
            SectionTitle(stringResource(CoreR.string.sound_title))
            SoundSettings(
                volume = settings.volume,
                musicEnabled = settings.musicEnabled,
                onVolumeChange = onVolumeChange,
                onMusicToggle = onMusicToggle
            )
        }
        ParentCard {
            SwitchRow(
                title = stringResource(R.string.parent_calm_title),
                hint = stringResource(R.string.parent_calm_hint),
                isChecked = settings.calmMode,
                onToggle = onCalmModeToggle
            )
            SwitchRow(
                title = stringResource(R.string.parent_large_font_title),
                hint = stringResource(R.string.parent_large_font_hint),
                isChecked = settings.largeFont,
                onToggle = onLargeFontToggle
            )
        }
        FinButton(
            text = stringResource(R.string.parent_reset),
            onClick = onResetClick,
            style = FinButtonStyle.Secondary
        )
        FinButton(
            text = stringResource(R.string.parent_delete),
            onClick = onDeleteClick,
            style = FinButtonStyle.Secondary
        )
    }
}

@Composable
private fun BonusCard(
    bonus: BonusUiState,
    onMinusClick: () -> Unit,
    onPlusClick: () -> Unit,
    onReasonSelect: (BonusReason) -> Unit,
    onGrantClick: (String) -> Unit
) {
    val sound = LocalSoundPlayer.current
    ParentCard {
        SectionTitle(stringResource(R.string.parent_bonus_title))
        SecondaryText(stringResource(R.string.parent_bonus_lead))
        AmountStepper(
            value = Dzynki(bonus.amount),
            onMinus = onMinusClick,
            onPlus = onPlusClick,
            canMinus = bonus.canMinus,
            canPlus = bonus.canPlus,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(text = stringResource(R.string.parent_bonus_reason), style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            BonusReason.entries.forEach { reason ->
                OptionChip(
                    text = stringResource(reason.labelRes),
                    isSelected = bonus.reason == reason,
                    onClick = { onReasonSelect(reason) }
                )
            }
        }
        val reasonText = stringResource(bonus.reason.labelRes)
        FinButton(
            text = stringResource(R.string.parent_bonus_grant, bonus.amount),
            onClick = {
                sound.play(SoundEffect.INCOME)
                onGrantClick(reasonText)
            }
        )
        bonus.granted?.let { granted ->
            Row(
                modifier = Modifier.semantics(mergeDescendants = true) { liveRegion = LiveRegionMode.Polite },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_check),
                    contentDescription = null,
                    tint = FinEduTheme.colors.savings
                )
                Text(
                    text = stringResource(R.string.parent_bonus_granted, granted),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/** Выбор варианта: выбранный — со «стеклом» посильнее и галочкой, не только цветом. */
@Composable
private fun OptionChip(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .glass(
                shape = MaterialTheme.shapes.small,
                style = if (isSelected) GlassStyle.Strong else GlassStyle.Regular
            )
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally)
    ) {
        if (isSelected) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_check),
                contentDescription = null,
                tint = FinEduTheme.colors.gold,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SwitchRow(title: String, hint: String, isChecked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .toggleable(value = isChecked, role = Role.Switch, onValueChange = { onToggle() }),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            SecondaryText(hint)
        }
        Switch(
            checked = isChecked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
        )
    }
}

private val AgeMode.labelRes: Int
    get() = when (this) {
        AgeMode.YOUNGER -> R.string.parent_age_younger
        AgeMode.OLDER -> R.string.parent_age_older
    }

private val AgeMode.hintRes: Int
    get() = when (this) {
        AgeMode.YOUNGER -> R.string.parent_age_younger_hint
        AgeMode.OLDER -> R.string.parent_age_older_hint
    }

private val BonusReason.labelRes: Int
    get() = when (this) {
        BonusReason.HELPED -> R.string.parent_bonus_reason_helped
        BonusReason.STUDIES -> R.string.parent_bonus_reason_studies
        BonusReason.JUST_BECAUSE -> R.string.parent_bonus_reason_just_because
    }

@Preview(heightDp = 1100)
@Composable
private fun SettingsTabPreview() {
    FinEduPreview {
        SettingsTab(
            bonus = BonusUiState(amount = 50, granted = 50),
            settings = Settings(calmMode = true),
            onBonusMinusClick = {},
            onBonusPlusClick = {},
            onBonusReasonSelect = {},
            onGrantBonusClick = {},
            onAgeModeSelect = {},
            onCalmModeToggle = {},
            onLargeFontToggle = {},
            onVolumeChange = {},
            onMusicToggle = {},
            onResetClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}
