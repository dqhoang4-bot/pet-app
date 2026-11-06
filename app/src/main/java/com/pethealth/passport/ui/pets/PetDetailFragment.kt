package com.pethealth.passport.ui.pets

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.databinding.DialogQrBinding
import com.pethealth.passport.databinding.FragmentPetDetailBinding
import com.pethealth.passport.ui.common.ageLabel
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.speciesLabel
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import com.pethealth.passport.ui.health.MedicalAdapter
import com.pethealth.passport.ui.health.VaccinationAdapter
import com.pethealth.passport.util.Dates
import com.pethealth.passport.util.PetQr
import com.pethealth.passport.util.QrCode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class PetDetailFragment : Fragment() {

    private var _binding: FragmentPetDetailBinding? = null
    private val binding get() = _binding!!
    private val petId: Long by lazy { requireArguments().getLong("petId") }
    private var petLoaded = false

    private val viewModel: PetDetailViewModel by viewModels {
        viewModelFactory { PetDetailViewModel(petId, container.petRepository, container.healthRepository) }
    }

    private val vaccinationAdapter = VaccinationAdapter { vaccination ->
        findNavController().navigate(
            R.id.vaccinationEditFragment,
            bundleOf("petId" to vaccination.petId, "vaccinationId" to vaccination.id)
        )
    }

    private val medicalAdapter = MedicalAdapter { record ->
        findNavController().navigate(
            R.id.medicalEditFragment,
            bundleOf("petId" to record.petId, "recordId" to record.id)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerVaccinations.adapter = vaccinationAdapter
        binding.recyclerMedical.adapter = medicalAdapter

        binding.btnEdit.setOnClickListener {
            findNavController().navigate(R.id.petEditFragment, bundleOf("petId" to petId))
        }
        binding.btnAddVaccination.setOnClickListener {
            findNavController().navigate(
                R.id.vaccinationEditFragment,
                bundleOf("petId" to petId, "vaccinationId" to 0L)
            )
        }
        binding.btnAllVaccinations.setOnClickListener {
            findNavController().navigate(R.id.vaccinationListFragment, bundleOf("petId" to petId))
        }
        binding.btnAddMedical.setOnClickListener {
            findNavController().navigate(
                R.id.medicalEditFragment,
                bundleOf("petId" to petId, "recordId" to 0L)
            )
        }
        binding.btnAllMedical.setOnClickListener {
            findNavController().navigate(R.id.medicalListFragment, bundleOf("petId" to petId))
        }
        binding.cardWeight.setOnClickListener {
            findNavController().navigate(R.id.weightFragment, bundleOf("petId" to petId))
        }

        collectFlow(viewModel.state) { state ->
            val pet = state.pet
            if (pet == null) {
                if (petLoaded) findNavController().navigateUp()
                return@collectFlow
            }
            petLoaded = true
            bindPet(pet)
            binding.btnQr.setOnClickListener {
                showQr(pet, state.vaccinations.maxByOrNull { it.date })
            }

            vaccinationAdapter.submitList(state.vaccinations.take(3))
            binding.txtEmptyVaccinations.visible(state.vaccinations.isEmpty())
            binding.recyclerVaccinations.visible(state.vaccinations.isNotEmpty())
            binding.btnAllVaccinations.visible(state.vaccinations.size > 3)

            medicalAdapter.submitList(state.medical.take(3))
            binding.txtEmptyMedical.visible(state.medical.isEmpty())
            binding.recyclerMedical.visible(state.medical.isNotEmpty())
            binding.btnAllMedical.visible(state.medical.size > 3)

            val latest = state.weights.lastOrNull()
            binding.txtWeight.text = if (latest != null) {
                getString(R.string.common_kg, latest.weightKg)
            } else {
                getString(R.string.weight_empty)
            }
            binding.txtWeightMeta.text = if (latest != null) {
                buildString {
                    append(Dates.format(latest.date))
                    if (state.weights.size > 1) {
                        val change = latest.weightKg - state.weights[state.weights.size - 2].weightKg
                        append(" • ")
                        append(
                            if (change >= 0) "+%.1f kg".format(change)
                            else "%.1f kg".format(change)
                        )
                    }
                }
            } else {
                getString(R.string.weight_chart_empty)
            }
        }
    }

    private fun bindPet(pet: Pet) {
        binding.txtName.text = pet.name
        binding.txtSpeciesBreed.text = listOf(pet.speciesLabel(requireContext()), pet.breed)
            .filter { it.isNotBlank() }
            .joinToString(" • ")
        binding.txtAgeGender.text = listOf(pet.ageLabel(requireContext()), genderLabel(pet.gender))
            .filter { it.isNotBlank() }
            .joinToString(" • ")
        binding.txtChip.text = pet.microchip.ifBlank { "—" }
        binding.txtOwner.text = pet.ownerName.ifBlank { "—" }
        binding.txtPhone.text = pet.ownerPhone.ifBlank { "—" }
        binding.txtNotes.text = pet.notes.ifBlank { getString(R.string.pet_note_empty) }

        if (pet.photoUri.isNullOrBlank()) {
            binding.imgPhoto.visible(false)
            binding.txtEmoji.visible(true)
            binding.txtEmoji.text = com.pethealth.passport.data.model.Species.emoji(pet.species)
        } else {
            binding.imgPhoto.visible(true)
            binding.txtEmoji.visible(false)
            binding.imgPhoto.load(File(pet.photoUri!!.removePrefix("file://"))) {
                crossfade(true)
                placeholder(R.drawable.bg_avatar)
                error(R.drawable.bg_avatar)
            }
        }
    }

    private fun genderLabel(gender: String): String = when (gender) {
        "male" -> getString(R.string.gender_male)
        "female" -> getString(R.string.gender_female)
        else -> getString(R.string.gender_unknown)
    }

    private fun showQr(pet: Pet, latestVaccination: Vaccination?) {
        val qrBinding = DialogQrBinding.inflate(layoutInflater)
        qrBinding.txtQrPetName.text = pet.name
        val payload = PetQr.encode(pet, latestVaccination)
        runCatching {
            qrBinding.imgQr.setImageBitmap(QrCode.generate(payload))
        }.onFailure {
            qrBinding.imgQr.visible(false)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.qr_title)
            .setView(qrBinding.root)
            .setPositiveButton(R.string.pet_share) { _, _ -> shareProfile(pet, latestVaccination) }
            .setNegativeButton(R.string.common_close, null)
            .show()
    }

    private fun shareProfile(pet: Pet, latestVaccination: Vaccination?) {
        val text = buildString {
            appendLine("${getString(R.string.qr_pet_label)}: ${pet.name}")
            appendLine("${getString(R.string.pet_species)}: ${pet.speciesLabel(requireContext())}")
            if (pet.breed.isNotBlank()) appendLine("${getString(R.string.pet_breed)}: ${pet.breed}")
            if (pet.microchip.isNotBlank()) {
                appendLine("${getString(R.string.pet_microchip)}: ${pet.microchip}")
            }
            if (pet.ownerName.isNotBlank()) {
                appendLine("${getString(R.string.qr_owner_label)}: ${pet.ownerName}")
            }
            if (pet.ownerPhone.isNotBlank()) {
                appendLine("${getString(R.string.qr_phone_label)}: ${pet.ownerPhone}")
            }
            if (latestVaccination != null) {
                appendLine("${getString(R.string.qr_vaccine_label)}: ${latestVaccination.name}")
                latestVaccination.nextDueDate?.let {
                    appendLine("${getString(R.string.vaccination_next_due)}: ${Dates.format(it)}")
                }
            }
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.qr_share_title))
            putExtra(Intent.EXTRA_TEXT, text.trim())
        }
        runCatching {
            startActivity(Intent.createChooser(intent, getString(R.string.qr_share_title)))
        }.onFailure { toast(getString(R.string.qr_shared)) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
