package com.dsjz.android.dueremember.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dsjz.android.dueremember.Reminder


@Database(entities = [Reminder::class], version = 2)
@TypeConverters(ReminderConverters::class)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
}

val migration_1_2 = object : Migration(1,2){
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE Reminder ADD COLUMN photoFileName TEXT"
        )
    }
}