package com.github.nacabaro.vbhelper.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun CharacterEntry(
    icon: ImageBitmap,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {  }
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
            .size(96.dp)
    ) {
        Image(
            bitmap = icon,
            contentDescription = "Icon",
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        )
    }
}