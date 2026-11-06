package com.pethealth.passport.ui.content

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pethealth.passport.R
import com.pethealth.passport.data.repository.Clinic
import com.pethealth.passport.databinding.FragmentClinicsBinding
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.toast

class ClinicsFragment : Fragment() {

    private var _binding: FragmentClinicsBinding? = null
    private val binding get() = _binding!!

    private val repository get() = container.contentRepository
    private val adapter by lazy {
        ClinicAdapter(
            english = repository.isEnglish(),
            onCall = { call(it) },
            onMaps = { openMap(it) }
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClinicsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerClinics.adapter = adapter
        adapter.submitList(repository.clinics)
    }

    private fun call(clinic: Clinic) {
        val uri = Uri.parse("tel:${clinic.phone.replace(" ", "")}")
        try {
            startActivity(Intent(Intent.ACTION_DIAL, uri))
        } catch (_: ActivityNotFoundException) {
            toast(getString(R.string.clinics_no_app))
        }
    }

    private fun openMap(clinic: Clinic) {
        val location = "${clinic.latitude},${clinic.longitude}"
        val uri = Uri.parse("geo:$location?q=$location(${Uri.encode(clinic.name)})")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (_: ActivityNotFoundException) {
            toast(getString(R.string.clinics_no_app))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
