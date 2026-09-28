package ru.lct2026.finedu.feature.hero.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.feature.hero.ui.R
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Выбор внешности: шёрстка и убор. Каждый вариант — мини-Дзынь с подписью, выбранный отмечен рамкой и галочкой. */
@Composable
internal fun LookPicker(
    look: PetLook,
    onFurClick: (PetFur) -> Unit,
    onHatClick: (PetHat) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OptionGroup(title = stringResource(R.string.hero_fur)) {
            PetFur.entries.forEach { fur ->
                OptionTile(
                    label = stringResource(fur.labelRes),
                    look = PetLook(fur, PetHat.NONE),
                    isSelected = fur == look.fur,
                    onClick = { onFurClick(fur) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        OptionGroup(title = stringResource(R.string.hero_hat)) {
            PetHat.entries.forEach { hat ->
                OptionTile(
                    label = stringResource(hat.labelRes),
                    look = PetLook(look.fur, hat),
                    isSelected = hat == look.hat,
                    onClick = { onHatClick(hat) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun OptionGroup(title: String, options: @Composable RowScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            content = options
        )
    }
}

@Composable
private fun OptionTile(
    label: String,
    look: PetLook,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.medium
    val selection = FinEduTheme.colors.selection
    Box(
        modifier = modifier
            .heightIn(min = MinTouchTarget)
            .glass(shape = shape)
            .then(if (isSelected) Modifier.border(2.dp, selection, shape) else Modifier)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PetView(look = look, modifier = Modifier.width(56.dp), stage = PetStage.BABY)
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
        if (isSelected) {
            Icon(
                painter = painterResource(CoreR.drawable.ic_check),
                contentDescription = null,
                tint = selection,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(20.dp)
            )
        }
    }
}

@get:StringRes
private val PetFur.labelRes: Int
    get() = when (this) {
        PetFur.LILAC -> R.string.hero_fur_lilac
        PetFur.MINT -> R.string.hero_fur_mint
        PetFur.CORAL -> R.string.hero_fur_coral
    }

@get:StringRes
private val PetHat.labelRes: Int
    get() = when (this) {
        PetHat.NONE -> R.string.hero_hat_none
        PetHat.CAP -> R.string.hero_hat_cap
        PetHat.HEADPHONES -> R.string.hero_hat_headphones
    }
