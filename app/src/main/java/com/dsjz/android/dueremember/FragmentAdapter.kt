package com.dsjz.android.dueremember

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dsjz.android.dueremember.databinding.ListItemAllTasksBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.Date

class ReminderHolder(
    private val binding: ListItemAllTasksBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(reminder: Reminder, onReminderClicked: (reminderId: UUID) -> Unit, onReminderStatusChanged: (reminder: Reminder) -> Unit) {
        binding.reminderTitle.text = reminder.title
        binding.reminderCreatedDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy, h:mm a", Locale.getDefault()).format(reminder.creationDate)
        binding.reminderDate.text = SimpleDateFormat("EEEE, MMMM d, yyyy | h:mm a", Locale.getDefault()).format(reminder.date)

        val currentDate = Date()
        if (reminder.date.before(currentDate) && !reminder.isSolved) {
            // Change text color for past due and not solved reminders
            binding.reminderTitle.setTextColor(Color.RED)
        } else {
            // Default text color for other reminders
            binding.reminderTitle.setTextColor(Color.BLACK)
        }

        binding.root.setOnClickListener {
            onReminderClicked(reminder.id)
        }

        binding.reminderCheckbox.setOnCheckedChangeListener(null)
        binding.reminderCheckbox.isChecked = reminder.isSolved

        binding.reminderCheckbox.setOnCheckedChangeListener { _, isChecked ->
            binding.reminderCheckbox.visibility = if (isChecked) View.GONE else View.VISIBLE
            binding.reminderSolved.visibility = if (isChecked) View.VISIBLE else View.GONE
            reminder.isSolved = isChecked
            onReminderStatusChanged(reminder)
        }

        binding.reminderSolved.visibility = if (reminder.isSolved) View.VISIBLE else View.GONE
        binding.reminderCheckbox.visibility = if (reminder.isSolved) View.GONE else View.VISIBLE
    }
}

class AllTaskListAdapter(
    private var reminders: List<Reminder>,
    private val onReminderClicked: (reminderId: UUID) -> Unit,
    private val onReminderStatusChanged: (reminder: Reminder) -> Unit
) : RecyclerView.Adapter<ReminderHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemAllTasksBinding.inflate(inflater, parent, false)
        return ReminderHolder(binding)
    }

    override fun onBindViewHolder(holder: ReminderHolder, position: Int) {
        val reminder = reminders[position]
        holder.bind(reminder, onReminderClicked, onReminderStatusChanged)
    }

    override fun getItemCount() = reminders.size

    fun updateReminders(newReminders: List<Reminder>) {
        reminders = newReminders
        notifyDataSetChanged()
    }
}
