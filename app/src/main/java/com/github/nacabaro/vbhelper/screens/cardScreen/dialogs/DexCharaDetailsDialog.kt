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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getImageBitmap


@Composable
fun DexCharaDetailsDialog(
    currentChara: CharacterDtos.CardCharaProgress,
    possibleTransformations: List<CharacterDtos.EvolutionRequirementsWithSpritesAndObtained>,
    obscure: Boolean,
    onClickClose: () -> Unit
) {
    val nameMultiplier = 3
    val charaMultiplier = 4

    val currentCharaPossibleTransformations = possibleTransformations.filter { it.fromCharaId == currentChara.id }

    val romanNumeralsStage = when (currentChara.stage) {
        1 -> "II"
        2 -> "III"
        3 -> "IV"
        4 -> "V"
        5 -> "VI"
        6 -> "VII"
        else -> "I"
    }

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
        onDismissRequest = onClickClose
    ) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
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
                            Spacer(modifier = Modifier.padding(4.dp))
                            if (currentChara.baseHp != 65535) {
                                Text(
                                    text = "HP: ${currentChara.baseHp}, BP: ${currentChara.baseBp}, AP: ${currentChara.baseAp}"
                                )
                                Text(text = "Stg: ${romanNumeralsStage}, Atr: ${currentChara.attribute.toString().substring(0, 2)}")
                            }
                        }
                    } else {
                        Column {
                            Text(text = "????????????????")
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text(text = "Stg: -, Atr: -")
                            Text(text = "HP: -, BP: -, AP: -")
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(16.dp))
                Column {
                    currentCharaPossibleTransformations.map {
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
                                    Text("Tr: ${it.requiredTrophies}; Bt: ${it.requiredBattles}; Vr: ${it.requiredVitals}; Wr: ${it.requiredWinRate}%; Ct: ${it.changeTimerHours}h")
                                    Text("AdvLvl ${it.requiredAdventureLevelCompleted + 1}")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onClickClose
                ) {
                    Text("Close")
                }
            }
        }
    }
}