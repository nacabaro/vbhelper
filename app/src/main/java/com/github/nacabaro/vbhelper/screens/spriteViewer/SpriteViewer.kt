package com.github.nacabaro.vbhelper.screens.spriteViewer

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.source.SpriteRepo
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

@Composable
fun SpriteViewer(
    navController: NavController,
    spriteViewerController: SpriteViewerController
) {
    val coroutineScope = rememberCoroutineScope()

    val spriteList = remember { mutableStateListOf<Bitmap>() }

    Log.d("SpriteViewer", "spriteList: $spriteList")

    LaunchedEffect(spriteViewerController) {
        val sprites = spriteViewerController.getAllSprites()
        val bitmapData = spriteViewerController.convertToBitmap(sprites)
        spriteList.addAll(bitmapData)
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = "Sprite viewer",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { contentPadding ->
        LazyColumn (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(spriteList) { sprite ->
                val imageBitmap = remember(sprite) { sprite.asImageBitmap() }
                Image(
                    bitmap = imageBitmap,
                    contentDescription = "Sprite",
                    modifier = Modifier
                        .size(256.dp)
                        .padding(8.dp)
                )
            }
        }
    }
}

