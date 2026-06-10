package com.mawelly.blitzmath.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.os.Bundle
import android.speech.tts.Voice
import com.mawelly.blitzmath.localization.AppLanguage
import java.util.*

class VoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var currentLanguage: AppLanguage = AppLanguage.TURKISH

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (t: Throwable) {
            Log.e("VoiceManager", "TTS Constructor failed: ${t.message}")
            isInitialized = false
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setLanguage(currentLanguage)
            // Hızlı ve enerjik ayarlar
            tts?.setPitch(1.1f)
            tts?.setSpeechRate(1.2f)
            
            pendingText?.let {
                speak(it)
                pendingText = null
            }
        } else {
            Log.e("VoiceManager", "Initialization failed")
        }
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        if (!isInitialized) return

        val locale = when (language) {
            AppLanguage.TURKISH -> Locale("tr", "TR")
            AppLanguage.ENGLISH -> Locale.ENGLISH
            AppLanguage.SPANISH -> Locale("es", "ES")
            AppLanguage.GERMAN -> Locale.GERMAN
            AppLanguage.FRENCH -> Locale.FRENCH
            AppLanguage.ITALIAN -> Locale.ITALIAN
            AppLanguage.PORTUGUESE -> Locale("pt", "PT")
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.CHINESE -> Locale.CHINESE
            AppLanguage.RUSSIAN -> Locale("ru", "RU")
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e("VoiceManager", "Language not supported: $language")
        } else {
            selectBestVoice(locale)
        }
    }

    private fun selectBestVoice(locale: Locale) {
        try {
            val allVoices = tts?.voices ?: return
            val targetVoices = allVoices.filter { it.locale.language == locale.language }
            
            // Müsait ses adaylarını logla
            Log.d("VoiceManager", "--- Uygun Ses Adayları ---")
            val bestVoices = targetVoices.filter { 
                val n = it.name.lowercase()
                n.contains("network") || n.contains("neural") || n.contains("female") || n.contains("male")
            }.sortedByDescending { it.isNetworkConnectionRequired }
            
            bestVoices.forEach { 
                Log.d("VoiceManager", "Ses: ${it.name}, Network: ${it.isNetworkConnectionRequired}, Kalite: ${it.quality}")
            }
            
            // Seçim Stratejisi:
            // Özellikle beğenilen o yüksek kaliteli "koç" sesini (Network/Neural) seç
            val bestVoice = bestVoices.find { it.isNetworkConnectionRequired && it.name.lowercase().contains("male") }
                ?: bestVoices.find { it.isNetworkConnectionRequired }
                ?: bestVoices.find { it.quality >= Voice.QUALITY_HIGH }
                ?: bestVoices.firstOrNull()
            
            bestVoice?.let {
                tts?.voice = it
                Log.d("VoiceManager", "Dinamik Seçilen Yeni Ses: ${it.name}")
            }
        } catch (e: Exception) {
            Log.e("VoiceManager", "Ses seçimi hatası", e)
        }
    }

    fun speak(text: String, speedMultiplier: Float = 1.0f, isProfessional: Boolean = false) {
        if (!isInitialized) {
            pendingText = text
            return
        }
        
        val randomPitch: Float
        val randomRate: Float
        
        if (isProfessional) {
            // Profesyonel mod: Daha ağırbaşlı, sakin ve yumuşak tını
            randomPitch = 1.08f + (Random().nextFloat() * 0.05f) // Daha düşük ve stabil perde
            val baseRate = 0.95f * speedMultiplier
            randomRate = baseRate + (Random().nextFloat() * 0.10f) // Daha yavaş ve kontrollü hız
        } else {
            // "Gençleştirme" için pitch (perde) değerini artırıyoruz
            // 1.30f - 1.45f aralığı sesi daha ince, genç ve dinamik yapar
            randomPitch = 1.30f + (Random().nextFloat() * 0.15f) 
            val baseRate = 1.10f * speedMultiplier
            randomRate = baseRate + (Random().nextFloat() * 0.20f)
        }
        
        tts?.setPitch(randomPitch)
        tts?.setSpeechRate(randomRate)
        
        // Utterance ID eklemek bazen bazı cihazlarda daha kararlı çalışmasını sağlar
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "msgID")
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "msgID")
    }

    fun speakQuestion(displayText: String) {
        val spokenText = translateMathSymbols(displayText)
        speak(spokenText)
    }

    private fun translateMathSymbols(text: String): String {
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

    fun stop() {
        tts?.stop()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
