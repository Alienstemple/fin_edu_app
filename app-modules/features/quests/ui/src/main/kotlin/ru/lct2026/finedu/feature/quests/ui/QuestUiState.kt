package ru.lct2026.finedu.feature.quests.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PetStage
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestOption

/** Задание с мем-форматом «Никто: / Реклама:». */
internal const val AD_QUEST_ID = "ad"

@Immutable
internal sealed interface QuestUiState {

    data object Loading : QuestUiState

    /** Задание с выбором: [result] `null`, пока вариант не выбран. */
    data class Choice(
        val quest: Quest.Choice,
        val petLook: PetLook,
        val petStage: PetStage,
        val result: ChoiceResult? = null
    ) : QuestUiState {
        val isAd: Boolean get() = quest.id == AD_QUEST_ID
    }

    /** «Это мошенники?». */
    data class Scam(val quest: Quest.Scam, val petLook: PetLook, val petStage: PetStage, val step: ScamStep) :
        QuestUiState
}

/** Выбранный вариант и награда (ноль, если задание уже проходили). */
@Immutable
internal data class ChoiceResult(val option: QuestOption, val reward: Dzynki)

@Immutable
internal sealed interface ScamStep {

    /** Сообщение и вопрос «что это?». */
    data class Message(val index: Int) : ScamStep

    /** Разбор сообщения после ответа. */
    data class Review(val index: Int, val answer: ScamAnswer) : ScamStep

    /** Итог: главное правило и награда (ноль, если задание уже проходили). */
    data class Summary(val reward: Dzynki) : ScamStep
}

/** Ответ на сообщение. Любой ответ — не ошибка: дальше всегда разбор. */
internal enum class ScamAnswer { SUSPICIOUS, NORMAL }
