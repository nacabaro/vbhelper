package com.github.nacabaro.vbhelper.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class CharacterDisplayWidgetWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // Get all widget IDs for your ImageSwapWidget
        val glanceIds = GlanceAppWidgetManager(applicationContext)
            .getGlanceIds(CharacterDisplayWidget::class.java)

        glanceIds.forEach { glanceId ->
            // Update the widget's state (toggle the boolean)
            updateAppWidgetState(
                context = applicationContext,
                glanceId = glanceId
            ) { prefs ->
                val currentImageIsImage1 = prefs[CURRENT_IMAGE_KEY] ?: true
                prefs[CURRENT_IMAGE_KEY] = !currentImageIsImage1 // Toggle the boolean
            }
            // Trigger the widget to redraw itself with the new state
            CharacterDisplayWidget().update(applicationContext, glanceId)
        }

        // Indicate that the work was successful
        return Result.success()
    }
}