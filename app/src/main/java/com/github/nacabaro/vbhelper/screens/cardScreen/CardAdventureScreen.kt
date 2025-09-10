package com.github.nacabaro.vbhelper.screens.cardScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.dtos.CardDtos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CardAdventureScreen(
    navController: NavController,
    cardScreenController: CardScreenControllerImpl,
    cardId: Long
) {
    val cardAdventureMissions = remember { mutableStateOf(emptyList<CardDtos.CardAdventureWithSprites>()) }
    var currentCardAdventure = remember { 0 }

    LaunchedEffect(cardId) {
        withContext(Dispatchers.IO) {
            cardAdventureMissions.value =
                cardScreenController
                    .getCardAdventureMissions(cardId)

            currentCardAdventure =
                cardScreenController
                    .getCardProgress(cardId)
        }
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = "Adventure missions",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    ) { contentPadding ->
        Column (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .verticalScroll(state = rememberScrollState())
        ) {
            cardAdventureMissions.value.mapIndexed { index, it ->
                CardAdventureEntry(
                    cardAdventureEntry = it,
                    obscure = index > currentCardAdventure
                )
            }
        }
    }
}