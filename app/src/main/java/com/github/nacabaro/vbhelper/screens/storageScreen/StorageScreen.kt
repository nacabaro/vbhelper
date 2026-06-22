package com.github.nacabaro.vbhelper.screens.storageScreen

import android.util.Log
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.screens.adventureScreen.AdventureScreenControllerImpl
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.R


@Composable
fun StorageScreen(
    navController: NavController,
    storageScreenController: StorageScreenControllerImpl,
    adventureScreenController: AdventureScreenControllerImpl
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val characterList by storageRepository.getAllCharacters().collectAsState(initial = emptyList())

    var selectedCharacter by remember { mutableStateOf<Long?>(null) }

    Scaffold (
        topBar = {
            TopBanner(
                text = stringResource(R.string.storage_my_characters_title),
                onAdventureClick = {
                    navController.navigate(NavigationItems.Adventure.route)
                }
            )
        }
    ) { contentPadding ->
        if (characterList.isEmpty()) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(top = contentPadding.calculateTopPadding())
                    .fillMaxSize()
            ) {
                Text(
                    text = stringResource(R.string.storage_nothing_to_see_here),
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
                items(characterList) { index ->
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
                                    application.getString(R.string.storage_in_adventure_toast),
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate(
                                    NavigationItems.Adventure.route
                                )
                            }
                        },
                        cardColors = if (index.active) {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                        }

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
                },
                onClickDelete = {
                    storageScreenController
                        .deleteCharacter(
                            characterId = selectedCharacter!!,
                            onCompletion = {
                                Log.d("StorageScreen", "Character deleted")
                            }
                        )
                    selectedCharacter = null
                }
            )
        }
    }
}
