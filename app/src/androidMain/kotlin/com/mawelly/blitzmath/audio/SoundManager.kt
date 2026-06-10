package com.mawelly.blitzmath.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import android.media.AudioAttributes
import android.os.Handler
import android.os.Looper
import com.mawelly.blitzmath.R
import com.mawelly.blitzmath.data.GameDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class SoundManager(private val context: Context) {

    private var soundPool: SoundPool
    private var bgmPlayer: MediaPlayer? = null


    private val soundMap = mutableMapOf<Int, Int>()
    private val dataStore = GameDataStore(context)
    private val scope = CoroutineScope(Dispatchers.Main)

    var musicVolume = 0.5f
        private set
    var sfxVolume = 0.8f
        private set

    var isMusicEnabled = true
        private set
    var isSoundEnabled = true
        private set

    companion object {
        const val SOUND_CORRECT = 1
        const val SOUND_WRONG = 2
        const val SOUND_LEVEL_UP = 3
        const val SOUND_GAME_OVER = 4

    }

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds()
        observeSettings()
    }

    private fun loadSounds() {
        soundMap[SOUND_CORRECT] = soundPool.load(context, R.raw.sound_correct, 1)
        soundMap[SOUND_WRONG] = soundPool.load(context, R.raw.sound_wrong, 1)
        soundMap[SOUND_LEVEL_UP] = soundPool.load(context, R.raw.sound_level_up, 1)
        soundMap[SOUND_GAME_OVER] = soundPool.load(context, R.raw.sound_game_over, 1)
    }

    private fun observeSettings() {
        scope.launch {
            dataStore.musicVolume.collect { volume ->
                musicVolume = volume
                bgmPlayer?.setVolume(musicVolume * musicVolume, musicVolume * musicVolume)
            }
        }
        scope.launch {
            dataStore.sfxVolume.collect { volume ->
                sfxVolume = volume
            }
        }
    }

    fun setMusicVolume(volume: Float) {
        musicVolume = volume.coerceIn(0f, 1f)
        bgmPlayer?.setVolume(musicVolume * musicVolume, musicVolume * musicVolume)
        scope.launch { dataStore.saveMusicVolume(musicVolume) }
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume.coerceIn(0f, 1f)
        scope.launch { dataStore.saveSfxVolume(sfxVolume) }
    }

    fun startBGM() {
        // Background music disabled as per user request
    }

    fun stopBGM() {
        bgmPlayer?.stop()
        bgmPlayer?.release()
        bgmPlayer = null
    }

    fun pauseBGM() {
        // Disabled
    }

    fun resumeBGM() {
        // Disabled
    }



    fun playSound(soundId: Int, volume: Float = sfxVolume) {
        if (!isSoundEnabled || volume <= 0) return
        soundMap[soundId]?.let { loadedId ->
            soundPool.play(loadedId, volume, volume, 1, 0, 1.0f)
        }
    }

    fun playCorrect() = playSound(SOUND_CORRECT, sfxVolume)
    fun playWrong() = playSound(SOUND_WRONG, sfxVolume)
    fun playLevelUp() = playSound(SOUND_LEVEL_UP, sfxVolume)
    fun playGameOver() = playSound(SOUND_GAME_OVER, sfxVolume)

    fun release() {
        soundPool.release()
        stopBGM()

        scope.cancel()
    }
}