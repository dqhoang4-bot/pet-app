package com.pethealth.passport.ui.health

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.MedicalRecord
import com.pethealth.passport.databinding.FragmentMedicalEditBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.pickDate
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import com.pethealth.passport.util.Dates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MedicalEditFragment : Fragment() {

    private var _binding: FragmentMedicalEditBinding? = null
    private val binding get() = _binding!!
    private val petId: Long by lazy { requireArguments().getLong("petId") }
    private val recordId: Long by lazy { requireArguments().getLong("recordId") }

    private val viewModel: MedicalEditViewModel by viewModels {
        viewModelFactory {
            MedicalEditViewModel(recordId, container.healthRepository)
        }
    }

    private var current: MedicalRecord? = null
    private var initialized = false
    private var date: Long = System.currentTimeMillis()
    private var photoPath: String? = null

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        persistPhoto(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicalEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.editDate.setText(Dates.format(date))

        val pickDateClick = View.OnClickListener {
            pickDate(date) { picked ->
                date = picked
                binding.editDate.setText(Dates.format(picked))
            }
        }
        binding.editDate.setOnClickListener(pickDateClick)
        binding.inputDate.setEndIconOnClickListener(pickDateClick)

        binding.btnPickPhoto.setOnClickListener { pickPhoto.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener {
            photoPath = null
            binding.imgPhoto.setImageResource(R.drawable.bg_image_placeholder)
            binding.btnRemovePhoto.visible(false)
        }
        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        collectFlow(viewModel.record) { record ->
            if (!initialized && record != null) {
                initialized = true
                bind(record)
            }
        }
        collectFlow(viewModel.loaded) { loaded ->
            if (loaded && recordId > 0 && viewModel.record.value == null) {
                findNavController().navigateUp()
            }
        }
    }

    private fun bind(record: MedicalRecord) {
        current = record
        date = record.date
        binding.editDate.setText(Dates.format(date))
        binding.editClinic.setText(record.clinic)
        binding.editVet.setText(record.vetName)
        binding.editDiagnosis.setText(record.diagnosis)
        binding.editPrescription.setText(record.prescription)
        binding.editNote.setText(record.note)
        photoPath = record.photoUri
        showPhoto()
        binding.btnDelete.visible(true)
    }

    private fun showPhoto() {
        val path = photoPath
        if (path.isNullOrBlank()) {
            binding.imgPhoto.setImageResource(R.drawable.bg_image_placeholder)
            binding.btnRemovePhoto.visible(false)
        } else {
            binding.imgPhoto.load(File(path.removePrefix("file://"))) {
                crossfade(true)
                placeholder(R.drawable.bg_image_placeholder)
                error(R.drawable.bg_image_placeholder)
            }
            binding.btnRemovePhoto.visible(true)
        }
    }

    private fun persistPhoto(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val stored = withContext(Dispatchers.IO) {
                container.imageStore.copyToInternal(uri.toString())
            }
            if (stored != null) {
                photoPath = stored
                showPhoto()
            }
        }
    }

    private fun save() {
        val diagnosis = binding.editDiagnosis.text?.toString()?.trim().orEmpty()
        if (diagnosis.isBlank()) {
            binding.inputDiagnosis.error = getString(R.string.medical_diagnosis_required)
            return
        }
        binding.inputDiagnosis.error = null
        val record = MedicalRecord(
            id = current?.id ?: 0L,
            petId = petId,
            date = date,
            clinic = binding.editClinic.text?.toString()?.trim().orEmpty(),
            vetName = binding.editVet.text?.toString()?.trim().orEmpty(),
            diagnosis = diagnosis,
            prescription = binding.editPrescription.text?.toString()?.trim().orEmpty(),
            note = binding.editNote.text?.toString()?.trim().orEmpty(),
            photoUri = photoPath
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(record)
            toast(getString(R.string.medical_saved))
            findNavController().navigateUp()
        }
    }

    private fun confirmDelete() {
        val record = current ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.medical_delete)
            .setMessage(R.string.medical_delete_confirm)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.delete(record)
                    toast(getString(R.string.medical_deleted))
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
