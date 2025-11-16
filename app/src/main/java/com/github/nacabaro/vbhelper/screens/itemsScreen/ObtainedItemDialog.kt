package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.dtos.ItemDtos

@Composable
fun ObtainedItemDialog(
    obtainedItem: ItemDtos.PurchasedItem,
    obtainedCurrency: Int,
    onClickDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onClickDismiss
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Column (
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row {
                        Box(modifier = Modifier) {
                            Icon(
                                painter = painterResource(id = getIconResource(obtainedItem.itemIcon)),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(96.dp)
                                    .align(Alignment.Center)
                            )
                            Icon(
                                painter = painterResource(id = getLengthResource(obtainedItem.itemLength)),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier
                                    .size(64.dp)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                        Column (
                            modifier = Modifier
                                .padding(16.dp)
                        ) {
                            Text(
                                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                                text = obtainedItem.itemName,
                                modifier = Modifier
                                    .fillMaxWidth()
                            )
                        }
                    }
                    Text(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                        fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                        text = obtainedItem.itemDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)

                    )
                    Text(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        text = "You have obtained ${obtainedItem.itemAmount} of this item",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    )
                    Text(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        text = "You also got $obtainedCurrency credits",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp)
                    )
                    Button(
                        onClick = onClickDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(text = "Dismiss")
                    }
                }
            }
        }
    }
}