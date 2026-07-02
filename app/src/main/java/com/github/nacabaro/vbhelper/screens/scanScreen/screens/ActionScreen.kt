package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.R

@Composable
fun ActionScreen(
    topBannerText: String,
    onClickCancel: () -> Unit,
) {
    Scaffold (
        topBar = {
            TopBanner(
                text = topBannerText,
                onBackClick = onClickCancel
            )
        }
    ) { innerPadding ->
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text(stringResource(R.string.action_place_near_reader))
            Text(
                stringResource(R.string.action_keep_device_stable),
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Button(
                onClick = onClickCancel,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    }
}