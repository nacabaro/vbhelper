package com.github.nacabaro.vbhelper.screens

import android.util.Log
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.domain.Character
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.source.DexRepository
import kotlinx.coroutines.launch

@Composable
fun DiMScreen(
    navController: NavController,
    dimId: Int
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val dexRepository = DexRepository(application.container.db)

    val characterList = remember { mutableStateOf<List<Character>>(emptyList()) }

    LaunchedEffect(dexRepository) {
        coroutineScope.launch {
            val newCharacterList = dexRepository.getCharactersByDimId(dimId)
            characterList.value = newCharacterList
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
            items(characterList.value) { character ->
                CharacterEntry(
                    onClick = {  },
                    obscure = true,
                    icon = BitmapData(
                        bitmap = character.sprite1,
                        width = character.spritesWidth,
                        height = character.spritesHeight
                    ),
                )
            }
        }
    }
}