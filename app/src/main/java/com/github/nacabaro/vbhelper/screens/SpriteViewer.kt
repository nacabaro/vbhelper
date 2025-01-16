package com.github.nacabaro.vbhelper.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.Sprites
import com.github.nacabaro.vbhelper.source.SpriteRepo
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

@Composable
fun SpriteViewer(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val db = application.container.db
    val spriteRepo = SpriteRepo(db)

    val spriteList = remember { mutableStateListOf<Sprites>() }

    Log.d("SpriteViewer", "spriteList: $spriteList")

    LaunchedEffect(spriteRepo) {
        coroutineScope.launch {
            spriteList.clear()
            spriteList.addAll(spriteRepo.getAllSprites())
        }
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
                val bitmap = remember (sprite.sprite) {
                    Log.d("SpriteViewer", "sprite: $sprite")
                    Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.RGB_565).apply {
                        copyPixelsFromBuffer(ByteBuffer.wrap(sprite.sprite))
                    }
                }
                val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
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

