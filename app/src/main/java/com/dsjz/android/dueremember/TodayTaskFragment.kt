package com.dsjz.android.dueremember

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.dsjz.android.dueremember.databinding.FragmentAllTaskBinding
import com.dsjz.android.dueremember.databinding.FragmentTodayTaskBinding


class TodayTaskFragment : Fragment() {

    private var _binding: FragmentTodayTaskBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Binding is inaccessible due to null, is the view visible?"
        }
    private val todayTaskViewModel : TodayTaskViewModel by viewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentTodayTaskBinding.inflate(inflater, container, false)

        binding.todayTaskRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Set up the MenuProvider
//        val menuHost: MenuHost = requireActivity()
//        menuHost.addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.fragment_all_task, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                return when (menuItem.itemId) {
//
//                }
//            }
//        }
//        )
//    }
}