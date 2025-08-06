package com.github.nacabaro.vbhelper.screens.cardScreen.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CardDeleteDialog(
    cardName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss

    ) {
        Card ( ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(text = "Are you sure you want to delete $cardName. This action will also delete all the characters raised from this card.")
                Spacer(modifier = Modifier.padding(8.dp))
                Row {
                    Button(
                        onClick = {
                            onDismiss()
                        }
                    ) {
                        Text(text = "Confirm")
                    }
                    Button(
                        onClick = {
                            onConfirm()
                        }
                    ) {
                        Text(text = "Delete")
                    }
                }
            }
        }
    }
}