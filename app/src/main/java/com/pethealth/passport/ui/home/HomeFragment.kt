package com.pethealth.passport.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pethealth.passport.MainActivity
import com.pethealth.passport.R
import com.pethealth.passport.data.model.DueStatus
import com.pethealth.passport.data.model.dueStatus
import com.pethealth.passport.databinding.FragmentHomeBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        viewModelFactory {
            HomeViewModel(container.petRepository, container.healthRepository)
        }
    }

    private val petAdapter = PetCardAdapter { pet ->
        findNavController().navigate(R.id.petDetailFragment, bundleOf("petId" to pet.id))
    }

    private val upcomingAdapter = UpcomingAdapter { item ->
        findNavController().navigate(
            R.id.petDetailFragment,
            bundleOf("petId" to item.vaccination.petId)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerPets.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerPets.adapter = petAdapter
        binding.recyclerUpcoming.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerUpcoming.adapter = upcomingAdapter

        val addPet = View.OnClickListener {
            findNavController().navigate(R.id.petEditFragment, bundleOf("petId" to 0L))
        }
        binding.btnAddPet.setOnClickListener(addPet)
        binding.btnAddFirstPet.setOnClickListener(addPet)

        binding.cardTips.setOnClickListener { findNavController().navigate(R.id.tipsFragment) }
        binding.cardQuiz.setOnClickListener { findNavController().navigate(R.id.quizFragment) }
        binding.cardClinics.setOnClickListener { findNavController().navigate(R.id.clinicsFragment) }
        binding.cardScan.setOnClickListener { (requireActivity() as MainActivity).startQrScan() }

        collectFlow(viewModel.state) { state ->
            petAdapter.submitList(state.pets)
            upcomingAdapter.submitList(state.upcoming)
            binding.emptyPets.visible(state.pets.isEmpty())
            binding.recyclerPets.visible(state.pets.isNotEmpty())
            binding.txtStatPets.text = state.pets.size.toString()
            binding.txtStatDue.text =
                state.upcoming.count { it.vaccination.dueStatus() == DueStatus.DUE_SOON }.toString()
            binding.txtStatOverdue.text =
                state.upcoming.count { it.vaccination.dueStatus() == DueStatus.OVERDUE }.toString()
            binding.txtUpcomingCount.text = state.upcoming.size.toString()
            binding.txtUpcomingEmpty.visible(state.upcoming.isEmpty())
            binding.recyclerUpcoming.visible(state.upcoming.isNotEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
