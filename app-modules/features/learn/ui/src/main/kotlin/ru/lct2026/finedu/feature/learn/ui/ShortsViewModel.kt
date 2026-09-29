package ru.lct2026.finedu.feature.learn.ui

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.QuestEngine
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.repository.ContentRepository
import ru.lct2026.finedu.productcore.domain.repository.GameRepository
import ru.lct2026.finedu.productcore.ui.viewmodel.StatefulViewModel

@HiltViewModel
internal class ShortsViewModel @Inject constructor(
    private val gameRepository: GameRepository,
    private val contentRepository: ContentRepository
) : StatefulViewModel<ShortsUiState>(ShortsUiState.Loading) {

    init {
        viewModelScope.launch {
            val content = contentRepository.content()
            val look = gameRepository.state.first()?.profile?.look ?: DefaultLook
            setState(
                ShortsUiState.Content(
                    title = content.shorts.title,
                    frames = content.shorts.frames,
                    feed = content.feed,
                    look = look
                )
            )
        }
    }

    /** «Дальше» или тап по правой половине. После последнего кадра засчитывает задание и открывает ленту. */
    fun onNextFrameClick() {
        val content = when (val state = currentState) {
            ShortsUiState.Loading -> return
            is ShortsUiState.Content -> state
        }
        if (content.frameIndex < content.frames.lastIndex) {
            updateContent { copy(frameIndex = frameIndex + 1) }
        } else {
            updateContent { copy(mode = ShortsMode.FEED) }
            completeShortsQuest()
        }
    }

    fun onPreviousFrameClick() {
        updateContent { copy(frameIndex = (frameIndex - 1).coerceAtLeast(0)) }
    }

    fun onLikeClick() {
        updateContent { copy(isLiked = !isLiked) }
    }

    fun onUnderstoodClick() {
        updateContent { copy(isUnderstood = !isUnderstood) }
    }

    /** Переключатель «Смотреть / Лента»: плеер всегда открывается с начала. */
    fun onModeClick(mode: ShortsMode) {
        updateContent {
            when (mode) {
                ShortsMode.PLAYER -> copy(mode = ShortsMode.PLAYER, frameIndex = 0)
                ShortsMode.FEED -> copy(mode = ShortsMode.FEED)
            }
        }
    }

    fun onNextCardClick() {
        updateContent { if (feed.isEmpty()) this else copy(cardIndex = (cardIndex + 1) % feed.size) }
    }

    private fun completeShortsQuest() {
        viewModelScope.launch {
            val game = gameRepository.state.first() ?: return@launch
            val quests = contentRepository.content().quests
            val quest = QuestEngine.questFor(game, quests, QuestTrigger.SHORTS_WATCHED) ?: return@launch
            val outcome = QuestEngine.complete(game, quest)
            gameRepository.save(outcome.state)
            if (outcome.reward > Dzynki.ZERO) {
                updateContent { copy(reward = ShortsReward(outcome.reward, quest.title)) }
            }
        }
    }

    private fun updateContent(block: ShortsUiState.Content.() -> ShortsUiState.Content) {
        updateState {
            when (this) {
                ShortsUiState.Loading -> this
                is ShortsUiState.Content -> block()
            }
        }
    }

    private companion object {
        val DefaultLook = PetLook(PetFur.LILAC, PetHat.NONE)
    }
}
