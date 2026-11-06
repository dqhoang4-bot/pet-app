package com.pethealth.passport.ui.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.pethealth.passport.R
import com.pethealth.passport.databinding.FragmentMedicalListBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible

class MedicalListFragment : Fragment() {

    private var _binding: FragmentMedicalListBinding? = null
    private val binding get() = _binding!!
    private val petId: Long by lazy { requireArguments().getLong("petId") }

    private val viewModel: MedicalListViewModel by viewModels {
        viewModelFactory {
            MedicalListViewModel(petId, container.healthRepository)
        }
    }

    private val adapter = MedicalAdapter { record ->
        findNavController().navigate(
            R.id.medicalEditFragment,
            bundleOf("petId" to petId, "recordId" to record.id)
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMedicalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerMedical.adapter = adapter

        val add = View.OnClickListener {
            findNavController().navigate(
                R.id.medicalEditFragment,
                bundleOf("petId" to petId, "recordId" to 0L)
            )
        }
        binding.fabAdd.setOnClickListener(add)
        binding.btnEmptyAdd.setOnClickListener(add)

        collectFlow(viewModel.records) { items ->
            adapter.submitList(items)
            binding.emptyState.visible(items.isEmpty())
            binding.recyclerMedical.visible(items.isNotEmpty())
            binding.fabAdd.visible(items.isNotEmpty())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
