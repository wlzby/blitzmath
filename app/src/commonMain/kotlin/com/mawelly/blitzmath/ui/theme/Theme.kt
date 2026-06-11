package com.mawelly.blitzmath.ui.theme


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mawelly.blitzmath.data.AppTheme

@Immutable
data class BlitzMathColors(
    val backgroundGradient: List<Color>,
    val cardBackground: Color,
    val glassBorder: Color,
    val primaryVariant: Color,
    val accent: Color
)

val LocalBlitzMathColors = staticCompositionLocalOf {
    BlitzMathColors(
        backgroundGradient = listOf(Color(0xFF000000), Color(0xFF121212), Color(0xFF000000)),
        cardBackground = Color.White.copy(alpha = 0.1f),
        glassBorder = Color.White.copy(alpha = 0.2f),
        primaryVariant = Color(0xFFBB86FC),
        accent = Color(0xFFFFD700)
    )
}

// 1. MIDNIGHT (OLED Black)
private val MidnightColorScheme = darkColorScheme(
    primary = Color(0xFFD1D1D1),
    secondary = Color(0xFFBB86FC),
    tertiary = Color(0xFFFFB300),
    background = Color(0xFF000000)
)
private val MidnightExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF000000), Color(0xFF121212), Color(0xFF000000)),
    cardBackground = Color.White.copy(alpha = 0.05f),
    glassBorder = Color.White.copy(alpha = 0.15f),
    primaryVariant = Color(0xFF757575),
    accent = Color(0xFFFFB300)
)

// 2. DEEP SPACE
private val DeepSpaceColorScheme = darkColorScheme(
    primary = Color(0xFF00d9ff),
    secondary = Color(0xFFe94560),
    tertiary = Color(0xFFf9a825),
    background = Color(0xFF1a1a2e)
)
private val DeepSpaceExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF1a1a2e), Color(0xFF16213e), Color(0xFF0f3460)),
    cardBackground = Color.White.copy(alpha = 0.1f),
    glassBorder = Color.White.copy(alpha = 0.2f),
    primaryVariant = Color(0xFF00bcd4),
    accent = Color(0xFFe94560)
)

// 3. CYBERPUNK
private val CyberpunkColorScheme = darkColorScheme(
    primary = Color(0xFFFF00FF),
    secondary = Color(0xFF00FFFF),
    tertiary = Color(0xFFFFFF00),
    background = Color(0xFF000000)
)
private val CyberpunkExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF000000), Color(0xFF2D0036), Color(0xFF002D36)),
    cardBackground = Color(0xFFFF00FF).copy(alpha = 0.1f),
    glassBorder = Color(0xFF00FFFF).copy(alpha = 0.3f),
    primaryVariant = Color(0xFFBC00BC),
    accent = Color(0xFFFFFF00)
)

// 4. FOREST
private val ForestColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    secondary = Color(0xFF8BC34A),
    tertiary = Color(0xFFFFEB3B),
    background = Color(0xFF0B190E)
)
private val ForestExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF0B190E), Color(0xFF1B3320), Color(0xFF2E4D34)),
    cardBackground = Color.White.copy(alpha = 0.05f),
    glassBorder = Color(0xFF4CAF50).copy(alpha = 0.3f),
    primaryVariant = Color(0xFF2E7D32),
    accent = Color(0xFFFFEB3B)
)

// 5. AQUA
private val AquaColorScheme = darkColorScheme(
    primary = Color(0xFF7FDBFF),
    secondary = Color(0xFF0074D9),
    tertiary = Color(0xFF39CCCC),
    background = Color(0xFF001F3F)
)
private val AquaExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF001F3F), Color(0xFF003366), Color(0xFF0074D9)),
    cardBackground = Color.White.copy(alpha = 0.1f),
    glassBorder = Color(0xFF7FDBFF).copy(alpha = 0.3f),
    primaryVariant = Color(0xFF0056b3),
    accent = Color(0xFF39CCCC)
)

// 6. SUNSET (Warm)
private val SunsetColorScheme = darkColorScheme(
    primary = Color(0xFFFF9E80),
    secondary = Color(0xFFFF5722),
    tertiary = Color(0xFFFFD740),
    background = Color(0xFF2D1B36)
)
private val SunsetExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF2D1B36), Color(0xFF4A148C), Color(0xFF880E4F)),
    cardBackground = Color.White.copy(alpha = 0.1f),
    glassBorder = Color(0xFFFF9E80).copy(alpha = 0.3f),
    primaryVariant = Color(0xFFE64A19),
    accent = Color(0xFFFFD740)
)

