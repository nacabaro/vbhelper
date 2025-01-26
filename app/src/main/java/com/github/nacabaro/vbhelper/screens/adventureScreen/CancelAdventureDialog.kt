package com.github.nacabaro.vbhelper.screens.adventureScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap

@Composable
fun CancelAdventureDialog(
    characterSprite: BitmapData,
    onDismissRequest: () -> Unit,
    onClickConfirm: () -> Unit
) {
    val bitmap = remember (characterSprite) { characterSprite.getBitmap() }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val density: Float = LocalContext.current.resources.displayMetrics.density
    val dpSize = (characterSprite.width * 4 / density).dp

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Row {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = null,
                        filterQuality = FilterQuality.None,
                        modifier = Modifier
                            .size(dpSize)
                    )
                    Text(
                        text = "Are you sure you want to cancel this character's adventure?"
                    )
                }
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Button(
                        onClick = onClickConfirm
                    ) {
                        Text(text = "Confirm")
                    }
                    Spacer(modifier = Modifier.padding(4.dp))
                    Button(
                        onClick = onDismissRequest
                    ) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}