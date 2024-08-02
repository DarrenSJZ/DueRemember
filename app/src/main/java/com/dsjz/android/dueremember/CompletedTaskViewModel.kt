package com.dsjz.android.dueremember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompletedTaskViewModel : ViewModel() {
    private val reminderRepository = ReminderRepository.get()
    private val _completedReminders = MutableStateFlow<List<Reminder>>(emptyList())
    val completedReminders: StateFlow<List<Reminder>> = _completedReminders.asStateFlow()

    init {
        viewModelScope.launch {
            reminderRepository.getReminders().collect { reminders ->
                _completedReminders.value = reminders.filter { it.isSolved }
            }
        }
    }

    fun updateReminder(reminder: Reminder) {
        viewModelScope.launch {
            reminderRepository.updateReminder(reminder)
            _completedReminders.value = reminderRepository.getReminders().first().filter { it.isSolved }
        }
    }
}
