package com.pethealth.passport.ui.community

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.pethealth.passport.R
import com.pethealth.passport.data.model.Species
import com.pethealth.passport.databinding.FragmentCreatePostBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import kotlinx.coroutines.launch
import java.io.File

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreatePostViewModel by viewModels {
        viewModelFactory {
            CreatePostViewModel(container.communityRepository, container.petRepository)
        }
    }

    private var speciesKey = Species.all.first().key
    private var imagePath: String? = null
    private var autoFilled = false

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        imagePath = uri.toString()
        showImage()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val labels = Species.all.map { getString(it.labelRes) }
        binding.actvSpecies.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, labels)
        )
        binding.actvSpecies.setText(labels.first(), false)
        binding.actvSpecies.setOnItemClickListener { _, _, position, _ ->
            speciesKey = Species.all[position].key
        }

        binding.btnPickImage.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRemoveImage.setOnClickListener {
            imagePath = null
            binding.imgPreview.setImageResource(R.drawable.bg_image_placeholder)
            binding.btnRemoveImage.visible(false)
        }
        binding.btnPublish.setOnClickListener { publish() }

        collectFlow(viewModel.pets) { pets ->
            if (!autoFilled && pets.isNotEmpty()) {
                autoFilled = true
                val pet = pets.first()
                binding.editPetName.setText(pet.name)
                speciesKey = pet.species
                val index = Species.all.indexOfFirst { it.key == pet.species }.coerceAtLeast(0)
                binding.actvSpecies.setText(labels[index], false)
            }
        }
    }

    private fun showImage() {
        val path = imagePath ?: return
        binding.imgPreview.load(File(path.removePrefix("file://"))) {
            crossfade(true)
            placeholder(R.drawable.bg_image_placeholder)
            error(R.drawable.bg_image_placeholder)
        }
        binding.btnRemoveImage.visible(true)
    }

    private fun publish() {
        val content = binding.editContent.text?.toString()?.trim().orEmpty()
        if (content.isBlank()) {
            binding.inputContent.error = getString(R.string.community_content_required)
            return
        }
        binding.inputContent.error = null
        val petName = binding.editPetName.text?.toString()?.trim().orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.createPost(content, speciesKey, petName, imagePath)
            toast(getString(R.string.community_posted))
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
