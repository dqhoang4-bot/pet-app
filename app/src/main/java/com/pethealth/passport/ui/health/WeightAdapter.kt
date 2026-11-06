package com.pethealth.passport.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.WeightEntry
import com.pethealth.passport.databinding.ItemWeightBinding
import com.pethealth.passport.util.Dates

class WeightAdapter(
    private val onDelete: (WeightEntry) -> Unit
) : ListAdapter<WeightEntry, WeightAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemWeightBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemWeightBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            txtWeight.text = context.getString(R.string.common_kg, entry.weightKg)
            txtMeta.text = listOf(Dates.format(entry.date), entry.note)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
            btnDelete.setOnClickListener { onDelete(entry) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WeightEntry>() {
            override fun areItemsTheSame(a: WeightEntry, b: WeightEntry) = a.id == b.id
            override fun areContentsTheSame(a: WeightEntry, b: WeightEntry) = a == b
        }
    }
}
