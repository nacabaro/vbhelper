package com.github.nacabaro.vbhelper.screens.cardScreen.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun CardRenameDialog(
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
    currentName: String
) {
    var cardName by remember { mutableStateOf(currentName) }

    Dialog(
        onDismissRequest = onDismiss

    ) {
        Card ( ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                TextField(
                    value = cardName,
                    onValueChange = { cardName = it }
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    onClick = {
                        onRename(cardName)
                        onDismiss()
                    }
                ) {
                    Text(text = "Rename")
                }
            }
        }
    }
}