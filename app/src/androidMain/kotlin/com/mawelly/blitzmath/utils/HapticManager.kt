package com.mawelly.blitzmath.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticManager(context: Context) {
    private val appContext = context.applicationContext

    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator ?: @Suppress("DEPRECATION") (appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
        } else {
            @Suppress("DEPRECATION")
            appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    private val audioAttributes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
    } else null

    /**
     * Heavy double-pulse for wrong answers or game over.
     */
    fun vibrateWrong(enabled: Boolean, strength: Float) {
        if (!enabled || strength <= 0.05f) return
        
        try {
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 180, 80, 300)
                    val baseAmplitudes = intArrayOf(0, 255, 0, 255)
                    val scaledAmplitudes = baseAmplitudes.map { (it * strength).toInt().coerceIn(0, 255) }.toIntArray()
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audioAttributes != null) {
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, scaledAmplitudes, -1), audioAttributes)
                    } else {
                        vibrator.vibrate(VibrationEffect.createWaveform(timings, scaledAmplitudes, -1))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 180, 80, 300), -1)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Very light tick for UI interactions (optional bonus)
     */
    fun vibrateTick(enabled: Boolean, strength: Float) {
        if (!enabled || strength <= 0.01f) return
        
        try {
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Widen the range (40 to 255) to make the difference obvious
                    val amp = (40 + (215 * strength)).toInt().coerceIn(1, 255)
                    val effect = VibrationEffect.createOneShot(150, amp)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && audioAttributes != null) {
                        vibrator.vibrate(effect, audioAttributes)
                    } else {
                        vibrator.vibrate(effect)
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(150)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * diagnostic test: forces a basic vibration ignoring all settings.
     * Uses USAGE_ALARM to bypass most system-level "touch feedback" blocks.
     */
    fun vibrateDiagnostic() {
        try {
            if (vibrator != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val attrs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                            .build()
                    } else null
                    
                    if (attrs != null) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, 255), attrs)
                    } else {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, 255))
                    }
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
