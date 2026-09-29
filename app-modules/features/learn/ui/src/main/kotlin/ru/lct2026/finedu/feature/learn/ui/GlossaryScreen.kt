package ru.lct2026.finedu.feature.learn.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.productcore.domain.model.GlossaryTerm
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.GlassStyle
import ru.lct2026.finedu.productcore.ui.components.MinTouchTarget
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@Composable
internal fun GlossaryRoute(
    onBack: () -> Unit,
    onNavigate: (FinEduRoute) -> Unit,
    viewModel: GlossaryViewModel = hiltViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    GlossaryScreen(state = state, onBack = onBack, onNavigate = onNavigate)
}

@Composable
internal fun GlossaryScreen(
    state: GlossaryUiState,
    onBack: () -> Unit,
    onNavigate: (FinEduRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = {
            FinTopBar(
                title = stringResource(R.string.glossary_title),
                onBack = onBack,
                onParent = { onNavigate(FinEduRoute.Parent) }
            )
        }
    ) { padding ->
        val terms = when (state) {
            GlossaryUiState.Loading -> emptyList()
            is GlossaryUiState.Content -> state.terms
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { GlossaryIntro() }
            itemsIndexed(terms) { index, term -> GlossaryTermCard(term = term, index = index) }
            item {
                GlossaryFooter(
                    onShortsClick = { onNavigate(FinEduRoute.Shorts) },
                    onOnboardingClick = { onNavigate(FinEduRoute.Onboarding) }
                )
            }
        }
    }
}

@Composable
private fun GlossaryIntro() {
    Text(text = stringResource(R.string.glossary_intro), style = MaterialTheme.typography.bodyLarge)
}

/**
 * Карточка термина: рисунок понятия на цветной плашке. Для понятия без рисунка (добавленного в контент позже) —
 * мини-Дзынь; его шёрстка, эмоция и наклон плашки меняются по [index].
 */
@Composable
private fun GlossaryTermCard(term: GlossaryTerm, index: Int) {
    val plateColors = with(FinEduTheme.colors) { listOf(wants, gold, selection, needs, savings) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .semantics(mergeDescendants = true) {}
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .rotate(if (index % 2 == 0) -TILT_DEGREES else TILT_DEGREES)
                .background(
                    plateColors[index % plateColors.size].copy(alpha = PLATE_ALPHA),
                    MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            val pictureRes = term.pictureRes
            if (pictureRes != null) {
                Image(painter = painterResource(pictureRes), contentDescription = null, modifier = Modifier.size(56.dp))
            } else {
                PetView(
                    look = PetLook(
                        fur = PetFur.entries[index % PetFur.entries.size],
                        hat = PetHat.entries[index / PetFur.entries.size % PetHat.entries.size]
                    ),
                    mood = GlossaryMoods[index % GlossaryMoods.size],
                    modifier = Modifier.width(64.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = term.term, style = MaterialTheme.typography.titleMedium)
            Text(
                text = term.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GlossaryFooter(onShortsClick: () -> Unit, onOnboardingClick: () -> Unit) {
    Column(
        modifier = Modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MinTouchTarget)
                .glass(style = GlassStyle.Strong)
                .clickable(role = Role.Button, onClick = onShortsClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(FinEduTheme.colors.gold, CircleShape)
            )
            Text(
                text = stringResource(R.string.glossary_shorts_link),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            Icon(painter = painterResource(CoreR.drawable.ic_chevron_right), contentDescription = null)
        }
        FinButton(
            text = stringResource(R.string.glossary_onboarding_again),
            onClick = onOnboardingClick,
            style = FinButtonStyle.Secondary
        )
    }
}

@get:DrawableRes
private val GlossaryTerm.pictureRes: Int?
    get() = when (id) {
        "income_regular" -> R.drawable.ic_term_income_regular
        "income_irregular" -> R.drawable.ic_term_income_irregular
        "expense" -> R.drawable.ic_term_expense
        "expense_mandatory" -> R.drawable.ic_term_expense_mandatory
        "savings" -> R.drawable.ic_term_savings
        "goal" -> R.drawable.ic_term_goal
        "term" -> R.drawable.ic_term_term
        "price" -> R.drawable.ic_term_price
        "change" -> R.drawable.ic_term_change
        "ad" -> R.drawable.ic_term_ad
        "safety_cushion" -> R.drawable.ic_term_safety_cushion
        else -> null
    }

private const val TILT_DEGREES = 3f
private const val PLATE_ALPHA = 0.35f

/** Эмоции мини-Дзыня для понятий без рисунка — по порядку терминов, как в макете. */
private val GlossaryMoods = listOf(
    PetMood.HAPPY,
    PetMood.SHOCK,
    PetMood.JOY,
    PetMood.HAPPY,
    PetMood.PROUD,
    PetMood.HAPPY,
    PetMood.THINKING,
    PetMood.NEUTRAL,
    PetMood.HAPPY,
    PetMood.SHOCK,
    PetMood.COLD
)

@Preview
@Composable
private fun GlossaryScreenPreview() {
    FinEduPreview {
        GlossaryScreen(
            state = GlossaryUiState.Content(
                listOf(
                    GlossaryTerm(
                        "income_regular",
                        "Доход регулярный",
                        "Деньги, что приходят по расписанию — как дзыньки каждую неделю."
                    ),
                    GlossaryTerm("expense", "Расход", "Всё, на что уходят деньги — от каши до шляпы с пером."),
                    GlossaryTerm("savings", "Накопления", "Деньги, которые не тратишь сразу, а откладываешь в Копилку.")
                )
            ),
            onBack = {},
            onNavigate = {}
        )
    }
}

@Preview
@Composable
private fun GlossaryScreenLoadingPreview() {
    FinEduPreview {
        GlossaryScreen(state = GlossaryUiState.Loading, onBack = {}, onNavigate = {})
    }
}
