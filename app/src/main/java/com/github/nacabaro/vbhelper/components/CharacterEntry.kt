package com.github.nacabaro.vbhelper.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.domain.Sprites
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import java.nio.ByteBuffer

@Composable
fun CharacterEntry(
    icon: BitmapData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {  }
) {
    val bitmap = remember (icon.bitmap) {
        icon.getBitmap()
    }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

    Card(
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
            .size(96.dp)
    ) {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Icon",
            filterQuality = FilterQuality.None,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
        )
    }
}