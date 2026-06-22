package com.github.nacabaro.vbhelper.screens.homeScreens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.utils.DeviceType
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.screens.homeScreens.screens.BEBEmHomeScreen
import com.github.nacabaro.vbhelper.screens.homeScreens.screens.BEDiMHomeScreen
import com.github.nacabaro.vbhelper.screens.homeScreens.screens.VBDiMHomeScreen
import com.github.nacabaro.vbhelper.screens.itemsScreen.ObtainedItemDialog
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.source.CardRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import kotlinx.coroutines.flow.flowOf
import kotlin.collections.emptyList

@Composable
fun HomeScreen(
    navController: NavController,
    homeScreenController: HomeScreenControllerImpl
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val cardRepository = CardRepository(application.container.db)

    val activeMon by storageRepository
        .getActiveCharacter()
        .collectAsState(initial = null)

    val cardIconData by (
        activeMon
            ?.let { chara ->
                cardRepository.getCardIconByCharaId(chara.charId)
            }
            ?: flowOf<CardDtos.CardIcon?>(null)
    ).collectAsState(initial = null)

    val transformationHistory by (
        activeMon
            ?.let { chara ->
                storageRepository.getTransformationHistory(chara.id)
            }
            ?: flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    val vbSpecialMissions by (
        activeMon
            ?.takeIf { it.characterType == DeviceType.VBDevice }
            ?.let { chara ->
                storageRepository.getSpecialMissions(chara.id)
            }
            ?: flowOf(emptyList())
    ).collectAsState(initial = emptyList())

    val vbData by (
        activeMon
            ?.takeIf { it.characterType == DeviceType.VBDevice }
            ?.let { chara ->
                storageRepository.getCharacterVbData(chara.id)
            }
            ?: flowOf<VBCharacterData?>(null)
    ).collectAsState(initial = null)

    val beData by (
        activeMon
            ?.takeIf { it.characterType == DeviceType.BEDevice }
            ?.let { chara ->
                storageRepository.getCharacterBeData(chara.id)
            }
            ?: flowOf<BECharacterData?>(null)
    ).collectAsState(initial = null)

    var adventureMissionsFinished by rememberSaveable { mutableStateOf(false) }
    var betaWarning by rememberSaveable { mutableStateOf(true) }
    var collectedItem by remember { mutableStateOf<ItemDtos.PurchasedItem?>(null) }
    var collectedCurrency by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(true) {
        homeScreenController
            .didAdventureMissionsFinish {
                adventureMissionsFinished = it
            }
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = stringResource(R.string.home_title),
                onScanClick = {
                    navController.navigate(NavigationItems.Scan.route)
                },
                onGearClick = {
                    navController.navigate(NavigationItems.Settings.route)
                }
            )
        }
    ) { contentPadding ->
        if (activeMon == null || (beData == null && vbData == null) || cardIconData == null || transformationHistory.isEmpty()) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = contentPadding.calculateTopPadding())
            ) {
                Text(text = stringResource(R.string.adventure_empty_state))
            }
        } else {
            val cardIcon = BitmapData(
                bitmap = cardIconData!!.cardIcon,
                width = cardIconData!!.cardIconWidth,
                height = cardIconData!!.cardIconHeight
            )

            if (activeMon!!.isBemCard && beData != null) {
                BEBEmHomeScreen(
                    activeMon = activeMon!!,
                    beData = beData!!,
                    transformationHistory = transformationHistory,
                    contentPadding = contentPadding,
                    cardIcon = cardIcon
                )
            } else if (!activeMon!!.isBemCard && activeMon!!.characterType == DeviceType.BEDevice && beData != null) {
                BEDiMHomeScreen(
                    activeMon = activeMon!!,
                    beData = beData!!,
                    transformationHistory = transformationHistory,
                    contentPadding = contentPadding,
                    cardIcon = cardIcon
                )
            } else if (vbData != null) {
                VBDiMHomeScreen(
                    activeMon = activeMon!!,
                    vbData = vbData!!,
                    transformationHistory = transformationHistory,
                    contentPadding = contentPadding,
                    specialMissions = vbSpecialMissions,
                    homeScreenController = homeScreenController,
                    onClickCollect = { item, currency ->
                        collectedItem = item
                        collectedCurrency = currency
                    },
                    cardIcon = cardIcon
                )
            }
        }
    }

    if (collectedItem != null) {
        ObtainedItemDialog(
            obtainedItem = collectedItem!!,
            obtainedCurrency = collectedCurrency!!,
            onClickDismiss = {
                collectedItem = null
                collectedCurrency = null
            }
        )
    }

    if (adventureMissionsFinished) {
        Dialog(
            onDismissRequest = { adventureMissionsFinished = false },
        ) {
            Card {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.home_adventure_mission_finished),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            adventureMissionsFinished = false
                        },
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.beta_warning_button_dismiss))
                    }
                }
            }
        }
    }

    if (betaWarning) {
        BetaWarning {
            betaWarning = false
        }
    }
}


