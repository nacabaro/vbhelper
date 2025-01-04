package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.nacabaro.vbhelper.components.TopBanner

@Composable
fun ScanScreen() {
    Scaffold (
    topBar = { TopBanner(text = "Scan a Vital Bracelet") }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            ScanButton(
                text = "Vital Bracelet to App",
                onClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScanButton(
                text = "App to Vital Bracelet",
                onClick = {}
            )
        }
    }
}

@Composable
fun ScanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    ScanScreen()
}