// 7. LAVENDER (Soft)
private val LavenderColorScheme = darkColorScheme(
    primary = Color(0xFFE1BEE7),
    secondary = Color(0xFF9575CD),
    tertiary = Color(0xFFB39DDB),
    background = Color(0xFF1A1625)
)
private val LavenderExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF1A1625), Color(0xFF311B92), Color(0xFF4A148C)),
    cardBackground = Color.White.copy(alpha = 0.1f),
    glassBorder = Color(0xFFE1BEE7).copy(alpha = 0.2f),
    primaryVariant = Color(0xFF7B1FA2),
    accent = Color(0xFFB39DDB)
)

// 8. FIRE (Energy)
private val FireColorScheme = darkColorScheme(
    primary = Color(0xFFFF5252),
    secondary = Color(0xFFFFD740),
    tertiary = Color(0xFFFF8F00),
    background = Color(0xFF1A0505)
)
private val FireExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF1A0505), Color(0xFF420D09), Color(0xFF80110A)),
    cardBackground = Color.White.copy(alpha = 0.05f),
    glassBorder = Color(0xFFFF5252).copy(alpha = 0.3f),
    primaryVariant = Color(0xFFD32F2F),
    accent = Color(0xFFFFD740)
)

// 9. GLACIER (Ice)
private val GlacierColorScheme = darkColorScheme(
    primary = Color(0xFFB2EBF2),
    secondary = Color(0xFF00BCD4),
    tertiary = Color(0xFF00ACC1),
    background = Color(0xFF0A1921)
)
private val GlacierExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF0A1921), Color(0xFF004D40), Color(0xFF00838F)),
    cardBackground = Color.White.copy(alpha = 0.1f),
    glassBorder = Color(0xFFB2EBF2).copy(alpha = 0.3f),
    primaryVariant = Color(0xFF0097A7),
    accent = Color(0xFF00ACC1)
)

// 10. GOLDEN (Luxury)
private val GoldenColorScheme = darkColorScheme(
    primary = Color(0xFFFFD700),
    secondary = Color(0xFFFFECB3),
    tertiary = Color(0xFFB8860B),
    background = Color(0xFF121212)
)
private val GoldenExtras = BlitzMathColors(
    backgroundGradient = listOf(Color(0xFF121212), Color(0xFF3E2723), Color(0xFF000000)),
    cardBackground = Color.White.copy(alpha = 0.05f),
    glassBorder = Color(0xFFFFD700).copy(alpha = 0.25f),
    primaryVariant = Color(0xFFFFA000),
    accent = Color(0xFFFFECB3)
)

@Composable
fun BlitzMathTheme(
    themeType: AppTheme = AppTheme.MIDNIGHT,
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeType) {
        AppTheme.MIDNIGHT -> MidnightColorScheme
        AppTheme.DEEP_SPACE -> DeepSpaceColorScheme
        AppTheme.CYBERPUNK -> CyberpunkColorScheme
        AppTheme.FOREST -> ForestColorScheme
        AppTheme.AQUA -> AquaColorScheme
        AppTheme.SUNSET -> SunsetColorScheme
        AppTheme.LAVENDER -> LavenderColorScheme
        AppTheme.FIRE -> FireColorScheme
        AppTheme.GLACIER -> GlacierColorScheme
        AppTheme.GOLDEN -> GoldenColorScheme
    }

    val extras = when (themeType) {
        AppTheme.MIDNIGHT -> MidnightExtras
        AppTheme.DEEP_SPACE -> DeepSpaceExtras
        AppTheme.CYBERPUNK -> CyberpunkExtras
        AppTheme.FOREST -> ForestExtras
        AppTheme.AQUA -> AquaExtras
        AppTheme.SUNSET -> SunsetExtras
        AppTheme.LAVENDER -> LavenderExtras
        AppTheme.FIRE -> FireExtras
        AppTheme.GLACIER -> GlacierExtras
        AppTheme.GOLDEN -> GoldenExtras
    }

    CompositionLocalProvider(LocalBlitzMathColors provides extras) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}