package com.github.nacabaro.vbhelper.screens.spriteViewer

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import com.github.nacabaro.vbhelper.di.VBHelper
import java.nio.ByteBuffer
import androidx.core.graphics.createBitmap
import com.github.nacabaro.vbhelper.domain.card.CardBackground

class SpriteViewerControllerImpl(
    private val context: ComponentActivity
) : SpriteViewerController {
    override suspend fun getAllSprites(): List<CardBackground> {
        val applicationContext = context.applicationContext as VBHelper
        val db = applicationContext.container.db
        val sprites = db.cardBackgroundDao().getBackgrounds()
        return sprites
    }

    // I don't like this, chief
    override fun convertToBitmap(sprites: List<CardBackground>): List<Bitmap> {
        val bitmapList = mutableListOf<Bitmap>()

        for (sprite in sprites) {
            bitmapList.add(createBitmap(sprite.backgroundWidth, sprite.backgroundHeight, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.background))
            })
        }

        return bitmapList
    }
}