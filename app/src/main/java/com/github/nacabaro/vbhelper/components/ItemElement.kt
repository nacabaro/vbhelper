package com.github.nacabaro.vbhelper.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.R

@Composable
fun ItemElement(
    itemIcon: Int,
    lengthIcon: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) = {  }
) {
    val iconResource = when (itemIcon) {
        1 -> R.drawable.baseline_agility_24
        2 -> R.drawable.baseline_attack_24
        3 -> R.drawable.baseline_shield_24
        else -> R.drawable.baseline_question_mark_24
    }

    val lengthResource = when (lengthIcon) {
        1 -> R.drawable.baseline_15_min_timer
        2 -> R.drawable.baseline_30_min_timer
        3 -> R.drawable.baseline_60_min_timer
        else -> R.drawable.baseline_question_mark_24
    }

    Card (
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image (full size)
            Image(
                painter = painterResource(id = iconResource),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Image(
                painter = painterResource(id = lengthResource),
                contentDescription = null,
                modifier = Modifier
                    .size(100.dp) // Set the size of the overlay image
                    .align(Alignment.TopEnd) // Align to the top end (top-right corner)
                    .padding(16.dp) // Add some padding from the edges
            )
        }
    }
}