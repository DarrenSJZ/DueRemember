package com.dsjz.android.dueremember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AllTaskViewModel : ViewModel(){
    private val reminderRepository = ReminderRepository.get()

    private val _reminders: MutableStateFlow<List<Reminder>> = MutableStateFlow(emptyList())
    val reminders : StateFlow<List<Reminder>>
        get() =  _reminders.asStateFlow()

    init {
        viewModelScope.launch {
            reminderRepository.getReminders().collect {
                _reminders.value = it
            }
        }
    }
    suspend fun addReminder(reminder: Reminder) {
        reminderRepository.addReminder(reminder)
    }


    fun deleteAllReminders() {
        viewModelScope.launch {
            reminderRepository.deleteAllReminders()
            _reminders.value = emptyList()
        }
    }
}