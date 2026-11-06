package com.pethealth.passport.ui.health

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.WeightEntry
import com.pethealth.passport.databinding.DialogWeightBinding
import com.pethealth.passport.databinding.FragmentWeightBinding
import com.pethealth.passport.ui.common.collectFlow
import com.pethealth.passport.ui.common.container
import com.pethealth.passport.ui.common.pickDate
import com.pethealth.passport.ui.common.toast
import com.pethealth.passport.ui.common.viewModelFactory
import com.pethealth.passport.ui.common.visible
import com.pethealth.passport.util.Dates
import kotlinx.coroutines.launch
import java.util.Locale

class WeightFragment : Fragment() {

    private var _binding: FragmentWeightBinding? = null
    private val binding get() = _binding!!
    private val petId: Long by lazy { requireArguments().getLong("petId") }

    private val viewModel: WeightViewModel by viewModels {
        viewModelFactory { WeightViewModel(petId, container.healthRepository) }
    }

    private val adapter = WeightAdapter { entry -> confirmDelete(entry) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeightBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerWeights.adapter = adapter
        binding.btnAddWeight.setOnClickListener { showAddDialog() }
        setupChart()

        collectFlow(viewModel.weights) { weights ->
            val sorted = weights.sortedBy { it.date }
            adapter.submitList(weights.sortedByDescending { it.date })
            binding.emptyWeights.visible(weights.isEmpty())
            binding.recyclerWeights.visible(weights.isNotEmpty())
            renderChart(sorted)
            renderStats(sorted)
        }
    }

    private fun setupChart() {
        binding.chartWeight.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            axisRight.isEnabled = false
            axisLeft.setDrawAxisLine(false)
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.divider)
            axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawAxisLine(false)
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.text_secondary)
        }
    }

    private fun renderChart(weights: List<WeightEntry>) {
        val showChart = weights.size >= 2
        binding.chartWeight.visible(showChart)
        binding.txtChartEmpty.visible(!showChart)
        if (!showChart) {
            binding.chartWeight.clear()
            return
        }

        val labels = weights.map { Dates.format(it.date, "dd/MM") }
        val entries = weights.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.weightKg.toFloat())
        }
        val dataSet = LineDataSet(entries, "").apply {
            color = ContextCompat.getColor(requireContext(), R.color.brand_teal)
            lineWidth = 2.5f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.brand_teal))
            circleRadius = 4f
            setDrawCircleHole(false)
            setDrawValues(false)
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.brand_teal)
            fillAlpha = 40
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        binding.chartWeight.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String =
                labels.getOrNull(value.toInt()) ?: ""
        }
        binding.chartWeight.xAxis.labelCount = minOf(labels.size, 6)
        val min = weights.minOf { it.weightKg }.toFloat()
        val max = weights.maxOf { it.weightKg }.toFloat()
        val padding = ((max - min).coerceAtLeast(0.5f)) * 0.25f
        binding.chartWeight.axisLeft.axisMinimum = (min - padding).coerceAtLeast(0f)
        binding.chartWeight.axisLeft.axisMaximum = max + padding
        binding.chartWeight.data = LineData(dataSet)
        binding.chartWeight.invalidate()
    }

    private fun renderStats(weights: List<WeightEntry>) {
        val latest = weights.lastOrNull()
        binding.txtLatest.text = latest?.let { getString(R.string.common_kg, it.weightKg) } ?: "—"
        binding.txtRange.text = if (weights.isNotEmpty()) {
            getString(R.string.weight_range, weights.minOf { it.weightKg }, weights.maxOf { it.weightKg })
        } else {
            ""
        }
        binding.txtChange.text = if (weights.size >= 2) {
            val change = weights.last().weightKg - weights.first().weightKg
            String.format(Locale.getDefault(), "%s%.1f kg", if (change >= 0) "+" else "", change)
        } else {
            "—"
        }
        binding.txtChange.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (weights.size >= 2 && weights.last().weightKg < weights.first().weightKg) {
                    R.color.status_overdue
                } else {
                    R.color.brand_teal
                }
            )
        )
    }

    private fun showAddDialog() {
        val dialogBinding = DialogWeightBinding.inflate(layoutInflater)
        var pickedDate = System.currentTimeMillis()
        dialogBinding.editDate.setText(Dates.format(pickedDate))

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.weight_add)
            .setView(dialogBinding.root)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_save, null)
            .create()

        val pickDateClick = View.OnClickListener {
            pickDate(pickedDate) { picked ->
                pickedDate = picked
                dialogBinding.editDate.setText(Dates.format(picked))
            }
        }
        dialogBinding.editDate.setOnClickListener(pickDateClick)
        dialogBinding.inputDate.setEndIconOnClickListener(pickDateClick)

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val value = dialogBinding.editWeight.text?.toString()?.trim()
                    ?.replace(',', '.')?.toDoubleOrNull()
                if (value == null || value <= 0.0) {
                    dialogBinding.inputWeight.error = getString(R.string.weight_invalid)
                    return@setOnClickListener
                }
                dialogBinding.inputWeight.error = null
                val entry = WeightEntry(
                    petId = petId,
                    date = pickedDate,
                    weightKg = value,
                    note = dialogBinding.editNote.text?.toString()?.trim().orEmpty()
                )
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.save(entry)
                    toast(getString(R.string.weight_saved))
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun confirmDelete(entry: WeightEntry) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.weight_title)
            .setMessage(R.string.weight_delete_confirm)
            .setNegativeButton(R.string.common_cancel, null)
            .setPositiveButton(R.string.common_delete) { _, _ ->
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.delete(entry)
                    toast(getString(R.string.weight_deleted))
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
