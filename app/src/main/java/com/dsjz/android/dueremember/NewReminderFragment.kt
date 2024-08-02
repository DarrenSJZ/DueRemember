package com.dsjz.android.dueremember

import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.core.view.doOnLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dsjz.android.dueremember.databinding.FragmentNewReminderBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.Date

class NewReminderFragment : Fragment() {

    private var _binding: FragmentNewReminderBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "No access to binding due to null. View visible?"
        }

    private val args: NewReminderFragmentArgs by navArgs()

    private val newReminderViewModel: NewReminderViewModel by viewModels {
        NewReminderVMF(args.reminderId)
    }

    private var photoName: String? = null

    private val takePhoto = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { didTakePhoto: Boolean ->
        if (didTakePhoto && photoName != null) {
            newReminderViewModel.updateReminder { oldReminder ->
                oldReminder.copy(photoFileName = photoName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewReminderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.reminderTitle.text.isBlank() || binding.reminderDesc.text.isBlank()) {
                    Toast.makeText(activity, "Fill in the Title and Description!", Toast.LENGTH_SHORT).show()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Hide the bottom navigation bar
        val bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavView?.visibility = View.GONE

        // Setup MenuProvider
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.fragment_new_reminder, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.delete_reminder -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    R.id.share_reminder -> {
                        shareReminder()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.apply {
            reminderTitle.hint = getString(R.string.reminder_title_placeholder)
            reminderDesc.hint = getString(R.string.reminder_desc_placeholder)

            // Bind Title to Text Changes
            reminderTitle.doOnTextChanged { text, _, _, _ ->
                newReminderViewModel.updateReminder { oldReminder ->
                    reminderSaveBtn.isEnabled = reminderTitle.text.isNotBlank()
                    oldReminder.copy(title = text.toString())
                }
            }

            // Bind Description to Text Changes
            reminderDesc.doOnTextChanged { text, _, _, _ ->
                newReminderViewModel.updateReminder { oldReminder ->
                    reminderSaveBtn.isEnabled = reminderDesc.text.isNotBlank()
                    oldReminder.copy(desc = text.toString())
                }
            }

            // Bind completed status to Checkbox status
            completedCheckbox.setOnCheckedChangeListener { _, isChecked ->
                newReminderViewModel.updateReminder { oldReminder ->
                    oldReminder.copy(isSolved = isChecked)
                }
            }

            // Bind Camera Button to Launch Camera
            reminderCam.setOnClickListener {
                photoName = "IMG_${Date().time}.jpg"
                val photoFile = File(
                    requireContext().applicationContext.filesDir,
                    photoName.toString()
                )

                val photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.dsjz.android.dueremember.fileprovider",
                    photoFile
                )
                takePhoto.launch(photoUri)
            }

            // Check if the device can handle the camera intent
            val captureImageIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            reminderCam.isEnabled = canResolveIntent(captureImageIntent)

            // Bind Date Button to Launch DatePicker
            dateBtn.setOnClickListener {
                newReminderViewModel.reminder.value?.let { reminder ->
                    findNavController().navigate(
                        NewReminderFragmentDirections.selectDate(reminder.date)
                    )
                }
            }

            // Bind Time Button to Launch TimePicker
            timeBtn.setOnClickListener {
                newReminderViewModel.reminder.value?.let { reminder ->
                    findNavController().navigate(
                        NewReminderFragmentDirections.selectTime(reminder.date.time)
                    )
                }
            }

            // Save reminder and return to AllTaskFragment when save button is clicked
            reminderSaveBtn.setOnClickListener {
                if (binding.reminderTitle.text.isBlank() || binding.reminderDesc.text.isBlank()) {
                    Toast.makeText(activity, "Fill in the Title and Description!", Toast.LENGTH_SHORT).show()
                } else {
                    saveReminderAndReturn()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                newReminderViewModel.reminder.collect { reminder ->
                    reminder?.let { updateUi(it) }
                }
            }
        }

        setFragmentResultListener(
            DatePickerFragment.REQUEST_KEY_DATE
        ) { _, bundle ->
            val newDate =
                bundle.getSerializable(DatePickerFragment.BUNDLE_KEY_DATE) as Date
            newReminderViewModel.updateReminder { it.copy(date = newDate) }
        }

        setFragmentResultListener(
            TimePickerFragment.REQUEST_KEY_TIME
        ) { _, bundle ->
            val newTime = bundle.getSerializable(TimePickerFragment.BUNDLE_KEY_TIME) as Date
            newReminderViewModel.updateReminder { oldReminder ->
                val calendar = Calendar.getInstance().apply {
                    time = oldReminder.date
                    set(Calendar.HOUR_OF_DAY, newTime.hours)
                    set(Calendar.MINUTE, newTime.minutes)
                }
                oldReminder.copy(date = calendar.time)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        // Show the bottom navigation bar again
        val bottomNavView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavView?.visibility = View.VISIBLE
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete this reminder?")
            .setPositiveButton("Yes") { _, _ ->
                deleteReminder()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteReminder() {
        viewLifecycleOwner.lifecycleScope.launch {
            newReminderViewModel.deleteReminder(args.reminderId)
            findNavController().popBackStack()
        }
    }

    private fun shareReminder() {
        newReminderViewModel.reminder.value?.let { reminder ->
            val message = getMessage(reminder)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.reminder_report_subject))
            }
            val chooserIntent = Intent.createChooser(intent, getString(R.string.send_report))
            startActivity(chooserIntent)
        }
    }

    private fun getMessage(reminder: Reminder): String {
        val solvedString = if (reminder.isSolved) {
            getString(R.string.reminder_solved)
        } else {
            getString(R.string.reminder_not_solved)
        }

        val dateString = DateFormat.format("EEEE, MMMM d, yyyy | h:mm a", reminder.date).toString()
        val descriptionText = if (reminder.desc.isBlank()) {
            getString(R.string.reminder_no_desc)
        } else {
            reminder.desc
        }

        return getString(
            R.string.reminder_message,
            reminder.title, dateString, descriptionText, solvedString
        )
    }

    private fun updateUi(reminder: Reminder) {
        binding.apply {
            if (reminderTitle.text.toString() != reminder.title) {
                reminderTitle.setText(reminder.title)
            }

            if (reminderDesc.text.toString() != reminder.desc) {
                reminderDesc.setText(reminder.desc)
            }

            if (reminderDesc.text.isBlank() || reminderTitle.text.isBlank())
                reminderSaveBtn.isEnabled = false
            else reminderSaveBtn.isEnabled = true

            val dateString = DateFormat.format("EEEE, MMMM d, yyyy", reminder.date).toString()
            dateBtn.text = dateString

            val timeString = DateFormat.format("h:mm a", reminder.date).toString()
            timeBtn.text = timeString

            completedCheckbox.isChecked = reminder.isSolved
        }
        updatePhoto(reminder.photoFileName)
    }

    private fun canResolveIntent(intent: Intent): Boolean {
        val packageManager: PackageManager = requireActivity().packageManager
        val resolvedActivity: ResolveInfo? =
            packageManager.resolveActivity(
                intent,
                PackageManager.MATCH_DEFAULT_ONLY
            )
        return resolvedActivity != null
    }

    private fun updatePhoto(photoFileName: String?) {
        if (binding.reminderImage.tag != photoFileName) {
            val photoFile = photoFileName?.let {
                File(requireContext().applicationContext.filesDir, it)
            }
            if (photoFile?.exists() == true) {
                binding.reminderImage.doOnLayout { measuredView ->
                    val rotatedBitmap = getRotatedBitmap(photoFile)
                    val scaledBitmap = rotatedBitmap?.let {
                        getScaledBitmap(it, measuredView.width, measuredView.height)
                    }
                    binding.reminderImage.setImageBitmap(scaledBitmap)
                    binding.reminderImage.tag = photoFileName
                    binding.reminderImage.contentDescription = getString(R.string.desc_reminder_image)
                }
            } else {
                binding.reminderImage.setImageBitmap(null)
                binding.reminderImage.tag = null
                binding.reminderImage.contentDescription = getString(R.string.desc_reminder_no_image)
            }
        }
    }

    private fun saveReminderAndReturn() {
        viewLifecycleOwner.lifecycleScope.launch {
            newReminderViewModel.reminder.value?.let { reminder ->
                newReminderViewModel.updateReminder { reminder }
                findNavController().popBackStack()
            }
        }
    }

    fun isReminderUnsaved(): Boolean {
        return binding.reminderTitle.text.isNotBlank() || binding.reminderDesc.text.isNotBlank()
    }
}
