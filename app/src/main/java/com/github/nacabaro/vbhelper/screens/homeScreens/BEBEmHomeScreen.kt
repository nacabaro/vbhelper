package com.github.nacabaro.vbhelper.screens.homeScreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.ItemDisplay
import com.github.nacabaro.vbhelper.components.TransformationHistoryCard
import com.github.nacabaro.vbhelper.components.getIconResource
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsScreenControllerImpl
import com.github.nacabaro.vbhelper.utils.BitmapData
import java.util.Locale

@Composable
fun BEBEmHomeScreen(
    activeMon: CharacterDtos.CharacterWithSprites,
    beData: BECharacterData,
    transformationHistory: List<CharacterDtos.TransformationHistory>,
    contentPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(state = rememberScrollState())
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CharacterEntry(
                icon = BitmapData(
                    bitmap = activeMon.spriteIdle,
                    width = activeMon.spriteWidth,
                    height = activeMon.spriteHeight
                ),
                multiplier = 8,
                shape = androidx.compose.material.MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            Column (
                modifier = Modifier
                    .weight(0.5f)
                    .aspectRatio(0.5f)
            ) {
                ItemDisplay(
                    icon = R.drawable.baseline_vitals_24,
                    textValue = activeMon.vitalPoints.toString(),
                    definition = "Vitals",
                    modifier = Modifier
                        .weight(0.5f)
                        .aspectRatio(1f)
                        .padding(8.dp)
                )
                ItemDisplay(
                    icon = R.drawable.baseline_trophy_24,
                    textValue = activeMon.trophies.toString(),
                    definition = "Trophies",
                    modifier = Modifier
                        .weight(0.5f)
                        .aspectRatio(1f)
                        .padding(8.dp)
                )
            }
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ItemDisplay(
                icon = R.drawable.baseline_mood_24,
                textValue = activeMon.mood.toString(),
                definition = "Mood",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            val timeInHours = (beData.remainingTrainingTimeInMinutes / 60)
            ItemDisplay(
                icon = R.drawable.baseline_timer_24,
                textValue = "$timeInHours h",
                definition = "Training limit",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            ItemDisplay(
                icon = R.drawable.baseline_rank_24,
                textValue = beData.rank.toString(),
                definition = "Rank",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val transformationCountdownInHours = activeMon.transformationCountdown / 60
            ItemDisplay(
                icon = R.drawable.baseline_next_24,
                textValue = when (transformationCountdownInHours) {
                    0 -> "${activeMon.transformationCountdown} m"
                    else -> "$transformationCountdownInHours h"
                },
                definition = "Next timer",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            ItemDisplay(
                icon = R.drawable.baseline_swords_24,
                textValue = when {
                    activeMon.totalBattlesLost == 0 -> "0.00 %"
                    else -> {
                        val battleWinPercentage = activeMon.totalBattlesWon.toFloat() / (activeMon.totalBattlesWon + activeMon.totalBattlesLost).toFloat()
                        String.format(Locale.getDefault(), "%.2f", battleWinPercentage * 100) + " %" // Specify locale
                    }
                },
                definition = "Total battle win %",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            ItemDisplay(
                icon = R.drawable.baseline_swords_24,
                textValue = when {
                    activeMon.totalBattlesLost == 0 -> "0.00 %"
                    else -> {
                        val battleWinPercentage = activeMon.currentPhaseBattlesWon.toFloat() / (activeMon.currentPhaseBattlesWon + activeMon.currentPhaseBattlesLost).toFloat()
                        String.format(Locale.getDefault(), "%.2f", battleWinPercentage * 100) + " %" // Specify locale
                    }
                },
                definition = "Current phase win %",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            if (beData.itemRemainingTime != 0) {
                ItemDisplay(
                    icon = getIconResource(beData.itemType),
                    textValue = "${beData.itemRemainingTime} m",
                    definition = when (beData.itemType) {
                        ItemsScreenControllerImpl.ItemTypes.PPTraining.id -> "PP Training"
                        ItemsScreenControllerImpl.ItemTypes.HPTraining.id -> "HP Training"
                        ItemsScreenControllerImpl.ItemTypes.APTraining.id -> "AP Training"
                        ItemsScreenControllerImpl.ItemTypes.BPTraining.id -> "BP Training"
                        ItemsScreenControllerImpl.ItemTypes.AllTraining.id -> "All Training"
                        else -> ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .padding(8.dp)
                )
            }
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            TransformationHistoryCard(
                transformationHistory = transformationHistory,
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            )
        }
        Row (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ItemDisplay(
                icon = R.drawable.baseline_health_24,
                textValue = "+${beData.trainingHp}",
                definition = "Training HP",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            ItemDisplay(
                icon = R.drawable.baseline_agility_24,
                textValue = "+${beData.trainingBp}",
                definition = "Training BP",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            ItemDisplay(
                icon = R.drawable.baseline_attack_24,
                textValue = "+${beData.trainingAp}",
                definition = "Training AP",
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
        }
    }
}