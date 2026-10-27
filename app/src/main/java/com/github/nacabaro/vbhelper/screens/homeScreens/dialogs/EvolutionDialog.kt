package com.github.nacabaro.vbhelper.screens.homeScreens.dialogs

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.screens.homeScreens.EvolutionController
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class Requirement(
    val label: String,
    val have: String,
    val need: String,
    val met: Boolean
)

@Composable
fun EvolutionDialog(
    activeMon: CharacterDtos.CharacterWithSprites,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { (context.applicationContext as VBHelper).container.db }
    val scope = rememberCoroutineScope()

    val options by remember(activeMon.charId) {
        database.characterDao().getEvolutionRequirementsForCard(activeMon.charId)
    }.collectAsState(initial = emptyList())

    // Next adventure stage of the card: the levels completed are the ones before it
    val nextAdventureStage by produceState(initialValue = 1, activeMon.charId) {
        val cardId = withContext(Dispatchers.IO) {
            database.characterDao().getCardCharacterById(activeMon.charId).cardId
        }
        database.cardProgressDao().getCardProgress(cardId).collect { value = it }
    }

    val battles = activeMon.currentPhaseBattlesWon + activeMon.currentPhaseBattlesLost
    val winRate = if (battles == 0) 0 else activeMon.currentPhaseBattlesWon * 100 / battles

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.home_evolve_title),
                    style = MaterialTheme.typography.titleLarge
                )

                if (options.isEmpty()) {
                    Text(
                        text = stringResource(R.string.home_evolve_none),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    for (option in options) {
                        val requirements = buildList {
                            if (option.requiredVitals > 0) add(
                                Requirement(
                                    stringResource(R.string.home_evolve_vitals),
                                    activeMon.vitalPoints.toString(),
                                    option.requiredVitals.toString(),
                                    activeMon.vitalPoints >= option.requiredVitals
                                )
                            )
                            if (option.requiredTrophies > 0) add(
                                Requirement(
                                    stringResource(R.string.home_evolve_trophies),
                                    activeMon.trophies.toString(),
                                    option.requiredTrophies.toString(),
                                    activeMon.trophies >= option.requiredTrophies
                                )
                            )
                            if (option.requiredBattles > 0) add(
                                Requirement(
                                    stringResource(R.string.home_evolve_battles),
                                    battles.toString(),
                                    option.requiredBattles.toString(),
                                    battles >= option.requiredBattles
                                )
                            )
                            if (option.requiredWinRate > 0) add(
                                Requirement(
                                    stringResource(R.string.home_evolve_win_rate),
                                    "$winRate %",
                                    "${option.requiredWinRate} %",
                                    winRate >= option.requiredWinRate
                                )
                            )
                            if (option.requiredAdventureLevelCompleted > 0) add(
                                Requirement(
                                    stringResource(R.string.home_evolve_adventure),
                                    (nextAdventureStage - 1).toString(),
                                    option.requiredAdventureLevelCompleted.toString(),
                                    nextAdventureStage - 1 >= option.requiredAdventureLevelCompleted
                                )
                            )
                        }
                        val allMet = requirements.all { it.met }
                        val sprite = remember(option.charaId) {
                            BitmapData(option.spriteIdle, option.spriteWidth, option.spriteHeight)
                                .getBitmap().asImageBitmap()
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Image(
                                        bitmap = sprite,
                                        contentDescription = null,
                                        filterQuality = FilterQuality.None,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        if (option.nameSprite != null && option.nameWidth > 0) {
                                            val nameBitmap = remember(option.charaId) {
                                                BitmapData(option.nameSprite, option.nameWidth, option.nameHeight)
                                                    .getBitmap().asImageBitmap()
                                            }
                                            Image(
                                                bitmap = nameBitmap,
                                                contentDescription = null,
                                                filterQuality = FilterQuality.None,
                                                modifier = Modifier
                                                    .height(option.nameHeight.dp * 2)
                                                    .padding(bottom = 4.dp)
                                            )
                                        }
                                        for (req in requirements) {
                                            Text(
                                                text = (if (req.met) "✓ " else "✗ ") +
                                                        "${req.label}: ${req.have} / ${req.need}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (req.met) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.error
                                            )
                                        }
                                        if (requirements.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.home_evolve_no_requirements),
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }
                                }
                                Button(
                                    enabled = allMet,
                                    onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            EvolutionController(database)
                                                .evolve(activeMon.id, option.charaId)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.home_evolve_done),
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                onDismiss()
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) {
                                    Text(stringResource(R.string.home_evolve_button))
                                }
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.item_dialog_cancel))
                    }
                }
            }
        }
    }
}
