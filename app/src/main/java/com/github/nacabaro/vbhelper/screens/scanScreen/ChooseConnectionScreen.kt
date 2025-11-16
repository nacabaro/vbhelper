package com.github.nacabaro.vbhelper.screens.scanScreen

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner

@Composable
fun ChooseConnectOption(
    onClickRead: (() -> Unit)? = null,
    onClickWrite: (() -> Unit)? = null,
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopBanner(
                text = "Scan a Vital Bracelet",
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
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
                disabled = onClickRead == null,
                onClick = onClickRead?: {  },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScanButton(
                text = "App to Vital Bracelet",
                disabled = onClickWrite == null,
                onClick = onClickWrite?: {  },
            )
        }
    }
}


@Composable
fun ScanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = !disabled,
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(4.dp)
        )
    }
}