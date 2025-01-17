package com.github.nacabaro.vbhelper.screens.homeScreens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.utils.DeviceType
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.source.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    navController: NavController
) {
    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val activeMon = remember { mutableStateOf<CharacterDtos.CharacterWithSprites?>(null) }
    val transformationHistory = remember { mutableStateOf<List<CharacterDtos.TransformationHistory>?>(null) }
    val beData = remember { mutableStateOf<BECharacterData?>(null) }
    val vbData = remember { mutableStateOf<VBCharacterData?>(null) }

    LaunchedEffect(storageRepository, activeMon) {
        withContext(Dispatchers.IO) {
            activeMon.value = storageRepository.getActiveCharacter()
            if (activeMon.value != null) {
                beData.value = storageRepository.getCharacterBeData(activeMon.value!!.id)
                transformationHistory.value = storageRepository.getTransformationHistory(activeMon.value!!.id)
            }
        }
    }

    Scaffold (
        topBar = {
            TopBanner(
                text = "VB Helper",
                onScanClick = {
                    navController.navigate(NavigationItems.Scan.route)
                },
                onGearClick = {
                    navController.navigate(NavigationItems.Settings.route)
                }
            )
        }
    ) { contentPadding ->
        if (activeMon.value == null || (beData.value == null && vbData.value == null) || transformationHistory.value == null) {
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = contentPadding.calculateTopPadding())
            ) {
                Text(text = "Nothing to see here")
            }
        } else {
            if (activeMon.value!!.isBemCard) {
                Log.d("HomeScreen", "BEDeviceBEm")
                BEBEmHomeScreen(
                    activeMon = activeMon.value!!,
                    beData = beData.value!!,
                    transformationHistory = transformationHistory.value!!,
                    contentPadding = contentPadding
                )
            } else if (!activeMon.value!!.isBemCard && activeMon.value!!.characterType == DeviceType.BEDevice) {
                Log.d("HomeScreen", "BEDevice")
                BEDiMHomeScreen(
                    activeMon = activeMon.value!!,
                    beData = beData.value!!,
                    transformationHistory = transformationHistory.value!!,
                    contentPadding = contentPadding
                )
            } else {
                VBDiMHomeScreen(
                    activeMon = activeMon.value!!,
                    vbData = vbData.value!!,
                    transformationHistory = transformationHistory.value!!,
                    contentPadding = contentPadding
                )
            }
        }
    }
}


