package com.kushalgangadhar.sling.utils

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.kushalgangadhar.sling.worker.SleepTimerWorker
import java.util.concurrent.TimeUnit

object TimerHelper {

    private const val SLEEP_TIMER_WORK_NAME = "sleep_timer_work"

    fun startSleepTimer(context: Context, minutes: Long) {
        // Build the work request with the requested delay
        val workRequest = OneTimeWorkRequestBuilder<SleepTimerWorker>()
            .setInitialDelay(minutes, TimeUnit.MINUTES)
            .build()

        // Enqueue unique work. If a timer is already running, this will REPLACE it
        // with the new timer.
        WorkManager.getInstance(context).enqueueUniqueWork(
            SLEEP_TIMER_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun cancelSleepTimer(context: Context) {
        // Cancels the timer if the user changes their mind
        WorkManager.getInstance(context).cancelUniqueWork(SLEEP_TIMER_WORK_NAME)
    }
}