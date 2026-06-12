package com.mawelly.blitzmath.audio

interface IVoiceManager {
    fun speak(text: String, speedMultiplier: Float = 1.0f, isProfessional: Boolean = false)
    fun speakQuestion(displayText: String)
    fun stop()
    fun release()
}
