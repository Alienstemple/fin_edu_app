package ru.lct2026.finedu.productcore.domain.model

/** Прохождение заданий. Награда — за первое прохождение, при любом выборе: ошибка — сюжет, а не провал. */
object QuestEngine {

    /** Задание с выбором: показатели меняются по варианту. */
    fun choose(state: GameState, quest: Quest.Choice, optionId: String): QuestOutcome {
        val option = quest.options.first { it.id == optionId }
        return complete(state, quest, option.statChanges)
    }

    /** Задание без последствий выбора: «Это развод?» или засчитанное действием. */
    fun complete(state: GameState, quest: Quest, statChanges: Map<PetStat, Int> = emptyMap()): QuestOutcome {
        val reward = if (quest.id in state.completedQuestIds) Dzynki.ZERO else quest.reward
        val pet = statChanges.entries.fold(state.pet) { pet, (stat, delta) -> pet.change(stat, delta) }
        val period = state.period
        val after = state.copy(
            unallocated = state.unallocated + reward,
            pet = pet,
            completedQuestIds = state.completedQuestIds + quest.id,
            period = period.copy(completedQuestIds = period.completedQuestIds + quest.id),
            ledger = if (reward > Dzynki.ZERO) {
                state.ledger + LedgerEntry(period.number, IncomeSource.QUEST_REWARD, reward, quest.id)
            } else {
                state.ledger
            }
        )
        return QuestOutcome(after, reward, pet.changesSince(state.pet))
    }

    /** Задание, которое засчитывает [trigger], если оно ещё не пройдено. */
    fun questFor(state: GameState, quests: List<Quest>, trigger: QuestTrigger): Quest.Action? =
        quests.firstNotNullOfOrNull { quest ->
            when (quest) {
                is Quest.Action -> quest.takeIf { it.trigger == trigger && it.id !in state.completedQuestIds }
                is Quest.Choice, is Quest.Scam -> null
            }
        }
}
