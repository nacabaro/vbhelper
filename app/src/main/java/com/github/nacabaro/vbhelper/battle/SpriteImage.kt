package com.github.nacabaro.vbhelper.battle

import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext

@Composable
fun SpriteImage(
    characterId: String,
    frameNumber: Int,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val context = LocalContext.current
    val spriteManager = remember { IndividualSpriteManager(context) }
    
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    
    LaunchedEffect(characterId, frameNumber) {
        println("Loading sprite frame: $frameNumber for character: $characterId")
        bitmap = spriteManager.loadSpriteFrame(characterId, frameNumber)
        if (bitmap == null) {
            println("Failed to load sprite frame: $frameNumber for character: $characterId")
        }
    }
    
    bitmap?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Sprite: $characterId frame $frameNumber",
            modifier = modifier,
            contentScale = contentScale
        )
    }
}