package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.components.DexDiMEntry
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.Dim
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.source.DexRepository
import kotlinx.coroutines.launch

@Composable
fun DexScreen(
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val dexRepository = DexRepository(application.container.db)

    val dimList = remember { mutableStateOf<List<Dim>>(emptyList()) }

    LaunchedEffect(dexRepository) {
        coroutineScope.launch {
            val newDimList = dexRepository.getAllDims()
            dimList.value = newDimList // Replace the entire list atomically
        }
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = "Discovered Digimon",
                onGearClick = {
                    navController.navigate(NavigationItems.Viewer.route)
                }
            )
        }
    ) { contentPadding ->
        LazyColumn (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(dimList.value) {
                DexDiMEntry(
                    name = it.name,
                    logo = BitmapData(
                        bitmap = it.logo,
                        width = it.logoWidth,
                        height = it.logoHeight
                    ),
                    onClick = {
                        navController
                            .navigate(
                                NavigationItems
                                    .CardView.route
                                    .replace("{dimId}", "${it.id}")
                            )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}

