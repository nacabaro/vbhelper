package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.source.ItemsRepository

@Composable
fun MyItems(
    navController: NavController,
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val itemsRepository = ItemsRepository(application.container.db)
    val myItems by itemsRepository.getUserItems().collectAsState(emptyList())

    var selectedElementIndex by remember { mutableStateOf<Int?>(null) }

    if (myItems.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("No items")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
        ) {
            items(myItems) { index ->
                ItemElement(
                    item = index,
                    modifier = Modifier
                        .padding(8.dp),
                    onClick = {
                        selectedElementIndex = myItems.indexOf(index)
                    }
                )
            }
        }

        if (selectedElementIndex != null) {
            ItemDialog(
                item = myItems[selectedElementIndex!!],
                onClickUse = {
                    navController
                        .navigate(
                            NavigationItems
                                .ApplyItem.route
                                .replace(
                                    "{itemId}",
                                    myItems[selectedElementIndex!!].id.toString()
                                )
                        )
                    selectedElementIndex = null
                },
                onClickCancel = { selectedElementIndex = null }
            )
        }
    }
}