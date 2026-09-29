package ru.lct2026.finedu.app.sound

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import androidx.annotation.RawRes
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.lct2026.finedu.app.R
import ru.lct2026.finedu.productcore.ui.sound.MusicTrack
import ru.lct2026.finedu.productcore.ui.sound.SoundEffect
import ru.lct2026.finedu.productcore.ui.sound.SoundPlayer

/**
 * Звук игры: фоновая мелодия раздела ([MediaPlayer], по кругу, со сменой через плавное затухание) и короткие звуки
 * ([SoundPool] — играют поверх мелодии и не прерывают её).
 *
 * Звук идёт как игровой (`USAGE_GAME`, поток медиа): он слышен и в беззвучном режиме, и в режиме «Не беспокоить»,
 * если там разрешены медиа (так по умолчанию), — громче системной громкости медиа приложение звучать не может.
 * Вызовы — только с главного потока.
 */
@Singleton
internal class GameAudio @Inject constructor(@ApplicationContext private val context: Context) : SoundPlayer {

    private val audioManager = context.getSystemService(AudioManager::class.java)
    private val scope = MainScope()
    private val musicAttributes = attributes(AudioAttributes.CONTENT_TYPE_MUSIC)
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(MAX_EFFECT_STREAMS)
        .setAudioAttributes(attributes(AudioAttributes.CONTENT_TYPE_SONIFICATION))
        .build()
    private val effectIds = SoundEffect.entries.associateWith { soundPool.load(context, it.rawRes, 1) }
    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(musicAttributes)
        .setWillPauseWhenDucked(false)
        .setOnAudioFocusChangeListener(::onFocusChange)
        .build()

    private var mix = SoundMix()
    private var music: Music? = null
    private var hasFocus = false
    private var fade: Job? = null

    override fun play(effect: SoundEffect) {
        val gain = mix.effectGain
        if (gain > 0f) soundPool.play(effectIds.getValue(effect), gain, gain, 1, 0, 1f)
    }

    fun setSettings(volume: Int, musicEnabled: Boolean) = update { copy(volume = volume, musicEnabled = musicEnabled) }

    fun setTrack(track: MusicTrack?) = update { copy(track = track) }

    /** Приложение снова на экране: мелодия продолжается с того же места. */
    fun onForeground() = update { copy(isForeground = true, focus = MusicFocus.GRANTED) }

    /** Приложение ушло в фон: всё на паузу, фокус отдаём другим приложениям. */
    fun onBackground() {
        soundPool.autoPause()
        update { copy(isForeground = false) }
        abandonFocus()
    }

    private fun update(transform: SoundMix.() -> SoundMix) {
        val wasForeground = mix.isForeground
        mix = mix.transform()
        if (mix.isForeground && !wasForeground) soundPool.autoResume()
        sync()
    }

    private fun sync() {
        val target = mix.activeTrack
        val current = music
        when {
            target == null || !requestFocus() -> {
                // Затухающая мелодия освобождается при отмене смены — в фоне ничего не доигрывает.
                fade?.cancel()
                current?.player?.pause()
            }

            current?.track == target -> {
                if (fade?.isActive != true) current.player.setVolume(mix.musicGain, mix.musicGain)
                if (!current.player.isPlaying) current.player.start()
            }

            else -> crossfade(from = current, to = target)
        }
    }

    /** Старая мелодия затихает, новая нарастает — без обрыва при переходе между разделами. */
    private fun crossfade(from: Music?, to: MusicTrack) {
        fade?.cancel()
        val next = createPlayer(to) ?: return
        music = next
        next.player.setVolume(0f, 0f)
        next.player.start()
        val fromGain = mix.musicGain
        fade = scope.launch {
            repeat(FADE_STEPS) { step ->
                delay(FADE_MILLIS / FADE_STEPS)
                val progress = (step + 1f) / FADE_STEPS
                val target = mix.musicGain
                next.player.setVolume(target * progress, target * progress)
                from?.player?.setVolume(fromGain * (1 - progress), fromGain * (1 - progress))
            }
            from?.player?.release()
        }.also { job -> job.invokeOnCompletion { if (it != null) from?.player?.release() } }
    }

    private fun createPlayer(track: MusicTrack): Music? = runCatching {
        val player = MediaPlayer()
        context.resources.openRawResourceFd(track.rawRes).use { file ->
            player.setAudioAttributes(musicAttributes)
            player.setDataSource(file.fileDescriptor, file.startOffset, file.length)
            player.isLooping = true
            player.prepare()
        }
        player.setOnErrorListener { failed, _, _ ->
            if (music?.player == failed) music = null
            failed.release()
            true
        }
        Music(track, player)
    }.getOrNull()

    private fun requestFocus(): Boolean {
        if (!hasFocus) {
            hasFocus = audioManager.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
        return hasFocus
    }

    private fun abandonFocus() {
        if (hasFocus) audioManager.abandonAudioFocusRequest(focusRequest)
        hasFocus = false
    }

    private fun onFocusChange(change: Int) {
        when (change) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                hasFocus = true
                update { copy(focus = MusicFocus.GRANTED) }
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> update { copy(focus = MusicFocus.DUCKED) }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> update { copy(focus = MusicFocus.PAUSED) }

            // Другое приложение включило свою музыку: молчим, пока ребёнок не вернётся в игру.
            AudioManager.AUDIOFOCUS_LOSS -> {
                hasFocus = false
                update { copy(focus = MusicFocus.PAUSED) }
            }
        }
    }

    private class Music(val track: MusicTrack, val player: MediaPlayer)

    private companion object {
        const val MAX_EFFECT_STREAMS = 4
        const val FADE_MILLIS = 600L
        const val FADE_STEPS = 20

        fun attributes(contentType: Int): AudioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(contentType)
            .build()
    }
}

@get:RawRes
private val SoundEffect.rawRes: Int
    get() = when (this) {
        SoundEffect.TAP -> R.raw.sfx_tap
        SoundEffect.BUBBLE -> R.raw.sfx_bubble
        SoundEffect.COIN -> R.raw.sfx_coin
        SoundEffect.INCOME -> R.raw.sfx_income
    }

@get:RawRes
private val MusicTrack.rawRes: Int
    get() = when (this) {
        MusicTrack.CALM -> R.raw.music_calm
        MusicTrack.THOUGHTFUL -> R.raw.music_thoughtful
        MusicTrack.DISCO -> R.raw.music_disco
        MusicTrack.BUILDER -> R.raw.music_builder
    }
