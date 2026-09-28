package ru.lct2026.finedu.feature.home.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.AgeMode
import ru.lct2026.finedu.productcore.domain.model.GameContent
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.PetStat
import ru.lct2026.finedu.productcore.domain.model.SavingsEngine
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.illustration.PetMood
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<HomeUiState>(HomeUiState.Loading) {

    private var isReturn = false
    private var isEasterEgg = false
    private var easterEggJob: Job? = null
    private var game: GameState? = null
    private var content: GameContent? = null

    init {
        viewModelScope.launch {
            content = contentRepository.content()
            gameRepository.state.first()?.let { current ->
                val visit = GameEngine.welcomeBack(current, System.currentTimeMillis())
                isReturn = visit.isReturn
                gameRepository.save(visit.state)
            }
            gameRepository.state.filterNotNull().collect { state ->
                game = state
                render()
            }
        }
    }

    /** Пасхалка: долгое нажатие на питомца — «Ой. Это я?» на пару секунд. */
    fun onPetLongPress() {
        isEasterEgg = true
        render()
        easterEggJob?.cancel()
        easterEggJob = viewModelScope.launch {
            delay(EASTER_EGG_MILLIS)
            hideEasterEgg()
        }
    }

    /** Тап по питомцу закрывает пасхалку. */
    fun onPetClick() {
        easterEggJob?.cancel()
        hideEasterEgg()
    }

    private fun hideEasterEgg() {
        if (!isEasterEgg) return
        isEasterEgg = false
        render()
    }

    private fun render() {
        val state = game ?: return
        val content = content ?: return
        setState(state.toUiState(content))
    }

    private fun GameState.toUiState(content: GameContent): HomeUiState.Content {
        val tiredStat = PetStat.entries.filter { pet.isTired(it) }.minByOrNull { pet[it] }
        val goal = SavingsEngine.currentGoal(this, content.goals)
        val (mood, line) = when {
            isEasterEgg -> PetMood.SHOCK to HomeLine.EASTER_EGG
            tiredStat != null -> PetMood.COLD to HomeLine.LOW
            isReturn -> PetMood.HAPPY to HomeLine.RETURN
            else -> defaultMood() to if (unallocated.amount > 0) HomeLine.UNALLOCATED else HomeLine.NORMAL
        }
        return HomeUiState.Content(
            weekNumber = period.number,
            weekTitle = content.weeks.firstOrNull { it.number == period.number }?.title,
            isDemo = profile.isDemo,
            isYounger = settings.ageMode == AgeMode.YOUNGER,
            look = profile.look,
            pet = pet,
            mood = mood,
            line = line,
            notice = when {
                tiredStat != null -> HomeNotice.Low(tiredStat)
                isReturn -> HomeNotice.Return
                else -> null
            },
            isDim = tiredStat != null,
            placedGoalIds = placedGoalIds,
            stars = stars,
            balance = balance,
            needsLeft = needsLeft,
            wantsLeft = wantsLeft,
            savings = savings,
            unallocated = unallocated,
            goal = goal?.let {
                HomeGoal(
                    title = it.title,
                    saved = savings,
                    price = it.price,
                    weeksLeft = SavingsEngine.weeksToGoal(this, it)
                )
            },
            quest = content.quests.firstOrNull { it.id !in completedQuestIds }?.let { HomeQuest(it.id, it.title) }
        )
    }

    private fun GameState.defaultMood(): PetMood =
        if (pet.charge >= HAPPY_STAT || pet.vibe >= HAPPY_STAT) PetMood.HAPPY else PetMood.NEUTRAL

    private companion object {
        const val EASTER_EGG_MILLIS = 2_000L

        /** Заряд или вайб не ниже этого — Дзынь радуется. */
        const val HAPPY_STAT = 80
    }
}
