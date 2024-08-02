package com.dsjz.android.dueremember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AllTaskViewModel : ViewModel() {
    private val reminderRepository = ReminderRepository.get()
    private val _reminders: MutableStateFlow<List<Reminder>> = MutableStateFlow(emptyList())
    val reminders: StateFlow<List<Reminder>> = _reminders.asStateFlow()

    init {
        viewModelScope.launch {
            reminderRepository.getReminders().collect { reminderList ->
                _reminders.value = reminderList
            }
        }
    }

    fun addReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.addReminder(reminder)
        }
    }

    fun deleteAllReminders() {
        viewModelScope.launch {
            reminderRepository.deleteAllReminders()
        }
    }

    fun getUnresolvedReminders(): List<Reminder> {
        return _reminders.value.filter { !it.isSolved }
    }


    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.updateReminder(reminder)
            _reminders.value = reminderRepository.getReminders().first()
        }
    }
}

