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
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.components.CharacterEntry
import com.github.nacabaro.vbhelper.components.ItemDisplay
import com.github.nacabaro.vbhelper.components.SpecialMissionsEntry
import com.github.nacabaro.vbhelper.components.TransformationHistoryCard
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.screens.homeScreens.HomeScreenControllerImpl
import com.github.nacabaro.vbhelper.utils.BitmapData
import java.util.Locale
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.screens.homeScreens.dialogs.DeleteSpecialMissionDialog


@Composable
fun VBDiMHomeScreen(
    activeMon: CharacterDtos.CharacterWithSprites,
    cardIcon: BitmapData,
    vbData: VBCharacterData,
    specialMissions: List<SpecialMissions>,
    homeScreenController: HomeScreenControllerImpl,
    transformationHistory: List<CharacterDtos.TransformationHistory>,
    contentPadding: PaddingValues,
    onClickCollect: (ItemDtos.PurchasedItem?, Int?) -> Unit
) {
    var selectedSpecialMissionId by remember { mutableStateOf<Long>(-1) }

    Column(
        modifier = Modifier
            .padding(top = contentPadding.calculateTopPadding())
            .verticalScroll(state = rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CharacterEntry(
                icon = BitmapData(
                    bitmap = activeMon.spriteIdle,
                    width = activeMon.spriteWidth,
                    height = activeMon.spriteHeight
                ),
                cardIcon = cardIcon,
                multiplier = 8,
                shape = androidx.compose.material.MaterialTheme.shapes.small,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
            Column(
                modifier = Modifier
                    .weight(0.5f)
                    .aspectRatio(0.5f)
            ) {
                ItemDisplay(
                    icon = R.drawable.baseline_vitals_24,
                    textValue = activeMon.vitalPoints.toString(),
                    definition = stringResource(R.string.home_vbdim_vitals),
                    modifier = Modifier
                        .weight(0.5f)
                        .aspectRatio(1f)
                        .padding(8.dp)
                )
                ItemDisplay(
                    icon = R.drawable.baseline_trophy_24,
                    textValue = activeMon.trophies.toString(),
                    definition = stringResource(R.string.home_vbdim_trophies),
                    modifier = Modifier
                        .weight(0.5f)
                        .aspectRatio(1f)
                        .padding(8.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ItemDisplay(
                icon = R.drawable.baseline_mood_24,
                textValue = activeMon.mood.toString(),
                definition = stringResource(R.string.home_vbdim_mood),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            val transformationCountdownInHours = activeMon.transformationCountdown / 60
            ItemDisplay(
                icon = R.drawable.baseline_next_24,
                textValue = when (transformationCountdownInHours) {
                    0 -> "${activeMon.transformationCountdown} m"
                    else -> "$transformationCountdownInHours h"
                },
                definition = stringResource(R.string.home_vbdim_next_timer),
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
                        val battleWinPercentage =
                            activeMon.totalBattlesWon.toFloat() / (activeMon.totalBattlesWon + activeMon.totalBattlesLost).toFloat()
                        String.format(
                            Locale.getDefault(),
                            "%.2f",
                            battleWinPercentage * 100
                        ) + " %" // Specify locale
                    }
                },
                definition = stringResource(R.string.home_vbdim_total_battle_win),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
            ItemDisplay(
                icon = R.drawable.baseline_swords_24,
                textValue = when {
                    activeMon.currentPhaseBattlesWon + activeMon.currentPhaseBattlesLost == 0 -> "0.00 %"
                    else -> {
                        val battleWinPercentage =
                            activeMon.currentPhaseBattlesWon.toFloat() / (activeMon.currentPhaseBattlesWon + activeMon.currentPhaseBattlesLost).toFloat()
                        String.format(
                            Locale.getDefault(),
                            "%.2f",
                            battleWinPercentage * 100
                        ) + " %"
                    }
                },
                definition = stringResource(R.string.home_vbdim_current_phase_win),
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .padding(8.dp)
            )
        }
        Row(
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
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.home_vbdim_special_missions),
                fontSize = 24.sp
                )
        }
        for (mission in specialMissions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                SpecialMissionsEntry(
                    specialMission = mission,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    onClickMission = { missionId ->
                        selectedSpecialMissionId = missionId
                    },
                    onClickCollect = { missionId ->
                        homeScreenController
                            .clearSpecialMission(missionId, onClickCollect)
                    }
                )
            }
        }
    }

    if (selectedSpecialMissionId.toInt() != -1) {
        DeleteSpecialMissionDialog(
            onClickDismiss = {
                selectedSpecialMissionId = -1
            },
            onClickDelete = {
                homeScreenController
                    .clearSpecialMission(selectedSpecialMissionId, onClickCollect)
                selectedSpecialMissionId = -1
            }
        )
    }
}