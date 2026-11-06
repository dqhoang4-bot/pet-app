package com.pethealth.passport.ui.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.databinding.FragmentVaccinationEditBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.pickDate
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import com.pethealth.passport.util.Dates
import kotlinx.coroutines.launch

class VaccinationEditFragment : Fragment() {

    private var _binding: FragmentVaccinationEditBinding? = null
    private val binding get() = _binding!!
    private val petId: Long by lazy { requireArguments().getLong("petId") }
    private val vaccinationId: Long by lazy { requireArguments().getLong("vaccinationId") }

    private val viewModel: VaccinationEditViewModel by viewModels {
        viewModelFactory {
            VaccinationEditViewModel(petId, vaccinationId, container.healthRepository)
        }
    }

    private var current: Vaccination? = null
    private var initialized = false
    private var date: Long = System.currentTimeMillis()
    private var nextDue: Long? = null
    private var kindKey = Vaccination.KIND_VACCINE
    private val kindKeys = listOf(Vaccination.KIND_VACCINE, Vaccination.KIND_DEWORMER)
    private var kindLabels: List<String> = emptyList()
    private var autoDue = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVaccinationEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        kindLabels = listOf(getString(R.string.kind_vaccine), getString(R.string.kind_dewormer))
        binding.actvKind.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, kindLabels)
        )
        binding.actvKind.setText(kindLabels.first(), false)
        binding.actvKind.setOnItemClickListener { _, _, position, _ ->
            kindKey = kindKeys[position]
            updateAutoDue()
            buildPresets()
        }

        binding.editDate.setText(Dates.format(date))
        updateAutoDue()
        buildPresets()

        val pickDateClick = View.OnClickListener {
            pickDate(date) { picked ->
                date = picked
                binding.editDate.setText(Dates.format(picked))
                updateAutoDue()
            }
        }
        binding.editDate.setOnClickListener(pickDateClick)
        binding.inputDate.setEndIconOnClickListener(pickDateClick)

        val pickDueClick = View.OnClickListener {
            pickDate(nextDue ?: Dates.nextDose(date, kindKey)) { picked ->
                nextDue = picked
                autoDue = false
                binding.switchAuto.isChecked = false
                binding.editNextDue.setText(Dates.format(picked))
            }
        }
        binding.editNextDue.setOnClickListener(pickDueClick)
        binding.inputNextDue.setEndIconOnClickListener(pickDueClick)

        binding.switchAuto.setOnCheckedChangeListener { _, checked ->
            autoDue = checked
            updateAutoDue()
        }

        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        collectFlow(viewModel.vaccination) { vaccination ->
            if (!initialized && vaccination != null) {
                initialized = true
                bind(vaccination)
            }
        }
        collectFlow(viewModel.loaded) { loaded ->
            if (loaded && vaccinationId > 0 && viewModel.vaccination.value == null) {
                findNavController().navigateUp()
            }
        }
    }

    private fun bind(vaccination: Vaccination) {
        current = vaccination
        binding.editName.setText(vaccination.name)
        kindKey = vaccination.kind
        val index = kindKeys.indexOf(vaccination.kind).coerceAtLeast(0)
        binding.actvKind.setText(kindLabels[index], false)
        date = vaccination.date
        binding.editDate.setText(Dates.format(date))
        nextDue = vaccination.nextDueDate
        autoDue = vaccination.nextDueDate == null ||
            vaccination.nextDueDate == Dates.nextDose(vaccination.date, vaccination.kind)
        binding.switchAuto.isChecked = autoDue
        binding.editNextDue.setText(
            vaccination.nextDueDate?.let { Dates.format(it) } ?: "—"
        )
        binding.editClinic.setText(vaccination.clinic)
        binding.editVet.setText(vaccination.vetName)
        binding.editNote.setText(vaccination.note)
        binding.switchReminder.isChecked = vaccination.reminderEnabled
        binding.btnDelete.visible(true)
        buildPresets()
    }

    private fun updateAutoDue() {
        if (!autoDue) return
        nextDue = Dates.nextDose(date, kindKey)
        nextDue?.let { binding.editNextDue.setText(Dates.format(it)) }
    }

    private fun buildPresets() {
        if (_binding == null) return
        val english = container.settings.isEnglish()
        val presets = if (kindKey == Vaccination.KIND_DEWORMER) {
            if (english) listOf("Dewormer", "Dewormer – 3 months")
            else listOf("Tẩy giun", "Tẩy giun định kỳ")
        } else {
            if (english) {
                listOf("Rabies vaccine", "Core vaccine (5-in-1)", "FVRCP vaccine", "Leptospirosis vaccine")
            } else {
                listOf("Vaccine dại", "Vaccine 5 bệnh", "Vaccine FVRCP", "Vaccine Leptospirosis")
            }
        }
        binding.chipPresets.removeAllViews()
        presets.forEach { label ->
            val chip = Chip(requireContext()).apply {
                text = label
                isCheckable = false
                setOnClickListener { binding.editName.setText(label) }
            }
            binding.chipPresets.addView(chip)
        }
    }

    private fun save() {
        val name = binding.editName.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) {
            binding.inputName.error = getString(R.string.vaccination_name_required)
            return
        }
        binding.inputName.error = null
        val due = if (autoDue) Dates.nextDose(date, kindKey) else nextDue
        val vaccination = Vaccination(
            id = current?.id ?: 0L,
            petId = petId,
            name = name,
            kind = kindKey,
            date = date,
            nextDueDate = due,
            clinic = binding.editClinic.text?.toString()?.trim().orEmpty(),
            vetName = binding.editVet.text?.toString()?.trim().orEmpty(),
            note = binding.editNote.text?.toString()?.trim().orEmpty(),
            reminderEnabled = binding.switchReminder.isChecked
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(vaccination)
            toast(getString(R.string.vaccination_saved))
            findNavController().navigateUp()
        }
    }

    private fun confirmDelete() {
        val vaccination = current ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.vaccination_delete)
            .setMessage(R.string.vaccination_delete_confirm)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.delete(vaccination)
                    toast(getString(R.string.vaccination_deleted))
                    findNavController().navigateUp()
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
