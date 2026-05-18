package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.domain.items.ItemType
import com.github.nacabaro.vbhelper.R

@Composable
fun ItemElement(
    item: ItemDtos.ItemsWithQuantities,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) = {  }
) {
    Card (
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ItemCompatibilityBadge(
                itemType = item.itemType,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
            Icon(
                painter = painterResource(id = getIconResource(item.itemIcon)),
                contentDescription = null,
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
            Icon(
                painter = painterResource(id = getLengthResource(item.itemLength)),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.surfaceTint,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun ItemCompatibilityBadge(
    itemType: ItemType,
    modifier: Modifier = Modifier,
) {
    val (label, containerColor, contentColor) = when (itemType) {
        ItemType.VBITEM -> Triple(
            "VB",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        ItemType.BEITEM -> Triple(
            "BE",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        ItemType.UNIVERSAL -> Triple(
            "VB+BE",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
        )
        ItemType.SPECIALMISSION -> Triple(
            "MISSION",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
    ) {
        Text(
            text = label,
            color = contentColor,
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

