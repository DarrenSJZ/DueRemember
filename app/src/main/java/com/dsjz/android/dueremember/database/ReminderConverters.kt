package com.dsjz.android.dueremember.database

import androidx.room.TypeConverter
import java.sql.Time
import java.util.Date

class ReminderConverters {
    @TypeConverter
    fun fromDate(date: Date): Long {
        return date.time
    }

    @TypeConverter
    fun toDate(millisSinceEpoch: Long): Date {
        return Date(millisSinceEpoch)
    }

    class TimeTypeConverters {
        @TypeConverter
        fun fromTime(time: Time): Long {
            return time.time
        }

        @TypeConverter
        fun toTime(millisSinceEpoch: Long): Time {
            return Time(millisSinceEpoch)
        }
    }
}