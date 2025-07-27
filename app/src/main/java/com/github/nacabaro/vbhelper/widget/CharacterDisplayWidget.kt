package com.github.nacabaro.vbhelper.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.glance.GlanceTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.background
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.unit.ColorProvider
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.getBitmap

/*
    RANT: Why did they have to create the components from Jetpack Compose
    with a different API for Glance? Now there are things I use in Jetpack Compose
    like the filterQuality, which isn't here.
 */

val CURRENT_IMAGE_KEY = booleanPreferencesKey("current_image")

class CharacterDisplayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val application = context.applicationContext as VBHelper
        val storageRepository = application.container.db

        val currentCharacter = storageRepository
            .userCharacterDao()
            .getActiveCharacter()

        val multiplier = 4

        provideContent {
            var bitmapIdle1 by remember { mutableStateOf<Bitmap?>(null) }
            var bitmapIdle2 by remember { mutableStateOf<Bitmap?>(null) }
            var dpSize by remember { mutableStateOf(0.dp) }

            if (currentCharacter != null) {
                val currentCharacterBitmapIdle1 = BitmapData(
                    currentCharacter.spriteIdle,
                    currentCharacter.spriteWidth,
                    currentCharacter.spriteHeight
                )

                val currentCharacterBitmapIdle2 = BitmapData(
                    currentCharacter.spriteIdle2,
                    currentCharacter.spriteWidth,
                    currentCharacter.spriteHeight
                )

                bitmapIdle1 = currentCharacterBitmapIdle1.getBitmap()
                bitmapIdle2 = currentCharacterBitmapIdle2.getBitmap()

                val density: Float = application.resources.displayMetrics.density
                dpSize = (currentCharacterBitmapIdle1.width * multiplier / density).dp
            }

            WidgetContent(
                imageBitmapFrame1 = bitmapIdle1,
                imageBitmapFrame2 = bitmapIdle2,
                dpSize = dpSize
            )
        }
    }

    @Composable
    private fun WidgetContent(
        imageBitmapFrame1: Bitmap?,
        imageBitmapFrame2: Bitmap?,
        dpSize: Dp
    ) {
        GlanceTheme {
            Column (
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically,
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(MaterialTheme.colorScheme.background))
            ) {
                if (imageBitmapFrame1 != null) {
                    Image(
                        provider = ImageProvider(
                            bitmap = imageBitmapFrame1
                        ),
                        contentDescription = "Character",
                        modifier = GlanceModifier
                            .size(dpSize)
                    )
                } else {
                    Text(
                        text = "No character selected"
                    )
                }
            }
        }
    }
}