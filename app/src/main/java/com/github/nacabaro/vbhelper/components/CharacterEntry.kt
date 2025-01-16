package com.github.nacabaro.vbhelper.components

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.github.nacabaro.vbhelper.utils.getObscuredBitmap
import java.nio.ByteBuffer

@Composable
fun CharacterEntry(
    icon: BitmapData,
    obscure: Boolean = false,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.medium,
    multiplier: Int = 3,
    onClick: () -> Unit = {  }
) {
    val bitmap = remember (icon.bitmap) {
        if(obscure) icon.getObscuredBitmap() else icon.getBitmap()
    }
    val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
    val density: Float = LocalContext.current.resources.displayMetrics.density
    val dpSize = (icon.width * multiplier / density).dp

    Card(
        shape = shape,
        onClick = onClick,
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Icon",
                filterQuality = FilterQuality.None,
                modifier = Modifier
                    .size(dpSize)
            )
        }
    }
}

@Composable
fun ItemDisplay(
    icon: Int,
    textValue: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    textSize: TextUnit = 24.sp,
    definition: String = "",
) {
    val context = LocalContext.current
    Card(
        modifier = modifier,
        shape = androidx.compose.material.MaterialTheme.shapes.small,
        onClick = {
            Toast.makeText(context, definition, Toast.LENGTH_SHORT).show()
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "Vitals",
                modifier = Modifier
                    .padding(8.dp)
                    .size(iconSize)
            )
            Text(
                text = textValue,
                textAlign = TextAlign.Center,
                fontSize = textSize,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                fontWeight = FontWeight.Bold
            )
        }
    }
}