package ru.lct2026.finedu.app.sound

import ru.lct2026.finedu.productcore.domain.model.Settings
import ru.lct2026.finedu.productcore.ui.sound.MusicTrack

/** Аудиофокус для фоновой мелодии: короткие звуки его не требуют. */
internal enum class MusicFocus {
    /** Можно играть. */
    GRANTED,

    /** Другое приложение просит потише (навигатор, уведомление): мелодия приглушена. */
    DUCKED,

    /** Звонок или голосовое сообщение: мелодия на паузе до возврата фокуса или до возвращения в приложение. */
    PAUSED
}

/**
 * Состояние звука без Android: из него [GameAudio] берёт громкость и решает, играть ли мелодию.
 * Мелодия играет, только когда приложение на экране, для раздела есть [track], она включена и громкость не 0.
 */
internal data class SoundMix(
    val volume: Int = Settings.MAX_VOLUME,
    val musicEnabled: Boolean = true,
    val track: MusicTrack? = null,
    val isForeground: Boolean = false,
    val focus: MusicFocus = MusicFocus.GRANTED
) {
    /**
     * Громкость коротких звуков 0..1. Слух воспринимает громкость логарифмически, поэтому ступени слайдера
     * переводятся квадратом: так «тише» и «громче» ощущаются равномерно, а не только в начале шкалы.
     */
    val effectGain: Float
        get() = if (isForeground) (volume.toFloat() / Settings.MAX_VOLUME).let { it * it } else 0f

    /** Мелодия тише коротких звуков, чтобы они были слышны поверх неё. */
    val musicGain: Float
        get() = effectGain * MUSIC_LEVEL * if (focus == MusicFocus.DUCKED) DUCK_LEVEL else 1f

    /** Какую мелодию играть сейчас; `null` — тишина или пауза. */
    val activeTrack: MusicTrack?
        get() = track.takeIf { musicEnabled && volume > 0 && isForeground && focus != MusicFocus.PAUSED }

    private companion object {
        const val MUSIC_LEVEL = 0.55f
        const val DUCK_LEVEL = 0.3f
    }
}
