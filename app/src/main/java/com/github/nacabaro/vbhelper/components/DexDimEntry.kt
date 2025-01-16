package com.github.nacabaro.vbhelper.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap

@Composable
fun DexDiMEntry(
    name: String,
    logo: BitmapData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember (logo.bitmap) { logo.getBitmap() }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val density: Float = LocalContext.current.resources.displayMetrics.density
    val dpSize = (logo.width * 4 / density).dp

    Card (
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
        onClick = onClick
    ) {
        Row (
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Image (
                bitmap = imageBitmap,
                contentDescription = name,
                filterQuality = FilterQuality.None,
                modifier = Modifier
                    .padding(8.dp)
                    .size(dpSize)
            )
            Text(
                text = name,
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}