package com.dsjz.android.dueremember

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dsjz.android.dueremember.databinding.ListItemAllTasksBinding
import java.util.UUID


class ReminderHolder(
    private  val binding: ListItemAllTasksBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind (reminder: Reminder, onReminderClicked: (reminderId: UUID) -> Unit) {
        binding.reminderTitle.text = reminder.title
        binding.reminderDate.text = reminder.dateString

        binding.root.setOnClickListener {
            onReminderClicked(reminder.id)
        }

        binding.reminderSolved.visibility = if (reminder.isSolved) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }
}

class AllTaskListAdapter (
    private val reminders: List<Reminder>,
    private val onReminderClicked: (reminderId: UUID) -> Unit
) : RecyclerView.Adapter<ReminderHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAllTasksBinding.inflate(inflater, parent,false)
        return ReminderHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder, onReminderClicked)
    }
    override fun getItemCount() = reminders.size
}