package ru.lct2026.finedu.productcore.data.local

import kotlinx.serialization.Serializable
import ru.lct2026.finedu.productcore.domain.model.Settings

/** Файл профиля. [state] `null` — профиль ещё не создан или удалён. */
@Serializable
internal data class StoredGameDto(val state: GameStateDto? = null)

@Serializable
internal data class GameStateDto(
    val profile: ProfileDto,
    val unallocated: Int,
    val needsLeft: Int,
    val wantsLeft: Int,
    val savings: Int,
    val placedGoalIds: Set<String> = emptySet(),
    val stars: Int = 0,
    val period: PeriodDto,
    val pet: PetDto,
    val completedQuestIds: Set<String> = emptySet(),
    val history: List<PeriodResultDto> = emptyList(),
    val ledger: List<LedgerEntryDto> = emptyList(),
    val settings: SettingsDto = SettingsDto(),
    val lastVisitMillis: Long
)

@Serializable
internal data class ProfileDto(
    val playerName: String,
    val petName: String,
    val fur: String,
    val hat: String,
    val isDemo: Boolean
)

@Serializable
internal data class SettingsDto(
    val ageMode: String = "OLDER",
    val calmMode: Boolean = false,
    val largeFont: Boolean = false,
    // Поля звука появились позже: в старых сохранениях их нет, берутся значения по умолчанию.
    val volume: Int = Settings.MAX_VOLUME,
    val musicEnabled: Boolean = true
)

@Serializable
internal data class BagAmountsDto(val needs: Int = 0, val wants: Int = 0, val savings: Int = 0)

@Serializable
internal data class PeriodDto(
    val number: Int,
    val income: Int,
    val plan: BagAmountsDto? = null,
    val actual: BagAmountsDto = BagAmountsDto(),
    val withdrawn: Int = 0,
    val completedQuestIds: Set<String> = emptySet(),
    val isChallengeJoined: Boolean = false
)

@Serializable
internal data class PetDto(val charge: Int, val vibe: Int, val calm: Int, val xp: Int)

@Serializable
internal data class PeriodResultDto(
    val number: Int,
    val income: Int,
    val plan: BagAmountsDto? = null,
    val actual: BagAmountsDto,
    val leftoverToSavings: Int,
    val withdrawn: Int,
    val xpReasons: List<String>,
    val hasEarnedStar: Boolean,
    val stageBefore: String,
    val stageAfter: String
)

@Serializable
internal data class LedgerEntryDto(val periodNumber: Int, val source: String, val amount: Int, val note: String? = null)
