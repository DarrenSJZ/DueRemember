package com.dsjz.android.dueremember

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val NOTIFICATION_REMINDER_DUE_SOON_CID = "reminder_due_soon"
const val NOTIFICATION_REMINDER_PAST_DUE_CID = "reminder_past_due"

class DueRememberApp : Application(){
    override fun onCreate() {
        super.onCreate()
        ReminderRepository.initialize(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)

            var name = getString(R.string.notification_due_soon_channel_name)
            var importance = NotificationManager.IMPORTANCE_HIGH
            var channel =
                NotificationChannel(NOTIFICATION_REMINDER_DUE_SOON_CID, name, importance)
            notificationManager.createNotificationChannel(channel)

            name = getString(R.string.notification_past_due_channel_name)
            importance = NotificationManager.IMPORTANCE_DEFAULT
            channel =
                NotificationChannel(NOTIFICATION_REMINDER_PAST_DUE_CID, name, importance)
        }
        scheduleWorker(this)
    }
}