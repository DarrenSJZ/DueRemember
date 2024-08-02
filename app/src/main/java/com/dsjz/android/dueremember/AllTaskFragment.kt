package com.dsjz.android.dueremember

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsjz.android.dueremember.databinding.FragmentAllTaskBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AllTaskFragment : Fragment() {

    private var _binding: FragmentAllTaskBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Binding is inaccessible due to null, is the view visible?"
        }
    private val allTaskViewModel: AllTaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentAllTaskBinding.inflate(inflater, container, false)

        binding.allTaskListRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up the MenuProvider
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_all_task, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.new_reminder -> {
                        showNewReminder()
                        true
                    }
                    R.id.delete_all -> {
                        deleteAllReminders()
                        true
                    }

                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                allTaskViewModel.reminders.collect { reminders ->
                    val formattedReminders = reminders.map { reminder ->
                        reminder.copy(dateString = formatDate(reminder.date))
                    }
                    binding.allTaskListRecyclerView.adapter =
                        AllTaskListAdapter(formattedReminders) { reminderId ->
                            findNavController().navigate(
                                AllTaskFragmentDirections.showNewReminder(reminderId)
                            )
                        }
                    // Show or hide the empty list text based on whether there are reminders
                    binding.emptyListText.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showNewReminder() {
        viewLifecycleOwner.lifecycleScope.launch {
            val newReminder = Reminder(
                id = UUID.randomUUID(),
                title = "",
                desc = "",
                date = Date(),
                isSolved = false,
                dateString = formatDate(Date())
            )
            allTaskViewModel.addReminder(newReminder)
            findNavController().navigate(
                AllTaskFragmentDirections.showNewReminder(newReminder.id)
            )
        }
    }

    private fun deleteAllReminders() {
        val builder = AlertDialog.Builder(requireContext())
        builder.apply {
            setPositiveButton("Yes") { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    allTaskViewModel.deleteAllReminders()
                    Toast.makeText(activity, "All reminders deleted", Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton("No") { _, _ -> }
            setTitle("Delete everything?")
            setMessage("Are you sure you want to delete everything?")
            create().show()
        }
    }

    private fun formatDate(date: Date): String {
        val sdf = SimpleDateFormat("EEEE, MMMM d, yyyy | h:mm a", Locale.getDefault())
        return sdf.format(date)
    }
}