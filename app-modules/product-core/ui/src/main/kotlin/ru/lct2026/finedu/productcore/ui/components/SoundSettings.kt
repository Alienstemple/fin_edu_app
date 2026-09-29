package ru.lct2026.finedu.productcore.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.ui.R
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.sound.SoundControls
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/**
 * Громкость игры ступенями от «без звука» до максимума и выключатель фоновой мелодии.
 * Один и тот же блок — в шторке из шапки и в настройках раздела для взрослого.
 */
@Composable
fun SoundSettings(
    volume: Int,
    musicEnabled: Boolean,
    onVolumeChange: (Int) -> Unit,
    onMusicToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FinEduTheme.colors
    val volumeText = if (volume == 0) {
        stringResource(R.string.sound_volume_off)
    } else {
        stringResource(R.string.sound_volume_value, volume, Settings.MAX_VOLUME)
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(
                painter = painterResource(if (volume == 0) R.drawable.ic_sound_off else R.drawable.ic_sound_on),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.sound_volume),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Text(text = volumeText, style = MaterialTheme.typography.bodyMedium, color = colors.gold)
        }
        Slider(
            value = volume.toFloat(),
            onValueChange = { value ->
                val step = value.roundToInt()
                if (step != volume) onVolumeChange(step)
            },
            valueRange = 0f..Settings.MAX_VOLUME.toFloat(),
            steps = Settings.MAX_VOLUME - 1,
            colors = SliderDefaults.colors(
                thumbColor = colors.gold,
                activeTrackColor = colors.gold,
                inactiveTrackColor = colors.glassBorder,
                activeTickColor = colors.onGold,
                inactiveTickColor = colors.glassHighlight
            ),
            modifier = Modifier.semantics { stateDescription = volumeText }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTarget)
                .toggleable(value = musicEnabled, role = Role.Switch, onValueChange = { onMusicToggle() }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_music),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = stringResource(R.string.sound_music),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = musicEnabled,
                onCheckedChange = null,
                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

/** Шторка «Звук» из шапки. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SoundSheet(controls: SoundControls, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.sound_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() }
            )
            SoundSettings(
                volume = controls.volume,
                musicEnabled = controls.musicEnabled,
                onVolumeChange = controls.onVolumeChange,
                onMusicToggle = controls.onMusicToggle
            )
        }
    }
}

@Preview
@Composable
private fun SoundSettingsPreview() {
    FinEduPreview {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            SoundSettings(volume = 7, musicEnabled = true, onVolumeChange = {}, onMusicToggle = {})
            SoundSettings(volume = 0, musicEnabled = false, onVolumeChange = {}, onMusicToggle = {})
        }
    }
}
