package com.dsjz.android.dueremember

import android.app.Application

class DueRememberApp : Application(){
    override fun onCreate() {
        super.onCreate()
        ReminderRepository.initialize(this)
    }
}