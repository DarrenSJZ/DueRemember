package com.dsjz.android.dueremember

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity
data class Reminder (
    @PrimaryKey val id: UUID,
    var title: String,
    var desc: String,
    var date: Date = Date(),
    var dateString: String = "",
    var isSolved : Boolean,
    var author : String = "",
    val photoFileName: String? = null
)
