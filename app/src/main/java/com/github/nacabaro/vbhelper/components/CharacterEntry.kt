package com.github.nacabaro.vbhelper.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CharacterEntry(
    name: ImageBitmap,
    icon: ImageBitmap,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {  }
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                bitmap = icon,
                contentDescription = "Icon",
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
            )
            Image(
                bitmap = name,
                contentDescription = "Name",
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}