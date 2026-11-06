package com.pethealth.passport.ui.pets

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import coil.load
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.model.Species
import com.pethealth.passport.databinding.FragmentPetEditBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.pickDate
import com.pethealth.passport.ui.common.speciesLabel
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import com.pethealth.passport.util.Dates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PetEditFragment : Fragment() {

    private var _binding: FragmentPetEditBinding? = null
    private val binding get() = _binding!!
    private val petId: Long by lazy { requireArguments().getLong("petId") }

    private val viewModel: PetEditViewModel by viewModels {
        viewModelFactory { PetEditViewModel(container.petRepository, petId) }
    }

    private var currentPet: Pet? = null
    private var initialized = false
    private var photoPath: String? = null
    private var birthDate: Long = 0L
    private var speciesKey = "dog"
    private var genderKey = "male"
    private var speciesLabels: List<String> = emptyList()
    private var genderLabels: List<String> = emptyList()

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        persistPhoto(uri)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        speciesLabels = Species.all.map { getString(it.labelRes) }
        genderLabels = listOf(
            getString(R.string.gender_male),
            getString(R.string.gender_female),
            getString(R.string.gender_unknown)
        )
        binding.actvSpecies.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, speciesLabels)
        )
        binding.actvGender.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, genderLabels)
        )
        binding.actvSpecies.setText(speciesLabels.first(), false)
        binding.actvGender.setText(genderLabels.first(), false)

        binding.actvSpecies.setOnItemClickListener { _, _, position, _ ->
            speciesKey = Species.all[position].key
            updateAvatar()
        }
        binding.actvGender.setOnItemClickListener { _, _, position, _ ->
            genderKey = listOf("male", "female", "unknown")[position]
        }

        binding.editBirthDate.setOnClickListener {
            pickDate(birthDate.takeIf { it > 0 }) { picked ->
                birthDate = picked
                binding.editBirthDate.setText(Dates.format(picked))
            }
        }
        binding.btnPickPhoto.setOnClickListener { pickPhoto.launch("image/*") }
        binding.btnRemovePhoto.setOnClickListener {
            photoPath = null
            binding.imgPhoto.visible(false)
            binding.txtEmoji.visible(true)
            binding.btnRemovePhoto.visible(false)
        }
        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener { confirmDelete() }

        collectFlow(viewModel.pet) { pet ->
            if (!initialized && pet != null) {
                initialized = true
                bindPet(pet)
            }
        }
        collectFlow(viewModel.loaded) { loaded ->
            if (loaded && petId > 0 && viewModel.pet.value == null) {
                findNavController().navigateUp()
            }
        }
    }

    private fun bindPet(pet: Pet) {
        currentPet = pet
        photoPath = pet.photoUri
        binding.editName.setText(pet.name)
        binding.editBreed.setText(pet.breed)
        binding.editWeight.setText(if (pet.weightKg > 0) pet.weightKg.toString() else "")
        binding.editColor.setText(pet.color)
        binding.editChip.setText(pet.microchip)
        binding.editOwner.setText(pet.ownerName)
        binding.editPhone.setText(pet.ownerPhone)
        binding.editNotes.setText(pet.notes)
        birthDate = pet.birthDate
        if (pet.birthDate > 0) binding.editBirthDate.setText(Dates.format(pet.birthDate))

        speciesKey = pet.species
        val speciesIndex = Species.all.indexOfFirst { it.key == pet.species }.coerceAtLeast(0)
        binding.actvSpecies.setText(speciesLabels[speciesIndex], false)

        genderKey = pet.gender
        val genderIndex = listOf("male", "female", "unknown").indexOf(pet.gender).coerceAtLeast(0)
        binding.actvGender.setText(genderLabels[genderIndex], false)

        binding.btnDelete.visible(true)
        updateAvatar()
    }

    private fun updateAvatar() {
        if (photoPath != null) {
            binding.imgPhoto.visible(true)
            binding.txtEmoji.visible(false)
            binding.btnRemovePhoto.visible(true)
            binding.imgPhoto.load(File(photoPath!!.removePrefix("file://"))) {
                crossfade(true)
                placeholder(R.drawable.bg_avatar)
                error(R.drawable.bg_avatar)
            }
        } else {
            binding.imgPhoto.visible(false)
            binding.txtEmoji.visible(true)
            binding.txtEmoji.text = Species.emoji(speciesKey)
            binding.btnRemovePhoto.visible(false)
        }
    }

    private fun persistPhoto(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            val stored = withContext(Dispatchers.IO) {
                container.imageStore.copyToInternal(uri.toString())
            }
            if (stored != null) {
                photoPath = stored
                updateAvatar()
            }
        }
    }

    private fun save() {
        val name = binding.editName.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) {
            binding.inputName.error = getString(R.string.common_error_name_required)
            return
        }
        binding.inputName.error = null
        val weight = binding.editWeight.text?.toString()?.trim()?.replace(',', '.')?.toDoubleOrNull() ?: 0.0
        val pet = (currentPet ?: Pet(name = name, species = speciesKey)).copy(
            id = currentPet?.id ?: 0L,
            name = name,
            species = speciesKey,
            breed = binding.editBreed.text?.toString()?.trim().orEmpty(),
            gender = genderKey,
            birthDate = birthDate,
            weightKg = weight,
            color = binding.editColor.text?.toString()?.trim().orEmpty(),
            microchip = binding.editChip.text?.toString()?.trim().orEmpty(),
            ownerName = binding.editOwner.text?.toString()?.trim().orEmpty(),
            ownerPhone = binding.editPhone.text?.toString()?.trim().orEmpty(),
            photoUri = photoPath,
            notes = binding.editNotes.text?.toString()?.trim().orEmpty()
        )
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.save(pet)
            toast(getString(R.string.pet_saved))
            findNavController().navigateUp()
        }
    }

    private fun confirmDelete() {
        val pet = currentPet ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.pet_delete)
            .setMessage(R.string.pet_delete_confirm)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.delete(pet)
                    toast(getString(R.string.pet_deleted))
                    val navController = findNavController()
                    navController.navigate(
                        R.id.petListFragment,
                        null,
                        NavOptions.Builder()
                            .setPopUpTo(navController.graph.id, false)
                            .build()
                    )
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
