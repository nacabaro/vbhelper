package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.components.TopBanner

@Composable
fun DexScreen() {
    Scaffold (
        topBar = { TopBanner("Discovered Digimon") }
    ) { contentPadding ->
        LazyColumn (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(100) { i ->
                DexDiMEntry(
                    name = "Digimon $i",
                    icon = R.drawable.baseline_egg_24,
                    onClick = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            vertical = 8.dp,
                            horizontal = 16.dp
                        )
                )
            }
        }
    }
}

@Composable
fun DexDiMEntry(
    name: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card (
        shape = MaterialTheme.shapes.medium,
        modifier = modifier,
        onClick = onClick
    ) {
        Row (
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Image (
                painter = painterResource(id = icon),
                contentDescription = name,
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
            )
            Text(
                text = name,
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}