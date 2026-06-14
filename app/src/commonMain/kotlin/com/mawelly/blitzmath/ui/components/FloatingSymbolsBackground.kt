package com.mawelly.blitzmath.ui.components

import com.mawelly.blitzmath.core.LocalPlatformServices

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
import androidx.compose.ui.text.TextLayoutResult
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
    var textLayoutResult: TextLayoutResult? = null

    fun update(width: Float, height: Float, touchPos: Offset?, deltaTime: Float) {
        x += vx * deltaTime
        y += vy * deltaTime
        rotation += rotationSpeed * deltaTime

        if (x < -50f) x = width + 50f
        if (x > width + 50f) x = -50f
        if (y < -50f) y = height + 50f
        if (y > height + 50f) y = -50f

        if (touchPos != null) {
            val dx = x - touchPos.x
            val dy = y - touchPos.y
            val distanceSq = dx * dx + dy * dy
            val radius = 300f
            
            if (distanceSq < radius * radius) {
                val distance = sqrt(distanceSq).coerceAtLeast(1f)
                val force = (radius - distance) / radius
                val pushX = (dx / distance) * force * 150f
                val pushY = (dy / distance) * force * 150f
                
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
        
        val currentTouchPosition by rememberUpdatedState(touchPosition)
        val textMeasurer = rememberTextMeasurer()

        val symbolStates = remember(widthPx, heightPx) {
            List(symbolCount) {
                val angle = Random.nextFloat() * 2 * PI.toFloat()
                val speed = Random.nextFloat() * 40f + 20f
                val symbol = symbols.random()
                val size = Random.nextInt(18, 32).toFloat()
                val baseAlpha = Random.nextFloat() * 0.08f + 0.04f
                val style = TextStyle(
                    color = Color.White.copy(alpha = baseAlpha),
                    fontSize = size.sp,
                    fontWeight = FontWeight.Bold
                )
                SymbolState(
                    initialX = Random.nextFloat() * widthPx,
                    initialY = Random.nextFloat() * heightPx,
                    symbol = symbol,
                    size = size,
                    baseAlpha = baseAlpha,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 60f
                ).apply {
                    textLayoutResult = textMeasurer.measure(
                        text = symbol,
                        style = style
                    )
                }
            }
        }

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

        Canvas(modifier = Modifier.fillMaxSize()) {
            symbolStates.forEach { state ->
                val layoutResult = state.textLayoutResult ?: return@forEach
                val finalX = state.x + state.touchOffsetX
                val finalY = state.y + state.touchOffsetY
                
                withTransform({
                    translate(left = finalX, top = finalY)
                    rotate(degrees = state.rotation)
                }) {
                    drawText(layoutResult)
                }
            }
        }
    }
}
