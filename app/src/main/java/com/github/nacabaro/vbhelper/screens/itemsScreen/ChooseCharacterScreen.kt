package com.github.nacabaro.vbhelper.screens.itemsScreen

import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.items.ItemType
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import kotlinx.coroutines.launch


@Composable
fun ChooseCharacterScreen(
    navController: NavController,
    itemsScreenController: ItemsScreenControllerImpl,
    itemId: Long
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val characterList = remember {
        mutableStateOf<List<CharacterDtos.CharacterWithSprites>>(emptyList())
    }

    var selectedCharacter by remember { mutableStateOf<Long?>(null) }
    var selectedItem by remember { mutableStateOf<ItemDtos.ItemsWithQuantities?>(null) }

    LaunchedEffect(storageRepository) {
        coroutineScope.launch {
            selectedItem = storageRepository.getItem(itemId)
            when (selectedItem?.itemType) {
                ItemType.BEITEM -> {
                    characterList.value = storageRepository.getBEBEmCharacters()
                }
                ItemType.VBITEM -> {
                    characterList.value = storageRepository.getVBCharacters()
                }
                ItemType.SPECIALMISSION-> {
                    characterList.value = storageRepository.getVBCharacters()
                }
                else -> {
                    characterList.value = storageRepository.getAllCharacters()
                }
            }
        }
    }

    LaunchedEffect (selectedCharacter) {
        if (selectedCharacter != null) {
            itemsScreenController.applyItem(itemId, selectedCharacter!!) {
                Toast.makeText(
                    application.applicationContext,
                    "Item applied!",
                    Toast.LENGTH_SHORT
                ).show()
                navController.popBackStack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopBanner(
                text = "Choose character",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    ) { contentPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(characterList.value) {
                CharacterEntry(
                    icon = BitmapData(
                        bitmap = it.spriteIdle,
                        width = it.spriteWidth,
                        height = it.spriteHeight
                    )
                ) {
                    selectedCharacter = it.id
                }
            }
        }
    }
}