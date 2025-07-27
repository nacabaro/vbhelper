package com.github.nacabaro.vbhelper.screens.spriteViewer

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import java.nio.ByteBuffer

class SpriteViewerControllerImpl(
    private val context: ComponentActivity
) : SpriteViewerController {
    override suspend fun getAllSprites(): List<Sprite> {
        val applicationContext = context.applicationContext as VBHelper
        val db = applicationContext.container.db
        val sprites = db.spriteDao().getAllSprites()
        return sprites
    }

    // I don't like this, chief
    override fun convertToBitmap(sprites: List<Sprite>): List<Bitmap> {
        val bitmapList = mutableListOf<Bitmap>()

        for (sprite in sprites) {
            Log.d("SpriteViewer", "sprite: $sprite")
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteIdle1))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteIdle2))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteWalk1))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteWalk2))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteRun1))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteRun2))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteTrain1))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteTrain2))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteHappy))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteSleep))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteAttack))
            })
            bitmapList.add(Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                copyPixelsFromBuffer(ByteBuffer.wrap(sprite.spriteDodge))
            })
        }

        return bitmapList
    }
}