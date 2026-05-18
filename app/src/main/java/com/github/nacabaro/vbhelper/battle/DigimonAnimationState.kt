package com.github.nacabaro.vbhelper.battle

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

enum class DigimonAnimationType {
    IDLE,
    IDLE2,
    WALK,
    SLEEP,
    ATTACK
}

data class AnimationState(
    val type: DigimonAnimationType,
    val frameNumber: Int, // 1-12 for individual PNG files
    val duration: Long = 100L, // Duration in milliseconds
    val loop: Boolean = true
)

class DigimonAnimationStateMachine(
    private val characterId: String,
    private val initialFrameOffset: Int = 0, // New parameter for offsetting the starting frame
    private val timingOffset: Long = 0L // New parameter for offsetting the timing
) {
    var currentAnimation by mutableStateOf<DigimonAnimationType>(DigimonAnimationType.IDLE)
        private set
    
    var currentFrameNumber by mutableStateOf(1)
        private set
    
    var isPlaying by mutableStateOf(false)
        private set
    
    // Direct mapping of frame numbers used by current battle animations.
    private val frameToAnimationType = mapOf(
        1 to DigimonAnimationType.IDLE,
        2 to DigimonAnimationType.IDLE2,
        3 to DigimonAnimationType.WALK,
        10 to DigimonAnimationType.SLEEP,
        11 to DigimonAnimationType.ATTACK
    )
    
    // Reverse mapping for getting frame numbers for each animation type
    private val animationTypeToFrames = frameToAnimationType.entries.groupBy({ it.value }, { it.key })
    
    // Animation durations for each type
    private val animationDurations = mapOf(
        DigimonAnimationType.IDLE to 750L,
        DigimonAnimationType.IDLE2 to 750L,
        DigimonAnimationType.WALK to 200L,
        DigimonAnimationType.SLEEP to 1500L,
        DigimonAnimationType.ATTACK to 650L
    )

    /*
    init {
        println("Initialized DigimonAnimationStateMachine for character: $characterId with frame offset: $initialFrameOffset, timing offset: $timingOffset")
        println("Available animation types: ${animationTypeToFrames.keys}")
    }
    */
    
    suspend fun playAnimation(animationType: DigimonAnimationType) {
        if (currentAnimation == animationType && isPlaying) {
            return // Already playing this animation
        }
        
        currentAnimation = animationType
        isPlaying = true
        
        val frameNumbers = animationTypeToFrames[animationType] ?: listOf(1)
        val duration = animationDurations[animationType] ?: 100L
        
        // For non-looping animations like ATTACK, play once and return to IDLE
        if (animationType == DigimonAnimationType.ATTACK) {
            currentFrameNumber = frameNumbers.firstOrNull() ?: 1
            delay(duration)
            playAnimation(DigimonAnimationType.IDLE)
        } else {
            // For looping animations, cycle through frames
            var frameIndex = 0
            while (isPlaying && currentAnimation == animationType) {
                val frameNumber = frameNumbers[frameIndex % frameNumbers.size]
                currentFrameNumber = frameNumber
                delay(duration)
                frameIndex++
            }
        }
    }
    
    // Special method for idle animation that cycles between IDLE and IDLE2
    suspend fun playIdleAnimation() {
        if (currentAnimation == DigimonAnimationType.IDLE && isPlaying) {
            return // Already playing idle animation
        }
        
        currentAnimation = DigimonAnimationType.IDLE
        isPlaying = true

        val idleFrames = animationTypeToFrames[DigimonAnimationType.IDLE] ?: listOf(1)
        val idle2Frames = animationTypeToFrames[DigimonAnimationType.IDLE2] ?: listOf(2)
        
        // Combine frames for cycling idle animation
        val combinedFrames = (idleFrames + idle2Frames).distinct()
        
        val duration = animationDurations[DigimonAnimationType.IDLE] ?: 500L
        
        // Apply initial timing offset
        if (timingOffset > 0L) {
            delay(timingOffset)
        }
        
        // Cycle through idle frames, starting from the offset
        var frameIndex = initialFrameOffset
        while (isPlaying && currentAnimation == DigimonAnimationType.IDLE) {
            val frameNumber = combinedFrames[frameIndex % combinedFrames.size]
            currentFrameNumber = frameNumber
            delay(duration)
            frameIndex++
        }
    }
    
    fun stopAnimation() {
        isPlaying = false
    }
    
    fun getCurrentFrame(): Int {
        return currentFrameNumber
    }
    
}