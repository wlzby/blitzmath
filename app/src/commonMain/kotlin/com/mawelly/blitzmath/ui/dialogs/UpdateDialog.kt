package com.mawelly.blitzmath.ui.dialogs

import com.mawelly.blitzmath.core.LocalPlatformServices

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mawelly.blitzmath.localization.Strings
import org.jetbrains.compose.resources.painterResource
import blitzmath.app.generated.resources.Res
import blitzmath.app.generated.resources.einstein_portrait
import androidx.compose.ui.layout.ContentScale

@Composable
fun UpdateDialog() {
    val platformServices = LocalPlatformServices.current
    
    // Shimmer effect for the button
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Dialog(
        onDismissRequest = { /* FORCED */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(40.dp, RoundedCornerShape(32.dp), spotColor = Color(0xFFFFD700))
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1E1E2E), // Deep Space
                            Color(0xFF16213E)  // Navy
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(Color(0xFFFFD700).copy(alpha = 0.5f), Color.Transparent, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Friendly Mascot (Einstein)
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(15.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(Res.drawable.einstein_portrait),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().padding(4.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = Strings.updateTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFFFFD700), // Gold
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = Strings.updateMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Action Button (Gold Premium Look)
                Button(
                    onClick = {
                        val packageName = "com.mawelly.blitzmath"
                        try {
                            platformServices.openUrl("market://details?id=$packageName")
                        } catch (e: Exception) {
                            platformServices.openUrl("https://play.google.com/store/apps/details?id=$packageName")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .alpha(shimmerAlpha),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700)
                    ),
                    shape = RoundedCornerShape(18.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = Strings.updateButton,
                        color = Color.Black,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "BlitzMath Laboratory",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }
    }
}
