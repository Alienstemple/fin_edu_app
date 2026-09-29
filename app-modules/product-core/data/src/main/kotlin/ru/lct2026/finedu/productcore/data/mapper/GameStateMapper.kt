package ru.lct2026.finedu.productcore.data.mapper

import ru.lct2026.finedu.productcore.data.local.BagAmountsDto
import ru.lct2026.finedu.productcore.data.local.GameStateDto
import ru.lct2026.finedu.productcore.data.local.LedgerEntryDto
import ru.lct2026.finedu.productcore.data.local.PeriodDto
import ru.lct2026.finedu.productcore.data.local.PeriodResultDto
import ru.lct2026.finedu.productcore.data.local.PetDto
import ru.lct2026.finedu.productcore.data.local.ProfileDto
import ru.lct2026.finedu.productcore.data.local.SettingsDto
import ru.lct2026.finedu.productcore.domain.model.BagAmounts
import ru.lct2026.finedu.productcore.domain.model.Dzynki
import ru.lct2026.finedu.productcore.domain.model.GameState
import ru.lct2026.finedu.productcore.domain.model.LedgerEntry
import ru.lct2026.finedu.productcore.domain.model.Period
import ru.lct2026.finedu.productcore.domain.model.PeriodResult
import ru.lct2026.finedu.productcore.domain.model.Pet
import ru.lct2026.finedu.productcore.domain.model.PetLook
import ru.lct2026.finedu.productcore.domain.model.Profile
import ru.lct2026.finedu.productcore.domain.model.Settings

internal fun GameState.toDto(): GameStateDto = GameStateDto(
    profile = ProfileDto(
        profile.playerName,
        profile.petName,
        profile.look.fur.name,
        profile.look.hat.name,
        profile.isDemo
    ),
    unallocated = unallocated.amount,
    needsLeft = needsLeft.amount,
    wantsLeft = wantsLeft.amount,
    savings = savings.amount,
    placedGoalIds = placedGoalIds,
    stars = stars,
    period = period.toDto(),
    pet = PetDto(pet.charge, pet.vibe, pet.calm, pet.xp),
    completedQuestIds = completedQuestIds,
    history = history.map { it.toDto() },
    ledger = ledger.map { LedgerEntryDto(it.periodNumber, it.source.name, it.amount.amount, it.note) },
    settings = SettingsDto(
        settings.ageMode.name,
        settings.calmMode,
        settings.largeFont,
        settings.volume,
        settings.musicEnabled
    ),
    lastVisitMillis = lastVisitMillis
)

internal fun GameStateDto.toDomain(): GameState = GameState(
    profile = Profile(
        playerName = profile.playerName,
        petName = profile.petName,
        look = PetLook(profile.fur.toEnum(), profile.hat.toEnum()),
        isDemo = profile.isDemo
    ),
    unallocated = Dzynki(unallocated),
    needsLeft = Dzynki(needsLeft),
    wantsLeft = Dzynki(wantsLeft),
    savings = Dzynki(savings),
    placedGoalIds = placedGoalIds,
    stars = stars,
    period = period.toDomain(),
    pet = Pet(pet.charge, pet.vibe, pet.calm, pet.xp),
    completedQuestIds = completedQuestIds,
    history = history.map { it.toDomain() },
    ledger = ledger.map { LedgerEntry(it.periodNumber, it.source.toEnum(), Dzynki(it.amount), it.note) },
    settings = Settings(
        settings.ageMode.toEnum(),
        settings.calmMode,
        settings.largeFont,
        settings.volume.coerceIn(0, Settings.MAX_VOLUME),
        settings.musicEnabled
    ),
    lastVisitMillis = lastVisitMillis
)

private fun Period.toDto() = PeriodDto(
    number = number,
    income = income.amount,
    plan = plan?.toDto(),
    actual = actual.toDto(),
    withdrawn = withdrawn.amount,
    completedQuestIds = completedQuestIds,
    isChallengeJoined = isChallengeJoined
)

private fun PeriodDto.toDomain() = Period(
    number = number,
    income = Dzynki(income),
    plan = plan?.toDomain(),
    actual = actual.toDomain(),
    withdrawn = Dzynki(withdrawn),
    completedQuestIds = completedQuestIds,
    isChallengeJoined = isChallengeJoined
)

private fun PeriodResult.toDto() = PeriodResultDto(
    number = number,
    income = income.amount,
    plan = plan?.toDto(),
    actual = actual.toDto(),
    leftoverToSavings = leftoverToSavings.amount,
    withdrawn = withdrawn.amount,
    xpReasons = xpReasons.map { it.name },
    hasEarnedStar = hasEarnedStar,
    stageBefore = stageBefore.name,
    stageAfter = stageAfter.name
)

private fun PeriodResultDto.toDomain() = PeriodResult(
    number = number,
    income = Dzynki(income),
    plan = plan?.toDomain(),
    actual = actual.toDomain(),
    leftoverToSavings = Dzynki(leftoverToSavings),
    withdrawn = Dzynki(withdrawn),
    xpReasons = xpReasons.map { it.toEnum() },
    hasEarnedStar = hasEarnedStar,
    stageBefore = stageBefore.toEnum(),
    stageAfter = stageAfter.toEnum()
)

private fun BagAmounts.toDto() = BagAmountsDto(needs.amount, wants.amount, savings.amount)

private fun BagAmountsDto.toDomain() = BagAmounts(Dzynki(needs), Dzynki(wants), Dzynki(savings))
