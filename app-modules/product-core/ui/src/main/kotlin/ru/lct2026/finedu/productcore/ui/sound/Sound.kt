package ru.lct2026.finedu.productcore.ui.sound

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Короткие звуки. Играют поверх фоновой мелодии и не прерывают её. */
enum class SoundEffect {
    /** Кнопки и вкладки. */
    TAP,

    /** Пузырёк на главном раскрылся или свернулся. */
    BUBBLE,

    /** Покупка и пополнение копилки. */
    COIN,

    /** Пришли дзыньки: доход новой недели, награда за задание, бонус от взрослого. */
    INCOME
}

/** Фоновые мелодии разделов: какая играет — решает навигация приложения. */
enum class MusicTrack {
    /** «Не напрягает»: главный, знакомство, «Полезное», раздел для взрослого. */
    CALM,

    /** «Настраивает на размышления»: план недели и задания. */
    THOUGHTFUL,

    /** «Энергичная, немножко диско»: магазин и итоги недели. */
    DISCO,

    /** «Как в играх-стройках»: копилка, где строится уголок. */
    BUILDER
}

/** Проигрывает короткие звуки. Громкость и паузу в фоне учитывает реализация в `app`. */
fun interface SoundPlayer {
    fun play(effect: SoundEffect)
}

/** По умолчанию — тишина: превью и тесты работают без звукового движка. */
val LocalSoundPlayer = staticCompositionLocalOf { SoundPlayer {} }

/**
 * Настройки звука для шторки в шапке: [volume] от 0 до `Settings.MAX_VOLUME`, [musicEnabled] — фоновая мелодия.
 * `null` в [LocalSoundControls] — профиля ещё нет (знакомство), кнопка звука в шапке не показывается.
 */
class SoundControls(
    val volume: Int,
    val musicEnabled: Boolean,
    val onVolumeChange: (Int) -> Unit,
    val onMusicToggle: () -> Unit
)

val LocalSoundControls = compositionLocalOf<SoundControls?> { null }

/**
 * Звук [effect] один раз, когда появляется элемент с ключом [key] (награда, итог и т. п.). Отметка «уже звучал»
 * переживает возврат на экран по стеку и поворот: баннер, который остался на экране, не звенит повторно.
 */
@Composable
fun PlaySoundOnce(effect: SoundEffect, key: Any) {
    val player = LocalSoundPlayer.current
    var isPlayed by rememberSaveable(key) { mutableStateOf(false) }
    LaunchedEffect(key) {
        if (!isPlayed) {
            player.play(effect)
            isPlayed = true
        }
    }
}
