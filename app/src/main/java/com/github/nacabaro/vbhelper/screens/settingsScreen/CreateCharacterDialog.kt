package com.github.nacabaro.vbhelper.screens.settingsScreen

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.card.Card as CardModel
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.screens.settingsScreen.controllers.CreateCharacterController
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CreateCharacterDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val database = remember { (context.applicationContext as VBHelper).container.db }
    val scope = rememberCoroutineScope()

    var cards by remember { mutableStateOf<List<CardModel>>(emptyList()) }
    var selectedCard by remember { mutableStateOf<CardModel?>(null) }
    var options by remember { mutableStateOf<List<CharacterDtos.CardCharacterOption>>(emptyList()) }
    var selectedOption by remember { mutableStateOf<CharacterDtos.CardCharacterOption?>(null) }

    var age by remember { mutableStateOf("5") }
    var vitals by remember { mutableStateOf("0") }
    var trophies by remember { mutableStateOf("0") }
    var won by remember { mutableStateOf("0") }
    var lost by remember { mutableStateOf("0") }

    LaunchedEffect(Unit) {
        cards = withContext(Dispatchers.IO) { database.cardDao().getAllCards() }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_create_character_title),
                    style = MaterialTheme.typography.titleLarge
                )

                val card = selectedCard
                val option = selectedOption
                when {
                    card == null -> {
                        Text(
                            text = stringResource(R.string.settings_create_character_pick_card),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        if (cards.isEmpty()) {
                            Text(stringResource(R.string.settings_create_character_no_cards))
                        }
                        LazyColumn(modifier = Modifier.height(320.dp)) {
                            items(cards) { item ->
                                Text(
                                    text = item.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCard = item
                                            scope.launch {
                                                options = withContext(Dispatchers.IO) {
                                                    database.characterDao().getCharactersOfCard(item.id)
                                                }
                                            }
                                        }
                                        .padding(vertical = 12.dp)
                                )
                            }
                        }
                    }

                    option == null -> {
                        Text(
                            text = stringResource(R.string.settings_create_character_pick_character, card.name),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        LazyColumn(modifier = Modifier.height(320.dp)) {
                            items(options) { item ->
                                CharacterOptionRow(item) { selectedOption = item }
                            }
                        }
                    }

                    else -> {
                        CharacterOptionRow(option) {}
                        Text(
                            text = stringResource(R.string.settings_create_character_stats_hint),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        NumberField(stringResource(R.string.settings_create_character_age), age) { age = it }
                        NumberField(stringResource(R.string.settings_create_character_vitals), vitals) { vitals = it }
                        NumberField(stringResource(R.string.settings_create_character_trophies), trophies) { trophies = it }
                        NumberField(stringResource(R.string.settings_create_character_won), won) { won = it }
                        NumberField(stringResource(R.string.settings_create_character_lost), lost) { lost = it }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.item_dialog_cancel))
                    }
                    if (card != null && option != null) {
                        Button(onClick = {
                            val stats = CreateCharacterController.Stats(
                                ageInDays = age.toIntOrNull() ?: 5,
                                vitalPoints = vitals.toIntOrNull() ?: 0,
                                trophies = trophies.toIntOrNull() ?: 0,
                                battlesWon = won.toIntOrNull() ?: 0,
                                battlesLost = lost.toIntOrNull() ?: 0
                            )
                            scope.launch(Dispatchers.IO) {
                                CreateCharacterController(database).createCharacter(card, option.id, stats)
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.settings_create_character_done),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onDismiss()
                                }
                            }
                        }) {
                            Text(stringResource(R.string.settings_create_character_create))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CharacterOptionRow(
    item: CharacterDtos.CardCharacterOption,
    onClick: () -> Unit
) {
    val idle = remember(item.id) { BitmapData(item.spriteIdle, item.spriteWidth, item.spriteHeight).getBitmap().asImageBitmap() }
    val name = remember(item.id) { BitmapData(item.nameSprite, item.nameWidth, item.nameHeight).getBitmap().asImageBitmap() }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Image(
                bitmap = idle,
                contentDescription = null,
                filterQuality = FilterQuality.None,
                modifier = Modifier.size(64.dp)
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Image(
                    bitmap = name,
                    contentDescription = null,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.height((item.nameHeight * 2).dp)
                )
                Text(
                    text = stringResource(R.string.settings_create_character_stage, item.stage, item.charaIndex),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    )
}
