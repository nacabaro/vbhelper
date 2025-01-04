package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.components.TopBanner

@Composable
fun StorageScreen() {
    Scaffold (
        topBar = { TopBanner(text = "My Digimon") }
    ) { contentPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(100) { i ->
                StorageEntry(
                    name = "Digimon $i",
                    icon = R.drawable.baseline_question_mark_24
                )
            }
        }
    }
}

@Composable
fun StorageEntry(
    name: String,
    icon: Int,
    modifier: Modifier = Modifier
) {
    Card (
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .padding(8.dp)
    ) {
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = name,
                modifier = Modifier
                    .padding(8.dp)
                    .size(64.dp)
            )
            Text(
                text = name,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(8.dp)
            )
        }
    }
}