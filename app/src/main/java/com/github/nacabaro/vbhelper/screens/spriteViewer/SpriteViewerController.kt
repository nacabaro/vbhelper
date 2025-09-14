package com.github.nacabaro.vbhelper.screens.spriteViewer

import android.graphics.Bitmap
import com.github.nacabaro.vbhelper.domain.card.CardBackground

interface SpriteViewerController {
    suspend fun getAllSprites(): List<CardBackground>
    fun convertToBitmap(sprites: List<CardBackground>): List<Bitmap>
}