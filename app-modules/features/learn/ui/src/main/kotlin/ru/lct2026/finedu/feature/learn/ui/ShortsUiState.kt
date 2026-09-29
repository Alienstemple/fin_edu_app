package ru.lct2026.finedu.feature.learn.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.FeedCard
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.ShortsFrame

@Immutable
internal sealed interface ShortsUiState {

    data object Loading : ShortsUiState

    data class Content(
        val title: String,
        val frames: List<ShortsFrame>,
        val feed: List<FeedCard>,
        val look: PetLook,
        val mode: ShortsMode = ShortsMode.PLAYER,
        val frameIndex: Int = 0,
        val cardIndex: Int = 0,
        val isLiked: Boolean = false,
        val isUnderstood: Boolean = false,
        /** Награда за задание «Шортс», если его засчитали при этом просмотре. */
        val reward: ShortsReward? = null
    ) : ShortsUiState
}

/** Что показано: плеер шортса или лента карточек. */
internal enum class ShortsMode { PLAYER, FEED }

internal data class ShortsReward(val amount: Dzynki, val questTitle: String)
