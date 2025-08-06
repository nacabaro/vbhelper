package com.github.nacabaro.vbhelper.screens.cardScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.screens.scanScreen.cardSelect.ScanCardEntry
import com.github.nacabaro.vbhelper.utils.BitmapData

@Composable
fun ChooseCard(
    cards: List<Card>,
    onCardSelected: (Card) -> Unit
) {
    Scaffold (
        topBar = {
            TopBanner(
                text = "Choose card",
            )
        }
    ) { contentPadding ->
        LazyColumn (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(cards) {
                ScanCardEntry(
                    name = it.name,
                    logo = BitmapData(
                        it.logo,
                        it.logoWidth,
                        it.logoHeight
                    ),
                    onClick = {
                        onCardSelected(it)
                    },
                    modifier = Modifier
                        .padding(8.dp)
                )
            }
        }
    }
}