package ru.lct2026.finedu.productcore.domain.model

internal object Fixtures {
    val profile = Profile("Капитан Носок", "Дзынь", PetLook(PetFur.MINT, PetHat.CAP), isDemo = true)
    val start = GameEngine.newGame(profile, nowMillis = 0L)
    val weekPlan = BagAmounts(Dzynki(150), Dzynki(120), Dzynki(30))
    val planned = GameEngine.distribute(start, weekPlan).success()

    val kasha = ShopItem("kasha", "Каша", Dzynki(20), Bag.NEEDS, shortageLine = null)
    val groceries = ShopItem("groceries", "Продукты на неделю", Dzynki(150), Bag.NEEDS, shortageLine = null)
    val hat = ShopItem("hat", "Шляпа с пером", Dzynki(100), Bag.WANTS, shortageLine = "Шляпа такая красивая…")

    val lamp = Goal("lamp", "Лампа", Dzynki(100))
    val window = Goal("window", "Окно с видом", Dzynki(200))
    val scooter = Goal("scooter", "Самокат", Dzynki(300))

    fun GameResult.success(): GameResult.Success = when (this) {
        is GameResult.Success -> this
        is GameResult.NotEnoughMoney -> error("Ожидали успех, а не хватает $missing")
    }

    fun PlanResult.success(): GameState = when (this) {
        is PlanResult.Success -> state
        is PlanResult.NotBalanced -> error("План не сходится: $left")
    }
}
