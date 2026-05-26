package com.github.nacabaro.vbhelper.battle

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State

class ArenaBattleSystem {
    companion object {
        private const val TAG = "ArenaBattleSystem"
    }

    // Attack phases: 0=Idle, 1=Player attack on player screen, 2=Player attack on opponent screen, 
    // 3=Opponent attack on opponent screen, 4=Opponent attack on player screen
    private var _attackPhase by mutableStateOf(0)
    val attackPhase: Int get() = _attackPhase

    private var _attackProgress by mutableStateOf(0f)
    val attackProgress: Float get() = _attackProgress

    private var _isPlayerAttacking by mutableStateOf(false)
    val isPlayerAttacking: Boolean get() = _isPlayerAttacking

    private var _attackIsHit by mutableStateOf(false)
    val attackIsHit: Boolean get() = _attackIsHit

    private var _isAttackButtonEnabled by mutableStateOf(true)
    val isAttackButtonEnabled: Boolean get() = _isAttackButtonEnabled

    private var _currentView by mutableStateOf(0)
    val currentView: Int get() = _currentView

    private var _playerHP by mutableStateOf(100f)
    val playerHP: Float get() = _playerHP

    private var _opponentHP by mutableStateOf(100f)
    val opponentHP: Float get() = _opponentHP

    private var _isBattleOver by mutableStateOf(false)
    val isBattleOver: Boolean get() = _isBattleOver

    private var _critBarProgress by mutableStateOf(0)
    val critBarProgress: Int get() = _critBarProgress

    // Dodge animation states
    private var _isDodging by mutableStateOf(false)
    val isDodging: Boolean get() = _isDodging

    private var _dodgeProgress by mutableStateOf(0f)
    val dodgeProgress: Float get() = _dodgeProgress

    private var _dodgeDirection by mutableStateOf(1f) // 1f = up, -1f = down
    val dodgeDirection: Float get() = _dodgeDirection

    private var _isHit by mutableStateOf(false)
    val isHit: Boolean get() = _isHit

    private var _hitProgress by mutableStateOf(0f)
    val hitProgress: Float get() = _hitProgress

    // Separate states for player and opponent
    private var _isPlayerDodging by mutableStateOf(false)
    val isPlayerDodging: Boolean get() = _isPlayerDodging

    private var _isOpponentDodging by mutableStateOf(false)
    val isOpponentDodging: Boolean get() = _isOpponentDodging

    // Separate dodge progress and direction for player and opponent
    private var _playerDodgeProgress by mutableStateOf(0f)
    val playerDodgeProgress: Float get() = _playerDodgeProgress

    private var _playerDodgeDirection by mutableStateOf(1f)
    val playerDodgeDirection: Float get() = _playerDodgeDirection

    private var _opponentDodgeProgress by mutableStateOf(0f)
    val opponentDodgeProgress: Float get() = _opponentDodgeProgress

    private var _opponentDodgeDirection by mutableStateOf(1f)
    val opponentDodgeDirection: Float get() = _opponentDodgeDirection

    private var _isPlayerHit by mutableStateOf(false)
    val isPlayerHit: Boolean get() = _isPlayerHit

    private var _isOpponentHit by mutableStateOf(false)
    val isOpponentHit: Boolean get() = _isOpponentHit

    // Delayed hit states for SLEEP animation timing
    private var _isPlayerHitDelayed by mutableStateOf(false)
    val isPlayerHitDelayed: Boolean get() = _isPlayerHitDelayed

    private var _isOpponentHitDelayed by mutableStateOf(false)
    val isOpponentHitDelayed: Boolean get() = _isOpponentHitDelayed

    // Delayed shake states for shake animation timing
    private var _isPlayerShakeDelayed by mutableStateOf(false)
    val isPlayerShakeDelayed: Boolean get() = _isPlayerShakeDelayed

    private var _isOpponentShakeDelayed by mutableStateOf(false)
    val isOpponentShakeDelayed: Boolean get() = _isOpponentShakeDelayed

    // Counter-attack tracking
    private var _shouldCounterAttack by mutableStateOf(false)
    val shouldCounterAttack: Boolean get() = _shouldCounterAttack

    private var _counterAttackIsHit by mutableStateOf(false)
    val counterAttackIsHit: Boolean get() = _counterAttackIsHit

    // Separate tracking for opponent attack result
    private var _opponentAttackIsHit by mutableStateOf(false)
    val opponentAttackIsHit: Boolean get() = _opponentAttackIsHit

    fun startPlayerAttack() {
        _attackPhase = 1
        _attackProgress = 0f
        _isPlayerAttacking = true
        _isAttackButtonEnabled = false
        _currentView = 0
    }

    fun startOpponentAttack() {
        _attackPhase = 3
        _attackProgress = 0f
        _isPlayerAttacking = false
        _currentView = 1
    }

    fun advanceAttackPhase() {
        _attackPhase++
        _attackProgress = 0f
    }

    fun setAttackProgress(progress: Float) {
        _attackProgress = progress
    }

    fun setAttackHitState(isHit: Boolean) {
        _attackIsHit = isHit
    }

    fun switchToView(view: Int) {
        _currentView = view
    }

    fun enableAttackButton() {
        _isAttackButtonEnabled = true
    }

    fun applyDamage(isPlayer: Boolean, damage: Float) {
        if (isPlayer) {
            _playerHP = (_playerHP - damage).coerceAtLeast(0f)
        } else {
            _opponentHP = (_opponentHP - damage).coerceAtLeast(0f)
        }
    }

    fun updateHPFromAPI(playerHP: Float, opponentHP: Float) {
        _playerHP = playerHP
        _opponentHP = opponentHP
    }

