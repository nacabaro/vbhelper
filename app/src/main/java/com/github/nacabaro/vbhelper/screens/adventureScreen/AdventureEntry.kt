package com.github.nacabaro.vbhelper.screens.adventureScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import java.util.Locale

@Composable
fun AdventureEntry(
    icon: BitmapData,
    timeLeft: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bitmap = remember (icon.bitmap) { icon.getBitmap() }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val density: Float = LocalContext.current.resources.displayMetrics.density
    val dpSize = (icon.width * 4 / density).dp

    Card(
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .height(96.dp)
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = null,
                filterQuality = FilterQuality.None,
                modifier = Modifier
                    .size(dpSize)
            )
            Text(
                text = when {
                    timeLeft < 0 -> "Adventure finished"
                    else -> "Time left: ${formatSeconds(timeLeft)}"
                }
            )
        }
    }
}

fun formatSeconds(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}