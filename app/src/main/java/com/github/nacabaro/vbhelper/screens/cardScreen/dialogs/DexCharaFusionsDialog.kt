package com.github.nacabaro.vbhelper.screens.cardScreen.dialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getImageBitmap

@Composable
fun DexCharaFusionsDialog(
    currentChara: CharacterDtos.CardCharaProgress,
    currentCharaPossibleFusions: List<CharacterDtos.FusionsWithSpritesAndObtained>,
    obscure: Boolean,
    onClickDismiss: () -> Unit,
) {
    val nameMultiplier = 3
    val charaMultiplier = 4

    val charaBitmapData = BitmapData(
        bitmap = currentChara.spriteIdle,
        width = currentChara.spriteWidth,
        height = currentChara.spriteHeight
    )
    val charaImageBitmapData = charaBitmapData.getImageBitmap(
        context = LocalContext.current,
        multiplier = charaMultiplier,
        obscure = obscure
    )

    val nameBitmapData = BitmapData(
        bitmap = currentChara.nameSprite,
        width = currentChara.nameSpriteWidth,
        height = currentChara.nameSpriteHeight
    )
    val nameImageBitmapData = nameBitmapData.getImageBitmap(
        context = LocalContext.current,
        multiplier = nameMultiplier,
        obscure = obscure
    )

    Dialog(
        onDismissRequest = onClickDismiss,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row {
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

                    Spacer(
                        modifier = Modifier
                            .padding(16.dp)
                    )

                    if (!obscure) {
                        Column {
                            Image(
                                bitmap = nameImageBitmapData.imageBitmap,
                                contentDescription = "Icon",
                                modifier = Modifier
                                    .width(nameImageBitmapData.dpWidth)
                                    .height(nameImageBitmapData.dpHeight),
                                filterQuality = FilterQuality.None
                            )
                        }
                    } else {
                        Column {
                            Text(text = "????????????????")
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(16.dp))
                Column {
                    currentCharaPossibleFusions.map {
                        val selectedCharaBitmap = BitmapData(
                            bitmap = it.spriteIdle,
                            width = it.spriteWidth,
                            height = it.spriteHeight
                        )
                        val selectedCharaImageBitmap = selectedCharaBitmap.getImageBitmap(
                            context = LocalContext.current,
                            multiplier = 4,
                            obscure = it.discoveredOn == null
                        )

                        Card (
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                        ) {
                            Row (
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
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
                                        bitmap = selectedCharaImageBitmap.imageBitmap,
                                        contentDescription = "Icon",
                                        modifier = Modifier
                                            .size(selectedCharaImageBitmap.dpWidth)
                                            .padding(8.dp),
                                        colorFilter = when (it.discoveredOn == null) {
                                            true -> ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                                            false -> null
                                        },
                                        filterQuality = FilterQuality.None
                                    )
                                }
                                Spacer(
                                    modifier = Modifier
                                        .padding(16.dp)
                                )
                                Column {
                                    Text("Combine with ${it.fusionAttribute}")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onClickDismiss
                ) {
                    Text("Close")
                }
            }
        }
    }
}