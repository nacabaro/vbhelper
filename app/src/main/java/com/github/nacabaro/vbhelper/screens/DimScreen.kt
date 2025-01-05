package com.github.nacabaro.vbhelper.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.StorageEntry
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.domain.Character
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.navigation.BottomNavItem
import com.github.nacabaro.vbhelper.source.DexRepository
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

@Composable
fun DiMScreen(
    navController: NavController,
    dimId: Int
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val dexRepository = DexRepository(application.container.db)

    val characterList = remember { mutableStateListOf<Character>() }

    Log.d("dimId", dimId.toString())

    LaunchedEffect(dexRepository) {
        coroutineScope.launch {
            characterList.clear()
            characterList.addAll(dexRepository.getCharactersByDimId(dimId))
        }
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = "Discovered Digimon",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    ) { contentPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = contentPadding
        ) {
            items(characterList) { character ->
                val bitmapName = remember (character.name) {
                    Bitmap.createBitmap(character.nameWidth, character.nameHeight, Bitmap.Config.RGB_565).apply {
                        copyPixelsFromBuffer(ByteBuffer.wrap(character.name))
                    }
                }
                val bitmapCharacter = remember (character.sprite1) {
                    Bitmap.createBitmap(character.spritesWidth, character.spritesHeight, Bitmap.Config.RGB_565).apply {
                        copyPixelsFromBuffer(ByteBuffer.wrap(character.sprite1))
                    }
                }
                val imageBitmapName = remember(bitmapName) { bitmapName.asImageBitmap() }
                val imageBitmapCharacter = remember(bitmapCharacter) { bitmapCharacter.asImageBitmap() }
                CharacterEntry(
                    name = imageBitmapName,
                    icon = imageBitmapCharacter,
                    onClick = {  }
                )
            }
        }
    }
}