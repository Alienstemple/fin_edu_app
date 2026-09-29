package ru.lct2026.finedu.feature.hero.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.lct2026.finedu.feature.hero.ui.component.LookPicker
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.ui.R as CoreR
import ru.lct2026.finedu.productcore.ui.components.FinButton
import ru.lct2026.finedu.productcore.ui.components.FinButtonStyle
import ru.lct2026.finedu.productcore.ui.components.FinTopBar
import ru.lct2026.finedu.productcore.ui.components.glass
import ru.lct2026.finedu.productcore.ui.event.ObserveEvents
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.illustration.PetView
import ru.lct2026.finedu.productcore.ui.preview.FinEduPreview
import ru.lct2026.finedu.productcore.ui.theme.FinEduTheme

@Composable
internal fun HeroRoute(onBack: () -> Unit, onOpenHome: () -> Unit, viewModel: HeroViewModel = hiltViewModel()) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.events) { event ->
        when (event) {
            HeroEvent.OpenHome -> onOpenHome()
        }
    }
    val defaultPetName = stringResource(R.string.hero_default_pet_name)
    val defaultPlayerName = stringResource(R.string.hero_player_name_placeholder)
    HeroScreen(
        state = state,
        onBack = onBack,
        onFurClick = viewModel::onFurClick,
        onHatClick = viewModel::onHatClick,
        onPetNameChanged = viewModel::onPetNameChanged,
        onPlayerNameChanged = viewModel::onPlayerNameChanged,
        onDoneClick = { viewModel.onDoneClick(defaultPetName, defaultPlayerName) },
        onDemoClick = { viewModel.onDemoClick(defaultPetName, defaultPlayerName) }
    )
}

@Composable
internal fun HeroScreen(
    state: HeroUiState,
    onBack: () -> Unit,
    onFurClick: (PetFur) -> Unit,
    onHatClick: (PetHat) -> Unit,
    onPetNameChanged: (String) -> Unit,
    onPlayerNameChanged: (String) -> Unit,
    onDoneClick: () -> Unit,
    onDemoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val defaultPetName = stringResource(R.string.hero_default_pet_name)
    val petName = state.petName ?: defaultPetName
    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        topBar = { FinTopBar(title = stringResource(R.string.hero_title), onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PetPreview(state = state, name = petName.trim().ifEmpty { defaultPetName })
            LookPicker(look = state.look, onFurClick = onFurClick, onHatClick = onHatClick)
            NameField(
                title = stringResource(R.string.hero_pet_name),
                value = petName,
                onValueChange = onPetNameChanged,
                imeAction = ImeAction.Next
            )
            NameField(
                title = stringResource(R.string.hero_player_name),
                value = state.playerName,
                onValueChange = onPlayerNameChanged,
                imeAction = ImeAction.Done,
                placeholder = stringResource(R.string.hero_player_name_placeholder),
                hint = stringResource(R.string.hero_player_name_hint)
            )
            FinButton(
                text = stringResource(R.string.hero_done),
                onClick = onDoneClick,
                enabled = !state.isSaving,
                modifier = Modifier.padding(top = 8.dp)
            )
            FinButton(
                text = stringResource(R.string.hero_demo),
                onClick = onDemoClick,
                style = FinButtonStyle.Secondary,
                enabled = !state.isSaving
            )
        }
    }
}

@Composable
private fun PetPreview(state: HeroUiState, name: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .glass()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PetView(
            look = state.look,
            modifier = Modifier.width(PreviewPetWidth),
            mood = PetMood.HAPPY,
            stage = PetStage.BABY,
            contentDescription = stringResource(CoreR.string.pet_a11y, name)
        )
        Text(text = name, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun NameField(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    imeAction: ImeAction,
    placeholder: String? = null,
    hint: String? = null
) {
    val colors = FinEduTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            shape = MaterialTheme.shapes.medium,
            placeholder = placeholder?.let { { Text(text = it, style = MaterialTheme.typography.bodyLarge) } },
            supportingText = hint?.let { { Text(text = it, style = MaterialTheme.typography.bodyMedium) } },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = imeAction),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.selection,
                unfocusedBorderColor = colors.glassBorder,
                cursorColor = colors.selection,
                focusedContainerColor = colors.glassTop,
                unfocusedContainerColor = colors.glassBottom
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private val PreviewPetWidth = 180.dp

@Composable
private fun HeroPreview(state: HeroUiState) {
    FinEduPreview {
        HeroScreen(
            state = state,
            onBack = {},
            onFurClick = {},
            onHatClick = {},
            onPetNameChanged = {},
            onPlayerNameChanged = {},
            onDoneClick = {},
            onDemoClick = {}
        )
    }
}

@Preview(heightDp = 1100)
@Composable
private fun HeroScreenDefaultPreview() {
    HeroPreview(HeroUiState())
}

@Preview(heightDp = 1100)
@Composable
private fun HeroScreenFilledPreview() {
    HeroPreview(HeroUiState(fur = PetFur.MINT, hat = PetHat.HEADPHONES, petName = "Бублик", playerName = "Капитан"))
}
