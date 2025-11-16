package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.source.ScanRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getImageBitmap

@Composable
fun WriteCardScreen(
    characterId: Long,
    onClickCancel: () -> Unit,
    onClickConfirm: () -> Unit
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val database = application.container.db
    val scanRepository = ScanRepository(database)
    val cardDetails by scanRepository.getCardDetails(characterId).collectAsState(Card(
        id = 0,
        cardId = 0,
        name = "",
        logo = byteArrayOf(),
        logoHeight = 0,
        logoWidth = 0,
        stageCount = 0,
        isBEm = false
    ))

    Scaffold(
        topBar = {
            TopBanner(
                text = "Writing card details",
                onBackClick = onClickCancel
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Row (
                    modifier = Modifier.padding(16.dp),
                ){
                    if (cardDetails.logoHeight > 0 && cardDetails.logoWidth > 0) {
                        val charaBitmapData = BitmapData(
                            bitmap = cardDetails.logo,
                            width = cardDetails.logoWidth,
                            height = cardDetails.logoHeight
                        )
                        val charaImageBitmapData = charaBitmapData.getImageBitmap(
                            context = LocalContext.current,
                            multiplier = 4,
                            obscure = false
                        )

                        Card (
                            colors = CardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.contentColorFor(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                ),
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContentColor = MaterialTheme.colorScheme.contentColorFor(
                                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                                )
                            )
                        ) {
                            Image(
                                bitmap = charaImageBitmapData.imageBitmap,
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .size(charaImageBitmapData.dpWidth)
                                    .padding(8.dp),
                                filterQuality = FilterQuality.None
                            )
                        }
                    }

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Column {
                        Text("Get your device Ready!")
                        Text("You will need ${cardDetails.name} card!")
                    }
                }

            }

            Button(
                onClick = onClickConfirm,
            ) {
                Text("Confirm")
            }
        }
    }
}
