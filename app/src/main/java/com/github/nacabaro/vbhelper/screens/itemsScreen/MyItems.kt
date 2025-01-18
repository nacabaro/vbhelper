package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.ItemElement
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.source.ItemsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MyItems(
    navController: NavController,
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val itemsRepository = ItemsRepository(application.container.db)
    val myItems = remember { mutableStateOf(emptyList<ItemDtos.ItemsWithQuantities>()) }

    LaunchedEffect(itemsRepository) {
        withContext(Dispatchers.IO) {
            myItems.value = itemsRepository.getUserItems()
        }
    }

    Scaffold (
        topBar = { TopBanner("Available items") }
    ) { contentPadding ->
        if (myItems.value.isEmpty()) {
            Text("No items")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = contentPadding
            ) {
                items(myItems.value) { index ->
                    ItemElement(
                        itemIcon = index.itemIcon,
                        lengthIcon = index.lengthIcon,
                        modifier = Modifier
                            .padding(8.dp),
                        onClick = {  }
                    )
                }
            }
        }
    }
}