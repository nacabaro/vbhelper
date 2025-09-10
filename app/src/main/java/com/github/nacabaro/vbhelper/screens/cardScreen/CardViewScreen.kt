package com.github.nacabaro.vbhelper.screens.cardScreen

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
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.screens.cardScreen.dialogs.DexCharaDetailsDialog
import com.github.nacabaro.vbhelper.source.DexRepository
import kotlinx.coroutines.launch

@Composable
fun CardViewScreen(
    navController: NavController,
    cardId: Long
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val dexRepository = DexRepository(application.container.db)

    val characterList = remember { mutableStateOf<List<CharacterDtos.CardCharaProgress>>(emptyList()) }
    val cardPossibleTransformations = remember { mutableStateOf<List<CharacterDtos.EvolutionRequirementsWithSpritesAndObtained>>(emptyList()) }

    val selectedCharacter = remember { mutableStateOf<CharacterDtos.CardCharaProgress?>(null) }

    LaunchedEffect(dexRepository) {
        coroutineScope.launch {
            val newCharacterList = dexRepository.getCharactersByCardId(cardId)
            characterList.value = newCharacterList

            val newCardPossibleTransformations = dexRepository.getCardPossibleTransformations(cardId)
            cardPossibleTransformations.value = newCardPossibleTransformations
        }
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = "Discovered characters",
                onBackClick = {
                    navController.popBackStack()
                },
                onAdventureClick = {
                    navController
                        .navigate(route = NavigationItems
                            .CardAdventure
                            .route
                            .replace(
                                "{cardId}",
                                cardId.toString()
                            )
                        )
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
                    onClick = {
                        selectedCharacter.value = character
                    },
                    obscure = character.discoveredOn == null,
                    icon = BitmapData(
                        bitmap = character.spriteIdle,
                        width = character.spriteWidth,
                        height = character.spriteHeight,
                    ),
                )
            }
        }

        if (selectedCharacter.value != null) {
            DexCharaDetailsDialog(
                currentChara = selectedCharacter.value!!,
                possibleTransformations = cardPossibleTransformations.value,
                obscure = selectedCharacter.value!!.discoveredOn == null,
                onClickClose = {
                    selectedCharacter.value = null
                }
            )
        }
    }
}