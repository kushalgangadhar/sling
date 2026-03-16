package com.kushalgangadhar.sling.worker

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kushalgangadhar.sling.player.PlaybackService

class SleepTimerWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 1. Create a token to connect to your background PlaybackService
        val sessionToken = SessionToken(
            applicationContext,
            ComponentName(applicationContext, PlaybackService::class.java)
        )

        // 2. Build the MediaController asynchronously
        val controllerFuture = MediaController.Builder(applicationContext, sessionToken).buildAsync()

        return try {
            // 3. Since we are in a CoroutineWorker (background thread),
            // we can safely wait for the connection to establish using .get()
            val mediaController = controllerFuture.get()

            // 4. Send the pause command!
            mediaController.pause()

            // 5. Clean up the connection so we don't leak memory
            MediaController.releaseFuture(controllerFuture)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // If it fails, clean up and report failure
            MediaController.releaseFuture(controllerFuture)
            Result.failure()
        }
    }
}