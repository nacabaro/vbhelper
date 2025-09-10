package com.github.nacabaro.vbhelper.screens.cardScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getImageBitmap

@Composable
fun CardAdventureEntry(
    cardAdventureEntry: CardDtos.CardAdventureWithSprites,
    obscure: Boolean
) {
    val charaImageBitmapData = BitmapData(
        bitmap = cardAdventureEntry.characterIdleSprite,
        width = cardAdventureEntry.characterIdleSpriteWidth,
        height = cardAdventureEntry.characterIdleSpriteHeight
    ).getImageBitmap(
        context = LocalContext.current,
        multiplier = 4,
        obscure = obscure
    )

    val nameImageBitmapData = BitmapData(
        bitmap = cardAdventureEntry.characterName,
        width = cardAdventureEntry.characterNameWidth,
        height = cardAdventureEntry.characterNameHeight
    ).getImageBitmap(
        context = LocalContext.current,
        multiplier = 3,
        obscure = obscure
    )

    Card (
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row (
            modifier = Modifier
                .padding(8.dp)
        ){
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
                    colorFilter = when (obscure) {
                        true -> ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                        false -> null
                    },
                    filterQuality = FilterQuality.None
                )
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Column {
                if (!obscure) {
                    Image(
                        bitmap = nameImageBitmapData.imageBitmap,
                        contentDescription = "Icon",
                        modifier = Modifier
                            .width(nameImageBitmapData.dpWidth)
                            .height(nameImageBitmapData.dpHeight),
                        filterQuality = FilterQuality.None
                    )

                    Spacer(modifier = Modifier.padding(4.dp))

                    Text(
                        text = "HP: ${cardAdventureEntry.characterHp}, DP: ${cardAdventureEntry.characterDp}, AP: ${cardAdventureEntry.characterAp}"
                    )
                    if (cardAdventureEntry.characterBp != null) {
                        Text(text = "BP: ${cardAdventureEntry.characterBp}")
                    }
                    Text(text = "Steps: ${cardAdventureEntry.steps}")
                } else {
                    Text(text = "????????????????")
                    Text(
                        text = "HP: -, BP: -, AP: -"
                    )
                    if (cardAdventureEntry.characterBp != null) {
                        Text(text = "DP: -")
                    }
                    Text(text = "Steps: -")
                }
            }
        }
    }
}