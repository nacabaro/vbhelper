package com.github.nacabaro.vbhelper.screens.cardScreen

import android.util.Log
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.screens.cardScreen.dialogs.CardDeleteDialog
import com.github.nacabaro.vbhelper.screens.cardScreen.dialogs.CardRenameDialog
import com.github.nacabaro.vbhelper.source.DexRepository

@Composable
fun CardsScreen(
    navController: NavController,
    cardScreenController: CardScreenControllerImpl
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val dexRepository = DexRepository(application.container.db)
    val cardList by dexRepository.getAllDims().collectAsState(emptyList())

    val selectedCard = remember { mutableStateOf<CardDtos.CardProgress?>(null) }
    var clickedDelete by remember { mutableStateOf(false) }
    var clickedRename by remember { mutableStateOf(false) }

    var modifyCards by remember { mutableStateOf(false) }

    Scaffold (
        topBar = {
            TopBanner(
                text = "My cards",
                onModifyClick = {
                    modifyCards = !modifyCards
                }
            )
        }
    ) { contentPadding ->
        LazyColumn (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(cardList) {
                CardEntry(
                    name = it.cardName,
                    logo = BitmapData(
                        bitmap = it.cardLogo,
                        width = it.logoWidth,
                        height = it.logoHeight
                    ),
                    onClick = {
                        navController
                            .navigate(
                                NavigationItems
                                    .CardView.route
                                    .replace("{cardId}", "${it.cardId}")
                            )
                    },
                    obtainedCharacters = it.obtainedCharacters,
                    totalCharacters = it.totalCharacters,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    displayModify = modifyCards,
                    onClickModify = {
                        selectedCard.value = it
                        clickedRename = true
                    },
                    onClickDelete = {
                        selectedCard.value = it
                        clickedDelete = true
                    }
                )
            }
        }
    }

    if (clickedRename) {
        CardRenameDialog(
            onDismiss = {
                clickedRename = false
                selectedCard.value = null
            },
            onRename = { newName ->
                Log.d("CardsScreen", "New name: $newName")
                Log.d("CardsScreen", "Card: ${selectedCard.value.toString()}")
                cardScreenController
                    .renameCard(
                        cardId = selectedCard.value!!.cardId,
                        newName = newName,
                        onRenamed = {
                            clickedRename = false
                            selectedCard.value = null
                        }
                    )
            },
            currentName = selectedCard.value!!.cardName
        )
    }

    if (clickedDelete) {
        CardDeleteDialog(
            cardName = selectedCard.value!!.cardName,
            onDismiss = {
                clickedDelete = false
                selectedCard.value = null
            },
            onConfirm = {
                cardScreenController
                    .deleteCard(
                        cardId = selectedCard.value!!.cardId,
                        onDeleted = {
                            clickedDelete = false
                            selectedCard.value = null
                        }
                    )
            }
        )
    }
}

