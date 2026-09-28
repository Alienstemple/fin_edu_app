package ru.lct2026.finedu.feature.parent.ui

import androidx.compose.runtime.Immutable
import ru.lct2026.finedu.feature.parent.domain.model.ParentGate
import ru.lct2026.finedu.feature.parent.domain.model.SavingsChart
import ru.lct2026.finedu.feature.parent.domain.model.ThemeStatus
import ru.lct2026.finedu.productcore.domain.model.Article
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.ScamScheme
import ru.lct2026.finedu.productcore.domain.model.Settings

/**
 * Раздел для взрослого: барьер с примером, затем четыре вкладки внутри одного экрана.
 * [input] — набранный ответ барьера, [openedArticle] — открытая статья во вкладке «Статьи».
 */
@Immutable
internal data class ParentUiState(
    val gate: ParentGate,
    val input: String = "",
    val isWrongAnswer: Boolean = false,
    val isUnlocked: Boolean = false,
    val tab: ParentTab = ParentTab.PROGRESS,
    val petLook: PetLook? = null,
    val progress: ProgressUiState? = null,
    val articles: List<Article> = emptyList(),
    val openedArticle: Article? = null,
    val scamSchemes: List<ScamScheme> = emptyList(),
    val discussedSchemeIds: Set<String> = emptySet(),
    val talkQuestions: List<String> = emptyList(),
    val bonus: BonusUiState = BonusUiState(),
    val settings: Settings = Settings(),
    val dialog: ParentDialog? = null
)

/** Вкладка «Прогресс»: только своя динамика, без сравнения с другими детьми. */
@Immutable
internal data class ProgressUiState(
    val playerName: String,
    val petName: String,
    val week: Int,
    val totalWeeks: Int,
    val storyTitle: String?,
    val stars: Int,
    val savings: Int,
    val goalTitle: String?,
    val goalPrice: Int?,
    val chart: SavingsChart?,
    val themes: List<ThemeProgress>
)

@Immutable
internal data class ThemeProgress(val theme: QuestTheme, val status: ThemeStatus)

/** Бонус от взрослого: сумма 10…100 и причина. [granted] — сколько только что начислено. */
@Immutable
internal data class BonusUiState(
    val amount: Int = DEFAULT_AMOUNT,
    val reason: BonusReason = BonusReason.HELPED,
    val granted: Int? = null
) {
    val canMinus: Boolean get() = amount > MIN_AMOUNT
    val canPlus: Boolean get() = amount < MAX_AMOUNT

    companion object {
        const val MIN_AMOUNT = 10
        const val MAX_AMOUNT = 100
        const val DEFAULT_AMOUNT = 30
    }
}

internal enum class ParentTab { PROGRESS, ARTICLES, SCAMS, SETTINGS }

/** За что бонус: подпись — в ресурсах, в журнал поступлений пишется её текст. */
internal enum class BonusReason { HELPED, STUDIES, JUST_BECAUSE }

internal enum class ParentDialog { RESET, DELETE }
