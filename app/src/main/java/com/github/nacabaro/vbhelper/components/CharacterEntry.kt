package com.github.nacabaro.vbhelper.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import com.github.cfogrady.vbnfc.vb.SpecialMission
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.utils.getObscuredBitmap

@Composable
fun CharacterEntry(
    icon: BitmapData,
    modifier: Modifier = Modifier,
    obscure: Boolean = false,
    disabled: Boolean = false,
    shape: Shape = MaterialTheme.shapes.medium,
    multiplier: Int = 4,
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
        onClick = when (disabled) {
            true -> { {} }
            false -> onClick
        },
        modifier = modifier
            .aspectRatio(1f)
            .padding(8.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Icon",
                filterQuality = FilterQuality.None,
                colorFilter = when (obscure) {
                    true -> ColorFilter.tint(color = MaterialTheme.colorScheme.secondary)
                    false -> null
                },
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
                    .fillMaxSize(0.5f)
                    .padding(8.dp)
            )
            Text(
                text = textValue,
                textAlign = TextAlign.Center,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun SpecialMissionsEntry(
    specialMission: SpecialMissions,
    modifier: Modifier = Modifier,
    onClickCard: () -> Unit = {  },
) {
    val textValue = when (specialMission.missionType) {
        SpecialMission.Type.NONE -> "No mission selected"
        SpecialMission.Type.STEPS -> "Walk ${specialMission.goal} steps"
        SpecialMission.Type.BATTLES -> "Battle ${specialMission.goal} times"
        SpecialMission.Type.WINS -> "Win ${specialMission.goal} battles"
        SpecialMission.Type.VITALS -> "Earn ${specialMission.goal} vitals"
    }

    val progress = if (specialMission.status == SpecialMission.Status.COMPLETED) {
        specialMission.goal
    } else {
        specialMission.progress
    }

    val completion = when (specialMission.missionType) {
        SpecialMission.Type.NONE -> ""
        SpecialMission.Type.STEPS -> "Walked $progress steps"
        SpecialMission.Type.BATTLES -> "Battled $progress times"
        SpecialMission.Type.WINS -> "Won $progress battles"
        SpecialMission.Type.VITALS -> "Earned $progress vitals"
    }

    val icon = when (specialMission.missionType) {
        SpecialMission.Type.NONE -> R.drawable.baseline_free_24
        SpecialMission.Type.STEPS -> R.drawable.baseline_agility_24
        SpecialMission.Type.BATTLES -> R.drawable.baseline_swords_24
        SpecialMission.Type.WINS -> R.drawable.baseline_trophy_24
        SpecialMission.Type.VITALS -> R.drawable.baseline_vitals_24
    }

    val color = when (specialMission.status)
    {
        SpecialMission.Status.IN_PROGRESS -> MaterialTheme.colorScheme.secondary
        SpecialMission.Status.COMPLETED -> MaterialTheme.colorScheme.primary
        SpecialMission.Status.FAILED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.surfaceContainerHighest
    }

    Card(
        modifier = modifier,
        shape = androidx.compose.material.MaterialTheme.shapes.small,
        onClick = if (specialMission.status == SpecialMission.Status.COMPLETED || specialMission.status == SpecialMission.Status.FAILED) {
            onClickCard
        } else {
            {  }
        },
        colors = CardDefaults.cardColors(
            containerColor = color
        )

    ) {
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = "Vitals",
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
            )
            Column {
                Text(
                    text = textValue,
                    fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = completion,
                    fontFamily = MaterialTheme.typography.titleSmall.fontFamily,
                )
            }
        }
    }
}