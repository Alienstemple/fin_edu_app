package ru.lct2026.finedu.feature.parent.domain.model

import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestTheme

/** Статус темы заданий для взрослого: без оценок, только «где мы сейчас». */
enum class ThemeStatus {
    /** Все задания темы пройдены. */
    DONE,

    /** Часть заданий пройдена. */
    IN_PROGRESS,

    /** Ни одного задания темы ещё не пройдено. */
    AHEAD
}

/**
 * График накоплений: [weekly] — сколько всего легло в копилку к концу каждой закрытой недели, [forecast] —
 * продолжение до конца сюжета, если откладывать по [averagePerWeek].
 */
data class SavingsChart(val weekly: List<Dzynki>, val forecast: List<Dzynki>, val averagePerWeek: Dzynki)

/** Сводка прогресса для раздела взрослого. */
object ParentProgress {

    fun themeStatus(theme: QuestTheme, quests: List<Quest>, completedQuestIds: Set<String>): ThemeStatus {
        val themeQuests = quests.filter { it.theme == theme }
        val done = themeQuests.count { it.id in completedQuestIds }
        return when {
            done == 0 -> ThemeStatus.AHEAD
            done == themeQuests.size -> ThemeStatus.DONE
            else -> ThemeStatus.IN_PROGRESS
        }
    }

    /**
     * Копилка на конец каждой закрытой недели — накопительно по [PeriodResult.savedTotal]; снятия и предметы уголка
     * не вычитаются. Прогноз тянется до недели [totalWeeks] (и хотя бы на неделю вперёд). `null` — закрытых недель
     * ещё нет.
     */
    fun savingsChart(history: List<PeriodResult>, totalWeeks: Int): SavingsChart? {
        if (history.isEmpty()) return null
        val weekly = history.runningFold(0) { total, week -> total + week.savedTotal.amount }.drop(1)
        val last = weekly.last()
        val average = last / weekly.size
        val forecastWeeks = (totalWeeks - weekly.size).coerceAtLeast(1)
        val forecast = (1..forecastWeeks).map { Dzynki(last + average * it) }
        return SavingsChart(weekly.map(::Dzynki), forecast, Dzynki(average))
    }
}
