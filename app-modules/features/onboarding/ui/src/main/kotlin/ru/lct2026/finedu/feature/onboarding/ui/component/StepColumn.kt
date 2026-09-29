package ru.lct2026.finedu.feature.onboarding.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

/** Внешность Дзыня в онбординге: своего питомца ребёнок заведёт позже. */
internal val OnboardingPetLook = PetLook(PetFur.LILAC, PetHat.NONE)

/** Прокручиваемая колонка шага: при крупном шрифте текст не обрезается. */
@Composable
internal fun StepColumn(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

/** Наклонная бирка-шутка («финансовый гений (нет)»). */
@Composable
internal fun TiltedTag(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = FinEduTheme.colors.gold,
        modifier = modifier
            .rotate(TAG_TILT_DEGREES)
            .glass(shape = MaterialTheme.shapes.extraSmall, style = GlassStyle.Strong)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

private const val TAG_TILT_DEGREES = -3f
