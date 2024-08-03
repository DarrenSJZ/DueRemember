package com.dsjz.android.dueremember

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

const val NOTIFICATION_REMINDER_DUE_SOON_CID = "reminder_due_soon"
const val NOTIFICATION_REMINDER_PAST_DUE_CID = "reminder_past_due"

class DueRememberApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ReminderRepository.initialize(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }

        scheduleWorker(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannels() {
        val notificationManager: NotificationManager =
            getSystemService(NotificationManager::class.java)

        val dueSoonChannelName = getString(R.string.notification_due_soon_channel_name)
        val dueSoonChannelImportance = NotificationManager.IMPORTANCE_HIGH
        val dueSoonChannel =
            NotificationChannel(NOTIFICATION_REMINDER_DUE_SOON_CID, dueSoonChannelName, dueSoonChannelImportance)
        notificationManager.createNotificationChannel(dueSoonChannel)

        val pastDueChannelName = getString(R.string.notification_past_due_channel_name)
        val pastDueChannelImportance = NotificationManager.IMPORTANCE_DEFAULT
        val pastDueChannel =
            NotificationChannel(NOTIFICATION_REMINDER_PAST_DUE_CID, pastDueChannelName, pastDueChannelImportance)
        notificationManager.createNotificationChannel(pastDueChannel)
    }

    private fun scheduleWorker(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "CheckDue",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
