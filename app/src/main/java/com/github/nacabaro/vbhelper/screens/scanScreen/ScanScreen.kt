package com.github.nacabaro.vbhelper.screens.scanScreen

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.source.isMissingSecrets
import com.github.nacabaro.vbhelper.source.proto.Secrets
import com.github.nacabaro.vbhelper.utils.characterToNfc
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

const val SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER = "SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER"

@Composable
fun ScanScreen(
    navController: NavController,
    characterId: Long?,
    scanScreenController: ScanScreenController,
) {
    val secrets by scanScreenController.secretsFlow.collectAsState(null)
    var readingScreen by remember { mutableStateOf(false) }
    var writingScreen by remember { mutableStateOf(false) }
    var isDoneReadingCharacter by remember { mutableStateOf(false) }
    var isDoneSendingCard by remember { mutableStateOf(false) }
    var isDoneWritingCharacter by remember { mutableStateOf(false) }

    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    var nfcCharacter by remember { mutableStateOf<NfcCharacter?>(null) }

    val context = LocalContext.current
    LaunchedEffect(storageRepository) {
        withContext(Dispatchers.IO) {
            if(characterId != null) {
                nfcCharacter = characterToNfc(context, characterId)
            }
        }
    }

    DisposableEffect(readingScreen || writingScreen) {
        if(readingScreen) {
            scanScreenController.registerActivityLifecycleListener(SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER, object: ActivityLifecycleListener {
                override fun onPause() {
                    scanScreenController.cancelRead()
                }

                override fun onResume() {
                    scanScreenController.onClickRead(secrets!!) {
                        isDoneReadingCharacter = true
                    }
                }

            })
            scanScreenController.onClickRead(secrets!!) {
                isDoneReadingCharacter = true
            }
        } else if (writingScreen) {
            scanScreenController.registerActivityLifecycleListener(
                SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER,
                object : ActivityLifecycleListener {
                    override fun onPause() {
                        scanScreenController.cancelRead()
                    }

                    override fun onResume() {
                        if (!isDoneSendingCard) {
                            scanScreenController.onClickCheckCard(secrets!!, nfcCharacter!!) {
                                isDoneSendingCard = true
                            }
                        } else if (!isDoneWritingCharacter) {
                            scanScreenController.onClickWrite(secrets!!, nfcCharacter!!) {
                                isDoneWritingCharacter = true
                            }
                        }
                    }
                }
            )
            if (!isDoneSendingCard) {
                scanScreenController.onClickCheckCard(secrets!!, nfcCharacter!!) {
                    isDoneSendingCard = true
                }
            } else if (!isDoneWritingCharacter) {
                scanScreenController.onClickWrite(secrets!!, nfcCharacter!!) {
                    isDoneWritingCharacter = true
                }
            }
        }
        onDispose {
            if(readingScreen || writingScreen) {
                scanScreenController.unregisterActivityLifecycleListener(SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER)
                scanScreenController.cancelRead()
            }
        }
    }

    if (isDoneReadingCharacter) {
        readingScreen = false
        navController.navigate(NavigationItems.Home.route)
    } else if (isDoneSendingCard && isDoneWritingCharacter) {
        writingScreen = false
        navController.navigate(NavigationItems.Home.route)
    }

    if (readingScreen) {
        ReadingCharacterScreen("Reading character") {
            readingScreen = false
            scanScreenController.cancelRead()
        }
    } else if (writingScreen) {
        if (!isDoneSendingCard) {
            ReadingCharacterScreen("Sending card") {
                isDoneSendingCard = true
                scanScreenController.cancelRead()
            }
        } else if (!isDoneWritingCharacter) {
            ReadingCharacterScreen("Writing character") {
                isDoneWritingCharacter = true
                writingScreen = false
                scanScreenController.cancelRead()
            }
        }
    } else {
        ChooseConnectOption(
            onClickRead = when {
                characterId != null -> null
                else -> {
                    {
                        if(secrets == null) {
                            Toast.makeText(context, "Secrets is not yet initialized. Try again.", Toast.LENGTH_SHORT).show()
                        } else if(secrets?.isMissingSecrets() == true) {
                            Toast.makeText(context, "Secrets not yet imported. Go to Settings and Import APK", Toast.LENGTH_SHORT).show()
                        } else {
                            readingScreen = true // kicks off nfc adapter in DisposableEffect
                        }
                    }
                }
            },
            onClickWrite = when {
                nfcCharacter == null -> null
                else -> {
                    {
                        if(secrets == null) {
                            Toast.makeText(context, "Secrets is not yet initialized. Try again.", Toast.LENGTH_SHORT).show()
                        } else if(secrets?.isMissingSecrets() == true) {
                            Toast.makeText(context, "Secrets not yet imported. Go to Settings and Import APK", Toast.LENGTH_SHORT).show()
                        } else {
                            writingScreen = true // kicks off nfc adapter in DisposableEffect
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun ChooseConnectOption(
    onClickRead: (() -> Unit)? = null,
    onClickWrite: (() -> Unit)? = null,
) {
    Scaffold(
        topBar = { TopBanner(text = "Scan a Vital Bracelet") }
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            ScanButton(
                text = "Vital Bracelet to App",
                disabled = onClickRead == null,
                onClick = onClickRead?: {  },
            )
            Spacer(modifier = Modifier.height(16.dp))
            ScanButton(
                text = "App to Vital Bracelet",
                disabled = onClickWrite == null,
                onClick = onClickWrite?: {  },
            )
        }
    }
}


@Composable
fun ScanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    disabled: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = !disabled,
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier
                .padding(4.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScanScreenPreview() {
    ScanScreen(
        navController = rememberNavController(),
        scanScreenController = object: ScanScreenController {
            override val secretsFlow = MutableStateFlow<Secrets>(Secrets.getDefaultInstance())
            override fun unregisterActivityLifecycleListener(key: String) { }
            override fun registerActivityLifecycleListener(
                key: String,
                activityLifecycleListener: ActivityLifecycleListener
            ) {

            }
            override fun onClickRead(secrets: Secrets, onComplete: ()->Unit) {}
            override fun onClickCheckCard(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: () -> Unit) {}
            override fun onClickWrite(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: () -> Unit) {}
            override fun cancelRead() {}
        },
        characterId = null
    )
}