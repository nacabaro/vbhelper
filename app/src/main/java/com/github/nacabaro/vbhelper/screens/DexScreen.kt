package com.github.nacabaro.vbhelper.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.Dim
import com.github.nacabaro.vbhelper.navigation.BottomNavItem
import com.github.nacabaro.vbhelper.source.DexRepository
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

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
                    navController.navigate(BottomNavItem.Viewer.route)
                }
            )
        }
    ) { contentPadding ->
        LazyColumn (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            items(dimList.value) {
                val bitmap = remember (it.logo) {
                    Bitmap.createBitmap(it.logoWidth, it.logoHeight, Bitmap.Config.RGB_565).apply {
                        copyPixelsFromBuffer(ByteBuffer.wrap(it.logo))
                    }
                }
                val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }

                DexDiMEntry(
                    name = it.name,
                    logo = imageBitmap,
                    onClick = {
                        navController
                            .navigate(
                                BottomNavItem
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

@Composable
fun DexDiMEntry(
    name: String,
    logo: ImageBitmap,
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
                bitmap = logo,
                contentDescription = name,
                filterQuality = FilterQuality.None,
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