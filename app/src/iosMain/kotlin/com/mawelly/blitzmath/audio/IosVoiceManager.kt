package com.mawelly.blitzmath.audio

import platform.AVFoundation.AVSpeechSynthesizer
import platform.AVFoundation.AVSpeechUtterance
import platform.AVFoundation.AVSpeechSynthesisVoice
import platform.AVFoundation.AVSpeechBoundaryImmediate
import com.mawelly.blitzmath.localization.AppLanguage
import com.mawelly.blitzmath.localization.Strings

class IosVoiceManager : IVoiceManager {
    private val synthesizer = AVSpeechSynthesizer()

    override fun speak(text: String, speedMultiplier: Float, isProfessional: Boolean) {
        val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
        
        // Map language
        val langCode = when (Strings.currentLanguage) {
            AppLanguage.TURKISH -> "tr-TR"
            AppLanguage.ENGLISH -> "en-US"
            AppLanguage.SPANISH -> "es-ES"
            AppLanguage.GERMAN -> "de-DE"
            AppLanguage.FRENCH -> "fr-FR"
            AppLanguage.ITALIAN -> "it-IT"
            AppLanguage.PORTUGUESE -> "pt-PT"
            AppLanguage.HINDI -> "hi-IN"
            AppLanguage.CHINESE -> "zh-CN"
            AppLanguage.RUSSIAN -> "ru-RU"
        }
        
        val voice = AVSpeechSynthesisVoice.voiceWithLanguage(langCode)
        if (voice != null) {
            utterance.voice = voice
        }
        
        // Rate is between 0.0 and 1.0. AVSpeechUtteranceDefaultSpeechRate is 0.5.
        val baseRate = if (isProfessional) 0.48f else 0.52f
        utterance.rate = baseRate * speedMultiplier
        
        // Pitch multiplier is between 0.5 and 2.0. Default is 1.0.
        val basePitch = if (isProfessional) 1.0f else 1.15f
        utterance.pitchMultiplier = basePitch

        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
        synthesizer.speakUtterance(utterance)
    }

    override fun speakQuestion(displayText: String) {
        val spokenText = translateMathSymbols(displayText)
        speak(spokenText)
    }

    private fun translateMathSymbols(text: String): String {
        val currentLanguage = Strings.currentLanguage
        val plus = when (currentLanguage) {
            AppLanguage.TURKISH -> "artı"
            AppLanguage.ENGLISH -> "plus"
            AppLanguage.SPANISH -> "más"
            AppLanguage.GERMAN -> "plus"
            AppLanguage.FRENCH -> "plus"
            AppLanguage.ITALIAN -> "più"
            AppLanguage.PORTUGUESE -> "mais"
            AppLanguage.HINDI -> "प्लस"
            AppLanguage.CHINESE -> "加"
            AppLanguage.RUSSIAN -> "плюс"
        }
        val minus = when (currentLanguage) {
            AppLanguage.TURKISH -> "eksi"
            AppLanguage.ENGLISH -> "minus"
            AppLanguage.SPANISH -> "menos"
            AppLanguage.GERMAN -> "minus"
            AppLanguage.FRENCH -> "moins"
            AppLanguage.ITALIAN -> "meno"
            AppLanguage.PORTUGUESE -> "menos"
            AppLanguage.HINDI -> "माइनस"
            AppLanguage.CHINESE -> "减"
            AppLanguage.RUSSIAN -> "минус"
        }
        val times = when (currentLanguage) {
            AppLanguage.TURKISH -> "çarpı"
            AppLanguage.ENGLISH -> "times"
            AppLanguage.SPANISH -> "por"
            AppLanguage.GERMAN -> "mal"
            AppLanguage.FRENCH -> "fois"
            AppLanguage.ITALIAN -> "per"
            AppLanguage.PORTUGUESE -> "vezes"
            AppLanguage.HINDI -> "गुणा"
            AppLanguage.CHINESE -> "乘以"
            AppLanguage.RUSSIAN -> "умножить на"
        }
        val dividedBy = when (currentLanguage) {
            AppLanguage.TURKISH -> "bölü"
            AppLanguage.ENGLISH -> "divided by"
            AppLanguage.SPANISH -> "dividido por"
            AppLanguage.GERMAN -> "geteilt durch"
            AppLanguage.FRENCH -> "divisé par"
            AppLanguage.ITALIAN -> "diviso"
            AppLanguage.PORTUGUESE -> "dividido por"
            AppLanguage.HINDI -> "भाग"
            AppLanguage.CHINESE -> "除以"
            AppLanguage.RUSSIAN -> "разделить на"
        }

        return text.replace("+", plus)
            .replace("-", minus)
            .replace("×", times)
            .replace("÷", dividedBy)
    }

    override fun stop() {
        synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate)
    }

    override fun release() {
        stop()
    }
}
