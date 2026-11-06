package com.pethealth.passport.ui.pets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pethealth.passport.R
import com.pethealth.passport.databinding.FragmentPetListBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible

class PetListFragment : Fragment() {

    private var _binding: FragmentPetListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PetListViewModel by viewModels {
        viewModelFactory { PetListViewModel(container.petRepository) }
    }

    private val adapter = PetListAdapter { pet ->
        findNavController().navigate(R.id.petDetailFragment, bundleOf("petId" to pet.id))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerPets.adapter = adapter

        val add = View.OnClickListener {
            findNavController().navigate(R.id.petEditFragment, bundleOf("petId" to 0L))
        }
        binding.fabAdd.setOnClickListener(add)
        binding.btnEmptyAdd.setOnClickListener(add)

        collectFlow(viewModel.pets) { pets ->
            adapter.submitList(pets)
            binding.emptyState.visible(pets.isEmpty())
            binding.recyclerPets.visible(pets.isNotEmpty())
            binding.fabAdd.visible(pets.isNotEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
