package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme

@Composable
fun ItemElement(
    itemIcon: Int,
    lengthIcon: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) = {  }
) {
    Card (
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                painter = painterResource(id = itemIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
            Icon(
                painter = painterResource(id = lengthIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier
                    .size(48.dp) // Set the size of the overlay image
                    .align(Alignment.TopStart) // Align to the top end (top-right corner)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun ItemDialog(
    name: String,
    description: String,
    itemIcon: Int,
    lengthIcon: Int,
    amount: Int,
    onClickUse: () -> Unit,
    onClickCancel: () -> Unit
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
                            painter = painterResource(id = itemIcon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(96.dp)
                                .align(Alignment.Center)
                        )
                        Icon(
                            painter = painterResource(id = lengthIcon),
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
                            text = name,
                            modifier = Modifier
                                .fillMaxWidth()
                        )
                    }
                }
                Text(
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    fontFamily = MaterialTheme.typography.bodyMedium.fontFamily,
                    text = description
                )
                Text(
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    fontFamily = MaterialTheme.typography.bodySmall.fontFamily,
                    text = "You have $amount of this item",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )
                Row (
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = onClickUse
                    ) {
                        Text("Use item")
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

fun getIconResource(index: Int): Int {
    return when (index) {
        ItemsScreenControllerImpl.ItemTypes.PPTraining.id -> R.drawable.baseline_agility_24
        ItemsScreenControllerImpl.ItemTypes.APTraining.id -> R.drawable.baseline_attack_24
        ItemsScreenControllerImpl.ItemTypes.HPTraining.id -> R.drawable.baseline_shield_24
        ItemsScreenControllerImpl.ItemTypes.BPTraining.id -> R.drawable.baseline_trophy_24
        ItemsScreenControllerImpl.ItemTypes.AllTraining.id -> R.drawable.baseline_arrow_up_24
        6 -> R.drawable.baseline_timer_24
        7 -> R.drawable.baseline_rank_24
        8 -> R.drawable.baseline_vitals_24
        else -> R.drawable.baseline_question_mark_24
    }
}

fun getLengthResource(index: Int): Int {
    return when (index) {
        15 -> R.drawable.baseline_15_min_timer
        30 -> R.drawable.baseline_30_min_timer
        60 -> R.drawable.baseline_60_min_timer
        -60 -> R.drawable.baseline_60_min_timer
        300 -> R.drawable.baseline_5_hour_timer
        600 -> R.drawable.baseline_10_hour_timer
        -720 -> R.drawable.baseline_12_hour_timer
        -1440 -> R.drawable.baseline_24_hour_timer
        6000 -> R.drawable.baseline_reset_24
        1000 -> R.drawable.baseline_single_arrow_up
        2500 -> R.drawable.baseline_double_arrow_up
        5000 -> R.drawable.baseline_triple_arrow_up
        9999 -> R.drawable.baseline_health_24
        -500 -> R.drawable.baseline_single_arrow_down
        -1000 -> R.drawable.baseline_double_arrow_down
        -2500 -> R.drawable.baseline_triple_arrow_down
        -9999 -> R.drawable.baseline_reset_24
        else -> R.drawable.baseline_question_mark_24
    }
}

@Composable
@Preview(showBackground = true)
fun PreviewItemDialog() {
    VBHelperTheme {
        ItemDialog(
            name = "AP Training x3 (60 min)",
            description = "Boosts AP during training (for 60 minutes)",
            itemIcon = R.drawable.baseline_attack_24,
            lengthIcon = R.drawable.baseline_60_min_timer,
            onClickUse = {  },
            onClickCancel = {  },
            amount = 19
        )
    }
}