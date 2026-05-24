package com.github.nacabaro.vbhelper.battle

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HitEffectOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    isPlayerScreen: Boolean = false,
    onAnimationComplete: () -> Unit = {}
) {
    if (!isVisible) return
    
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscapeMode = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val hitEffectManager = remember { HitEffectSpriteManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    var currentFrame by remember { mutableStateOf(0) }
    var currentSprite by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var animationProgress by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(0.5f) }
    var alpha by remember { mutableStateOf(1f) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            
            // Add delay before starting hit effect animation
            delay(400) // Increased from 200ms to 400ms delay before hit effect appears
            
            // Randomly choose between hit_01, hit_02, and hit_02_white
            val hitSpriteName = when (kotlin.random.Random.nextInt(3)) {
                0 -> "hit_01"
                1 -> "hit_02"
                else -> "hit_02_white"
            }
            currentSprite = hitEffectManager.loadHitSprite(hitSpriteName)
            
            if (currentSprite != null) {
                // Animate the hit effect
                animationProgress = 0f
                scale = 0.5f
                alpha = 1f
                
                // Scale up animation - slowed down
                while (scale < 1.2f) {
                    scale += 0.05f  // Reduced from 0.1f to 0.05f
                    delay(32)       // Increased from 16ms to 32ms
                }
                
                // Hold for a moment - increased duration
                delay(300)  // Increased from 100ms to 300ms
                
                // Fade out - slowed down
                while (alpha > 0f) {
                    alpha -= 0.03f  // Reduced from 0.05f to 0.03f
                    delay(32)       // Increased from 16ms to 32ms
                }
                
                println("DEBUG: Hit effect animation completed")
                onAnimationComplete()
            } else {
                println("DEBUG: Failed to load hit sprite")
                onAnimationComplete()
            }
        }
    }
    
    currentSprite?.let { sprite ->
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                bitmap = sprite.asImageBitmap(),
                contentDescription = "Hit Effect",
                modifier = Modifier
                    .size((sprite.width * scale).dp, (sprite.height * scale).dp)
                    .offset(
                        x = if (isPlayerScreen) {
                            // On player screen, position further to the left
                            if (isLandscapeMode) {
                                // In landscape mode, move even further left for player screen
                                (-sprite.width * scale / 2 - 300).dp
                            } else {
                                // In portrait mode, use original positioning
                                (-sprite.width * scale / 2 - 100).dp
                            }
                        } else {
                            // On enemy screen, position further to the right
                            if (isLandscapeMode) {
                                // In landscape mode, move even further right for enemy screen
                                (-sprite.width * scale / 2 + 350).dp
                            } else {
                                // In portrait mode, use original positioning
                                (-sprite.width * scale / 2 + 150).dp
                            }
                        },
                        y = (-sprite.height * scale / 2 + 40).dp  // Position lower on screen (was -60, now +40)
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}
