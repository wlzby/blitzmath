package com.mawelly.blitzmath.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.mawelly.blitzmath.game.ScientistCard
import com.mawelly.blitzmath.game.ScientistCards
import com.mawelly.blitzmath.localization.Strings
import com.mawelly.blitzmath.ui.theme.LocalBlitzMathColors

@Composable
fun CollectionScreen(
    unlockedCardIds: Set<String>,
    equippedCardIds: Set<String>,
    starCount: Int,
    cardCharges: Map<String, Int>,
    cardLastUseTimes: Map<String, Long>,
    onBuyCard: (String, Int) -> Unit,
    onToggleEquip: (String) -> Unit,
    onBack: () -> Unit
) {
    val colors = LocalBlitzMathColors.current
    val allCards = ScientistCards.cards

    // Local state for charges and timers to allow UI updates without GameState
    val currentCharges = remember { mutableStateMapOf<String, Int>() }
    val currentLastUseTimes = remember { mutableStateMapOf<String, Long>() }

    // Initial sync
    LaunchedEffect(cardCharges, cardLastUseTimes) {
        currentCharges.clear()
        currentCharges.putAll(cardCharges)
        currentLastUseTimes.clear()
        currentLastUseTimes.putAll(cardLastUseTimes)
    }

    // Periodic sync to refresh timers - Optimized to run only when needed
    LaunchedEffect(currentCharges.toMap(), currentLastUseTimes.toMap()) {
        val needsRecharge = allCards.any { card ->
            val charge = currentCharges[card.id] ?: card.maxCharges
            charge < card.maxCharges
        }
        
        if (needsRecharge) {
            while (true) {
                kotlinx.coroutines.delay(1000)
                val currentTime = System.currentTimeMillis()
                var anyUpdate = false
                
                allCards.forEach { card ->
                    val id = card.id
                    val charge = currentCharges[id] ?: card.maxCharges
                    val lastTime = currentLastUseTimes[id] ?: 0L
                    
                    if (charge < card.maxCharges && lastTime > 0L) {
                        val durationMs = card.rechargeDurationMinutes * 60 * 1000L
                        val elapsedMs = currentTime - lastTime
                        val refilledCount = (elapsedMs / durationMs).toInt()
                        
                        if (refilledCount > 0) {
                            val newCount = (charge + refilledCount).coerceAtMost(card.maxCharges)
                            currentCharges[id] = newCount
                            currentLastUseTimes[id] = if (newCount < card.maxCharges) lastTime + (refilledCount * durationMs) else 0L
                            anyUpdate = true
                        }
                    }
                }
                
                // If everything is full, we can stop this loop (the outer LaunchedEffect will restart it if needed)
                if (!allCards.any { (currentCharges[it.id] ?: it.maxCharges) < it.maxCharges }) {
                    break
                }
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val columnsCount = if (screenWidth > 600.dp) 2 else 1
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = Strings.storeTitle,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Star Balance
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = starCount.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            // Equipped Abilities Summary
            EquippedAbilitiesCard(equippedCardIds)

            Spacer(modifier = Modifier.height(16.dp))

            // Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnsCount),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(allCards) { card ->
                    val isUnlocked = card.id in unlockedCardIds
                    val isEquipped = card.id in equippedCardIds

                    ScientistStoreItem(
                        card = card,
                        isUnlocked = isUnlocked,
                        isEquipped = isEquipped,
                        currentCharges = currentCharges[card.id] ?: card.maxCharges,
                        lastUseTime = currentLastUseTimes[card.id] ?: 0L,
                        canAfford = starCount >= card.price,
                        canEquipMore = equippedCardIds.size < 2 || isEquipped,
                        onBuy = { onBuyCard(card.id, card.price) },
                        onToggleEquip = { onToggleEquip(card.id) },
                        accentColor = colors.accent
                    )
                }
            }
        }
    }
}

@Composable
fun EquippedAbilitiesCard(equippedCardIds: Set<String>) {
    val equippedCards = ScientistCards.cards.filter { it.id in equippedCardIds }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Strings.equippedAbilities,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = "${equippedCards.size}/2",
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (equippedCards.isEmpty()) {
                Text(
                    text = Strings.noAbilitiesEquipped,
                    color = Color.White.copy(alpha = 0.4f),
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            } else {
                equippedCards.forEach { card ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${card.name}: ${card.description}",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScientistStoreItem(
    card: ScientistCard,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    currentCharges: Int,
    lastUseTime: Long,
    canAfford: Boolean,
    canEquipMore: Boolean,
    onBuy: () -> Unit,
    onToggleEquip: () -> Unit,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp))
            .border(
                width = if (isEquipped) 2.dp else 1.dp,
                color = if (isEquipped) accentColor else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEquipped) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Scientist Avatar
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.linearGradient(
                                if (isUnlocked) listOf(accentColor, Color(0xFF000000))
                                else listOf(Color.Gray, Color.DarkGray)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (card.id.isNotEmpty()) {
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val resId = remember(card.id) { context.resources.getIdentifier(card.id, "drawable", context.packageName) }
                        if (resId != 0) {
                            androidx.compose.foundation.Image(
                                painter = androidx.compose.ui.res.painterResource(id = resId),
                                contentDescription = card.name,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(4.dp)
                                    .clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    } else {
                        Text(
                            text = card.name.take(1),
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = card.description,
                        color = if (isUnlocked) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Charge info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(card.maxCharges) { i ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (!isUnlocked) Color.Gray.copy(alpha = 0.4f)
                                        else if (i < currentCharges) accentColor
                                        else Color.White.copy(alpha = 0.2f),
                                        shape = androidx.compose.foundation.shape.CircleShape
                                    )
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        val isRecharging = isUnlocked && currentCharges < card.maxCharges && lastUseTime > 0L
                        
                        if (isRecharging) {
                            val durationMs = card.rechargeDurationMinutes * 60 * 1000L
                            val remainingMs = (lastTime@ (lastUseTime + durationMs) - System.currentTimeMillis()).coerceAtLeast(0)
                            val seconds = (remainingMs / 1000) % 60
                            val minutes = (remainingMs / (1000 * 60)) % 60
                            val hours = (remainingMs / (1000 * 60 * 60))
                            
                            val timerText = if (hours > 0) String.format("%02d:%02d:%02d", hours, minutes, seconds)
                                            else String.format("%02d:%02d", minutes, seconds)
                            
                            Text(
                                text = "⏳ $timerText",
                                fontSize = 11.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Text(
                                text = "${if (isUnlocked) currentCharges else card.maxCharges} ${Strings.usageRights}",
                                fontSize = 11.sp,
                                color = if (isUnlocked) accentColor.copy(alpha = 0.9f) else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "(${Strings.getRechargeAdsInfo(card.rechargeAdsRequired)})",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.4f),
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Button
            if (isUnlocked) {
                Button(
                    onClick = onToggleEquip,
                    enabled = isEquipped || canEquipMore,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEquipped) Color(0xFFE94560) else accentColor,
                        disabledContainerColor = Color.Gray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isEquipped) Strings.remove else Strings.equip,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = canAfford,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${card.price} ${Strings.points}",
                            color = if (canAfford) Color.White else Color.White.copy(alpha = 0.3f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
