package com.github.nacabaro.vbhelper.battle

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.github.nacabaro.vbhelper.battle.AttackSpriteManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AttackSpriteImage(
    characterId: String,
    isLarge: Boolean = false,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    LaunchedEffect(characterId, isLarge) {
        //println("AttackSpriteImage: Loading attack sprite for characterId=$characterId, isLarge=$isLarge")
        coroutineScope.launch {
            val attackSpriteManager = AttackSpriteManager(context)
            val loadedBitmap = withContext(Dispatchers.IO) {
                attackSpriteManager.getAttackSprite(characterId, isLarge)
            }
            bitmap = loadedBitmap
        }
    }
    
    bitmap?.let { bmp ->
        Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Attack Sprite",
            modifier = modifier,
            contentScale = contentScale
        )
    }
} 