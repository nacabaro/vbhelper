package com.github.nacabaro.vbhelper.screens.itemsScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.source.CurrencyRepository
import com.github.nacabaro.vbhelper.source.ItemsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun ItemsStore(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val itemsRepository = ItemsRepository(application.container.db)
    val myItems by itemsRepository.getAllItems().collectAsState(emptyList())

    var selectedElementIndex by remember { mutableStateOf<Int?>(null) }

    val currencyRepository = application.container.currencyRepository
    val currentCurrency = currencyRepository.currencyValue.collectAsState(0)

    val scope = rememberCoroutineScope()

    if (myItems.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("No items")
        }
    } else {
        Column() {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "${currentCurrency.value} credits",
                    modifier = Modifier
                        .padding(8.dp)
                )
            }

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
        }
    }


    if (selectedElementIndex != null) {
        ItemDialog(
            item = myItems[selectedElementIndex!!],
            onClickPurchase = {
                scope.launch {
                    Toast.makeText(
                        application.applicationContext,
                        purchaseItem(
                            application.container.db,
                            myItems[selectedElementIndex!!],
                            currencyRepository
                        ),
                        Toast.LENGTH_SHORT
                    ).show(
                    )
                }
            },
            onClickCancel = { selectedElementIndex = null }
        )
    }
}

suspend fun purchaseItem(
    db: AppDatabase,
    item: ItemDtos.ItemsWithQuantities,
    currencyRepository: CurrencyRepository
): String {
    if (currencyRepository.currencyValue.first() < item.price) {
        return "Not enough credits"
    } else {
        db
            .itemDao()
            .purchaseItem(
                item.id,
                1
            )

        currencyRepository
            .setCurrencyValue(
                currencyRepository.currencyValue.first() - item.price
            )

        return "Purchase successful!"
    }
}