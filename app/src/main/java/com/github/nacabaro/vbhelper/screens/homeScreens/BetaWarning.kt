package com.github.nacabaro.vbhelper.screens.homeScreens

import androidx.compose.foundation.layout.Column
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
fun BetaWarning(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "This application is currently in alpha and it is not complete. Do not use to store important characters for you, as any future updates might delete all your characters. Sorry for the inconvenience!"
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Also, this application does not work yet with the original VB."
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Text(
                    text = "Thank you for your understanding and patience. Sincerely, the dev team."
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    onClick = onDismissRequest
                ) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}