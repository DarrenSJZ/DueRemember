package com.dsjz.android.dueremember

import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import android.Manifest
import android.content.pm.PackageManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ReminderWorker (
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker (context, workerParameters) {

    override suspend fun doWork() : Result {
        val reminderRepository = ReminderRepository.get()

        val reminderNotificationDueSoon = reminderRepository.getRemindersDueSoon(0, 1)
        for (reminder in reminderNotificationDueSoon) {
            val text = "You have an upcoming reminder: '${reminder.title}' due tomorrow."
            val title = "DueRemember Due Soon"
            sendNotification(context, reminder, text, title, NOTIFICATION_REMINDER_DUE_SOON_CID)
        }

        val reminderPastDue = reminderRepository.getRemindersPastDue()
        for (reminder in reminderPastDue) {
            val text = "'${reminder.title}' is past it's due date"
            val title = "DueRemember Past Due"
            sendNotification(context, reminder, text, title, NOTIFICATION_REMINDER_PAST_DUE_CID)
        }
        return Result.success()
    }

    private fun sendNotification(context: Context,
                                 reminder: Reminder,
                                 notificationText: String,
                                 title: String,
                                 channelID: String) {

        val builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) !=  PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            notify(reminder.id.hashCode(), builder.build())
        }
    }
}

fun scheduleWorker(context: Context) {
    val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
        .setInitialDelay(1, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        "CheckDue",
        ExistingPeriodicWorkPolicy.KEEP,
        workRequest
    )
}


