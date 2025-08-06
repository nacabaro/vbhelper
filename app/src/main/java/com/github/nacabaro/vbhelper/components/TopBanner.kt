package com.github.nacabaro.vbhelper.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nacabaro.vbhelper.R

@Composable
fun TopBanner(
    text: String,
    modifier: Modifier = Modifier,
    onGearClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onScanClick: (() -> Unit)? = null,
    onAdventureClick: (() -> Unit)? = null,
    onModifyClick: (() -> Unit)? = null
) {
    Box( // Use Box to overlay elements
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = text,
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.Center)
        )
         if (onGearClick != null) {
            IconButton(
                onClick = onGearClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_settings_24),
                    contentDescription = "Settings"
                )
            }
        } else if (onAdventureClick != null) {
            IconButton(
                onClick = onAdventureClick,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_fort_24),
                    contentDescription = "Adventure"
                )
            }
        } else if (onModifyClick != null) {
             IconButton(
                 onClick = onModifyClick,
                 modifier = Modifier
                     .align(Alignment.CenterEnd)
             ) {
                 Icon(
                     painter = painterResource(R.drawable.baseline_edit_24),
                     contentDescription = "Adventure"
                 )
             }
         }

        if (onScanClick != null) {
            IconButton(
                onClick = onScanClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_nfc_24),
                    contentDescription = "Scan"
                )
            }
        } else if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_arrow_back_24),
                    contentDescription = "Settings"
                )
            }
        }
    }
}
