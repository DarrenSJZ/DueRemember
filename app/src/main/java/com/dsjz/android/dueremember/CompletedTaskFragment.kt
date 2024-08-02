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
import com.dsjz.android.dueremember.databinding.FragmentCompletedTaskBinding
import kotlinx.coroutines.launch

class CompletedTaskFragment : Fragment() {

    private var _binding: FragmentCompletedTaskBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Binding is inaccessible due to null, is the view visible?"
        }
    private val completedTaskViewModel: CompletedTaskViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCompletedTaskBinding.inflate(inflater, container, false)

        binding.completedTaskRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                completedTaskViewModel.completedReminders.collect { reminders ->
                    binding.completedTaskRecyclerView.adapter =
                        AllTaskListAdapter(reminders, { reminderId ->
                            findNavController().navigate(
                                CompletedTaskFragmentDirections.showNewReminderFromCompleted(reminderId)
                            )
                        }, { reminder ->
                            completedTaskViewModel.updateReminder(reminder)
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
