package com.github.nacabaro.vbhelper.screens.adventureScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.screens.itemsScreen.ObtainedItemDialog
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

@Composable
fun AdventureScreen(
    navController: NavController,
    storageScreenController: AdventureScreenControllerImpl
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val database = application.container.db
    val storageRepository = StorageRepository(database)
    val characterList = remember {
        mutableStateOf<List<CharacterDtos.AdventureCharacterWithSprites>>(emptyList())
    }
    var obtainedItem by remember {
        mutableStateOf<ItemDtos.PurchasedItem?>(null)
    }

    val currentTime by produceState(initialValue = Instant.now().epochSecond) {
        while (true) {
            value = Instant.now().epochSecond
            delay(1000)
        }
    }

    var cancelAdventureDialog by remember {
        mutableStateOf<CharacterDtos.AdventureCharacterWithSprites?>(null)
    }

    LaunchedEffect(storageRepository) {
        coroutineScope.launch {
            characterList.value = storageRepository
                .getAdventureCharacters()
        }
    }

    Scaffold(
        topBar = {
            TopBanner(
                text = "Adventure",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(characterList.value) {
                AdventureEntry(
                    icon = BitmapData(
                        bitmap = it.spriteIdle,
                        width = it.spriteWidth,
                        height = it.spriteHeight
                    ),
                    timeLeft = it.timeLeft - currentTime,
                    onClick = {
                        if (it.timeLeft < currentTime) {
                            storageScreenController
                                .getItemFromAdventure(it.id) { adventureResult ->
                                    obtainedItem = adventureResult
                                }
                        } else {
                            cancelAdventureDialog = it
                        }
                    }
                )
            }
        }
    }

    if (obtainedItem != null) {
        ObtainedItemDialog(
            obtainedItem = obtainedItem!!,
            onClickDismiss = {
                obtainedItem = null
            }
        )
    }

    if (cancelAdventureDialog != null) {
        CancelAdventureDialog(
            characterSprite = BitmapData(
                bitmap = cancelAdventureDialog!!.spriteIdle,
                width = cancelAdventureDialog!!.spriteWidth,
                height = cancelAdventureDialog!!.spriteHeight
            ),
            onDismissRequest = {
                cancelAdventureDialog = null
            },
            onClickConfirm = {
                storageScreenController.cancelAdventure(cancelAdventureDialog!!.id) {
                    navController.navigate(NavigationItems.Storage.route)
                }
                cancelAdventureDialog = null
            }
        )
    }
}
