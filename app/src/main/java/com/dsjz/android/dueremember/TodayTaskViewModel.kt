package com.dsjz.android.dueremember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class TodayTaskViewModel : ViewModel() {
    private val reminderRepository = ReminderRepository.get()

    val todayReminders: Flow<List<Reminder>> = reminderRepository.getReminders()
        .map { reminders ->
            reminders.filter { reminder ->
                val calendar = Calendar.getInstance()
                calendar.time = Date()
                val today = calendar.get(Calendar.DAY_OF_YEAR)

                calendar.time = reminder.date
                val reminderDay = calendar.get(Calendar.DAY_OF_YEAR)

                reminderDay == today && !reminder.isSolved
            }
        }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.updateReminder(reminder)
        }
    }
}
