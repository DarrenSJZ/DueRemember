package com.dsjz.android.dueremember

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.dsjz.android.dueremember.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Add the periodic checker
        scheduleWorker(this)

        // Request For Android Notifications Perms
        checkAndRequestNotifcationPerms()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController
        val navView: BottomNavigationView = binding.navView

        navView.setOnItemSelectedListener { item ->
            val currentFragment = navController.currentDestination?.id

            if (currentFragment == R.id.newReminderFragment) {
                val newReminderFragment = navHostFragment.childFragmentManager.fragments.firstOrNull() as? NewReminderFragment
                newReminderFragment?.let { fragment ->
                    if (fragment.isReminderUnsaved()) {
                        showToast("Save the reminder first!")
                        return@setOnItemSelectedListener false
                    }
                }
            }
            NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

        NavigationUI.setupWithNavController(navView, navController)
    }

    private val requestNotificationPermsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { } // nothing

    private fun checkAndRequestNotifcationPerms() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                // Request the permission
                requestNotificationPermsLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
