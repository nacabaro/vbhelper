package com.github.nacabaro.vbhelper.utils

import android.graphics.Bitmap
import com.github.cfogrady.vb.dim.sprite.SpriteData

// simple, but smooth
data class BitmapData (
    val bitmap: ByteArray,
    val width: Int,
    val height: Int
)

fun BitmapData.getBitmap(): Bitmap {
    return Bitmap.createBitmap(createARGBIntArray(), this.width, this.height, Bitmap.Config.HARDWARE)
}

fun BitmapData.createARGBIntArray(): IntArray {
    // hack to get it into correct format by relying on the DIM Sprites methods since we haven't changed the raw pixel data at this point.
    val bytes = SpriteData.Sprite.builder().width(this.width).height(this.height).pixelData(this.bitmap).build()
        .get24BitRGB()
    val result = IntArray(this.width*this.height)
    for(i in result.indices) {
        val originalIndex = i*3
        val red = bytes[originalIndex].toUInt() and 0xFFu
        val green = bytes[originalIndex+1].toUInt() and 0xFFu
        val blue = bytes[originalIndex+2].toUInt() and 0xFFu
        val alpha = if(red == 0u && blue == 0u && green == 0xFFu) 0 else 0xFF
        result[i] = (alpha shl 24) or (red shl 16).toInt() or (green shl 8).toInt() or blue.toInt()
    }
    return result
}

fun List<BitmapData>.getBitmaps(): List<Bitmap> {
    return this.map{it.getBitmap()}
}