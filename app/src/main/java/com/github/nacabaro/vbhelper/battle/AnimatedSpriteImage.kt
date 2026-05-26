package com.github.nacabaro.vbhelper.battle

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun AnimatedSpriteImage(
    characterId: String,
    animationType: DigimonAnimationType = DigimonAnimationType.IDLE,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    reloadMappings: Boolean = false,
    animationOffset: Long = 0L // New parameter for offsetting animation timing
) {
    val context = LocalContext.current
    val spriteManager = remember { IndividualSpriteManager(context) }
    
    // Calculate frame offset based on animation offset
    // 750ms is the idle animation duration, so we calculate how many frames to offset
    val frameOffset = if (animationOffset > 0L) {
        // Convert time offset to frame offset (2 frames per cycle, 750ms per frame)
        ((animationOffset / 750L) * 2).toInt()
    } else {
        0
    }
    
    val animationStateMachine = remember { DigimonAnimationStateMachine(characterId, context, frameOffset, animationOffset) }
    val coroutineScope = rememberCoroutineScope()
    
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    // Reload mappings when reloadMappings parameter changes
    LaunchedEffect(reloadMappings) {
        if (reloadMappings) {
            animationStateMachine.reloadMappings()
        }
    }
    
    // Start the animation when the component is first created
    LaunchedEffect(characterId) {
        coroutineScope.launch {
            animationStateMachine.playIdleAnimation()
        }
    }
    
    // Change animation when animationType changes
    LaunchedEffect(animationType) {
        coroutineScope.launch {
            if (animationType == DigimonAnimationType.IDLE) {
                animationStateMachine.playIdleAnimation()
            } else {
                animationStateMachine.playAnimation(animationType)
            }
        }
    }
    
    // Update sprite when animation state changes
    LaunchedEffect(animationStateMachine.currentFrameNumber) {
        val frameNumber = animationStateMachine.getCurrentFrame()

        bitmap = spriteManager.loadSpriteFrame(characterId, frameNumber)
        
        if (bitmap == null) {
            println("Failed to load animated sprite frame: $frameNumber for character: $characterId")
        }
    }
    
    bitmap?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Animated Sprite: $characterId - ${animationStateMachine.currentAnimation}",
            modifier = modifier,
            contentScale = contentScale
        )
    }
} 