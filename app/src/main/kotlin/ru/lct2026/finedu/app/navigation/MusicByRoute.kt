package ru.lct2026.finedu.app.navigation

import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.sound.MusicTrack

/**
 * Мелодия раздела по маршруту текущего экрана ([androidx.navigation.NavDestination.route]).
 * Маршрут type-safe навигации начинается с `serialName` класса, аргументы идут после `/` или `?`, поэтому
 * сравниваем по нему — без рефлексии. Экран без мелодии в списке — `null`, играть нечему.
 */
internal fun musicTrackFor(route: String?): MusicTrack? {
    val name = route?.substringBefore('/')?.substringBefore('?') ?: return null
    return RouteMusic[name]
}

private val RouteMusic: Map<String, MusicTrack> = buildMap {
    put(FinEduRoute.Onboarding.serializer().descriptor.serialName, MusicTrack.CALM)
    put(FinEduRoute.Hero.serializer().descriptor.serialName, MusicTrack.CALM)
    put(FinEduRoute.Home.serializer().descriptor.serialName, MusicTrack.CALM)
    put(FinEduRoute.Glossary.serializer().descriptor.serialName, MusicTrack.CALM)
    put(FinEduRoute.Shorts.serializer().descriptor.serialName, MusicTrack.CALM)
    put(FinEduRoute.Parent.serializer().descriptor.serialName, MusicTrack.CALM)
    put(FinEduRoute.Budget.serializer().descriptor.serialName, MusicTrack.THOUGHTFUL)
    put(FinEduRoute.Quests.serializer().descriptor.serialName, MusicTrack.THOUGHTFUL)
    put(FinEduRoute.Quest.serializer().descriptor.serialName, MusicTrack.THOUGHTFUL)
    put(FinEduRoute.Shop.serializer().descriptor.serialName, MusicTrack.DISCO)
    put(FinEduRoute.PeriodSummary.serializer().descriptor.serialName, MusicTrack.DISCO)
    put(FinEduRoute.Savings.serializer().descriptor.serialName, MusicTrack.BUILDER)
}
