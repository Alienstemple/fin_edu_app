package ru.lct2026.finedu.feature.parent.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.feature.parent.domain.model.ParentGate
import ru.lct2026.finedu.feature.parent.domain.model.ParentProgress
import ru.lct2026.finedu.productcore.domain.model.AgeMode
import ru.lct2026.finedu.productcore.domain.model.Article
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameRules
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.SavingsEngine
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.components.STEPPER_STEP
import ru.lct2026.finedu.productcore.ui.event.Event
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

// Барьер и четыре вкладки — один экран (без новых маршрутов), поэтому действий у ViewModel много.
@Suppress("TooManyFunctions")
@HiltViewModel
internal class ParentViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<ParentUiState>(ParentUiState(gate = ParentGate.random(Random.Default))) {

    init {
        observeGame()
    }

    private fun observeGame() {
        viewModelScope.launch {
            val content = contentRepository.content()
            updateState {
                copy(
                    articles = content.articles,
                    scamSchemes = content.scamSchemes,
                    talkQuestions = content.talkQuestions
                )
            }
            gameRepository.state.collect { game ->
                if (game != null) {
                    updateState {
                        copy(petLook = game.profile.look, progress = game.toProgress(content), settings = game.settings)
                    }
                }
            }
        }
    }

    fun onDigitClick(digit: Int) {
        updateState {
            if (input.length >=
                ParentGate.MAX_INPUT_LENGTH
            ) {
                this
            } else {
                copy(input = input + digit, isWrongAnswer = false)
            }
        }
    }

    fun onEraseClick() {
        updateState { copy(input = input.dropLast(1)) }
    }

    fun onDoneClick() {
        updateState {
            if (gate.isCorrect(input)) {
                copy(input = "", isWrongAnswer = false, isUnlocked = true)
            } else {
                copy(input = "", isWrongAnswer = true)
            }
        }
    }

    fun onTabSelect(tab: ParentTab) {
        updateState { copy(tab = tab, openedArticle = null) }
    }

    /** Открывается только статья с текстом; остальные помечены «Скоро». */
    fun onArticleClick(article: Article) {
        if (article.paragraphs.isNotEmpty()) updateState { copy(openedArticle = article) }
    }

    fun onArticleClose() {
        updateState { copy(openedArticle = null) }
    }

    fun onSchemeToggle(schemeId: String) {
        updateState {
            val isDiscussed = schemeId in discussedSchemeIds
            copy(discussedSchemeIds = if (isDiscussed) discussedSchemeIds - schemeId else discussedSchemeIds + schemeId)
        }
    }

    fun onBonusMinusClick() {
        changeBonusAmount(-STEPPER_STEP)
    }

    fun onBonusPlusClick() {
        changeBonusAmount(STEPPER_STEP)
    }

    private fun changeBonusAmount(delta: Int) {
        updateState {
            val amount = (bonus.amount + delta).coerceIn(BonusUiState.MIN_AMOUNT, BonusUiState.MAX_AMOUNT)
            copy(bonus = bonus.copy(amount = amount, granted = null))
        }
    }

    fun onBonusReasonSelect(reason: BonusReason) {
        updateState { copy(bonus = bonus.copy(reason = reason, granted = null)) }
    }

    /** [reasonText] — подпись выбранной причины: она попадает в журнал поступлений. */
    fun onGrantBonusClick(reasonText: String) {
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            val amount = currentState.bonus.amount
            gameRepository.save(GameEngine.grantParentBonus(game, Dzynki(amount), reasonText))
            updateState { copy(bonus = bonus.copy(granted = amount)) }
        }
    }

    fun onAgeModeSelect(mode: AgeMode) {
        updateSettings { copy(ageMode = mode) }
    }

    fun onCalmModeToggle() {
        updateSettings { copy(calmMode = !calmMode) }
    }

    fun onVolumeChange(volume: Int) {
        updateSettings { copy(volume = volume.coerceIn(0, Settings.MAX_VOLUME)) }
    }

    fun onMusicToggle() {
        updateSettings { copy(musicEnabled = !musicEnabled) }
    }

    fun onLargeFontToggle() {
        updateSettings { copy(largeFont = !largeFont) }
    }

    fun onResetClick() {
        updateState { copy(dialog = ParentDialog.RESET) }
    }

    fun onDeleteClick() {
        updateState { copy(dialog = ParentDialog.DELETE) }
    }

    fun onDialogDismiss() {
        updateState { copy(dialog = null) }
    }

    fun onDialogConfirm() {
        val dialog = currentState.dialog ?: return
        updateState { copy(dialog = null) }
        viewModelScope.launch {
            when (dialog) {
                ParentDialog.RESET -> {
                    val game = gameRepository.state.first() ?: return@launch
                    gameRepository.save(GameEngine.newGame(game.profile, System.currentTimeMillis(), game.settings))
                    offerEvent(ParentEvent.OpenHome)
                }

                ParentDialog.DELETE -> {
                    gameRepository.clear()
                    offerEvent(ParentEvent.OpenOnboarding)
                }
            }
        }
    }

    private fun updateSettings(transform: Settings.() -> Settings) {
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            gameRepository.save(game.copy(settings = game.settings.transform()))
        }
    }

    private fun GameState.toProgress(content: GameContent): ProgressUiState {
        val goal = SavingsEngine.currentGoal(this, content.goals)
        return ProgressUiState(
            playerName = profile.playerName,
            petName = profile.petName,
            week = period.number,
            totalWeeks = GameRules.DEMO_WEEKS,
            storyTitle = content.weeks.firstOrNull { it.number == period.number }?.title,
            stars = stars,
            savings = savings.amount,
            goalTitle = goal?.title,
            goalPrice = goal?.price?.amount,
            chart = ParentProgress.savingsChart(history, GameRules.DEMO_WEEKS),
            themes = QuestTheme.entries.map { theme ->
                ThemeProgress(theme, ParentProgress.themeStatus(theme, content.quests, completedQuestIds))
            }
        )
    }
}

internal sealed interface ParentEvent : Event {
    /** Прогресс сброшен: игра начинается с первой недели на главном. */
    data object OpenHome : ParentEvent

    /** Профиль удалён: всё начинается с онбординга. */
    data object OpenOnboarding : ParentEvent
}
