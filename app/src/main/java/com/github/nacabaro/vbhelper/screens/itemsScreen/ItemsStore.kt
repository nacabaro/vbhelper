package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.source.ItemsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ItemsStore(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val itemsRepository = ItemsRepository(application.container.db)
    val myItems = remember { mutableStateOf(emptyList<ItemDtos.ItemsWithQuantities>()) }
    var selectedElementIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(itemsRepository) {
        withContext(Dispatchers.IO) {
            myItems.value = itemsRepository.getAllItems()
        }
    }

    if (myItems.value.isEmpty()) {
        Text("No items")
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
        ) {
            items(myItems.value) { index ->
                ItemElement(
                    itemIcon = getIconResource(index.itemIcon),
                    lengthIcon = getLengthResource(index.itemLength),
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = {
                        selectedElementIndex = myItems.value.indexOf(index)
                    }
                )
            }
        }

        if (selectedElementIndex != null) {
            ItemDialog(
                name = myItems.value[selectedElementIndex!!].name,
                description = myItems.value[selectedElementIndex!!].description,
                itemIcon = getIconResource(myItems.value[selectedElementIndex!!].itemIcon),
                lengthIcon = getLengthResource(myItems.value[selectedElementIndex!!].itemLength),
                amount = myItems.value[selectedElementIndex!!].quantity,
                onClickUse = { },
                onClickCancel = { selectedElementIndex = null }
            )
        }
    }
}