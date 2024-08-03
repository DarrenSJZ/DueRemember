package com.dsjz.android.dueremember

import android.content.Context
import androidx.room.Room
import com.dsjz.android.dueremember.database.ReminderDatabase
import com.dsjz.android.dueremember.database.migration_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

private const val DATABASE_NAME = "reminder-database"

class ReminderRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val database: ReminderDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            ReminderDatabase::class.java,
            DATABASE_NAME
        )
        .addMigrations(migration_1_2)
        .build()

    fun getReminders(): Flow<List<Reminder>> = database.reminderDao().getReminders()

    suspend fun getReminder(id: UUID): Reminder = database.reminderDao().getReminder(id)

    fun updateReminder(reminder: Reminder) {
        coroutineScope.launch {
            database.reminderDao().updateReminder(reminder)
        }
    }

    suspend fun getRemindersDueSoon(start: Int, end: Int): List<Reminder> {
        val startOfDay = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, start)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        val endOfDay = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, end)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time

        return database.reminderDao().getRemindersDueSoon(startOfDay, endOfDay)
    }

    suspend fun getRemindersPastDue(): List<Reminder> {
        val currentDate = Calendar.getInstance().time
        return database.reminderDao().getRemindersPastDue(currentDate)
    }

    fun addReminder(reminder: Reminder) {
        coroutineScope.launch {
            database.reminderDao().addReminder(reminder)
        }
    }

    fun deleteReminder(reminder: Reminder) {
        coroutineScope.launch {
            database.reminderDao().deleteReminder(reminder)
        }
    }

    fun deleteAllReminders() {
        coroutineScope.launch {
            database.reminderDao().deleteAllReminders()
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: ReminderRepository? = null

        fun initialize(context: Context) {
            INSTANCE ?: synchronized(this) {
                INSTANCE = ReminderRepository(context)
            }
        }

        fun get(): ReminderRepository {
            return INSTANCE
                ?: throw IllegalStateException("Initialize Reminder Repository First")
        }
    }
}
