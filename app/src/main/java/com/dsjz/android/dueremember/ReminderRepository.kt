package com.dsjz.android.dueremember

import android.content.Context
import androidx.room.Room
import com.dsjz.android.dueremember.database.ReminderDatabase
import com.dsjz.android.dueremember.database.migration_1_2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
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



