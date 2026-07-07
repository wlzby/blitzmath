package com.mawelly.blitzmath.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.draw.scale

@Composable
actual fun AppLifecycleObserver(onPause: () -> Unit, onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> onPause()
                Lifecycle.Event.ON_RESUME -> onResume()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
actual fun PlatformFlag(
    countryCode: String,
    modifier: Modifier,
    fallbackSize: androidx.compose.ui.unit.TextUnit,
    fallbackScale: Float
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val imageLoader = remember {
        coil.ImageLoader.Builder(context)
            .components {
                add(coil.decode.SvgDecoder.Factory())
            }
            .build()
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = getFlagEmoji(countryCode),
            fontSize = fallbackSize,
            modifier = Modifier.scale(fallbackScale)
        )
        
        coil.compose.AsyncImage(
            model = "https://hatscripts.github.io/circle-flags/flags/${countryCode.lowercase(java.util.Locale.ROOT)}.svg",
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}
