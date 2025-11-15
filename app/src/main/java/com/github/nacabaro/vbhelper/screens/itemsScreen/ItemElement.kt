package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.dtos.ItemDtos

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
