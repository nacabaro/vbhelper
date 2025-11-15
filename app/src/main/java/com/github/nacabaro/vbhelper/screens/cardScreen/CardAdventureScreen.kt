package com.github.nacabaro.vbhelper.screens.cardScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner

@Composable
fun CardAdventureScreen(
    navController: NavController,
    cardScreenController: CardScreenControllerImpl,
    cardId: Long
) {
    val cardAdventureMissions by cardScreenController
        .getCardAdventureMissions(cardId)
        .collectAsState(emptyList())
    val currentCardAdventure by cardScreenController
        .getCardProgress(cardId)
        .collectAsState(0)

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
            cardAdventureMissions.mapIndexed { index, it ->
                CardAdventureEntry(
                    cardAdventureEntry = it,
                    obscure = index > currentCardAdventure - 1
                )
            }
        }
    }
}