    fun initializeHP(playerHP: Float, opponentHP: Float) {
        _playerHP = playerHP
        _opponentHP = opponentHP
    }

    fun completeAttackAnimation(playerDamage: Float = 0f, opponentDamage: Float = 0f) {
        if (playerDamage > 0f) {
            applyDamage(true, playerDamage)
        }
        if (opponentDamage > 0f) {
            applyDamage(false, opponentDamage)
        }
    }

    fun resetAttackState() {
        _attackPhase = 0
        _attackProgress = 0f
        _isPlayerAttacking = false
        _attackIsHit = false
        _currentView = 0
        _isDodging = false
        _dodgeProgress = 0f
        _dodgeDirection = 1f
        _isHit = false
        _hitProgress = 0f
        _isPlayerDodging = false
        _isOpponentDodging = false
        _playerDodgeProgress = 0f
        _playerDodgeDirection = 1f
        _opponentDodgeProgress = 0f
        _opponentDodgeDirection = 1f
        _isPlayerHit = false
        _isOpponentHit = false
        _isPlayerHitDelayed = false
        _isOpponentHitDelayed = false
        _isPlayerShakeDelayed = false
        _isOpponentShakeDelayed = false
        _shouldCounterAttack = false
        _counterAttackIsHit = false
        _opponentAttackIsHit = false
    }

    fun checkBattleOver(): Boolean {
        return _playerHP <= 0f || _opponentHP <= 0f
    }

    fun endBattle() {
        _isBattleOver = true
    }

    fun updateCritBarProgress(progress: Int) {
        _critBarProgress = progress
        //Log.d(TAG, "Updated crit bar progress: $progress")
    }

    // Dodge animation methods
    fun startDodge() {
        _isDodging = true
        _dodgeProgress = 0f
        _dodgeDirection = 1f // Start moving up
    }

    fun setDodgeProgress(progress: Float) {
        _dodgeProgress = progress
    }

    fun setDodgeDirection(direction: Float) {
        _dodgeDirection = direction
    }

    fun endDodge() {
        _isDodging = false
        _dodgeProgress = 0f
    }

    // Hit animation methods
    fun startHit() {
        _isHit = true
        _hitProgress = 0f
    }

    fun setHitProgress(progress: Float) {
        _hitProgress = progress
    }

    fun endHit() {
        _isHit = false
        _hitProgress = 0f
    }

    // Player-specific dodge methods
    fun startPlayerDodge() {
        _isPlayerDodging = true
        _playerDodgeProgress = 0f
        _playerDodgeDirection = 1f
    }

    fun endPlayerDodge() {
        _isPlayerDodging = false
        _playerDodgeProgress = 0f
    }

    fun setPlayerDodgeProgress(progress: Float) {
        _playerDodgeProgress = progress
    }

    fun setPlayerDodgeDirection(direction: Float) {
        _playerDodgeDirection = direction
    }

    // Opponent-specific dodge methods
    fun startOpponentDodge() {
        _isOpponentDodging = true
        _opponentDodgeProgress = 0f
        _opponentDodgeDirection = 1f
    }

    fun endOpponentDodge() {
        _isOpponentDodging = false
        _opponentDodgeProgress = 0f
    }

    fun setOpponentDodgeProgress(progress: Float) {
        _opponentDodgeProgress = progress
    }

    fun setOpponentDodgeDirection(direction: Float) {
        _opponentDodgeDirection = direction
    }

    // Player-specific hit methods
    fun startPlayerHit() {
        _isPlayerHit = true
        _hitProgress = 0f
    }

    fun startPlayerHitDelayed() {
        _isPlayerHitDelayed = true
    }

    fun endPlayerHit() {
        _isPlayerHit = false
        _hitProgress = 0f
    }

    fun endPlayerHitDelayed() {
        _isPlayerHitDelayed = false
    }

    // Opponent-specific hit methods
    fun startOpponentHit() {
        _isOpponentHit = true
        _hitProgress = 0f
    }

    fun startOpponentHitDelayed() {
        _isOpponentHitDelayed = true
    }

    fun endOpponentHit() {
        _isOpponentHit = false
        _hitProgress = 0f
    }

    fun endOpponentHitDelayed() {
        _isOpponentHitDelayed = false
    }

    // Delayed shake methods
    fun startPlayerShakeDelayed() {
        _isPlayerShakeDelayed = true
    }

    fun endPlayerShakeDelayed() {
        _isPlayerShakeDelayed = false
    }

    fun startOpponentShakeDelayed() {
        _isOpponentShakeDelayed = true
    }

    fun endOpponentShakeDelayed() {
        _isOpponentShakeDelayed = false
    }

    // Combined method to handle attack result
    fun handleAttackResult(isHit: Boolean) {
        _attackIsHit = isHit
        if (isHit) {
            // Player attack hit - opponent gets hit
            startOpponentHit()
        } else {
            // Player attack missed - opponent dodges
            startOpponentDodge()
        }
    }

    // Method to handle opponent attack result
    fun handleOpponentAttackResult(isHit: Boolean) {
        _opponentAttackIsHit = isHit
        if (isHit) {
            // Opponent attack hit - player gets hit
            startPlayerHit()
        } else {
            // Opponent attack missed - player dodges
            startPlayerDodge()
        }
    }

    // Counter-attack methods
    fun setupCounterAttack(isHit: Boolean) {
        _shouldCounterAttack = true
        _counterAttackIsHit = isHit
    }

    fun startCounterAttack() {
        _attackPhase = 3
        _attackProgress = 0f
        _isPlayerAttacking = false
        _currentView = 1
        _opponentAttackIsHit = _counterAttackIsHit
    }
} 