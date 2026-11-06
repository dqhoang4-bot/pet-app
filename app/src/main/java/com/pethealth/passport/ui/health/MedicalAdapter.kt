package com.pethealth.passport.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.data.local.entity.MedicalRecord
import com.pethealth.passport.databinding.ItemMedicalBinding
import com.pethealth.passport.util.Dates

class MedicalAdapter(
    private val onClick: (MedicalRecord) -> Unit
) : ListAdapter<MedicalRecord, MedicalAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemMedicalBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemMedicalBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val record = getItem(position)
        with(holder.binding) {
            txtDiagnosis.text = record.diagnosis.ifBlank { "—" }
            txtMeta.text = buildString {
                append(Dates.format(record.date))
                if (record.clinic.isNotBlank()) append(" • ${record.clinic}")
            }
            txtPrescription.text = record.prescription.ifBlank { record.note }
            root.setOnClickListener { onClick(record) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MedicalRecord>() {
            override fun areItemsTheSame(a: MedicalRecord, b: MedicalRecord) = a.id == b.id
            override fun areContentsTheSame(a: MedicalRecord, b: MedicalRecord) = a == b
        }
    }
}
