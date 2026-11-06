package com.pethealth.passport.ui.settings

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.pethealth.passport.BuildConfig
import com.pethealth.passport.R
import com.pethealth.passport.databinding.FragmentSettingsBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.util.NotificationHelper
import com.pethealth.passport.util.ReminderScheduler
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        viewModelFactory { SettingsViewModel(container.settings, container.seeder) }
    }

    private var suppressSwitchCallback = false
    private var pendingPermissionAction: (() -> Unit)? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingPermissionAction
        pendingPermissionAction = null
        if (granted) {
            action?.invoke()
        } else {
            toast(getString(R.string.settings_notification_unavailable))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.cardLanguage.setOnClickListener { showLanguageDialog() }
        binding.btnSaveOwner.setOnClickListener { saveOwnerName() }
        binding.editOwnerName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveOwnerName()
                true
            } else {
                false
            }
        }

        binding.switchReminders.setOnCheckedChangeListener { _, checked ->
            if (suppressSwitchCallback) return@setOnCheckedChangeListener
            viewModel.setReminderEnabled(checked)
            if (checked) {
                ReminderScheduler.schedule(requireContext(), viewModel.state.value.reminderHour)
                withNotificationPermission { }
            } else {
                ReminderScheduler.cancel(requireContext())
            }
        }
        binding.cardReminderHour.setOnClickListener { showTimePicker() }
        binding.cardTestNotification.setOnClickListener { sendTestNotification() }
        binding.cardReset.setOnClickListener { confirmReset() }
        binding.cardAbout.setOnClickListener { showAbout() }

        collectFlow(viewModel.state) { render(it) }
    }

    private fun render(state: SettingsUiState) {
        binding.txtLanguage.text = getString(
            if (state.language == "en") R.string.settings_language_en
            else R.string.settings_language_vi
        )

        val currentName = binding.editOwnerName.text?.toString().orEmpty()
        if (currentName != state.ownerName && !binding.editOwnerName.hasFocus()) {
            binding.editOwnerName.setText(state.ownerName)
        }

        suppressSwitchCallback = true
        binding.switchReminders.isChecked = state.reminderEnabled
        suppressSwitchCallback = false

        binding.txtReminderHour.text = getString(
            R.string.settings_reminder_time_value,
            state.reminderHour
        )
        binding.cardReminderHour.isEnabled = state.reminderEnabled
        binding.cardReminderHour.alpha = if (state.reminderEnabled) 1f else 0.5f
        binding.txtAboutVersion.text = getString(R.string.more_version, BuildConfig.VERSION_NAME)
    }

    private fun saveOwnerName() {
        val name = binding.editOwnerName.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) {
            binding.editOwnerName.error = getString(R.string.common_error_name_required)
            return
        }
        binding.editOwnerName.error = null
        viewModel.setOwnerName(name)
        toast(getString(R.string.settings_owner_saved))
    }

    private fun showLanguageDialog() {
        val codes = arrayOf("vi", "en")
        val labels = arrayOf(
            getString(R.string.settings_language_vi),
            getString(R.string.settings_language_en)
        )
        val selected = codes.indexOf(viewModel.state.value.language).coerceAtLeast(0)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_language)
            .setSingleChoiceItems(labels, selected) { dialog, which ->
                val code = codes[which]
                viewModel.setLanguage(code)
                AppCompatDelegate.setApplicationLocales(
                    LocaleListCompat.forLanguageTags(code)
                )
                toast(getString(R.string.settings_language_changed))
                dialog.dismiss()
            }
            .setNegativeButton(R.string.common_cancel, null)
            .show()
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(viewModel.state.value.reminderHour)
            .setMinute(0)
            .setTitleText(R.string.settings_reminder_hour)
            .build()

        picker.addOnPositiveButtonClickListener {
            viewModel.setReminderHour(picker.hour)
            if (viewModel.state.value.reminderEnabled) {
                ReminderScheduler.schedule(requireContext(), picker.hour)
            }
        }
        picker.show(childFragmentManager, "reminder_time_picker")
    }

    private fun sendTestNotification() {
        withNotificationPermission {
            NotificationHelper.showTest(requireContext())
            toast(getString(R.string.settings_notification_sent))
        }
    }

    private fun withNotificationPermission(action: () -> Unit) {
        if (NotificationHelper.canNotify(requireContext())) {
            action()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingPermissionAction = action
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            toast(getString(R.string.settings_notification_unavailable))
        }
    }

    private fun confirmReset() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_reset)
            .setMessage(R.string.settings_reset_confirm)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_confirm) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.resetDemoData()
                    toast(getString(R.string.settings_reset_done))
                }
            }
            .show()
    }

    private fun showAbout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_about_title)
            .setMessage(
                getString(R.string.settings_about_text) +
                    "\n\n" + getString(R.string.more_version, BuildConfig.VERSION_NAME)
            )
            .setPositiveButton(R.string.common_close, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
