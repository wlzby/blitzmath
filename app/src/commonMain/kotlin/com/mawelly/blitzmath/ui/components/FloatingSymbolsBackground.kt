package com.mawelly.blitzmath.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random
import kotlinx.coroutines.isActive
import kotlin.math.*

private class SymbolState(
    initialX: Float,
    initialY: Float,
    val symbol: String,
    val size: Float,
    val baseAlpha: Float,
    val vx: Float,
    val vy: Float,
    val rotationSpeed: Float
) {
    var x by mutableStateOf(initialX)
    var y by mutableStateOf(initialY)
    var rotation by mutableStateOf(Random.nextFloat() * 360f)
    var touchOffsetX by mutableStateOf(0f)
    var touchOffsetY by mutableStateOf(0f)

    fun update(width: Float, height: Float, touchPos: Offset?, deltaTime: Float) {
        // Basic movement
        x += vx * deltaTime
        y += vy * deltaTime
        rotation += rotationSpeed * deltaTime

        // Screen wrapping
        if (x < -50f) x = width + 50f
        if (x > width + 50f) x = -50f
        if (y < -50f) y = height + 50f
        if (y > height + 50f) y = -50f

        // Touch Interaction (Smooth Repulsion)
        if (touchPos != null) {
            val dx = x - touchPos.x
            val dy = y - touchPos.y
            val distanceSq = dx * dx + dy * dy
            val radius = 300f // Influence area
            
            if (distanceSq < radius * radius) {
                val distance = sqrt(distanceSq).coerceAtLeast(1f)
                val force = (radius - distance) / radius
                val pushX = (dx / distance) * force * 150f
                val pushY = (dy / distance) * force * 150f
                
                // Lerp towards the pushed position for smoothness
                touchOffsetX = touchOffsetX * 0.9f + pushX * 0.1f
                touchOffsetY = touchOffsetY * 0.9f + pushY * 0.1f
            } else {
                touchOffsetX *= 0.95f
                touchOffsetY *= 0.95f
            }
        } else {
            touchOffsetX *= 0.9f
            touchOffsetY *= 0.9f
        }
    }
}

@Composable
fun FloatingSymbolsBackground(
    touchPosition: Offset? = null,
    symbolCount: Int = 30
) {
    val symbols = listOf("+", "-", "×", "÷", "π", "√", "∑", "∞", "∆", "∫", "λ", "Ω")
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }
        
        // Bu önemli: touchPosition değiştiğinde LaunchedEffect'i yeniden başlatmadan 
        // güncel değeri döngü içinde görebilmek için rememberUpdatedState kullanıyoruz.
        val currentTouchPosition by rememberUpdatedState(touchPosition)

        val symbolStates = remember {
            List(symbolCount) {
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                val speed = Random.nextFloat() * 40f + 20f
                SymbolState(
                    initialX = Random.nextFloat() * widthPx,
                    initialY = Random.nextFloat() * heightPx,
                    symbol = symbols.random(),
                    size = Random.nextInt(18, 32).toFloat(),
                    baseAlpha = Random.nextFloat() * 0.08f + 0.04f,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 60f
                )
            }
        }

        // Animation Loop
        LaunchedEffect(widthPx, heightPx) {
            var lastTime = withFrameNanos { it }
            while (isActive) {
                val currentTime = withFrameNanos { it }
                val deltaTime = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime
                
                symbolStates.forEach { state ->
                    state.update(widthPx, heightPx, currentTouchPosition, deltaTime)
                }
            }
        }

        val textMeasurer = androidx.compose.ui.text.rememberTextMeasurer()

        Canvas(modifier = Modifier.fillMaxSize()) {
            symbolStates.forEach { state ->
                val finalX = state.x + state.touchOffsetX
                val finalY = state.y + state.touchOffsetY
                
                withTransform({
                    translate(left = finalX, top = finalY)
                    rotate(degrees = state.rotation)
                }) {
                    drawText(
                        textMeasurer = textMeasurer,
                        text = state.symbol,
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color.White.copy(alpha = state.baseAlpha),
                            fontSize = (state.size).sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
