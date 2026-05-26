package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.LinearProgressIndicator
//import androidx.compose.material3.ExposedDropdownMenuBox
//import androidx.compose.material3.ExposedDropdownMenuDefaults
//import androidx.compose.material3.OutlinedTextField
//import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.ComponentActivity
import android.os.Build
import android.provider.Settings
import android.content.Intent
import android.net.Uri
import android.media.MediaPlayer
import android.os.Environment
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import com.github.nacabaro.vbhelper.battle.APIBattleCharacter
import android.util.Log
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.battle.RetrofitHelper
import com.github.nacabaro.vbhelper.battle.AttackSpriteImage
import com.github.nacabaro.vbhelper.battle.SpriteFileManager
import com.github.nacabaro.vbhelper.battle.ArenaBattleSystem
import com.github.nacabaro.vbhelper.battle.DigimonAnimationType
import com.github.nacabaro.vbhelper.battle.AnimatedSpriteImage
import com.github.nacabaro.vbhelper.battle.HitEffectOverlay
import com.github.nacabaro.vbhelper.battle.BattleAuthContainer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//import kotlin.math.sin
//import kotlin.math.PI
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.animateIntAsState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
//import android.os.Environment
import java.io.File
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.width
import com.github.nacabaro.vbhelper.di.VBHelper
import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.AlertDialog

@Composable
fun isLandscapeMode(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.screenWidthDp > configuration.screenHeightDp
}

@Composable
fun getLandscapeModifier(): Modifier {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    return if (isLandscape) {
        Modifier.width(200.dp).height(8.dp)
    } else {
        Modifier.fillMaxWidth().height(10.dp)
    }
}

@Composable
fun getLandscapeAlignment(): Alignment {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    return if (isLandscape) Alignment.Center else Alignment.TopStart
}

@Composable
fun getLandscapeHorizontalAlignment(): Alignment.Horizontal {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    return if (isLandscape) Alignment.CenterHorizontally else Alignment.Start
}

@Composable
fun getLandscapeFontSize(): androidx.compose.ui.unit.TextUnit {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    return if (isLandscape) 14.sp else 16.sp
}

@Composable
fun getLandscapeBoxModifier(): Modifier {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    return if (isLandscape) {
        Modifier
            .width(220.dp) // Slightly wider than the progress bar to accommodate padding
            .background(
                color = Color.Gray.copy(alpha = 0.6f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .background(
                color = Color.Gray.copy(alpha = 0.6f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    }
}

@Composable
fun AnimatedDamageNumber(
    damage: Int,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    var animationProgress by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    var alpha by remember { mutableStateOf(1f) }
    var yOffset by remember { mutableStateOf(0.dp) }
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            // Start animation
            animationProgress = 0f
            scale = 0.5f
            alpha = 1f
            yOffset = 0.dp
            
            // Animate scale up
            while (scale < 1.5f) {
                scale += 0.1f
                delay(16)
            }
            
            // Hold at max scale briefly
            delay(200)
            
            // Animate fade out and move up
            while (alpha > 0f) {
                alpha -= 0.05f
                yOffset -= 1.dp
                delay(16)
            }
        }
    }
    
    Text(
        text = "-$damage",
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Red,
        textAlign = TextAlign.Center,
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black,
                offset = androidx.compose.ui.geometry.Offset(2f, 2f),
                blurRadius = 4f
            )
        ),
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
            .offset(y = yOffset)
    )
}

@Composable
fun BattleScreen(
    userId: Long? = null,
    stage: String,
    playerName: String,
    opponentName: String,
    activeCharacter: APIBattleCharacter?,
    opponentCharacter: APIBattleCharacter?,
    onAttackClick: () -> Unit,
    context: android.content.Context? = null,
    selectedBackgroundSet: Int = 0
) {
    // Capture userId parameter for use in lambdas - use remember to ensure it's accessible in all scopes
    val currentUserId = remember { userId }
    
    val battleSystem = remember { ArenaBattleSystem() }
    val coroutineScope = rememberCoroutineScope()
    
    // Background music MediaPlayer
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    
    // Initialize HP when battle starts
    // Use currentHp if available (for resumed matches), otherwise use baseHp (for new matches)
    LaunchedEffect(activeCharacter, opponentCharacter) {
        val playerHP = activeCharacter?.currentHp?.toFloat() ?: activeCharacter?.baseHp?.toFloat() ?: 100f
        val opponentHP = opponentCharacter?.currentHp?.toFloat() ?: opponentCharacter?.baseHp?.toFloat() ?: 100f
        battleSystem.initializeHP(playerHP, opponentHP)
    }
    
    // Start background music when battle starts
    LaunchedEffect(Unit) {
        context?.let { ctx ->
            try {
                // Get external storage directory
                val externalDir = Environment.getExternalStorageDirectory()
                val musicDir = File(externalDir, "VBHelper/extracted_audio/background_music")
                
                // Pick a random BGM file (bgm_001.wav to bgm_004.wav)
                val bgmNumber = kotlin.random.Random.nextInt(1, 5) // 1 to 4
                val bgmFileName = String.format("bgm_%03d.wav", bgmNumber)
                val bgmFile = File(musicDir, bgmFileName)
                
                if (bgmFile.exists()) {
                    println("BATTLESCREEN: Starting background music: $bgmFileName")
                    val player = MediaPlayer().apply {
                        setDataSource(bgmFile.absolutePath)
                        prepare()
                        setOnCompletionListener {
                            // Stop after one playthrough
                            println("BATTLESCREEN: Background music completed, stopping")
                            it.release()
                            mediaPlayer = null
                        }
                        start()
                    }
                    mediaPlayer = player
                } else {
                    println("BATTLESCREEN: Background music file not found: ${bgmFile.absolutePath}")
                }
            } catch (e: Exception) {
                println("BATTLESCREEN: Error starting background music: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    // Clean up MediaPlayer when battle ends or composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                    println("BATTLESCREEN: Background music stopped and released")
                } catch (e: Exception) {
                    println("BATTLESCREEN: Error stopping background music: ${e.message}")
                }
            }
            mediaPlayer = null
        }
    }
    
    // Pending damage state for API integration
    var pendingPlayerDamage by remember { mutableStateOf(0f) }
    var pendingOpponentDamage by remember { mutableStateOf(0f) }
    
    // Damage number animation state
    var showPlayerDamageNumber by remember { mutableStateOf(false) }
    var showOpponentDamageNumber by remember { mutableStateOf(false) }
    var playerDamageValue by remember { mutableStateOf(0) }
    var opponentDamageValue by remember { mutableStateOf(0) }
    
    // Hit effect animation state
    var showPlayerHitEffect by remember { mutableStateOf(false) }
    var showOpponentHitEffect by remember { mutableStateOf(false) }
    
    // Attack sprite visibility state
    var hidePlayerAttackSprite by remember { mutableStateOf(false) }
    var hideEnemyAttackSprite by remember { mutableStateOf(false) }
    
    // Reset hit effect states when attack phase returns to idle
    LaunchedEffect(battleSystem.attackPhase) {
        if (battleSystem.attackPhase == 0) {
            // Reset hit effect states when returning to idle
            showPlayerHitEffect = false
            showOpponentHitEffect = false
            hidePlayerAttackSprite = false
            hideEnemyAttackSprite = false
            battleSystem.endPlayerHitDelayed()
            battleSystem.endOpponentHitDelayed()
            battleSystem.endPlayerShakeDelayed()
            battleSystem.endOpponentShakeDelayed()
        }
    }
    
    // Critical bar timer
    LaunchedEffect(Unit) {
        while (true) {
            delay(30)
            if (battleSystem.attackPhase == 0) { // Only update when not attacking
                battleSystem.updateCritBarProgress((battleSystem.critBarProgress + 5) % 101)
            }
        }
    }
    
    // Animation for attack phases
    LaunchedEffect(battleSystem.attackPhase) {
        when (battleSystem.attackPhase) {
            1 -> {
                // Phase 1: Both attacks from middle screen
                var progress = 0f
                while (progress < 1f) {
                    progress += 0.016f // 60 FPS
                    battleSystem.setAttackProgress(progress)
                    delay(16) // 60 FPS
                }
                battleSystem.advanceAttackPhase()
            }
            2 -> {
                // Phase 2: Player attack on enemy screen
                battleSystem.switchToView(2) // Enemy screen
                var progress = 0f
                while (progress < 1f) {
                    progress += 0.016f // 60 FPS
                    battleSystem.setAttackProgress(progress)
                    
                    // Trigger animation when attack reaches the enemy (around 50% progress for enemy dodge)
                    if (progress >= 0.50f && !battleSystem.isOpponentHit && !battleSystem.isOpponentDodging) {
                        if (battleSystem.attackIsHit) {
                            // Player attack hits enemy
                            battleSystem.startOpponentHit()
                            // Show hit effect and damage effect
                            showOpponentHitEffect = true
                            // Delay hiding the attack sprite to match hit effect timing
                            coroutineScope.launch {
                                delay(400) // Match the hit effect delay
                                hideEnemyAttackSprite = true
                            }
                            // Delay showing damage number to match hit effect timing
                            if (pendingOpponentDamage > 0) {
                                coroutineScope.launch {
                                    delay(400) // Match the hit effect delay
                                    showOpponentDamageNumber = true
                                }
                            }
                            // Delay SLEEP animation to match hit effect timing
                            coroutineScope.launch {
                                delay(400) // Match the hit effect delay
                                battleSystem.startOpponentHitDelayed()
                            }
                            // Delay shake animation to match hit effect timing
                            coroutineScope.launch {
                                delay(400) // Match the hit effect delay
                                battleSystem.startOpponentShakeDelayed()
                            }
                        } else {
                            // Player attack misses, enemy dodges
                            battleSystem.startOpponentDodge()
                        }
                    }
                    
                    delay(16) // 60 FPS
                }
                 battleSystem.completeAttackAnimation(opponentDamage = pendingOpponentDamage)
                
                // Hide damage number and reset pending damage after animation
                if (showOpponentDamageNumber) {
                    delay(800) // Wait for damage number animation (scale up + hold + fade out)
                    showOpponentDamageNumber = false
                 pendingOpponentDamage = 0f
                }
                
                delay(100)
                
                // Check if there should be a counter-attack
                if (battleSystem.shouldCounterAttack) {
                    battleSystem.startCounterAttack()
                } else {
                    battleSystem.advanceAttackPhase()
                }
            }
            3 -> {
                // Phase 3: Enemy attack on player screen
                battleSystem.switchToView(1) // Player screen
                var progress = 0f
                while (progress < 1f) {
                    progress += 0.016f // 60 FPS
                    battleSystem.setAttackProgress(progress)
                    
                    // Trigger animation when attack reaches the player (around 50% progress for player dodge)
                    if (progress >= 0.50f && !battleSystem.isPlayerHit && !battleSystem.isPlayerDodging) {
                        if (battleSystem.opponentAttackIsHit) {
                            // Enemy attack hits player
                            battleSystem.startPlayerHit()
                            // Show hit effect and damage effect
                            showPlayerHitEffect = true
                            // Delay hiding the attack sprite to match hit effect timing
                            coroutineScope.launch {
                                delay(400) // Match the hit effect delay
                                hidePlayerAttackSprite = true
                            }
                            // Delay showing damage number to match hit effect timing
                            if (pendingPlayerDamage > 0) {
                                coroutineScope.launch {
                                    delay(400) // Match the hit effect delay
                                    showPlayerDamageNumber = true
                                }
                            }
                            // Delay SLEEP animation to match hit effect timing
                            coroutineScope.launch {
                                delay(400) // Match the hit effect delay
                                battleSystem.startPlayerHitDelayed()
                            }
                            // Delay shake animation to match hit effect timing
                            coroutineScope.launch {
                                delay(400) // Match the hit effect delay
                                battleSystem.startPlayerShakeDelayed()
                            }
                        } else {
                            // Enemy attack misses, player dodges
                            battleSystem.startPlayerDodge()
                        }
                    }
                    
                    delay(16) // 60 FPS
                }
                 battleSystem.completeAttackAnimation(playerDamage = pendingPlayerDamage)
                
                // Hide damage number and reset pending damage after animation
                if (showPlayerDamageNumber) {
                    delay(800) // Wait for damage number animation (scale up + hold + fade out)
                    showPlayerDamageNumber = false
                 pendingPlayerDamage = 0f
                }
                
                 battleSystem.resetAttackState()
                 battleSystem.enableAttackButton()
                
                // Check if battle is over
                if (battleSystem.checkBattleOver()) {
                    battleSystem.endBattle()
                    onAttackClick()
                }
            }
        }
    }

    // Player dodge animation
    LaunchedEffect(battleSystem.isPlayerDodging) {
        if (battleSystem.isPlayerDodging) {
            var dodgeProgress = 0f
            var dodgeDirection = 1f // Start moving up
            
            // Move up
            while (dodgeProgress < 1f) {
                dodgeProgress += 0.05f // Faster dodge movement
                battleSystem.setPlayerDodgeProgress(dodgeProgress)
                battleSystem.setPlayerDodgeDirection(dodgeDirection)
                delay(16) // 60 FPS
            }
            
            // Wait at the top
            delay(200)
            
            // Move back down
            dodgeDirection = -1f
            dodgeProgress = 0f
            while (dodgeProgress < 1f) {
                dodgeProgress += 0.05f
                battleSystem.setPlayerDodgeProgress(dodgeProgress)
                battleSystem.setPlayerDodgeDirection(dodgeDirection)
                delay(16)
            }
            
            battleSystem.endPlayerDodge()
        }
    }

    // Opponent dodge animation
    LaunchedEffect(battleSystem.isOpponentDodging) {
        if (battleSystem.isOpponentDodging) {
            var dodgeProgress = 0f
            var dodgeDirection = 1f // Start moving up
            
            // Move up
            while (dodgeProgress < 1f) {
                dodgeProgress += 0.05f // Faster dodge movement
                battleSystem.setOpponentDodgeProgress(dodgeProgress)
                battleSystem.setOpponentDodgeDirection(dodgeDirection)
                delay(16) // 60 FPS
            }
            
            // Wait at the top
            delay(200)
            
            // Move back down
            dodgeDirection = -1f
            dodgeProgress = 0f
            while (dodgeProgress < 1f) {
                dodgeProgress += 0.05f
                battleSystem.setOpponentDodgeProgress(dodgeProgress)
                battleSystem.setOpponentDodgeDirection(dodgeDirection)
                delay(16)
            }
            
            battleSystem.endOpponentDodge()
        }
    }

    // Player hit animation
    LaunchedEffect(battleSystem.isPlayerHit) {
        if (battleSystem.isPlayerHit) {
            var hitProgress = 0f
            
            // Quick hit effect
            while (hitProgress < 1f) {
                hitProgress += 0.1f // Fast hit effect
                battleSystem.setHitProgress(hitProgress)
                delay(16)
            }
            
            delay(100) // Brief pause
            
            battleSystem.endPlayerHit()
        }
    }

    // Player delayed shake animation
    LaunchedEffect(battleSystem.isPlayerShakeDelayed) {
        if (battleSystem.isPlayerShakeDelayed) {
            var hitProgress = 0f
            
            // Quick hit effect
            while (hitProgress < 1f) {
                hitProgress += 0.1f // Fast hit effect
                battleSystem.setHitProgress(hitProgress)
                delay(16)
            }
            
            delay(100) // Brief pause
            
            battleSystem.endPlayerShakeDelayed()
        }
    }

    // Opponent hit animation
    LaunchedEffect(battleSystem.isOpponentHit) {
        if (battleSystem.isOpponentHit) {
            var hitProgress = 0f
            
            // Quick hit effect
            while (hitProgress < 1f) {
                hitProgress += 0.1f // Fast hit effect
                battleSystem.setHitProgress(hitProgress)
                delay(16)
            }
            
            delay(100) // Brief pause
            
            battleSystem.endOpponentHit()
        }
    }

    // Opponent delayed shake animation
    LaunchedEffect(battleSystem.isOpponentShakeDelayed) {
        if (battleSystem.isOpponentShakeDelayed) {
            var hitProgress = 0f
            
            // Quick hit effect
            while (hitProgress < 1f) {
                hitProgress += 0.1f // Fast hit effect
                battleSystem.setHitProgress(hitProgress)
                delay(16)
            }
            
            delay(100) // Brief pause
            
            battleSystem.endOpponentShakeDelayed()
        }
    }

    // Damage number handling - store pending damage but don't show immediately
    LaunchedEffect(pendingPlayerDamage) {
        if (pendingPlayerDamage > 0) {
            playerDamageValue = pendingPlayerDamage.toInt()
            // Don't show immediately - wait for attack animation to reach the Digimon
        }
    }

    LaunchedEffect(pendingOpponentDamage) {
        if (pendingOpponentDamage > 0) {
            opponentDamageValue = pendingOpponentDamage.toInt()
            // Don't show immediately - wait for attack animation to reach the Digimon
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (battleSystem.currentView) {
            0 -> {
                // Middle screen - both Digimon
                MiddleBattleView(
                    userId = currentUserId,
                    battleSystem = battleSystem,
                    stage = stage,
                    playerName = playerName,
                    opponentName = opponentName,
                    attackAnimationProgress = battleSystem.attackProgress,
                    onAttackClick = {
                        battleSystem.startPlayerAttack()
                    },
                    activeCharacter = activeCharacter,
                    opponentCharacter = opponentCharacter,
                    context = context,
                    onSetPendingDamage = { playerDamage, opponentDamage ->
                        pendingPlayerDamage = playerDamage
                        pendingOpponentDamage = opponentDamage
                    },
                    coroutineScope = coroutineScope,
                    hidePlayerAttackSprite = hidePlayerAttackSprite,
                    hideEnemyAttackSprite = hideEnemyAttackSprite,
                    selectedBackgroundSet = selectedBackgroundSet
                )
            }
            1 -> {
                // Player screen - enemy attack
                PlayerBattleView(
                    battleSystem = battleSystem,
                    stage = stage,
                    playerName = playerName,
                    attackAnimationProgress = battleSystem.attackProgress,
                    onAttackClick = {
                        battleSystem.startPlayerAttack()
                    },
                    activeCharacter = activeCharacter,
                    context = context,
                    opponent = opponentCharacter,
                    onSetPendingDamage = { playerDamage, opponentDamage ->
                        pendingPlayerDamage = playerDamage
                        pendingOpponentDamage = opponentDamage
                    },
                    coroutineScope = coroutineScope,
                    hidePlayerAttackSprite = hidePlayerAttackSprite,
                    selectedBackgroundSet = selectedBackgroundSet
                )
            }
            2 -> {
                // Enemy screen - player attack
                EnemyBattleView(
                    battleSystem = battleSystem,
                    stage = stage,
                    opponentName = opponentName,
                    attackAnimationProgress = battleSystem.attackProgress,
                    activeCharacter = opponentCharacter,
                    playerCharacter = activeCharacter,
                    hideEnemyAttackSprite = hideEnemyAttackSprite,
                    selectedBackgroundSet = selectedBackgroundSet
                )
            }
        }
        
        // Damage number overlays - moved inside the Box for proper positioning
        when (battleSystem.currentView) {
            0 -> {
                // Middle screen - NO damage numbers should show here
                // This screen is for the initial attack phase only
            }
            1 -> {
                // Player screen - show player damage (when opponent attacks player)
                AnimatedDamageNumber(
                    damage = playerDamageValue,
                    isVisible = showPlayerDamageNumber,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-50).dp)
                        //.background(Color.Yellow.copy(alpha = 0.3f)) // Debug background
                )
                
                // Player hit effects
                HitEffectOverlay(
                    isVisible = showPlayerHitEffect,
                    modifier = Modifier.fillMaxSize(),
                    isPlayerScreen = true,
                    onAnimationComplete = {
                        showPlayerHitEffect = false
                        hidePlayerAttackSprite = false // Show attack sprite again
                    }
                )
                

            }
            2 -> {
                // Enemy screen - show opponent damage (when player attacks opponent)
                AnimatedDamageNumber(
                    damage = opponentDamageValue,
                    isVisible = showOpponentDamageNumber,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-50).dp)
                )
                
                // Enemy hit effects
                HitEffectOverlay(
                    isVisible = showOpponentHitEffect,
                    modifier = Modifier.fillMaxSize(),
                    isPlayerScreen = false,
                    onAnimationComplete = {
                        showOpponentHitEffect = false
                        hideEnemyAttackSprite = false // Show attack sprite again
                    }
                )
                

            }
        }
    }
}

