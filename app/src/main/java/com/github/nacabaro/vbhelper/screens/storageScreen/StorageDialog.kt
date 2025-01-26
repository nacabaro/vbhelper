package com.github.nacabaro.vbhelper.screens.storageScreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap
import kotlinx.coroutines.launch

@Composable
fun StorageDialog(
    characterId: Long,
    onDismissRequest: () -> Unit,
    onSendToBracelet: () -> Unit,
    onClickSetActive: () -> Unit,
    onClickSendToAdventure: (time: Long) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val character = remember { mutableStateOf<CharacterDtos.CharacterWithSprites?>(null) }
    val characterSprite = remember { mutableStateOf<BitmapData?>(null) }
    val characterName = remember { mutableStateOf<BitmapData?>(null) }
    var onSendToAdventureClicked by remember { mutableStateOf(false) }

    LaunchedEffect(storageRepository) {
        coroutineScope.launch {
            character.value = storageRepository.getSingleCharacter(characterId)
            characterSprite.value = BitmapData(
                bitmap = character.value!!.spriteIdle,
                width = character.value!!.spriteWidth,
                height = character.value!!.spriteHeight
            )
            characterName.value = BitmapData(
                bitmap = character.value!!.nameSprite,
                width = character.value!!.nameSpriteWidth,
                height = character.value!!.nameSpriteHeight
            )
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column (
                modifier = Modifier
                    .padding(16.dp)
            ) {
                if (character.value != null &&
                    characterSprite.value != null &&
                    characterName.value != null
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val bitmap = remember (characterSprite.value!!) { characterSprite.value!!.getBitmap() }
                        val imageBitmap = remember(bitmap) { bitmap.asImageBitmap() }
                        val density: Float = LocalContext.current.resources.displayMetrics.density
                        val dpSize = (characterSprite.value!!.width * 4 / density).dp
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Character image",
                            filterQuality = FilterQuality.None,
                            modifier = Modifier
                                .size(dpSize)
                        )
                        val nameBitmap = remember (characterName.value!!) { characterName.value!!.getBitmap() }
                        val nameImageBitmap = remember(nameBitmap) { nameBitmap.asImageBitmap() }
                        val nameDpSize = (characterName.value!!.width * 4 / density).dp
                        Image(
                            bitmap = nameImageBitmap,
                            contentDescription = "Character image",
                            filterQuality = FilterQuality.None,
                            modifier = Modifier
                                .size(nameDpSize)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = onSendToBracelet,
                    ) {
                        Text(text = "Send to bracelet")
                    }
                    Spacer(
                        modifier = Modifier
                            .padding(4.dp)
                    )
                    Button(
                        onClick = onClickSetActive,
                    ) {
                        Text(text = "Set active")
                    }
                }
                Button(
                    onClick = {
                        onSendToAdventureClicked = true
                    },
                ) {
                    Text(text = "Send to adventure")
                }
                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onClick = onDismissRequest
                ) {
                    Text(text = "Close")
                }
            }
        }
    }

    if (onSendToAdventureClicked) {
        StorageAdventureTimeDialog(
            onClickSendToAdventure = { time ->
                onClickSendToAdventure(time)
            },
            onDismissRequest = { onSendToAdventureClicked = false }
        )
    }
}