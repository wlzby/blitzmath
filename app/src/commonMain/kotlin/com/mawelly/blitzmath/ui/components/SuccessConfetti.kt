package com.mawelly.blitzmath.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

data class ConfettiParticle(
    var xPercent: Float,
    var yPercent: Float,
    val size: Float,
    val color: Color,
    val speed: Float,
    val rotationSpeed: Float,
    val swayAmplitude: Float, // Yanlara sallanma genişliği
    val swayFrequency: Float, // Sallanma hızı
    val linearDrift: Float,   // Genel yön (sol/sağ)
    var rotation: Float = 0f,
    val phase: Float = Random.nextFloat() * 2f * PI.toFloat()
)

@Composable
fun SuccessConfetti() {
    val particles = remember {
        List(50) {
            ConfettiParticle(
                xPercent = Random.nextFloat(),
                yPercent = Random.nextFloat() * -1f, // Start above screen
                size = Random.nextInt(10, 25).toFloat(),
                color = listOf(
                    Color(0xFF00d9ff), // Cyan
                    Color(0xFFe94560), // Red
                    Color(0xFFFFD700), // Gold
                    Color(0xFF4CAF50), // Green
                    Color(0xFF9C27B0), // Purple
                    Color(0xFFFFFFFF)  // White
                ).random(),
                speed = Random.nextFloat() * 0.0025f + 0.0008f, // Hız çeşitliliği arttı
                rotationSpeed = Random.nextFloat() * 4f + 1f,
                swayAmplitude = Random.nextFloat() * 0.003f + 0.001f,
                swayFrequency = Random.nextFloat() * 5f + 3f, // Her parça farklı hızda sallanır
                linearDrift = (Random.nextFloat() - 0.5f) * 0.002f // Her parçanın kendi ana yönü var
            )
        }
    }

    // Animasyonun ne kadar süre yeni konfeti üreteceğini kontrol eder
    var isEmitting by remember { mutableStateOf(true) }
    var frame by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        // 5 saniye sonra yeni konfeti üretimini durdur
        delay(5000)
        isEmitting = false
    }

    LaunchedEffect(Unit) {
        while (true) {
            withFrameMillis { frame = it }
            particles.forEach { p ->
                p.yPercent += p.speed
                p.rotation += p.rotationSpeed
                
                // Organik Hareket: 
                // 1. Ana Yön (Linear Drift)
                p.xPercent += p.linearDrift
                // 2. Kişisel Sallanma (Sway)
                p.xPercent += sin(p.yPercent * p.swayFrequency + p.phase) * p.swayAmplitude
                
                // Reset if falls below screen AND we're still emitting
                if (p.yPercent > 1.1f) {
                    if (isEmitting) {
                        p.yPercent = -0.1f
                    }
                }
            }
            // Eğer artık üretim durduysa ve tüm konfetiler ekranın altına indiyse döngüyü kırabiliriz
            if (!isEmitting && particles.all { it.yPercent > 1.1f }) {
                break
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Bu satır kritik: frame değişkenini burada okuyarak 
        // Canvas'ın her karede (frame) yeniden çizilmesini sağlıyoruz.
        val _trigger = frame 
        
        val width = size.width
        val height = size.height
        
        particles.forEach { p ->
            withTransform({
                rotate(p.rotation, Offset(p.xPercent * width, p.yPercent * height))
            }) {
                drawRect(
                    color = p.color,
                    topLeft = Offset(p.xPercent * width, p.yPercent * height),
                    size = Size(p.size, p.size / 1.5f)
                )
            }
        }
    }
}