@Composable
fun MiddleBattleView(
    userId: Long? = null,
    battleSystem: ArenaBattleSystem,
    stage: String,
    playerName: String,
    opponentName: String,
    attackAnimationProgress: Float,
    onAttackClick: () -> Unit,
    activeCharacter: APIBattleCharacter?,
    opponentCharacter: APIBattleCharacter?,
    context: android.content.Context?,
    onSetPendingDamage: (Float, Float) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    hidePlayerAttackSprite: Boolean,
    hideEnemyAttackSprite: Boolean,
    selectedBackgroundSet: Int = 0
) {
    // Track previous character ID to detect transitions
    var previousCharacterId by remember { mutableStateOf<String?>(null) }
    var previousAttackPhase by remember { mutableStateOf<Int?>(null) }
    var isTransitioning by remember { mutableStateOf(false) }
    var lastApiResult by remember { mutableStateOf<com.github.nacabaro.vbhelper.battle.PVPDataModel?>(null) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Animated background - positioned underneath all other sprites
        MultiLayerAnimatedBattleBackground(
            modifier = Modifier.fillMaxSize(),
            backgroundSetIndex = selectedBackgroundSet
        )
        
        // Top section: HP bars and HP numbers
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Enemy HP bar and text with background box
            Box(
                modifier = getLandscapeBoxModifier(),
                contentAlignment = getLandscapeAlignment()
            ) {
                Column(
                    horizontalAlignment = getLandscapeHorizontalAlignment()
                ) {
                    // Enemy HP bar (top)
            LinearProgressIndicator(
                        progress = { battleSystem.opponentHP / (opponentCharacter?.baseHp?.toFloat() ?: 100f) },
                        modifier = getLandscapeModifier(),
                        color = Color.Red,
                trackColor = Color.Gray
            )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Enemy HP display numbers
            Text(
                        text = "Enemy HP: ${battleSystem.opponentHP.toInt()}/${opponentCharacter?.baseHp ?: 100}",
                        fontSize = getLandscapeFontSize(),
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                                //blurRadius = 2f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

        }

        // Middle section: Both Digimon with horizontal line separator
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                // Enemy Digimon (top half)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Determine animation type for enemy
                    val enemyAnimationType = when {
                        battleSystem.attackPhase == 1 -> DigimonAnimationType.ATTACK  // Both attacking in Phase 1
                        battleSystem.isOpponentDodging -> DigimonAnimationType.WALK
                        battleSystem.isOpponentHitDelayed -> DigimonAnimationType.SLEEP
                    else -> DigimonAnimationType.IDLE
                }
                    
                    // Calculate vertical offset for enemy dodge animation
                    val enemyVerticalOffset = if (battleSystem.isOpponentDodging) {
                        val dodgeHeight = 30.dp
                        val progress = battleSystem.opponentDodgeProgress
                        val direction = battleSystem.opponentDodgeDirection
                        
                        if (direction > 0) {
                            -(progress * dodgeHeight.value).dp
                        } else {
                            -((1f - progress) * dodgeHeight.value).dp
                        }
                    } else {
                        0.dp
                    }
                    
                    // Calculate hit effect for enemy
                    val enemyHitOffset = if (battleSystem.isOpponentShakeDelayed) {
                        val shakeAmount = 5.dp
                        val progress = battleSystem.hitProgress
                        val shake = if (progress < 0.5f) progress * 2f else (1f - progress) * 2f
                        (shake * shakeAmount.value).dp
                    } else {
                        0.dp
                }
                
                AnimatedSpriteImage(
                        characterId = opponentCharacter?.charaId ?: "dim011_mon01",
                        animationType = enemyAnimationType,
                    modifier = Modifier
                        .size(80.dp)
                            .offset(
                                x = enemyHitOffset,
                                y = enemyVerticalOffset + 40.dp
                            ),
                    contentScale = ContentScale.Fit,
                        reloadMappings = false,
                        animationOffset = 375L // Offset enemy animation by half the idle duration
                    )
                    
                    // Enemy attack sprite (Phase 1 only)
                    if (battleSystem.attackPhase == 1 && !hideEnemyAttackSprite) {
                        val xOffset = (-attackAnimationProgress * 400).dp  // Start at center, move left off screen
                        val yOffset = 30.dp  // Lower enemy attack sprite by 30 pixels
                        
                        AttackSpriteImage(
                            characterId = opponentCharacter?.charaId ?: "dim011_mon01",
                            isLarge = false,
                            modifier = Modifier
                                .size(60.dp)
                                .offset(
                                    x = xOffset,
                                    y = yOffset
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                // Horizontal line separator (hidden in landscape mode)
                val lineConfiguration = LocalConfiguration.current
                val isLandscapeMode = lineConfiguration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
                
                if (!isLandscapeMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color.Black)
                    )
                }
                
                // Player Digimon (bottom half)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Determine animation type for player
                    val playerAnimationType = when {
                        battleSystem.attackPhase == 1 -> DigimonAnimationType.ATTACK  // Both attacking in Phase 1
                        battleSystem.isPlayerDodging -> DigimonAnimationType.WALK
                        battleSystem.isPlayerHitDelayed -> DigimonAnimationType.SLEEP
                        else -> DigimonAnimationType.IDLE
                    }
                    
                    // Calculate vertical offset for player dodge animation
                    val playerVerticalOffset = if (battleSystem.isPlayerDodging) {
                        val dodgeHeight = 30.dp
                        val progress = battleSystem.playerDodgeProgress
                        val direction = battleSystem.playerDodgeDirection
                        
                        if (direction > 0) {
                            -(progress * dodgeHeight.value).dp
                        } else {
                            -((1f - progress) * dodgeHeight.value).dp
                        }
                    } else {
                        0.dp
                    }
                    
                    // Calculate hit effect for player
                    val playerHitOffset = if (battleSystem.isPlayerShakeDelayed) {
                        val shakeAmount = 5.dp
                        val progress = battleSystem.hitProgress
                        val shake = if (progress < 0.5f) progress * 2f else (1f - progress) * 2f
                        (shake * shakeAmount.value).dp
                    } else {
                        0.dp
                    }
                    
                    AnimatedSpriteImage(
                        characterId = activeCharacter?.charaId ?: "dim011_mon01",
                        animationType = playerAnimationType,
                        modifier = Modifier
                            .size(80.dp)
                            .scale(-1f, 1f) // Flip player Digimon horizontally
                            .offset(
                                x = playerHitOffset,
                                y = playerVerticalOffset - 40.dp
                            ),
                        contentScale = ContentScale.Fit,
                        reloadMappings = false,
                        animationOffset = 0L // Player animation starts immediately
                    )
                    
                    // Player attack sprite (Phase 1 only)
                    if (battleSystem.attackPhase == 1 && !hidePlayerAttackSprite) {
                        val xOffset = (attackAnimationProgress * 400).dp  // Start at center, move right off screen
                        val yOffset = (-30).dp  // Raise player attack sprite by 30 pixels
                        
                        AttackSpriteImage(
                            characterId = activeCharacter?.charaId ?: "dim011_mon01",
                            isLarge = false,
                            modifier = Modifier
                                .size(60.dp)
                                .offset(
                                    x = xOffset,
                                    y = yOffset
                                )
                                .scale(-1f, 1f), // Flip attack sprite
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }

        // Bottom section: Player HP bar, Critical bar and Attack button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Critical bar
            LinearProgressIndicator(
                progress = { battleSystem.critBarProgress / 100f },
                modifier = getLandscapeModifier(),
                color = Color.Yellow,
                trackColor = Color.Gray
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Player HP bar and text with background box
            Box(
                modifier = getLandscapeBoxModifier(),
                contentAlignment = getLandscapeAlignment()
            ) {
                Column(
                    horizontalAlignment = getLandscapeHorizontalAlignment()
                ) {
                    // Player HP bar
                    LinearProgressIndicator(
                        progress = { battleSystem.playerHP / (activeCharacter?.baseHp?.toFloat() ?: 100f) },
                        modifier = getLandscapeModifier(),
                        color = Color.Green,
                        trackColor = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Player HP display numbers
                    Text(
                        text = "HP: ${battleSystem.playerHP.toInt()}/${activeCharacter?.baseHp ?: 100}",
                        fontSize = getLandscapeFontSize(),
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                                //blurRadius = 2f
                            )
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Attack button
            Button(
                onClick = {
                    
                    // Capture userId for use in this lambda
                    val playerUserId = userId
                    
                    // Get crit bar progress as float (0.0f to 100.0f)
                    val critBarProgressFloat = battleSystem.critBarProgress.toFloat()
                    
                    // Determine player and opponent stages
                    val playerStage = when (activeCharacter?.stage) {
                        0 -> 0 // rookie
                        1 -> 1 // champion
                        2 -> 2 // ultimate
                        3 -> 3 // mega
                        else -> 0
                    }
                    
                    val opponentStage = when (opponentCharacter?.stage) {
                        0 -> 0 // rookie
                        1 -> 1 // champion
                        2 -> 2 // ultimate
                        3 -> 3 // mega
                        else -> 0
                    }
                    
                    // Send API call with all parameters
                    context?.let { ctx ->
                        // Start both attacks simultaneously
                        battleSystem.startPlayerAttack()
                        
                        RetrofitHelper().getPVPWinner(
                            ctx, 
                            1, 
                            playerUserId ?: 2L, 
                            activeCharacter?.name ?: "Player", 
                            playerStage, 
                            opponentStage, 
                            opponentCharacter?.name ?: "Opponent", 
                            opponentStage
                        ) { apiResult ->
                            // Handle API response here
                            println("API Result: $apiResult")
                            lastApiResult = apiResult // Store for debug display
                            
                            // Update HP based on API response
                            when (apiResult.state) {
                                 1 -> {
                                     // Match is still ongoing - update HP and continue
                                                                                                         // Set pending damage based on API result
                                     if (apiResult.playerAttackDamage > 0) {
                                          // Player attack hit - enemy takes damage at end of player animation
                                          onSetPendingDamage(0f, apiResult.playerAttackDamage.toFloat()) // Opponent takes damage
                                          battleSystem.setAttackHitState(true)
                                         
                                         // Also check if enemy counter-attacks and hits
                                         if (apiResult.opponentAttackDamage > 0) {
                                             onSetPendingDamage(apiResult.opponentAttackDamage.toFloat(), apiResult.playerAttackDamage.toFloat()) // Both take damage
                                         }
                                      } else {
                                         // Player attack missed - enemy counter-attacks
                                          battleSystem.setAttackHitState(false)
                                         // Set up counter-attack - determine if it hits based on API result
                                         val counterAttackHits = apiResult.opponentAttackDamage > 0
                                         
                                         // Use opponentAttackDamage to determine counter-attack hit
                                         val finalCounterAttackHits = counterAttackHits
                                         
                                         if (finalCounterAttackHits) {
                                             onSetPendingDamage(apiResult.opponentAttackDamage.toFloat(), 0f) // Player takes damage
                                         } else {
                                             onSetPendingDamage(0f, 0f) // No damage
                                         }
                                         battleSystem.setupCounterAttack(finalCounterAttackHits)
                                         // Set the opponent attack hit state for Phase 3
                                         battleSystem.handleOpponentAttackResult(finalCounterAttackHits)
                                      }
                                 }
                                 2 -> {
                                     // Match is over - transition to results screen
                                     println("Match is over! Winner: ${apiResult.winner}")
                                     battleSystem.updateHPFromAPI(apiResult.playerHP.toFloat(), apiResult.opponentHP.toFloat())
                                     onAttackClick() // This will transition to battle-results screen
                                 }
                                 -1 -> {
                                     // Error occurred
                                     println("API Error: ${apiResult.status}")
                                     battleSystem.resetAttackState()
                                     battleSystem.enableAttackButton()
                                 }
                             }
                         }
                     }
                 },
                 enabled = battleSystem.isAttackButtonEnabled,
                 modifier = Modifier
                     .fillMaxWidth(0.5f)
                     .height(35.dp),
                 colors = ButtonDefaults.buttonColors(
                     containerColor = Color.Blue,
                     disabledContainerColor = Color.Gray
                 ),
                 shape = RoundedCornerShape(8.dp)
             ) {
                 Text("Attack", color = Color.White, fontSize = 12.sp)
             }
        }
    }
}

@Composable
fun PlayerBattleView(
    battleSystem: ArenaBattleSystem,
    stage: String,
    playerName: String,
    attackAnimationProgress: Float,
    onAttackClick: () -> Unit,
    activeCharacter: APIBattleCharacter?,
    context: android.content.Context?,
    opponent: APIBattleCharacter?,
    onSetPendingDamage: (Float, Float) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    hidePlayerAttackSprite: Boolean,
    selectedBackgroundSet: Int = 0
) {
    // Track previous character ID to detect transitions
    var previousCharacterId by remember { mutableStateOf<String?>(null) }
    var previousAttackPhase by remember { mutableStateOf<Int?>(null) }
    var isTransitioning by remember { mutableStateOf(false) }
    var lastApiResult by remember { mutableStateOf<com.github.nacabaro.vbhelper.battle.PVPDataModel?>(null) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Multi-layer animated battle background
        MultiLayerAnimatedBattleBackground(modifier = Modifier.fillMaxSize(), backgroundSetIndex = selectedBackgroundSet)
        
        // Top section: HP bar and HP numbers
    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Health bar and text with background box
            Box(
                modifier = getLandscapeBoxModifier(),
                contentAlignment = getLandscapeAlignment()
            ) {
                Column(
                    horizontalAlignment = getLandscapeHorizontalAlignment()
                ) {
            // Health bar
            LinearProgressIndicator(
                progress = { battleSystem.playerHP / (activeCharacter?.baseHp?.toFloat() ?: 100f) },
                modifier = getLandscapeModifier(),
                color = Color.Green,
                trackColor = Color.Gray
            )

                    Spacer(modifier = Modifier.height(4.dp))

            // Health display numbers
            Text(
                text = "HP: ${battleSystem.playerHP.toInt()}/${activeCharacter?.baseHp ?: 100}",
                        fontSize = getLandscapeFontSize(),
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                                //blurRadius = 2f
                            )
                        )
                    )
                }
            }
        }

        // Middle section: Player Digimon only
        Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            contentAlignment = Alignment.Center
    ) {
            // Player Digimon (left side)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(80.dp),
                contentAlignment = Alignment.CenterStart
        ) {
            // Determine animation type based on battle state
                val animationType = when {
                    battleSystem.isPlayerDodging -> DigimonAnimationType.WALK  // Use walk animation for dodge
                    battleSystem.isPlayerHitDelayed -> DigimonAnimationType.SLEEP     // Use sleep animation for hit effect (injured sprite)
                    battleSystem.attackPhase == 1 -> DigimonAnimationType.ATTACK  // Player attack on player screen
                    battleSystem.attackPhase == 2 -> DigimonAnimationType.ATTACK  // Player attack on opponent screen
                    battleSystem.attackPhase == 3 -> DigimonAnimationType.IDLE    // Opponent attack on opponent screen
                    battleSystem.attackPhase == 4 -> DigimonAnimationType.IDLE    // Opponent attack on player screen
                else -> DigimonAnimationType.IDLE
            }
                
                // Calculate vertical offset for dodge animation
                val verticalOffset = if (battleSystem.isPlayerDodging) {
                    val dodgeHeight = 30.dp
                    val progress = battleSystem.playerDodgeProgress
                    val direction = battleSystem.playerDodgeDirection
                    
                    if (direction > 0) {
                        // Moving up (negative offset to move UP visually)
                        -(progress * dodgeHeight.value).dp
                    } else {
                        // Moving back down (from negative peak to 0)
                        -((1f - progress) * dodgeHeight.value).dp
                    }
                } else {
                    0.dp
                }
                
                // Calculate hit effect (slight shake)
                val hitOffset = if (battleSystem.isPlayerShakeDelayed) {
                    val shakeAmount = 5.dp
                    val progress = battleSystem.hitProgress
                    // Simple shake effect without complex math
                    val shake = if (progress < 0.5f) progress * 2f else (1f - progress) * 2f
                    (shake * shakeAmount.value).dp
                } else {
                    0.dp
            }
            
            AnimatedSpriteImage(
                characterId = activeCharacter?.charaId ?: "dim011_mon01",
                animationType = animationType,
                    modifier = Modifier
                        .size(80.dp)
                        .scale(-1f, 1f) // Flip player Digimon horizontally
                        .offset(
                            x = hitOffset,
                            y = verticalOffset
                        ),
                contentScale = ContentScale.Fit,
                    reloadMappings = false,
                    animationOffset = 0L // Player animation starts immediately
            )
            
            // Attack sprite visibility and positioning based on attack phase
            val shouldShowAttack = when (battleSystem.attackPhase) {
                    1 -> false // Both attacks from middle screen
                    2 -> false // Player attack on enemy screen
                    3 -> true  // Enemy attack on player screen
                else -> false
            }
            
            if (shouldShowAttack) {
                val xOffset = when (battleSystem.attackPhase) {
                        3 -> (-attackAnimationProgress * 400 + 350).dp  // Enemy attack on player screen - start more to the right
                    else -> 0.dp
                }
                
                    // Use opponent character ID for Phase 3 (enemy attack)
                val characterId = when (battleSystem.attackPhase) {
                        3 -> opponent?.charaId ?: "dim011_mon01"  // Use opponent's character ID
                        else -> activeCharacter?.charaId ?: "dim011_mon01"  // Use player's character ID
                }
                
                // Handle sprite transition
                LaunchedEffect(characterId, battleSystem.attackPhase) {
                    if ((previousCharacterId != null && previousCharacterId != characterId) ||
                        (previousAttackPhase != null && previousAttackPhase != battleSystem.attackPhase)) {
                        // Character ID or attack phase changed, start transition
                        isTransitioning = true
                        delay(100) // Brief invisibility period
                        isTransitioning = false
                    }
                    previousCharacterId = characterId
                    previousAttackPhase = battleSystem.attackPhase
                }
                
                    if (!isTransitioning && !hidePlayerAttackSprite) {
                    AttackSpriteImage(
                        characterId = characterId,
                        isLarge = false,
                        modifier = Modifier
                            .size(60.dp)
                            .offset(
                                x = xOffset,
                                y = 0.dp
                            )
                                .scale(1f, 1f), // Don't flip enemy attacks on player screen
                        contentScale = ContentScale.Fit
                    )
                    }
                }
            }
        }


    }
}

@Composable
fun EnemyBattleView(
    battleSystem: ArenaBattleSystem,
    stage: String,
    opponentName: String,
    attackAnimationProgress: Float,
    activeCharacter: APIBattleCharacter? = null,
    playerCharacter: APIBattleCharacter? = null,
    hideEnemyAttackSprite: Boolean,
    selectedBackgroundSet: Int = 0
) {
    // Track previous character ID to detect transitions
    var previousCharacterId by remember { mutableStateOf<String?>(null) }
    var previousAttackPhase by remember { mutableStateOf<Int?>(null) }
    var isTransitioning by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Multi-layer animated battle background
        MultiLayerAnimatedBattleBackground(modifier = Modifier.fillMaxSize(), backgroundSetIndex = selectedBackgroundSet)
        
        // Top section: Enemy HP bar and HP numbers
    Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Enemy HP bar and text with background box
            Box(
                modifier = getLandscapeBoxModifier(),
                contentAlignment = getLandscapeAlignment()
            ) {
                Column(
                    horizontalAlignment = getLandscapeHorizontalAlignment()
                ) {
        // Enemy HP bar
        LinearProgressIndicator(
            progress = { battleSystem.opponentHP / (activeCharacter?.baseHp?.toFloat() ?: 100f) },
                        modifier = getLandscapeModifier(),
            color = Color.Red,
            trackColor = Color.Gray
        )

                    Spacer(modifier = Modifier.height(4.dp))

        // Enemy HP display numbers
        Text(
            text = "Enemy HP: ${battleSystem.opponentHP.toInt()}/${activeCharacter?.baseHp ?: 100}",
                        fontSize = getLandscapeFontSize(),
                        color = Color.White,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(4f, 4f),
                                //blurRadius = 2f
                            )
                        )
                    )
                }
            }
        }

        // Middle section: Enemy Digimon
        Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
            contentAlignment = Alignment.Center
    ) {
            // Enemy Digimon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .size(80.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            // Determine animation type based on battle state
                val animationType = when {
                    battleSystem.isOpponentDodging -> DigimonAnimationType.WALK  // Use walk animation for dodge
                    battleSystem.isOpponentHitDelayed -> DigimonAnimationType.SLEEP     // Use sleep animation for hit effect (injured sprite)
                    battleSystem.attackPhase == 2 -> DigimonAnimationType.IDLE    // Player attack on enemy screen
                else -> DigimonAnimationType.IDLE
            }
                
                // Calculate vertical offset for dodge animation
                val verticalOffset = if (battleSystem.isOpponentDodging) {
                    val dodgeHeight = 30.dp
                    val progress = battleSystem.opponentDodgeProgress
                    val direction = battleSystem.opponentDodgeDirection
                    
                    if (direction > 0) {
                        // Moving up (negative offset to move UP visually)
                        -(progress * dodgeHeight.value).dp
                    } else {
                        // Moving back down (from negative peak to 0)
                        -((1f - progress) * dodgeHeight.value).dp
                    }
                } else {
                    0.dp
                }
                
                // Calculate hit effect (slight shake)
                val hitOffset = if (battleSystem.isOpponentShakeDelayed) {
                    val shakeAmount = 5.dp
                    val progress = battleSystem.hitProgress
                    // Simple shake effect without complex math
                    val shake = if (progress < 0.5f) progress * 2f else (1f - progress) * 2f
                    (shake * shakeAmount.value).dp
                } else {
                    0.dp
                }
            
            AnimatedSpriteImage(
                characterId = activeCharacter?.charaId ?: "dim011_mon01",
                animationType = animationType,
                    modifier = Modifier
                        .size(80.dp)
                        .offset(
                            x = hitOffset,
                            y = verticalOffset
                        ),
                contentScale = ContentScale.Fit,
                    reloadMappings = false,
                    animationOffset = 375L // Offset enemy animation by half the idle duration
            )
            
            // Attack sprite visibility and positioning based on attack phase
            val shouldShowAttack = when (battleSystem.attackPhase) {
                    2 -> true  // Player attack on enemy screen
                else -> false
            }
            
            if (shouldShowAttack) {
                    val xOffset = (attackAnimationProgress * 400 - 350).dp  // Player attack on enemy screen - start more to the left
                    
                    // Use player's character ID for player attack
                    val characterId = playerCharacter?.charaId ?: "dim011_mon01"
                
                // Handle sprite transition
                LaunchedEffect(characterId, battleSystem.attackPhase) {
                    if ((previousCharacterId != null && previousCharacterId != characterId) ||
                        (previousAttackPhase != null && previousAttackPhase != battleSystem.attackPhase)) {
                        // Character ID or attack phase changed, start transition
                        isTransitioning = true
                        delay(100) // Brief invisibility period
                        isTransitioning = false
                    }
                    previousCharacterId = characterId
                    previousAttackPhase = battleSystem.attackPhase
                }
                
                    if (!isTransitioning && !hideEnemyAttackSprite) {
                    AttackSpriteImage(
                        characterId = characterId,
                        isLarge = false,
                        modifier = Modifier
                            .size(60.dp)
                            .offset(
                                x = xOffset,
                                y = 0.dp
                            )
                                .scale(-1f, 1f), // Flip player attacks
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BattlesScreen() {
    val TAG = "BattleScreen"
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    
    // Permission state
    var hasStoragePermission by remember { mutableStateOf(false) }
    
    // Check if permission is already granted
    // For Android 11+ (API 30+), check MANAGE_EXTERNAL_STORAGE
    // For Android 10 and below, check READ_EXTERNAL_STORAGE
    val permissionCheck = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ - check for MANAGE_EXTERNAL_STORAGE
            android.os.Environment.isExternalStorageManager()
        } else {
            // Android 10 and below - check for READ_EXTERNAL_STORAGE
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    // Permission launcher for READ_EXTERNAL_STORAGE (Android 10 and below)
    val readStoragePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        hasStoragePermission = isGranted
        if (isGranted) {
        } else {
            println("BATTLESCREEN: READ_EXTERNAL_STORAGE permission denied")
        }
    }
    
    // Launcher for opening settings to grant MANAGE_EXTERNAL_STORAGE (Android 11+)
    val manageStorageSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Re-check permission after returning from settings
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            android.os.Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
        hasStoragePermission = hasPermission
        if (hasPermission) {
        } else {
            println("BATTLESCREEN: MANAGE_EXTERNAL_STORAGE permission not granted")
        }
    }
    
    // Initialize permission state
    LaunchedEffect(Unit) {
        hasStoragePermission = permissionCheck
        if (!permissionCheck && activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+ - need to request MANAGE_EXTERNAL_STORAGE
                // This requires user to go to settings
                println("BATTLESCREEN: Android 11+ detected - opening settings for MANAGE_EXTERNAL_STORAGE")
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.data = Uri.parse("package:${context.packageName}")
                    manageStorageSettingsLauncher.launch(intent)
                } catch (e: Exception) {
                    println("BATTLESCREEN: Error opening settings: ${e.message}")
                    // Fallback: try the general manage external storage settings
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        manageStorageSettingsLauncher.launch(intent)
                    } catch (e2: Exception) {
                        println("BATTLESCREEN: Error opening fallback settings: ${e2.message}")
                    }
                }
            } else {
                // Android 10 and below - request READ_EXTERNAL_STORAGE
                println("BATTLESCREEN: Requesting READ_EXTERNAL_STORAGE permission...")
                readStoragePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else if (permissionCheck) {
            //println("BATTLESCREEN: Storage permission already granted")
        }
    }

    var currentView by remember { mutableStateOf("main") }
    
    // Create BattleAuthContainer
    val battleAuthContainer = remember { BattleAuthContainer(context) }
    
    // Auth state
    var isAuthenticated by remember { mutableStateOf(false) }
    var isCheckingAuth by remember { mutableStateOf(true) }
    var userId by remember { mutableStateOf<Long?>(null) }
    // Track processed tokens to prevent duplicate API calls
    // Use rememberSaveable to persist across configuration changes (screen rotation)
    var processedTokens by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }

    var opponentsList by remember { mutableStateOf(ArrayList<APIBattleCharacter>()) }

    var activeCharacter by remember { mutableStateOf<APIBattleCharacter?>(null) }
    var selectedOpponent by remember { mutableStateOf<APIBattleCharacter?>(null) }
    var activeUserCharacter by remember { mutableStateOf<com.github.nacabaro.vbhelper.dtos.CharacterDtos.CharacterWithSprites?>(null) }
    var activeCardId by remember { mutableStateOf<String?>(null) }

    var expanded by remember { mutableStateOf(false) }
    var selectedStage by remember { mutableStateOf("") }
    var currentStage by remember { mutableStateOf("rookie") }
    
    // Random background set selection
    var selectedBackgroundSet by remember { mutableStateOf(0) }
    
    // Resume/Quit match dialog state
    var showResumeDialog by remember { mutableStateOf(false) }
    var existingMatchState by remember { mutableStateOf<com.github.nacabaro.vbhelper.battle.PVPDataModel?>(null) }
    var pendingOpponentForResume by remember { mutableStateOf<APIBattleCharacter?>(null) }
    var pendingCardIdForResume by remember { mutableStateOf<String?>(null) }
    var pendingApiStageForResume by remember { mutableStateOf<Int?>(null) }
    // Store the original opponent from the match (not the clicked one)
    var originalMatchOpponent by remember { mutableStateOf<APIBattleCharacter?>(null) }
    
    // Sprite animation tester state
    /*
    var showSpriteTester by remember { mutableStateOf(false) }
    var spriteTesterView by remember { mutableStateOf("entry") } // "entry" or "testing"
    var dimId by remember { mutableStateOf("") }
    var monId by remember { mutableStateOf("") }
    var currentTestAnimation by remember { mutableStateOf(DigimonAnimationType.IDLE) }
    var testCharacterId by remember { mutableStateOf("") }
     */

    /*
    // Create hardcoded character lists for each stage
    val rookieCharacters = listOf(
        APIBattleCharacter("AGUMON", "degimon_name_Dim012_003", "dim012_mon03", 0, 1, 1800, 1800, 2400.0f, 700.0f),
        APIBattleCharacter("PULSEMON", "degimon_name_Dim000_003", "dim000_mon03", 0, 1, 1800, 1800, 2400.0f, 700.0f),
        APIBattleCharacter("DORUMON", "degimon_name_dim137_mon03", "dim137_mon03", 0, 1, 3000, 3000, 5100.0f, 1050.0f)
    )

    val championCharacters = listOf(
        APIBattleCharacter("GREYMON","degimon_name_Dim012_004","dim012_mon04",1,1,2000, 2000, 3000.0f,900.0f),
        APIBattleCharacter("TYRANNOMON","degimon_name_Dim008_006","dim008_mon06",1,3,2000, 2000, 2400.0f,600.0f),
        APIBattleCharacter("DORUGAMON","degimon_name_dim137_mon05","dim137_mon05",1,3,3500, 3500, 5200.0f,1200.0f)
    )

    val ultimateCharacters = listOf(
        APIBattleCharacter("METALGREYMON (VIRUS)","degimon_name_Dim014_005","dim014_mon05",2,2,2640, 2640, 2450.0f,800.0f),
        APIBattleCharacter("MAMEMON", "degimon_name_Dim012_011", "dim012_mon11", 2, 1, 3000, 3000, 4000.0f, 1000.0f),
        APIBattleCharacter("DORUGREYMON","degimon_name_dim137_mon09","dim137_mon09",2,3,5000, 5000, 6400.0f,1400.0f)
    )

    val megaCharacters = listOf(
        APIBattleCharacter("WARGREYMON","degimon_name_Dim012_014","dim012_mon14",3,1,3080, 3080, 3825.0f,800.0f),
        APIBattleCharacter("SLAYERDRAMON","degimon_name_dim129_mon15","dim129_mon15",3,1,4800, 4800, 6300.0f,1950.0f),
        APIBattleCharacter("BREAKDRAMON","degimon_name_dim129_mon17","dim129_mon17",3,2,6000, 6000, 4000.0f,1980.0f)
    )
    // Get the appropriate character list based on current stage
    val characterList = when (currentStage.lowercase()) {
        "rookie" -> rookieCharacters
        "champion" -> championCharacters
        "ultimate" -> ultimateCharacters
        "mega" -> megaCharacters
        else -> rookieCharacters
    }
    */
    
    // Get the appropriate battle type based on player's stage (derived from activeUserCharacter)
    val playerBattleType = activeUserCharacter?.stage?.let { stage ->
        when (stage) {
            2 -> "rookie"    // Player stage 2 → Rookie opponents (API stage 0)
            3 -> "champion"  // Player stage 3 → Champion opponents (API stage 1) 
            4 -> "ultimate"  // Player stage 4 → Ultimate opponents (API stage 2)
            5 -> "mega"      // Player stage 5 → Mega opponents (API stage 3)
            else -> null
        }
    }

    // Determine if player can battle based on stage (derived from activeUserCharacter)
    val canBattle = activeUserCharacter?.stage?.let { it >= 2 } ?: false
    
    // Load opponents automatically based on player's stage
    // Only load if authenticated and character is ready
    LaunchedEffect(activeUserCharacter, isAuthenticated) {
        // Wait for authentication to complete before loading opponents
        if (!isAuthenticated) {
            return@LaunchedEffect
        }
        
        val currentCharacter = activeUserCharacter
        if (currentCharacter != null && canBattle && playerBattleType != null) {
            try {
                RetrofitHelper().getOpponents(context, playerBattleType!!) { opponents ->
                    try {
                        // Create a new list to trigger UI recomposition
                        opponentsList = ArrayList(opponents.opponentsList)
                        } catch (e: Exception) {
                            Log.d(TAG, "Error processing opponents data: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    Log.d(TAG,"Error calling getOpponents: ${e.message}")
                    e.printStackTrace()
                }
        } else {
            println("BATTLESCREEN: Cannot load opponents - activeUserCharacter: $currentCharacter")
            println("BATTLESCREEN: canBattle: $canBattle")
            println("BATTLESCREEN: playerBattleType: $playerBattleType")
            println("BATTLESCREEN: currentCharacter != null: ${currentCharacter != null}")
            if (currentCharacter != null) {
                println("BATTLESCREEN: currentCharacter.stage: ${currentCharacter.stage}")
                println("BATTLESCREEN: currentCharacter.stage >= 2: ${currentCharacter.stage >= 2}")
            }
        }
    }
    
    // Helper lambda to extract token from URI and authenticate
    // The token can be in 'c' parameter (from localhost:8080/authenticate?c=...) or 'token' parameter
    val handleTokenFromUri: (Uri) -> Unit = { uri ->
        // Try 'c' parameter first (from localhost:8080/authenticate?c=...)
        var token = uri.getQueryParameter("c")
        // Fall back to 'token' parameter if 'c' is not found
        if (token == null || token.isEmpty()) {
            token = uri.getQueryParameter("token")
        }
        
        if (token != null && token.isNotEmpty()) {
            // Check if we've already processed this token (either successfully or currently processing)
            if (!processedTokens.contains(token)) {
                // Mark token as being processed IMMEDIATELY to prevent duplicate API calls
                processedTokens = processedTokens + token
                
                // Exchange token with battle server
                RetrofitHelper().authenticate(context, token) { response ->
                    if (response.success) {
                        // Extract userId and sessionToken from response
                        val extractedUserId = response.userInfo?.userId?.toLongOrNull()
                        val sessionToken = response.sessionToken
                        
                        println("BATTLESCREEN: Authentication successful, userId: $extractedUserId, sessionToken: ${if (sessionToken != null) "present" else "missing"}")
                        
                        // Store both nacatech token (for re-auth) and sessionToken (for API calls)
                        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                            battleAuthContainer.authRepository.setAuthenticated(
                                isAuthenticated = true,
                                nacatechToken = token,
                                sessionToken = sessionToken,
                                userId = extractedUserId
                            )
                        }
                        // Update UI state on main thread
                        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                            isAuthenticated = true
                            isCheckingAuth = false
                            userId = extractedUserId
                            println("BATTLESCREEN: Authentication successful, userId: $extractedUserId")
                            android.widget.Toast.makeText(context, "Authentication successful!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        println("BATTLESCREEN: Authentication failed: ${response.message}")
                        // If it's an "Invalid user nonce" error, the token was already used - keep it marked to prevent retries
                        if (response.message?.contains("Invalid user nonce") == true || response.message?.contains("nonce") == true) {
                            println("BATTLESCREEN: Token was already used (Invalid user nonce), keeping it marked to prevent retries")
                            // Token already marked as processed, just handle the error
                            // Clear authentication state and open login page
                            kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                battleAuthContainer.authRepository.logout()
                            }
                            kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                                isAuthenticated = false
                                isCheckingAuth = false
                                // Small delay to ensure state is updated
                                kotlinx.coroutines.delay(100)
                                // Open auth URL to get a fresh token
                                val authUrl = "http://auth.nacatech.es/begin?app=443654920&redirect_uri=vbhelper://auth?token="
                                val authIntent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                                try {
                                    context.startActivity(authIntent)
                                    println("BATTLESCREEN: Opened auth URL after token expiration: $authUrl")
                        } catch (e: Exception) {
                                    println("BATTLESCREEN: Failed to open auth URL: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                        } else {
                            // For other errors, remove from processed set to allow retry with a new token
                            println("BATTLESCREEN: Authentication failed, removing token from processed set to allow retry")
                            processedTokens = processedTokens - token
                        }
                        // Show toast on main thread
                        kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                            android.widget.Toast.makeText(context, "Authentication failed: ${response.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        } else {
            println("BATTLESCREEN: No token found in URI: $uri (checked 'c' and 'token' parameters)")
        }
    }
    
    // Check authentication status on load
    LaunchedEffect(Unit) {
        try {
            val authRepository = battleAuthContainer.authRepository
            val localAuthState = authRepository.isAuthenticated.first()
            val storedToken = authRepository.authToken.first()
            val storedUserId = authRepository.userId.first()
            
            // Load stored userId if available
            if (storedUserId != null) {
                userId = storedUserId
            }
            
            // If we have a stored token, set authenticated state optimistically FIRST (before checking deep links)
            // This must happen immediately to prevent UI from showing "Checking authentication"
            if (localAuthState && storedToken != null && storedToken.isNotEmpty()) {
                // Set authenticated state immediately to prevent redirect on rotation and show UI
                isAuthenticated = true
                isCheckingAuth = false
                userId = storedUserId
            }
            
            // Only check for token in intent if it's a fresh deep link (ACTION_VIEW intent)
            // This prevents processing stale tokens from previous sessions
            val activity = context as? ComponentActivity
            val intent = activity?.intent
            if (intent?.action == Intent.ACTION_VIEW) {
                intent.data?.let { uri ->
                    if (uri.getQueryParameter("c") != null || uri.getQueryParameter("token") != null) {
                        handleTokenFromUri(uri)
                        return@LaunchedEffect // Don't open auth URL if we're processing a token
                    }
                }
            }
            
            // If we have a stored token and userId, assume session is still active
            // The single-use token can't be re-validated, but the server maintains a session after initial validation
            // We'll only re-authenticate if API calls fail with authentication errors
            if (localAuthState && storedToken != null && storedToken.isNotEmpty() && storedUserId != null) {
                // Session appears to be active - user is already authenticated
                // No need to re-validate the single-use token, just restore the session
                println("BATTLESCREEN: Restoring active session (userId: $storedUserId)")
                isAuthenticated = true
                isCheckingAuth = false
                userId = storedUserId
                // Session is restored, no need to validate token again
            } else if (localAuthState && storedToken != null && storedToken.isNotEmpty()) {
                // We have a token but no userId - try to validate once to get userId
                // This should only happen on first login or if userId was lost
                println("BATTLESCREEN: Have token but no userId, validating once to get userId...")
                RetrofitHelper().authenticate(context, storedToken) { response ->
                    // Update UI on main thread
                    kotlinx.coroutines.CoroutineScope(Dispatchers.Main).launch {
                        if (response.success) {
                            val extractedUserId = response.userInfo?.userId?.toLongOrNull()
                            val sessionToken = response.sessionToken
                            
                            // Update stored userId and sessionToken
                            if (extractedUserId != null) {
                                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                    authRepository.setAuthenticated(
                                        isAuthenticated = true,
                                        nacatechToken = storedToken,
                                        sessionToken = sessionToken,
                                        userId = extractedUserId
                                    )
                                }
                            }
                            isAuthenticated = true
                            isCheckingAuth = false
                            userId = extractedUserId
                            println("BATTLESCREEN: Got userId from validation: $extractedUserId, sessionToken: ${if (sessionToken != null) "present" else "missing"}")
                        } else {
                            println("BATTLESCREEN: Token validation failed: ${response.message}")
                            // Check if it's a critical error that requires re-authentication
                            val isCriticalError = response.message?.contains("Invalid user nonce") == true || 
                                                  response.message?.contains("nonce") == true ||
                                                  response.message?.contains("invalid") == true ||
                                                  response.message?.contains("expired") == true
                            
                            if (isCriticalError) {
                                // Critical error - token is invalid, need to re-authenticate
                                println("BATTLESCREEN: Critical authentication error, clearing state and redirecting")
                                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                    authRepository.logout()
                                }
                                isAuthenticated = false
                                isCheckingAuth = false
                                // Open auth URL
                                val authUrl = "http://auth.nacatech.es/begin?app=443654920&redirect_uri=vbhelper://auth?token="
                                val authIntent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                                context.startActivity(authIntent)
                                println("BATTLESCREEN: Opened auth URL after critical validation failure: $authUrl")
                            } else {
                                // Non-critical error (e.g., network issue) - keep authenticated state
                                println("BATTLESCREEN: Non-critical validation error, keeping authenticated state")
                                isAuthenticated = true
                                isCheckingAuth = false
                            }
                        }
                    }
                }
            } else {
                // No stored token or not authenticated locally
                isAuthenticated = false
                isCheckingAuth = false
                // If not authenticated and no fresh token in intent, open auth URL
                val authUrl = "http://auth.nacatech.es/begin?app=443654920&redirect_uri=vbhelper://auth?token="
                val authIntent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                context.startActivity(authIntent)
                println("BATTLESCREEN: Opened auth URL: $authUrl")
            }
        } catch (e: Exception) {
            println("BATTLESCREEN: Error checking authentication status: ${e.message}")
            isAuthenticated = false
            isCheckingAuth = false
        }
    }
    
    // Handle deep link callback to get token
    // Check intent data on initial load - handle both vbhelper:// and http://localhost:8080/authenticate?c=
    // Only process if it's a fresh ACTION_VIEW intent (deep link)
    LaunchedEffect(Unit) {
        // Small delay to ensure activity is fully initialized
        kotlinx.coroutines.delay(100)
        
        val activity = context as? ComponentActivity
        val intent = activity?.intent
        
        // Only process if this is a fresh deep link (ACTION_VIEW)
        if (intent?.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                
                // Handle vbhelper://auth?token= or vbhelper://auth?c= deep link
                if (uri.scheme == "vbhelper" && uri.host == "auth") {
                    handleTokenFromUri(uri)
                }
                // Handle http://localhost:8080/authenticate?c= redirect
                else if ((uri.scheme == "http" || uri.scheme == "https") && 
                         (uri.host == "localhost" || uri.host == "127.0.0.1" || uri.host?.contains("8080") == true)) {
                    handleTokenFromUri(uri)
                }
                // Also check if there's a 'c' or 'token' parameter in any URL
                else if (uri.getQueryParameter("c") != null || uri.getQueryParameter("token") != null) {
                    handleTokenFromUri(uri)
                } else {
                    println("BATTLESCREEN: URI found but no token parameter detected")
                }
            } else {
                println("BATTLESCREEN: ACTION_VIEW intent but no URI found")
            }
        } else {
            println("BATTLESCREEN: Not an ACTION_VIEW intent, skipping deep link processing")
        }
    }
    
    // Check intent when screen becomes visible or when authentication state changes
    // This handles cases where the app is already running and receives a deep link
    DisposableEffect(Unit) {
        val activity = context as? ComponentActivity
        val lifecycleOwner = activity as? LifecycleOwner
        
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && !isAuthenticated) {
                // Check intent data when activity resumes - only if it's a fresh ACTION_VIEW intent
                val intent = activity?.intent
                if (intent?.action == Intent.ACTION_VIEW) {
                    intent.data?.let { uri ->
                        if (uri.getQueryParameter("c") != null || uri.getQueryParameter("token") != null) {
                            handleTokenFromUri(uri)
                        }
                    }
                }
            }
        }
        
        lifecycleOwner?.lifecycle?.addObserver(observer)
        
        onDispose {
            lifecycleOwner?.lifecycle?.removeObserver(observer)
        }
    }
    
    // Watch auth repository state changes to detect when token is cleared (e.g., expired token)
    LaunchedEffect(Unit) {
        battleAuthContainer.authRepository.isAuthenticated.collect { authState ->
            if (!authState && isAuthenticated) {
                // Auth state was cleared (e.g., by RetrofitHelper due to expired token)
                println("BATTLESCREEN: Auth state cleared, triggering re-authentication")
                isAuthenticated = false
                isCheckingAuth = false
                // Open auth URL to get a fresh token
                val authUrl = "http://auth.nacatech.es/begin?app=443654920&redirect_uri=vbhelper://auth?token="
                val authIntent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
                try {
                    context.startActivity(authIntent)
                    println("BATTLESCREEN: Opened auth URL after token expiration: $authUrl")
                } catch (e: Exception) {
                    println("BATTLESCREEN: Failed to open auth URL: ${e.message}")
                    e.printStackTrace()
                }
            }
        }
    }
    
    // Also check intent when authentication state changes
    // Only process if it's a fresh ACTION_VIEW intent (deep link)
    LaunchedEffect(isAuthenticated) {
        if (!isAuthenticated) {
            kotlinx.coroutines.delay(200) // Small delay to ensure intent is available
            val activity = context as? ComponentActivity
            val intent = activity?.intent
            // Only process if this is a fresh deep link (ACTION_VIEW)
            if (intent?.action == Intent.ACTION_VIEW) {
                intent.data?.let { uri ->
                    println("BATTLESCREEN: Re-checking ACTION_VIEW intent data - URI: $uri, scheme: ${uri.scheme}, host: ${uri.host}")
                    // Handle vbhelper://auth?token= or vbhelper://auth?c= deep link
                    if (uri.scheme == "vbhelper" && uri.host == "auth") {
                        handleTokenFromUri(uri)
                    }
                    // Handle http://localhost:8080/authenticate?c= redirect
                    else if ((uri.scheme == "http" || uri.scheme == "https") && 
                             (uri.host == "localhost" || uri.host == "127.0.0.1" || uri.host?.contains("8080") == true)) {
                        handleTokenFromUri(uri)
                    }
                    // Also check if there's a 'c' or 'token' parameter in any URL
                    else if (uri.getQueryParameter("c") != null || uri.getQueryParameter("token") != null) {
                        handleTokenFromUri(uri)
                    }
                }
            }
        }
    }
    
    // Initialize sprite files on first load - check that they exist in external storage
    // Only check if permission is granted
    LaunchedEffect(hasStoragePermission) {
        if (hasStoragePermission) {
            val spriteFileManager = SpriteFileManager(context)
            if (spriteFileManager.checkSpriteFilesExist()) {
            } else {
                println("BATTLESCREEN: Sprite files not found in external storage")
            }
        } else {
            println("BATTLESCREEN: Cannot check sprite files - storage permission not granted")
        }
    }
    
    // Load active character from database
    LaunchedEffect(Unit) {
        try {
            val application = context.applicationContext as VBHelper
            val database = application.container.db
            
            // Move database operations to background thread
            kotlinx.coroutines.withContext(Dispatchers.IO) {
                // First, let's check all characters to see what's in the database
                val allCharacters = database.userCharacterDao().getAllCharacters()
                /*
                println("BATTLESCREEN: Found ${allCharacters.size} total characters in database")
                allCharacters.forEach { char ->
                    println("  - Character ID: ${char.id}, CharId: ${char.charId}")
                }
                 */
                
                val activeChar = database.userCharacterDao().getActiveCharacter().first()
                //println("BATTLESCREEN: getActiveCharacter() returned: $activeChar")
                
                if (activeChar != null) {
                    // Get the character data using the charId from activeChar
                    val characterData = database.characterDao().getCharacterInfo(activeChar.charId)
                    /*
                    println("BATTLESCREEN: CharacterData from getCharacterInfo:")
                    println("  - cardId: ${characterData.cardId}")
                    println("  - charId: ${characterData.charId}")
                    println("  - stage: ${characterData.stage}")
                    println("  - attribute: ${characterData.attribute}")
                    */
                    
                    // The cardId from getCharacterInfo is already the correct card ID we need!
                    val cardId = characterData.cardId
                    val charaIndex = characterData.charId // This is the charaIndex from the query
                    
                    // Format as "dim" + cardId + "_mon" + (charaIndex + 1)
                    val formattedCardId = String.format("dim%03d_mon%02d", cardId, charaIndex + 1)
                    
                    // Create APIBattleCharacter from database character
                    val playerCharacter = APIBattleCharacter(
                        name = "Player Digimon", // We could get this from the database if needed
                        namekey = "player_digimon", // Name key for the character
                        charaId = formattedCardId, // Use the formatted card ID for sprite loading
                        stage = characterData.stage,
                        attribute = characterData.attribute.ordinal, // Convert enum to int
                        baseHp = 1000, // Default values - API will provide correct values
                        currentHp = 1000,
                        baseBp = 1000.0f,
                        baseAp = 1000.0f
                    )
                    
                    // Update UI state on main thread
                    withContext(Dispatchers.Main) {
                        activeUserCharacter = activeChar
                        activeCardId = formattedCardId
                        activeCharacter = playerCharacter // Set the active character for battle
                    }

                    /*
                    println("BATTLESCREEN: Loaded active character from database:")
                    println("  - UserCharacter ID: ${activeChar.id}")
                    println("  - CharId: ${activeChar.charId}")
                    println("  - Stage: ${activeChar.stage}")
                    println("  - CharacterData cardId: ${characterData.cardId}")
                    println("  - CharacterData charaIndex: $charaIndex")
                    println("  - Final cardId: $cardId")
                    println("  - Formatted as: $activeCardId")
                    println("  - Can battle: ${activeChar.stage >= 2}")
                    println("  - Battle type: ${when (activeChar.stage) { 2 -> "rookie"; 3 -> "champion"; 4 -> "ultimate"; 5 -> "mega"; else -> "none" }}")
                     */
                } else {
                    println("BATTLESCREEN: No active character found in database")
                    withContext(Dispatchers.Main) {
                        activeCardId = null
                    }
                }
            }
        } catch (e: Exception) {
            println("BATTLESCREEN: Error loading active character: ${e.message}")
            e.printStackTrace()
        }
    }

    val backButton = @Composable {
                Button(
            onClick = { currentView = "main" }
        ) {
            Text("Back")
        }
    }

    Scaffold (
        topBar = {
            // Only show TopBanner when not in battle mode
            if (currentView != "battle-main" && currentView != "battle-results") {
            TopBanner(
                text = "Online Battles"
            )
            }
        }
    ) { contentPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            when (currentView) {
                "main" -> {
                    // Show loading/authentication message if not authenticated
                    if (isCheckingAuth || !isAuthenticated) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp)
                        ) {
                            if (isCheckingAuth) {
                                Text(
                                    text = "Checking authentication...",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "Please complete authentication in your browser",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                Text(
                                    text = "You will be redirected back to the app after logging in",
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Show active character info
                            activeUserCharacter?.let { character ->
                                Text("Active Digimon:")
                                Text("Stage: ${character.stage}")
                                activeCardId?.let { cardId ->
                                    Text("Digimon ID: $cardId", fontSize = 14.sp, color = Color.Blue, fontWeight = FontWeight.Bold)
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                if (canBattle) {
                                    Text("Available Opponents:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    //Text("Debug: opponentsList.size = ${opponentsList.size}", fontSize = 12.sp, color = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    if (opponentsList.isNotEmpty()) {
                                        // Show scrollable list of opponents
                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .padding(horizontal = 16.dp),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            items(opponentsList) { opponent ->
                                Button(
                                    onClick = {
                                                        activeCardId?.let { cardId ->
                                            selectedOpponent = opponent
                                                            // Randomly select background set (0, 1, or 2)
                                                            selectedBackgroundSet = kotlin.random.Random.nextInt(3)
                                                            
                                                            // Determine the correct stage parameter for API call
                                                            val apiStage = when (playerBattleType) {
                                                                "rookie" -> 0
                                                                "champion" -> 1
                                                                "ultimate" -> 2
                                                                "mega" -> 3
                                                                else -> 0
                                                            }
                                                            
                                                            RetrofitHelper().getPVPWinner(context, 0, userId ?: 2L, cardId, apiStage, 0, opponent.charaId, apiStage) { apiResult ->
                                                                // Check if there's an existing match
                                                                when {
                                                                    apiResult.status.contains("Existing match found", ignoreCase = true) -> {
                                                                        // Show resume/quit dialog
                                                                        // When resuming, we need to find the actual opponent from the match
                                                                        // For now, we'll use the clicked opponent, but we need to look it up from opponentsList
                                                                        // The server should return the opponent charaId in the response, but since it doesn't,
                                                                        // we'll try to find it by matching the opponent HP or look it up after rejoin
                                                                        existingMatchState = apiResult
                                                                        pendingOpponentForResume = opponent
                                                                        pendingCardIdForResume = cardId
                                                                        pendingApiStageForResume = apiStage
                                                                        // Store the clicked opponent temporarily, but we'll update it when resuming
                                                                        originalMatchOpponent = null // Will be set when we rejoin
                                                                        showResumeDialog = true
                                                                    }
                                                                    apiResult.status == "Match setup." -> {
                                                                        // New match created - proceed normally
                                                                        activeCharacter = activeCharacter?.copy(
                                                                            baseHp = apiResult.playerHP,
                                                                            currentHp = apiResult.playerHP
                                                                        )
                                                                        currentView = "battle-main"
                                                                    }
                                                                    apiResult.status == "Match resumed." -> {
                                                                        // Match was resumed (shouldn't happen on first call, but handle it)
                                                                        activeCharacter = activeCharacter?.copy(
                                                                            baseHp = apiResult.playerHP,
                                                                            currentHp = apiResult.playerHP
                                                                        )
                                                                        selectedOpponent = opponent.copy(
                                                                            baseHp = apiResult.opponentHP,
                                                                            currentHp = apiResult.opponentHP
                                                                        )
                                                currentView = "battle-main"
                                            }
                                                                    else -> {
                                                                        // Other status - log and proceed
                                                                        println("BATTLESCREEN: Unexpected status: ${apiResult.status}")
                                                                        activeCharacter = activeCharacter?.copy(
                                                                            baseHp = apiResult.playerHP,
                                                                            currentHp = apiResult.playerHP
                                                                        )
                                                                        currentView = "battle-main"
                                                                    }
                                                                }
                                                            }
                                                        } ?: run {
                                                            println("BATTLESCREEN: No active card ID found in database")
                                                        }
                                                    },
                                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Battle ${opponent.displayName?.takeIf { it.isNotBlank() } ?: opponent.name}")
                                }
                            }
                                        }
                                    } else {
                                        Text("No opponents available for your stage", 
                                             fontSize = 16.sp, 
                                             color = Color(0xFFFFA500), // Orange color
                                             textAlign = TextAlign.Center)
                                    }
                                } else {
                                    Text("Your Digimon must be at least Stage 2 to battle", 
                                         fontSize = 16.sp, 
                                         color = Color.Red,
                                         textAlign = TextAlign.Center)
                                }
                            } ?: run {
                                Text("No active character found in database", fontSize = 16.sp, color = Color.Red)
                            }
                        }
                    }
                }


                "battle-main" -> {
                    BattleScreen(
                        userId = userId,
                        stage = currentStage,
                        playerName = activeCharacter?.name ?: "Player",
                        opponentName = selectedOpponent?.name ?: "Opponent",
                        activeCharacter = activeCharacter,
                        opponentCharacter = selectedOpponent,
                        onAttackClick = {
                            // This will be called when the battle is over
                            currentView = "battle-results"
                        },
                        context = context,
                        selectedBackgroundSet = selectedBackgroundSet
                    )
                }

                "battle-results" -> {
                    var winnerName by remember { mutableStateOf("") }
                    var isWinnerLoaded by remember { mutableStateOf(false) }
                    
                    LaunchedEffect(Unit) {
                        // Determine player and opponent stages
                        val playerStage = when (activeCharacter?.stage) {
                            0 -> 0 // rookie
                            1 -> 1 // champion
                            2 -> 2 // ultimate
                            3 -> 3 // mega
                            else -> 0
                        }
                        
                        val opponentStage = when (selectedOpponent?.stage) {
                            0 -> 0 // rookie
                            1 -> 1 // champion
                            2 -> 2 // ultimate
                            3 -> 3 // mega
                            else -> 0
                        }
                        
                        // First get the winner info
                        RetrofitHelper().getPVPWinner(
                            context, 
                            1, 
                            userId ?: 2L, 
                            activeCharacter?.name ?: "Player", 
                            playerStage, 
                            opponentStage, 
                            selectedOpponent?.name ?: "Opponent", 
                            opponentStage
                        ) { apiResult ->
                            // Winner might be empty in first call, but we can check HP values
                            // If opponentHP is negative, player won. If playerHP is negative or 0, player lost.
                            val playerWonFromHP = apiResult.opponentHP <= 0 && apiResult.playerHP > 0
                            
                            // Also check winner field if it's not empty
                            val playerWonFromWinner = activeCardId?.let { cardId ->
                                val winner = apiResult.winner ?: ""
                                if (winner.isNotEmpty()) {
                                    if (winner.contains("|")) {
                                        // Pipe-separated format: first part is the winner
                                        val winnerParts = winner.split("|")
                                        val winnerCharaId = winnerParts.getOrNull(0) ?: ""
                                        winnerCharaId.contains(cardId, ignoreCase = true)
                                    } else {
                                        // Simple format: check if winner contains player's charaId or matches player name
                                        winner.contains(cardId, ignoreCase = true) ||
                                        winner.equals(activeCharacter?.name, ignoreCase = true)
                                    }
                                } else {
                                    false
                                }
                            } ?: false
                            
                            // Use HP-based determination if winner field is empty, otherwise use winner field
                            val playerWon = if (apiResult.winner.isNullOrEmpty()) {
                                playerWonFromHP
                            } else {
                                playerWonFromWinner
                            }
                            
                            println("BATTLESCREEN: Battle result (first call) - winner: '${apiResult.winner}', playerHP: ${apiResult.playerHP}, opponentHP: ${apiResult.opponentHP}, playerWonFromHP: $playerWonFromHP, playerWonFromWinner: $playerWonFromWinner, final playerWon: $playerWon")
                            
                            // Store winner name for display (will be updated in cleanup call if available)
                            winnerName = apiResult.winner ?: if (playerWon) "You" else "Opponent"
                            isWinnerLoaded = true
                            
                            // Then send the cleanup call - this will have the actual winner name
                            RetrofitHelper().getPVPWinner(
                                context, 
                                2, 
                                userId ?: 2L, 
                                activeCharacter?.name ?: "Player", 
                                playerStage, 
                                opponentStage, 
                                selectedOpponent?.name ?: "Opponent", 
                                opponentStage
                            ) { cleanupResult ->
                                // Update winner name from cleanup call if available
                                if (cleanupResult.winner.isNotEmpty()) {
                                    winnerName = cleanupResult.winner
                                }
                                
                                // Determine final winner from cleanup call (most reliable)
                                // Primary method: Check HP values (opponentHP <= 0 means opponent lost = player won)
                                // Secondary method: Check winner name (if winner doesn't match opponent, player won)
                                val opponentName = selectedOpponent?.name ?: ""
                                val winner = cleanupResult.winner ?: ""
                                
                                // Primary: HP-based determination (most reliable)
                                // If opponentHP <= 0, opponent is dead = player won
                                // If playerHP <= 0, player is dead = player lost
                                val playerWonFromHP = cleanupResult.opponentHP <= 0 && cleanupResult.playerHP > 0
                                
                                // Secondary: Winner name-based determination (only if HP check is inconclusive)
                                val playerWonFromName = if (winner.isNotEmpty() && opponentName.isNotEmpty()) {
                                    // If winner matches opponent name, player lost. Otherwise, player won.
                                    !winner.equals(opponentName, ignoreCase = true)
                                } else if (winner.isNotEmpty()) {
                                    // Check if winner matches player's charaId
                                    activeCardId?.let { cardId ->
                                        if (winner.contains("|")) {
                                            val winnerParts = winner.split("|")
                                            val winnerCharaId = winnerParts.getOrNull(0) ?: ""
                                            winnerCharaId.contains(cardId, ignoreCase = true)
                                        } else {
                                            winner.contains(cardId, ignoreCase = true)
                                        }
                                    } ?: false
                                } else {
                                    false
                                }
                                
                                // Use HP as primary (most reliable), name as fallback only if HP values are both positive
                                val finalPlayerWon = if (cleanupResult.opponentHP <= 0 || cleanupResult.playerHP <= 0) {
                                    // HP clearly indicates winner
                                    playerWonFromHP
                                } else {
                                    // Both have HP (shouldn't happen in cleanup, but use name as fallback)
                                    playerWonFromName
                                }
                                
                                println("BATTLESCREEN: Battle result (cleanup call) - winner: '${cleanupResult.winner}', playerHP: ${cleanupResult.playerHP}, opponentHP: ${cleanupResult.opponentHP}, finalPlayerWon: $finalPlayerWon")
                                
                                // Update battle stats in database using the most reliable determination
                                kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val application = context.applicationContext as VBHelper
                                        val database = application.container.db
                                        val activeChar = database.userCharacterDao().getActiveCharacter().first()
                                        
                                        if (activeChar != null) {
                                            // Get the full UserCharacter entity to update
                                            val userCharacter = database.userCharacterDao().getCharacter(activeChar.id)
                                            
                                            // Update battle stats
                                            if (finalPlayerWon) {
                                                userCharacter.currentPhaseBattlesWon += 1
                                                userCharacter.totalBattlesWon += 1
                                                println("BATTLESCREEN: Player won - updated wins: currentPhase=${userCharacter.currentPhaseBattlesWon}, total=${userCharacter.totalBattlesWon}")
                                            } else {
                                                userCharacter.currentPhaseBattlesLost += 1
                                                userCharacter.totalBattlesLost += 1
                                                println("BATTLESCREEN: Player lost - updated losses: currentPhase=${userCharacter.currentPhaseBattlesLost}, total=${userCharacter.totalBattlesLost}")
                                            }
                                            
                                            // Save updated character to database
                                            database.userCharacterDao().updateCharacter(userCharacter)
                                            println("BATTLESCREEN: Updated battle stats in database")
                                        } else {
                                            println("BATTLESCREEN: WARNING: Could not find active character to update battle stats")
                                        }
                                    } catch (e: Exception) {
                                        println("BATTLESCREEN: Error updating battle stats: ${e.message}")
                                        e.printStackTrace()
                                    }
                                }
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Battle Complete!",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            if (isWinnerLoaded) {
                                Text(
                                    text = "Winner: $winnerName",
                                    fontSize = 20.sp,
                                    color = Color.Gray
                                )
                            } else {
                                Text(
                                    text = "Loading results...",
                                    fontSize = 20.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        
                        // Exit button - stop music before exiting
                        Button(
                            onClick = { 
                                // Stop background music before exiting
                                // Note: Music will also be stopped by DisposableEffect in BattleScreen
                                currentView = "main" 
                            },
                            modifier = Modifier.align(Alignment.TopCenter),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Exit", color = Color.White)
                        }
                    }
                }
            }
        }
        
        // Resume/Quit Match Dialog
        if (showResumeDialog && existingMatchState != null) {
            ResumeMatchDialog(
                matchState = existingMatchState!!,
                onResume = {
                    // User chose to resume - call API with action="rejoin"
                    // Note: We need to pass the opponent, but the server should use the one from the stored match
                    // After rejoin, we need to find the actual opponent from the opponents list
                    pendingCardIdForResume?.let { cardId ->
                        pendingOpponentForResume?.let { clickedOpponent ->
                            pendingApiStageForResume?.let { apiStage ->
                                RetrofitHelper().getPVPWinner(
                                    context, 
                                    0, 
                                    userId ?: 2L, 
                                    cardId, 
                                    apiStage, 
                                    0, 
                                    clickedOpponent.charaId, 
                                    apiStage,
                                    "rejoin"
                                ) { apiResult ->
                                    println("BATTLESCREEN: Resuming match - opponentHP from API: ${apiResult.opponentHP}")
                                    println("BATTLESCREEN: Clicked opponent: ${clickedOpponent.name} (${clickedOpponent.charaId}), baseHp: ${clickedOpponent.baseHp}")
                                    
                                    // Update player character HP from API response
                                    // Use playerMaxHP from API if available, otherwise use current HP as fallback
                                    // NOTE: Server should provide playerMaxHP for resumed matches since DB stats use different scaling
                                    val playerMaxHp = apiResult.playerMaxHP ?: apiResult.playerHP
                                    println("BATTLESCREEN: Resuming match - playerMaxHP from API: ${apiResult.playerMaxHP}, using: $playerMaxHp")
                                    
                                    activeCharacter = activeCharacter?.copy(
                                        baseHp = playerMaxHp, // Use max HP from API (or current HP as fallback)
                                        currentHp = apiResult.playerHP // Current HP from API
                                    )
                                    
                                    // Find the actual opponent from the match
                                    println("BATTLESCREEN: Checking for opponent charaId - opponentCharaId: ${apiResult.opponentCharaId}, winner: '${apiResult.winner}'")
                                    val actualOpponent = if (apiResult.opponentCharaId != null) {
                                        // Server provides opponent charaId - use it directly (most reliable)
                                        println("BATTLESCREEN: Server provided opponent charaId: ${apiResult.opponentCharaId}")
                                        opponentsList.find { it.charaId == apiResult.opponentCharaId } ?: run {
                                            println("BATTLESCREEN: WARNING: Opponent charaId from server not found in opponentsList, using clicked opponent")
                                            clickedOpponent
                                        }
                                    } else if (apiResult.winner.isNotEmpty() && apiResult.winner.contains("|")) {
                                        println("BATTLESCREEN: Winner field contains pipe, attempting to parse: '${apiResult.winner}'")
                                        // Try to extract opponent charaId from winner field
                                        // Format appears to be: "playerDigi|playerStage|opponentDigi|opponentStage"
                                        try {
                                            val parts = apiResult.winner.split("|")
                                            if (parts.size >= 3) {
                                                val extractedOpponentCharaId = parts[2] // Third part is opponentDigi
                                                println("BATTLESCREEN: Extracted opponent charaId from winner field: $extractedOpponentCharaId")
                                                opponentsList.find { it.charaId == extractedOpponentCharaId } ?: run {
                                                    println("BATTLESCREEN: WARNING: Extracted opponent charaId not found in opponentsList, using clicked opponent")
                                                    clickedOpponent
                                                }
                                            } else {
                                                println("BATTLESCREEN: Winner field format unexpected, falling back to HP matching")
                                                null // Will fall through to HP matching
                                            }
                                        } catch (e: Exception) {
                                            println("BATTLESCREEN: Error parsing winner field: ${e.message}")
                                            null // Will fall through to HP matching
                                        }
                                    } else {
                                        println("BATTLESCREEN: Winner field is empty or doesn't contain pipe, winner='${apiResult.winner}'")
                                        null // Will fall through to HP matching
                                    } ?: run {
                                        // Fallback: Try to match by HP - but prioritize the clicked opponent if it matches
                                        println("BATTLESCREEN: All opponent identification methods failed, using HP matching fallback")
                                        run {
                                            // First, check if the clicked opponent matches the HP criteria
                                            // This is the most likely match since the user clicked on it
                                            val clickedOpponentMatches = run {
                                                val hpDiff = clickedOpponent.baseHp - apiResult.opponentHP
                                                hpDiff >= 0 && hpDiff <= (clickedOpponent.baseHp * 0.5)
                                            }
                                            
                                            if (clickedOpponentMatches) {
                                                println("BATTLESCREEN: Clicked opponent matches HP criteria, using it")
                                                clickedOpponent
                                            } else {
                                                // If clicked opponent doesn't match, search for others
                                                println("BATTLESCREEN: Clicked opponent doesn't match HP, searching for match")
                                                opponentsList.filter { opp ->
                                                    // Only consider opponents of the same stage
                                                    opp.stage == clickedOpponent.stage
                                                }.find { opp ->
                                                    // Match by checking if the opponent's baseHp is >= the current opponentHP
                                                    // and the difference is reasonable (opponent has taken some damage but not too much)
                                                    val hpDiff = opp.baseHp - apiResult.opponentHP
                                                    hpDiff >= 0 && hpDiff <= (opp.baseHp * 0.5) // Allow up to 50% damage
                                                } ?: run {
                                                    // If we can't find a match, try a broader search
                                                    println("BATTLESCREEN: Could not find opponent by HP matching, trying broader search")
                                                    opponentsList.find { opp ->
                                                        opp.stage == clickedOpponent.stage && 
                                                        opp.baseHp >= apiResult.opponentHP
                                                    } ?: run {
                                                        println("BATTLESCREEN: Still no match, using clicked opponent as fallback")
                                                        clickedOpponent
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    
                                    println("BATTLESCREEN: Selected opponent for resume: ${actualOpponent.name} (${actualOpponent.charaId}), baseHp: ${actualOpponent.baseHp}, currentHp: ${apiResult.opponentHP}")
                                    
                                    // Update opponent with correct HP from match
                                    // Use opponentMaxHP from API if available, otherwise use opponent's baseHp from opponentsList
                                    // NOTE: Server should provide opponentMaxHP for resumed matches since DB stats use different scaling
                                    val opponentMaxHp = apiResult.opponentMaxHP ?: actualOpponent.baseHp
                                    println("BATTLESCREEN: Resuming match - opponentMaxHP from API: ${apiResult.opponentMaxHP}, using: $opponentMaxHp")
                                    
                                    selectedOpponent = actualOpponent.copy(
                                        baseHp = opponentMaxHp, // Use max HP from API (or opponent's baseHp as fallback)
                                        currentHp = apiResult.opponentHP // Use current HP from match
                                    )
                                    
                                    showResumeDialog = false
                                    existingMatchState = null
                                    originalMatchOpponent = selectedOpponent
                                    currentView = "battle-main"
                                }
                            }
                        }
                    }
                },
                onQuit = {
                    // User chose to quit - call API with action="quit"
                    pendingCardIdForResume?.let { cardId ->
                        pendingOpponentForResume?.let { opponent ->
                            pendingApiStageForResume?.let { apiStage ->
                                RetrofitHelper().getPVPWinner(
                                    context, 
                                    0, 
                                    userId ?: 2L, 
                                    cardId, 
                                    apiStage, 
                                    0, 
                                    opponent.charaId, 
                                    apiStage,
                                    "quit"
                                ) { apiResult ->
                                    // New match created - proceed normally
                                    activeCharacter = activeCharacter?.copy(
                                        baseHp = apiResult.playerHP,
                                        currentHp = apiResult.playerHP
                                    )
                                    showResumeDialog = false
                                    existingMatchState = null
                                    currentView = "battle-main"
                                }
                            }
                        }
                    }
                },
                onDismiss = {
                    showResumeDialog = false
                    existingMatchState = null
                    pendingOpponentForResume = null
                    pendingCardIdForResume = null
                    pendingApiStageForResume = null
                }
            )
        }
    }
} 

@Composable
fun ResumeMatchDialog(
    matchState: com.github.nacabaro.vbhelper.battle.PVPDataModel,
    onResume: () -> Unit,
    onQuit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Ongoing Match Found", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text("You have an ongoing match:")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Round: ${matchState.currentRound + 1}", fontWeight = FontWeight.Bold)
                Text("Your HP: ${matchState.playerHP}")
                Text("Opponent HP: ${matchState.opponentHP}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("What would you like to do?")
            }
        },
        confirmButton = {
            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Resume Match")
            }
        },
        dismissButton = {
            Button(
                onClick = onQuit,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("Quit & Start New")
            }
        }
    )
}

@Composable
fun AnimatedBattleBackground(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var backgroundBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var xOffset by remember { mutableStateOf(0f) }
    var screenWidth by remember { mutableStateOf(0.dp) }
    var screenHeight by remember { mutableStateOf(0.dp) }

    // Get screen dimensions
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    LaunchedEffect(Unit) {
        screenWidth = with(density) { configuration.screenWidthDp.dp }
        screenHeight = with(density) { configuration.screenHeightDp.dp }
        println("DEBUG: Screen dimensions = ${screenWidth.value}x${screenHeight.value}dp")
    }

    // Load background image from external storage
    LaunchedEffect(Unit) {
        try {
            val externalDir = android.os.Environment.getExternalStorageDirectory()
            val backgroundFile = File(externalDir, "VBHelper/battle_sprites/extracted_battlebgs/BattleBg_0015_BattleBg_0012.png")
            if (backgroundFile.exists()) {
                backgroundBitmap = BitmapFactory.decodeFile(backgroundFile.absolutePath)
            } else {
                println("Battle background file not found: ${backgroundFile.absolutePath}")
            }
        } catch (e: Exception) {
            println("Error loading battle background: ${e.message}")
        }
    }

    // Animate horizontal movement to the left with perfect loop
    LaunchedEffect(screenWidth) {
        if (screenWidth > 0.dp) {
            while (true) {
                delay(50) // Update every 50ms for smooth animation
                xOffset -= 1f // Move 1 pixel to the left
                
                // Create perfect loop by resetting when one full screen width has moved
                if (xOffset <= -screenWidth.value) {
                    xOffset = 0f
                }
            }
        }
    }
    
    backgroundBitmap?.let { bitmap ->
        Box(modifier = modifier.fillMaxSize()) {
            // Calculate how many times to repeat the image to fill the screen width
            val configuration = LocalConfiguration.current
            val isLandscapeMode = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            val imageWidth = if (isLandscapeMode) screenWidth.value * 1.5f else screenWidth.value
            val repeatCount = (imageWidth / screenWidth.value).toInt() + 2 // Add 2 for seamless looping
            
            repeat(repeatCount) { index ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Animated Battle Background ${index + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (xOffset + (index * screenWidth.value)).dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
}

// Data class to define background sets
data class BackgroundSet(
    val backLayer: String,
    val middleLayer: String,
    val frontLayer: String
)

// Define the three background sets
val backgroundSets = listOf(
    BackgroundSet(
        backLayer = "BattleBg_0018_BattleBg_0013.png",
        middleLayer = "BattleBg_0015_BattleBg_0012.png",
        frontLayer = "BattleBg_0005_BattleBg_0011.png"
    ),
    BackgroundSet(
        backLayer = "BattleBg_0014_BattleBg_0013.png",
        middleLayer = "BattleBg_0010_BattleBg_0012.png",
        frontLayer = "BattleBg_0011_BattleBg_0011.png"
    ),
    BackgroundSet(
        backLayer = "BattleBg_0019_BattleBg_0013.png",
        middleLayer = "BattleBg_0004_BattleBg_0012.png",
        frontLayer = "BattleBg_0009_BattleBg_0011.png"
    )
)

@Composable
fun MultiLayerAnimatedBattleBackground(
    modifier: Modifier = Modifier,
    backgroundSetIndex: Int = 0
) {
    val context = LocalContext.current
    var backLayerBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var middleLayerBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var frontLayerBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    var backLayerXOffset by remember { mutableStateOf(0f) }
    var middleLayerXOffset by remember { mutableStateOf(0f) }
    var frontLayerXOffset by remember { mutableStateOf(0f) }
    
    var screenWidth by remember { mutableStateOf(0.dp) }
    var screenHeight by remember { mutableStateOf(0.dp) }

    // Get screen dimensions
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    LaunchedEffect(Unit) {
        screenWidth = with(density) { configuration.screenWidthDp.dp }
        screenHeight = with(density) { configuration.screenHeightDp.dp }
    }

    // Load all three background layers from external storage
    LaunchedEffect(backgroundSetIndex) {
        try {
            val externalDir = android.os.Environment.getExternalStorageDirectory()
            val selectedSet = backgroundSets[backgroundSetIndex]
            
            // Back layer
            val backLayerFile = File(externalDir, "VBHelper/battle_sprites/extracted_battlebgs/${selectedSet.backLayer}")
            if (backLayerFile.exists()) {
                backLayerBitmap = BitmapFactory.decodeFile(backLayerFile.absolutePath)
            } else {
                println("Back layer background file not found: ${backLayerFile.absolutePath}")
            }
            
            // Middle layer
            val middleLayerFile = File(externalDir, "VBHelper/battle_sprites/extracted_battlebgs/${selectedSet.middleLayer}")
            if (middleLayerFile.exists()) {
                middleLayerBitmap = BitmapFactory.decodeFile(middleLayerFile.absolutePath)
            } else {
                println("Middle layer background file not found: ${middleLayerFile.absolutePath}")
            }
            
            // Front layer
            val frontLayerFile = File(externalDir, "VBHelper/battle_sprites/extracted_battlebgs/${selectedSet.frontLayer}")
            if (frontLayerFile.exists()) {
                frontLayerBitmap = BitmapFactory.decodeFile(frontLayerFile.absolutePath)
            } else {
                println("Front layer background file not found: ${frontLayerFile.absolutePath}")
            }
        } catch (e: Exception) {
            println("Error loading multi-layer battle backgrounds: ${e.message}")
        }
    }

    // Animate all three layers with different speeds (slower in landscape mode)
    val bgConfiguration = LocalConfiguration.current
    val isLandscapeMode = bgConfiguration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    LaunchedEffect(screenWidth) {
        if (screenWidth > 0.dp) {
            while (true) {
                delay(50) // Update every 50ms for smooth animation
                
                // Adjust speed based on orientation
                val speedMultiplier = if (isLandscapeMode) 0.5f else 1f
                
                // Back layer moves slowest (parallax effect)
                backLayerXOffset -= 0.5f * speedMultiplier
                if (backLayerXOffset <= -screenWidth.value) {
                    backLayerXOffset = 0f
                }
                
                // Middle layer moves at medium speed
                middleLayerXOffset -= 1f * speedMultiplier
                if (middleLayerXOffset <= -screenWidth.value) {
                    middleLayerXOffset = 0f
                }
                
                // Front layer moves fastest
                frontLayerXOffset -= 1.5f * speedMultiplier
                if (frontLayerXOffset <= -screenWidth.value) {
                    frontLayerXOffset = 0f
                }
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Calculate how many times to repeat the image to fill the screen width
        val imageWidth = if (isLandscapeMode) screenWidth.value * 1.5f else screenWidth.value
        val repeatCount = (imageWidth / screenWidth.value).toInt() + 2 // Add 2 for seamless looping
        
        // Back layer (underneath everything)
        backLayerBitmap?.let { bitmap ->
            repeat(repeatCount) { index ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Back Layer Battle Background ${index + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (backLayerXOffset + (index * screenWidth.value)).dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
        
        // Middle layer
        middleLayerBitmap?.let { bitmap ->
            repeat(repeatCount) { index ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Middle Layer Battle Background ${index + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (middleLayerXOffset + (index * screenWidth.value)).dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
        
        // Front layer (on top of other backgrounds)
        frontLayerBitmap?.let { bitmap ->
            repeat(repeatCount) { index ->
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Front Layer Battle Background ${index + 1}",
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = (frontLayerXOffset + (index * screenWidth.value)).dp),
                    contentScale = ContentScale.FillBounds
                )
            }
        }
    }
} 