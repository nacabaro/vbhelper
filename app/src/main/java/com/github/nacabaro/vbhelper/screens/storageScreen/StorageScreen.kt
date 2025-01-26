package com.github.nacabaro.vbhelper.screens.storageScreen

import android.widget.Toast
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.screens.adventureScreen.AdventureScreenControllerImpl
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import kotlinx.coroutines.launch


@Composable
fun StorageScreen(
    navController: NavController,
    storageScreenController: StorageScreenControllerImpl,
    adventureScreenController: AdventureScreenControllerImpl
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val monList = remember { mutableStateOf<List<CharacterDtos.CharacterWithSprites>>(emptyList()) }
    var selectedCharacter by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(storageRepository, selectedCharacter) {
        coroutineScope.launch {
            val characterList = storageRepository.getAllCharacters()
            monList.value = characterList
        }
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = "My characters",
                onAdventureClick = {
                    navController.navigate(NavigationItems.Adventure.route)
                }
            )
        }
    ) { contentPadding ->
        if (monList.value.isEmpty()) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = contentPadding.calculateTopPadding())
                    .fillMaxSize()
            ) {
                Text(
                    text = "Nothing to see here",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
                    .padding(top = contentPadding.calculateTopPadding())
            ) {
                items(monList.value) { index ->
                    CharacterEntry(
                        icon = BitmapData(
                            bitmap = index.spriteIdle,
                            width = index.spriteWidth,
                            height = index.spriteHeight
                        ),
                        onClick = {
                            if (!index.isInAdventure) {
                                selectedCharacter = index.id
                            } else {
                                Toast.makeText(
                                    application,
                                    "This character is in an adventure",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(
                                    NavigationItems.Adventure.route
                                )
                            }
                        },
                    )
                }
            }
        }

        if (selectedCharacter != null) {
            StorageDialog(
                characterId = selectedCharacter!!,
                onDismissRequest = { selectedCharacter = null },
                onClickSetActive = {
                    storageScreenController
                        .setActive(selectedCharacter!!) {
                            selectedCharacter = null
                            navController.navigate(NavigationItems.Home.route)
                        }
                },
                onSendToBracelet = {
                    navController.navigate(
                        NavigationItems.Scan.route.replace(
                            "{characterId}",
                            selectedCharacter.toString()
                        )
                    )
                },
                onClickSendToAdventure = { time ->
                    adventureScreenController
                        .sendCharacterToAdventure(
                            characterId = selectedCharacter!!,
                            timeInMinutes = time
                        )
                    selectedCharacter = null
                }
            )
        }
    }
}
