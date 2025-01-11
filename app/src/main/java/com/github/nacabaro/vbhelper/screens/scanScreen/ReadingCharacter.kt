package com.github.nacabaro.vbhelper.screens.scanScreen

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
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.components.TopBanner

@Composable
fun ReadingCharacterScreen(
    topBannerText: String,
    onClickCancel: () -> Unit,
) {
    Scaffold (
        topBar = {
            TopBanner(topBannerText)
        }
    ) { innerPadding ->
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Text("Place your Vital Bracelet near the reader...")
            Button(
                onClick = onClickCancel,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Cancel")
            }
        }
    }
}