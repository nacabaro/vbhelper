package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.domain.items.ItemType
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme

@Composable
fun ItemDialog(
    item: ItemDtos.ItemsWithQuantities,
    onClickCancel: () -> Unit,
    onClickUse: (() -> Unit)? = null,
    onClickPurchase: (() -> Unit)? = null,
) {
    Dialog(
        onDismissRequest = onClickCancel,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card (
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row {
                    Box(modifier = Modifier) {
                        // Background image (full size)
                        Icon(
                            painter = painterResource(id = getIconResource(item.itemIcon)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .align(Alignment.Center)
                        )
                        Icon(
                            painter = painterResource(id = getLengthResource(item.itemLength)),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(64.dp) // Set the size of the overlay image
                                .align(Alignment.BottomEnd) // Align to the top end (top-right corner)
                        )
                    }
                    Column (
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Text(
                            fontSize = MaterialTheme.typography.titleLarge.fontSize,
                            text = item.name,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                Text(
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    text = item.description,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                )
                if (onClickPurchase != null) {
                    Text(
                        textAlign = TextAlign.Center,
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                        fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                        text = "Costs ${item.price} credits",
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
                Text(
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    text = "You have ${item.quantity} of this item",
                    modifier = Modifier
                        .fillMaxWidth()
                )

                Row (
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (onClickUse != null) {
                        Button(
                            onClick = onClickUse
                        ) {
                            Text("Use item")
                        }
                    }

                    if (onClickPurchase != null) {
                        Button(
                            onClick = onClickPurchase
                        ) {
                            Text("Purchase")
                        }
                    }

                    Spacer(modifier = Modifier.size(8.dp))
                    Button(
                        onClick = onClickCancel
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewItemDialog() {
    VBHelperTheme {
        ItemDialog(
            item = ItemDtos.ItemsWithQuantities(
                name = "AP Training x3 (60 min)",
                description = "Boosts AP during training (for 60 minutes)",
                itemIcon = R.drawable.baseline_attack_24,
                itemLength = R.drawable.baseline_60_min_timer,
                quantity = 19,
                id = 1,
                price = 500,
                itemType = ItemType.BEITEM
            ),
            onClickUse = {  },
            onClickCancel = {  }
        )
    }
}