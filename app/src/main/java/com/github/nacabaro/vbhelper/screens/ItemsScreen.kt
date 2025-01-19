package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsStore
import com.github.nacabaro.vbhelper.screens.itemsScreen.MyItems

@Composable
fun ItemsScreen(
    navController: NavController,
) {
    var selectedTabItem by remember { mutableStateOf(0) }
    val items = listOf(
        NavigationItems.MyItems,
        NavigationItems.ItemsStore
    )
    Scaffold(
        topBar = {
            Column {
                TopBanner("Items")
                TabRow(
                    selectedTabIndex = selectedTabItem,
                    modifier = Modifier
                ) {
                    items.forEachIndexed { index, item ->
                        Tab(
                            text = { Text(item.label) },
                            selected = selectedTabItem == index,
                            onClick = { selectedTabItem = index }
                        )
                    }
                }
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            when (selectedTabItem) {
                0 -> MyItems(navController)
                1 -> ItemsStore(navController)
            }
        }
    }
}