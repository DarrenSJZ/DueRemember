package com.dsjz.android.dueremember

import android.content.Context
import androidx.room.Room
import com.dsjz.android.dueremember.database.ReminderDatabase
import com.dsjz.android.dueremember.database.migration_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

private const val DATABASE_NAME = "reminder-database"


class ReminderRepository private constructor(
    context: Context,
    private val coroutineScope: CoroutineScope = GlobalScope
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

    suspend fun getRemindersDueSoon(start: Int, end: Int) : List<Reminder> {
        val calendar = Calendar.getInstance()

        calendar.add(Calendar.DAY_OF_YEAR, start)
        val targetStartDate = calendar.time
        val startOfDayCalendar = Calendar.getInstance()
        startOfDayCalendar.time = targetStartDate
        startOfDayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        startOfDayCalendar.set(Calendar.MINUTE, 0)
        startOfDayCalendar.set(Calendar.SECOND, 0)
        startOfDayCalendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = startOfDayCalendar.time

        calendar.add(Calendar.DAY_OF_YEAR, end)
        val targetEndDate = calendar.time
        val endOfDayCalendar = Calendar.getInstance()
        endOfDayCalendar.time = targetEndDate
        endOfDayCalendar.set(Calendar.HOUR_OF_DAY, 23)
        endOfDayCalendar.set(Calendar.MINUTE, 59)
        endOfDayCalendar.set(Calendar.SECOND, 59)
        endOfDayCalendar.set(Calendar.MILLISECOND, 999)
        val endOfDay = endOfDayCalendar.time
        return database.reminderDao().getRemindersDueSoon(startOfDay, endOfDay)
    }

    suspend fun getRemindersPastDue() : List<Reminder> {
        val currentDate = Calendar.getInstance().time
        return database.reminderDao().getRemindersPastDue(currentDate)
    }

    suspend fun addReminder(reminder: Reminder) {
        database.reminderDao().addReminder(reminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        database.reminderDao().deleteReminder(reminder)
    }

    suspend fun deleteAllReminders() {
        database.reminderDao().deleteAllReminders()
    }

    companion object {
        private var INSTANCE: ReminderRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = ReminderRepository(context)
            }
        }

        fun get(): ReminderRepository {
            return INSTANCE
                ?: throw IllegalStateException("Initialize Reminder Repository First")
        }
    }
}





