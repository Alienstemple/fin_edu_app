package ru.lct2026.finedu.productcore.domain.model

/** Показатели питомца. */
enum class PetStat {
    /** Заряд — от «Нужного». */
    CHARGE,

    /** Вайб — от «Хочу». */
    VIBE,

    /** Спокойствие — от «Копилки». */
    CALM
}

/** Стадии роста. Опыт не отнимается, поэтому стадия не откатывается. */
enum class PetStage(val requiredXp: Int) {
    /** Малыш. */
    BABY(0),

    /** Шустрик. */
    SPRY(GameRules.SPRY_XP),

    /** Мастер мешочка. */
    MASTER(GameRules.MASTER_XP)
    ;

    /** Следующая стадия или `null`, если эта последняя. */
    val next: PetStage? get() = entries.getOrNull(ordinal + 1)

    companion object {
        fun forXp(xp: Int): PetStage = entries.last { xp >= it.requiredXp }
    }
}

/** Шёрстка: Сирень, Мята, Коралл. */
enum class PetFur { LILAC, MINT, CORAL }

/** Головной убор. */
enum class PetHat { NONE, CAP, HEADPHONES }

data class PetLook(val fur: PetFur, val hat: PetHat)

/**
 * Состояние питомца. Показатели держатся в [GameRules.STAT_MIN]..[GameRules.STAT_MAX]: нижняя граница —
 * «Дзынь устал», питомец не болеет и не умирает (ТЗ 2.5.9).
 */
data class Pet(
    val charge: Int = GameRules.STAT_INITIAL,
    val vibe: Int = GameRules.STAT_INITIAL,
    val calm: Int = GameRules.STAT_INITIAL,
    val xp: Int = 0
) {
    val stage: PetStage get() = PetStage.forXp(xp)

    operator fun get(stat: PetStat): Int = when (stat) {
        PetStat.CHARGE -> charge
        PetStat.VIBE -> vibe
        PetStat.CALM -> calm
    }

    fun isTired(stat: PetStat): Boolean = get(stat) <= GameRules.TIRED_THRESHOLD

    /** Меняет показатель на [delta] с учётом границ. */
    fun change(stat: PetStat, delta: Int): Pet {
        val value = (get(stat) + delta).coerceIn(GameRules.STAT_MIN, GameRules.STAT_MAX)
        return when (stat) {
            PetStat.CHARGE -> copy(charge = value)
            PetStat.VIBE -> copy(vibe = value)
            PetStat.CALM -> copy(calm = value)
        }
    }

    /** Поднимает каждый показатель минимум до [floor]. */
    fun raiseTo(floor: Int): Pet = copy(
        charge = maxOf(charge, floor),
        vibe = maxOf(vibe, floor),
        calm = maxOf(calm, floor)
    )

    /** Фактические изменения показателей относительно [before], без нулевых. */
    fun changesSince(before: Pet): Map<PetStat, Int> =
        PetStat.entries.associateWith { get(it) - before[it] }.filterValues { it != 0 }
}
