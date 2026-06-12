package com.github.nacabaro.vbhelper.screens.homeScreens.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.screens.homeScreens.dialogs.DeleteSpecialMissionDialog
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.ItemDisplay
import com.github.nacabaro.vbhelper.components.SpecialMissionsEntry
import com.github.nacabaro.vbhelper.components.TransformationHistoryCard
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.domain.device_data.UnifiedCharacter
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.screens.homeScreens.HomeScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.itemsScreen.getIconResource
import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsScreenControllerImpl
import com.github.nacabaro.vbhelper.utils.BitmapData
import java.util.Locale

@Composable
fun UnifiedHomeScreen(
    activeMon: CharacterDtos.CharacterWithSprites,
    cardIcon: BitmapData,
    unifiedChar: UnifiedCharacter,
    specialMissions: List<SpecialMissions>,
    transformationHistory: List<CharacterDtos.TransformationHistory>,
    contentPadding: PaddingValues,
    homeScreenController: HomeScreenControllerImpl,
    onClickCollect: (ItemDtos.PurchasedItem?, Int?) -> Unit
) {
    var selectedSpecialMissionId by remember { mutableStateOf<Long>(-1) }

    Column(
        modifier = Modifier
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(state = rememberScrollState())
    ) {
        // CORE SLOT: Always the same for VB and BE
        Row(modifier = Modifier.fillMaxWidth()) {
            CharacterEntry(
                icon = BitmapData(
                    bitmap = activeMon.spriteIdle,
                    width = activeMon.spriteWidth,
                    height = activeMon.spriteHeight
                ),
                cardIcon = cardIcon,
                multiplier = 8,
                shape = androidx.compose.material.MaterialTheme.shapes.small,
                modifier = Modifier.weight(1f).aspectRatio(1f)
            )
            Column(modifier = Modifier.weight(0.5f).aspectRatio(0.5f)) {
                ItemDisplay(
                    icon = R.drawable.baseline_vitals_24,
                    textValue = activeMon.vitalPoints.toString(),
                    definition = stringResource(R.string.home_vbdim_vitals),
                    modifier = Modifier.weight(0.5f).aspectRatio(1f).padding(8.dp)
                )
                ItemDisplay(
                    icon = R.drawable.baseline_trophy_24,
                    textValue = activeMon.trophies.toString(),
                    definition = stringResource(R.string.home_vbdim_trophies),
                    modifier = Modifier.weight(0.5f).aspectRatio(1f).padding(8.dp)
                )
            }
        }

        // STATS ROW 1: mood and timing (VB timer vs BE training limit)
        Row(modifier = Modifier.fillMaxWidth()) {
            ItemDisplay(
                icon = R.drawable.baseline_mood_24,
                textValue = activeMon.mood.toString(),
                definition = stringResource(R.string.home_vbdim_mood),
                modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
            )
            
            if (unifiedChar.isVB()) {
                val transformationCountdownInHours = activeMon.transformationCountdown / 60
                ItemDisplay(
                    icon = R.drawable.baseline_next_24,
                    textValue = when (transformationCountdownInHours) {
                        0 -> "${activeMon.transformationCountdown} m"
                        else -> "$transformationCountdownInHours h"
                    },
                    definition = stringResource(R.string.home_vbdim_next_timer),
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
                )
            } else {
                val timeInHours = (unifiedChar.beData?.remainingTrainingTimeInMinutes ?: 0) / 60
                ItemDisplay(
                    icon = R.drawable.baseline_timer_24,
                    textValue = "$timeInHours h",
                    definition = "Training limit",
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
                )
            }

            if (unifiedChar.isBE()) {
                ItemDisplay(
                    icon = R.drawable.baseline_rank_24,
                    textValue = (unifiedChar.beData?.rank ?: 0).toString(),
                    definition = "Rank",
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
                )
            } else {
                // For VB, maybe put a spacer or other stat here to keep row balance
            }
        }

        // STATS ROW 2: Battle stats and Items
        Row(modifier = Modifier.fillMaxWidth()) {
            val transformationCountdownInHours = activeMon.transformationCountdown / 60
            ItemDisplay(
                icon = R.drawable.baseline_next_24,
                textValue = when (transformationCountdownInHours) {
                    0 -> "${activeMon.transformationCountdown} m"
                    else -> "$transformationCountdownInHours h"
                },
                definition = "Next timer",
                modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
            )
            
            // Battle stats (Common)
            ItemDisplay(
                icon = R.drawable.baseline_swords_24,
                textValue = calculateWinPercent(activeMon.totalBattlesWon, activeMon.totalBattlesLost),
                definition = "Total battle win %",
                modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
            )
            ItemDisplay(
                icon = R.drawable.baseline_swords_24,
                textValue = calculateWinPercent(activeMon.currentPhaseBattlesWon, activeMon.currentPhaseBattlesLost),
                definition = "Current phase win %",
                modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
            )

            if (unifiedChar.isBE()) {
                val beData = unifiedChar.beData
                if (beData != null && beData.itemRemainingTime != 0) {
                    ItemDisplay(
                        icon = getIconResource(beData.itemType),
                        textValue = "${beData.itemRemainingTime} m",
                        definition = getItemTypeName(beData.itemType),
                        modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
                    )
                }
            }
        }

        // HISTORY SLOT: Common
        Row(modifier = Modifier.fillMaxWidth()) {
            TransformationHistoryCard(
                transformationHistory = transformationHistory,
                modifier = Modifier.weight(1f).padding(8.dp)
            )
        }

        // CONTEXTUAL BOTTOM SLOT
        if (unifiedChar.isVB()) {
            Row(modifier = Modifier.padding(16.dp)) {
                Text(text = stringResource(R.string.home_vbdim_special_missions), fontSize = 24.sp)
            }
            for (mission in specialMissions) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    SpecialMissionsEntry(
                        specialMission = mission,
                        modifier = Modifier.weight(1f).padding(8.dp),
                        onClickMission = { missionId -> selectedSpecialMissionId = missionId },
                        onClickCollect = { homeScreenController.clearSpecialMission(selectedSpecialMissionId, onClickCollect) }
                    )
                }
            }
        } else if (unifiedChar.isBE()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                val beData = unifiedChar.beData ?: return@Column
                ItemDisplay(
                    icon = R.drawable.baseline_health_24,
                    textValue = "+${beData.trainingHp}",
                    definition = "Training HP",
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
                )
                ItemDisplay(
                    icon = R.drawable.baseline_agility_24,
                    textValue = "+${beData.trainingBp}",
                    definition = "Training BP",
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
                )
                ItemDisplay(
                    icon = R.drawable.baseline_attack_24,
                    textValue = "+${beData.trainingAp}",
                    definition = "Training AP",
                    modifier = Modifier.weight(1f).aspectRatio(1f).padding(8.dp)
                )
            }
        }
    }

    if (selectedSpecialMissionId.toInt() != -1 && unifiedChar.isVB()) {
        DeleteSpecialMissionDialog(
            onClickDismiss = { selectedSpecialMissionId = -1 },
            onClickDelete = {
                homeScreenController.clearSpecialMission(selectedSpecialMissionId, onClickCollect)
                selectedSpecialMissionId = -1
            }
        )
    }
}

private fun calculateWinPercent(won: Int, lost: Int): String {
    return if (lost == 0) "0.00 %" else {
        val percentage = won.toFloat() / (won + lost).toFloat()
        String.format(Locale.getDefault(), "%.2f", percentage * 100) + " %"
    }
}

private fun getItemTypeName(itemId: Int): String {
    return when (itemId) {
        ItemsScreenControllerImpl.ItemTypes.PPTraining.id -> "PP Training"
        ItemsScreenControllerImpl.ItemTypes.HPTraining.id -> "HP Training"
        ItemsScreenControllerImpl.ItemTypes.APTraining.id -> "AP Training"
        ItemsScreenControllerImpl.ItemTypes.BPTraining.id -> "BP Training"
        ItemsScreenControllerImpl.ItemTypes.AllTraining.id -> "All Training"
        else -> ""
    }
}
