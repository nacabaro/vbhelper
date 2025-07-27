package com.github.nacabaro.vbhelper.screens.spriteViewer

import android.graphics.Bitmap
import com.github.nacabaro.vbhelper.domain.characters.Sprite

interface SpriteViewerController {
    suspend fun getAllSprites(): List<Sprite>
    fun convertToBitmap(sprites: List<Sprite>): List<Bitmap>
}