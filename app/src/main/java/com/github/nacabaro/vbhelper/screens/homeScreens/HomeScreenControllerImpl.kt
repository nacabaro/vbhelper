package com.github.nacabaro.vbhelper.screens.homeScreens

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vbnfc.vb.SpecialMission
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import kotlinx.coroutines.launch
import java.time.Instant
import kotlin.math.roundToInt
import kotlin.random.Random

class HomeScreenControllerImpl(
    private val componentActivity: ComponentActivity,
): HomeScreenController {
    private val application = componentActivity.applicationContext as VBHelper
    private val database = application.container.db

    override fun didAdventureMissionsFinish(onCompletion: (Boolean) -> Unit) {
        componentActivity.lifecycleScope.launch {
            val currentTime = Instant.now().epochSecond
            val adventureCharacters = database
                .adventureDao()
                .getAdventureCharacters()

            val finishedAdventureCharacters = adventureCharacters.filter { character ->
                character.finishesAdventure <= currentTime
            }

            onCompletion(finishedAdventureCharacters.isNotEmpty())
        }
    }

    override fun clearSpecialMission(missionId: Long, missionCompletion: SpecialMission.Status, onCleared: (ItemDtos.PurchasedItem?) -> Unit) {
        componentActivity.lifecycleScope.launch {
            database
                .specialMissionDao()
                .clearSpecialMission(missionId)

            if (missionCompletion == SpecialMission.Status.COMPLETED) {
                val randomItem = database
                    .itemDao()
                    .getAllItems()
                    .random()

                val randomItemAmount = (Random.nextFloat() * 5).roundToInt()

                database
                    .itemDao()
                    .purchaseItem(
                        itemId = randomItem.id,
                        itemAmount = randomItemAmount
                    )

                val purchasedItem = ItemDtos.PurchasedItem(
                    itemId = randomItem.id,
                    itemName = randomItem.name,
                    itemDescription = randomItem.description,
                    itemIcon = randomItem.itemIcon,
                    itemLength = randomItem.itemLength,
                    itemAmount = randomItemAmount,
                    itemType = randomItem.itemType
                )

                onCleared(purchasedItem)
            } else {
                onCleared(null)
            }

        }
    }
}