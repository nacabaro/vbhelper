package com.github.nacabaro.vbhelper.screens.adventureScreen

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

class AdventureScreenControllerImpl(
    private val componentActivity: ComponentActivity,
) : AdventureScreenController {
    private val application = componentActivity.applicationContext as VBHelper
    private val database = application.container.db

    override fun sendCharacterToAdventure(characterId: Long, timeInMinutes: Long) {
        val finishesAdventureAt = timeInMinutes * 60
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            val characterData = database
                .userCharacterDao()
                .getCharacter(characterId)

            if (characterData.isActive) {
                database
                    .userCharacterDao()
                    .clearActiveCharacter()
            }

            database
                .adventureDao()
                .insertNewAdventure(characterId, timeInMinutes, finishesAdventureAt)
        }
    }

    override fun getItemFromAdventure(
        characterId: Long,
        onResult: (ItemDtos.PurchasedItem) -> Unit
    ) {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            database
                .adventureDao()
                .deleteAdventure(characterId)

            val generatedItem = generateItem(characterId)

            onResult(generatedItem)
        }
    }

    override fun cancelAdventure(characterId: Long, onResult: () -> Unit) {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            database
                .adventureDao()
                .deleteAdventure(characterId)

            componentActivity
                .runOnUiThread {
                    Toast.makeText(
                        componentActivity,
                        "Adventure canceled",
                        Toast.LENGTH_SHORT
                    ).show()
                    onResult()
                }

        }
    }

    private suspend fun generateItem(characterId: Long): ItemDtos.PurchasedItem {
        val character = database
            .userCharacterDao()
            .getCharacterInfo(characterId)

        val randomItem = database
            .itemDao()
            .getAllItems()
            .first()
            .random()

        val random = ((Random.nextFloat() * character.stage) + 3).roundToInt()

        database
            .itemDao()
            .purchaseItem(
                itemId = randomItem.id,
                itemAmount = random
            )

        return ItemDtos.PurchasedItem(
            itemId = randomItem.id,
            itemAmount = random,
            itemName = randomItem.name,
            itemIcon = randomItem.itemIcon,
            itemLength = randomItem.itemLength,
            itemDescription = randomItem.description,
            itemType = randomItem.itemType
        )
    }
}