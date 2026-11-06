package com.pethealth.passport.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pethealth.passport.BuildConfig
import com.pethealth.passport.MainActivity
import com.pethealth.passport.R
import com.pethealth.passport.databinding.FragmentMoreBinding
import com.pethealth.passport.ui.common.container

class MoreFragment : Fragment() {

    private var _binding: FragmentMoreBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMoreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val version = BuildConfig.VERSION_NAME
        binding.txtVersion.text = getString(R.string.more_version, version)
        binding.txtQuizSubtitle.text = getString(
            R.string.more_quiz_subtitle,
            container.contentRepository.quizQuestions.size
        )

        binding.cardTips.setOnClickListener { findNavController().navigate(R.id.tipsFragment) }
        binding.cardQuiz.setOnClickListener { findNavController().navigate(R.id.quizFragment) }
        binding.cardClinics.setOnClickListener { findNavController().navigate(R.id.clinicsFragment) }
        binding.cardSettings.setOnClickListener { findNavController().navigate(R.id.settingsFragment) }
        binding.cardScan.setOnClickListener {
            (requireActivity() as MainActivity).startQrScan()
        }
        binding.cardAbout.setOnClickListener { showAbout(version) }
    }

    private fun showAbout(version: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_about_title)
            .setMessage(
                getString(R.string.settings_about_text) +
                    "\n\n" + getString(R.string.more_version, version)
            )
            .setPositiveButton(R.string.common_close, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
