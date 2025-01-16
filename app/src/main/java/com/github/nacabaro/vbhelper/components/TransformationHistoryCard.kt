package com.github.nacabaro.vbhelper.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.domain.device_data.TransformationHistory
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap

@Composable
fun TransformationHistoryCard(
    transformationHistory: List<CharacterDtos.TransformationHistory>,
    modifier: Modifier= Modifier
) {
    Card (
        shape = androidx.compose.material.MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        LazyRow (
            modifier = Modifier
                .padding(8.dp)
        ) {
            items(transformationHistory) { transformation ->
                TransformationHistoryItem(transformation)
            }
        }
    }
}

@Composable
fun TransformationHistoryItem(
    transformation: CharacterDtos.TransformationHistory
) {
    val bitmapData = BitmapData(
        bitmap = transformation.spriteIdle,
        width = transformation.spriteWidth,
        height = transformation.spriteHeight
    )
    val bitmap = remember (bitmapData) { bitmapData.getBitmap() }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val density: Float = LocalContext.current.resources.displayMetrics.density
    val dpSize = (bitmap.width * 3 / density).dp

    Box (
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .size((64*3/density).dp)
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Transformation",
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .size(dpSize)

        )
    }

}