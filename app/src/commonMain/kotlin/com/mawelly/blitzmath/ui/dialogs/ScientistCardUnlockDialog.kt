package com.mawelly.blitzmath.ui.dialogs

import com.mawelly.blitzmath.core.LocalPlatformServices

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mawelly.blitzmath.game.ScientistCard
import com.mawelly.blitzmath.localization.Strings

@Composable
fun ScientistCardUnlockDialog(
    card: ScientistCard,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .shadow(24.dp, RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1A2E), Color(0xFF16213E))
                    )
                )
                .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = Strings.cardUnlocked,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card Visual
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .shadow(12.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (card.id.isNotEmpty()) {
                        val platformServices = LocalPlatformServices.current
                        val res = com.mawelly.blitzmath.ui.components.ScientistResources.getPortrait(card.id)
                        if (res != null) {
                            androidx.compose.foundation.Image(
                                painter = org.jetbrains.compose.resources.painterResource(res),
                                contentDescription = card.name,
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    } else {
                        Text(
                            text = card.name.take(1),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = card.description,
                    color = Color(0xFFFFD700).copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "⚡ ${card.maxCharges} ${Strings.statCharges}",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = Strings.ok,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
