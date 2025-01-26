package com.github.nacabaro.vbhelper.screens.storageScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.R

fun getAdventureTime(time: Int): String {
    return when (time) {
        360 -> "6 hours"
        720 -> "12 hours"
        1440 -> "24 hours"
        else -> "Unknown"
    }
}

@Composable
fun StorageAdventureTimeDialog(
    onClickSendToAdventure: (time: Long) -> Unit,
    onDismissRequest: () -> Unit
) {
    val times = arrayOf(360, 720, 1440)
    var expanded by remember { mutableStateOf(false) }
    var itemPosition by remember { mutableIntStateOf(-1) }

    Dialog(
        onDismissRequest = onDismissRequest
    ) {
        Card {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Row (
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .width(256.dp)
                            .clickable(true) {
                                expanded = true
                            }
                    ) {
                        Text(
                            text = when (itemPosition) {
                                -1 -> "Choose time"
                                else -> getAdventureTime(times[itemPosition])
                            }
                        )
                        Icon(
                            painter = painterResource(R.drawable.baseline_single_arrow_down),
                            contentDescription = "Show more"
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier
                            .width(256.dp)
                    ) {
                        times.forEach { time ->
                            DropdownMenuItem(
                                text = { Text(getAdventureTime(time)) },
                                onClick = {
                                    itemPosition = times.indexOf(time)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = {
                        if (itemPosition != -1) {
                            onClickSendToAdventure(times[itemPosition].toLong())
                            onDismissRequest()
                        }
                    }
                ) {
                    Text(text = "Send on adventure")
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onDismissRequest
                ) {
                    Text(text = "Dismiss")
                }
            }
        }
    }
}