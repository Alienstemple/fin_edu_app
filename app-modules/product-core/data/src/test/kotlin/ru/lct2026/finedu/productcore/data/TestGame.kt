package ru.lct2026.finedu.productcore.data

import kotlinx.serialization.json.Json
import ru.lct2026.finedu.productcore.domain.model.AgeMode
import ru.lct2026.finedu.productcore.domain.model.Bag
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameEngine
import ru.lct2026.finedu.productcore.domain.model.GameResult
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.PetFur
import ru.lct2026.finedu.productcore.domain.model.PetHat
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.PlanResult
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.Quest
import ru.lct2026.finedu.productcore.domain.model.QuestEngine
import ru.lct2026.finedu.productcore.domain.model.QuestLevel
import ru.lct2026.finedu.productcore.domain.model.QuestTheme
import ru.lct2026.finedu.productcore.domain.model.QuestTrigger
import ru.lct2026.finedu.productcore.domain.model.SavingsEngine
import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.domain.model.ShopItem

internal object TestGame {
    val json = Json { ignoreUnknownKeys = true }

    /** Состояние, в котором заполнены все поля: история, звёздочки, бонус, задание, настройки. */
    val played: GameState by lazy {
        val profile = Profile("Капитан Носок", "Дзынь", PetLook(PetFur.CORAL, PetHat.HEADPHONES), isDemo = true)
        val plan = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30))
        val groceries = ShopItem("groceries", "Продукты", Dzynki(150), Bag.NEEDS, shortageLine = null)
        val quest = Quest.Action(
            "plan",
            QuestTheme.BUDGET,
            QuestLevel.EASY,
            "План",
            Dzynki(20),
            QuestTrigger.PLAN_FIXED
        )

        var state = GameEngine.newGame(profile, nowMillis = 42L, Settings(AgeMode.YOUNGER, calmMode = true))
        state = GameEngine.distribute(state, plan).orFail()
        state = GameEngine.buy(state, groceries).orFail()
        state = QuestEngine.complete(state, quest).state
        state = GameEngine.closePeriod(state).state
        state = GameEngine.grantParentBonus(state, Dzynki(50), "Помог с уборкой")
        state = GameEngine.distribute(state, BagAmounts(Dzynki(100), Dzynki(200), Dzynki(50))).orFail()
        state = SavingsEngine.withdraw(state, Dzynki(10)).orFail()
        state.copy(placedGoalIds = setOf("plaid"), period = state.period.copy(isChallengeJoined = true))
    }

    private fun PlanResult.orFail(): GameState = when (this) {
        is PlanResult.Success -> state
        is PlanResult.NotBalanced -> error("План не сходится: $left")
    }

    private fun GameResult.orFail(): GameState = when (this) {
        is GameResult.Success -> state
        is GameResult.NotEnoughMoney -> error("Не хватает $missing")
    }
}
