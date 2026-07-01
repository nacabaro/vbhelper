package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.utils.ImageBitmapData
import kotlinx.coroutines.delay

@Composable
fun TransferAnimationScreen(
    topBannerText: String,
    detectedTransportMessage: String?,
    transferStatusMessage: String?,
    characterPreview: ImageBitmapData? = null,
    onClickCancel: () -> Unit,
    isTransferring: Boolean = true,
) {
    var pulseScale by remember { mutableStateOf(1f) }
    var animationProgress by remember { mutableStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = pulseScale,
        animationSpec = tween(durationMillis = 800),
        label = "pulse_scale"
    )

    val progress by animateIntAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing),
        label = "progress"
    )

    LaunchedEffect(isTransferring) {
        if (isTransferring) {
            while (true) {
                pulseScale = 1.2f
                delay(400)
                pulseScale = 1f
                delay(400)
            }
        }
    }

    LaunchedEffect(isTransferring) {
        if (isTransferring) {
            while (animationProgress < 100) {
                delay(50)
                animationProgress += 2
            }
        }
    }

    Scaffold(
        topBar = {
            TopBanner(
                text = topBannerText,
                onBackClick = onClickCancel
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            characterPreview?.let { preview ->
                Card(
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Image(
                        bitmap = preview.imageBitmap,
                        contentDescription = "Transferred character sprite",
                        modifier = Modifier
                            .size(preview.dpWidth, preview.dpHeight)
                            .padding(8.dp),
                        filterQuality = FilterQuality.None
                    )
                }
            }

            // Animated pulsing circle indicator
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        color = Color(0xFF6366F1).copy(alpha = 0.2f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = Color(0xFF6366F1),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔄",
                        fontSize = 40.sp,
                        modifier = Modifier.scale(if (scale > 1f) 1.1f else 1f)
                    )
                }
            }

            Text(
                text = stringResource(R.string.action_place_near_reader),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 24.dp),
                fontWeight = FontWeight.Medium
            )

            if (!detectedTransportMessage.isNullOrBlank()) {
                Text(
                    text = detectedTransportMessage,
                    modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
                    fontSize = 14.sp
                )
            }

            if (!transferStatusMessage.isNullOrBlank()) {
                Text(
                    text = transferStatusMessage,
                    modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6366F1)
                )
            }

            // Progress ring
            if (isTransferring) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .size(100.dp)
                        .padding(top = 24.dp),
                    strokeWidth = 4.dp
                )

                Text(
                    text = "$progress%",
                    modifier = Modifier.padding(top = 8.dp),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Button(
                onClick = onClickCancel,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}

@Composable
fun TransferCompleteScreen(
    topBannerText: String,
    resultMessage: String,
    onClickOk: () -> Unit,
    isSuccess: Boolean = true,
) {
    var showCheckmark by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (showCheckmark) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutLinearInEasing),
        label = "checkmark_scale"
    )

    LaunchedEffect(Unit) {
        delay(300)
        showCheckmark = true
    }

    Scaffold(
        topBar = {
            TopBanner(
                text = topBannerText,
                onBackClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .background(
                        color = if (isSuccess) Color(0xFF10B981) else Color(0xFFEF4444),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isSuccess) "✓" else "✕",
                    fontSize = 60.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = resultMessage,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 24.dp, start = 16.dp, end = 16.dp),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onClickOk,
                modifier = Modifier.padding(top = 32.dp, bottom = 16.dp)
            ) {
                Text("OK")
            }
        }
    }
}

