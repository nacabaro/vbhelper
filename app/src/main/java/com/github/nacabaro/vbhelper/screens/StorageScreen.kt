package com.github.nacabaro.vbhelper.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryCharacterData
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


@Composable
fun StorageScreen() {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val monList = remember { mutableStateListOf<TemporaryCharacterData>() }

    var selectedCharacter by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(storageRepository) {
        coroutineScope.launch {
            monList.clear()
            monList.addAll(storageRepository.getAllCharacters())
            Log.d("StorageScreen", "Updated data: $monList")
        }
    }

    Log.d("StorageScreen", "monList: $monList")

    Scaffold (
        topBar = { TopBanner(text = "My Digimon") }
    ) { contentPadding ->
        if (monList.isEmpty()) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            ) {
                Text(
                    text = "Nothing to see here",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .scrollable(state = rememberScrollState(), orientation = Orientation.Vertical)
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(monList) { index ->
                var showDialog by rememberSaveable { mutableStateOf(false) }

                StorageEntry(
                    name = index.dimId.toString() + " - " + index.charIndex.toString(),
                    icon = R.drawable.ic_launcher_foreground,
                    onClick = { selectedCharacter = index.id },
                    modifier = Modifier
                        .padding(8.dp)
                )

                if (selectedCharacter != null) {
                    StorageDialog(
                        characterId = selectedCharacter!!,
                        onDismissRequest = { selectedCharacter = null }
                    )
                }
            }
        }
    }
}

@Composable
fun StorageEntry(
    name: String,
    icon: Int,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        shape = MaterialTheme.shapes.medium,
        onClick = onClick,
        modifier = modifier
            .padding(8.dp)
    ) {
        Column(
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

@Composable
fun StorageDialog(
    characterId: Long,
    onDismissRequest: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val character = remember { mutableStateOf<TemporaryCharacterData?>(null) }

    LaunchedEffect(storageRepository) {
        coroutineScope.launch {
            character.value = storageRepository.getSingleCharacter(characterId)
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                if (character.value != null) {
                    Text(
                        text = character.value?.toString() ?: "Loading...",
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                }
                Button(
                    onClick = onDismissRequest
                ) {
                    Text(text = "Close")
                }
            }
        }
    }
}