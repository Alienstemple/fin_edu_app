package ru.lct2026.finedu.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ru.lct2026.finedu.productcore.navigation.api.FeatureNavigationContribution
import ru.lct2026.finedu.productcore.navigation.api.FinEduRoute
import ru.lct2026.finedu.productcore.ui.sound.MusicTrack

/** [onMusicChange] — мелодия раздела, открытого сейчас (см. [musicTrackFor]). */
@Composable
internal fun FinEduNavHost(
    contributions: Set<FeatureNavigationContribution>,
    startDestination: FinEduRoute,
    onMusicChange: (MusicTrack?) -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val track = musicTrackFor(entry?.destination?.route)
    LaunchedEffect(track) { onMusicChange(track) }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        contributions.forEach { contribution ->
            with(contribution) { register(navController) }
        }
    }
}
