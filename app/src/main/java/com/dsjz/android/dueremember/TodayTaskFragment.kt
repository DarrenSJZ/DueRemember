package com.dsjz.android.dueremember

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsjz.android.dueremember.databinding.FragmentTodayTaskBinding
import kotlinx.coroutines.launch

class TodayTaskFragment : Fragment() {

    private var _binding: FragmentTodayTaskBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Binding is inaccessible due to null, is the view visible?"
        }
    private val todayTaskViewModel: TodayTaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTodayTaskBinding.inflate(inflater, container, false)

        binding.todayTaskRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                todayTaskViewModel.todayReminders.collect { reminders ->
                    binding.todayTaskRecyclerView.adapter =
                        AllTaskListAdapter(reminders, { reminderId ->
                            findNavController().navigate(
                                TodayTaskFragmentDirections.showNewReminderFromToday(reminderId)
                            )
                        }, { reminder ->
                            todayTaskViewModel.updateReminder(reminder)
                        })
                    binding.emptyListText.visibility = if (reminders.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
