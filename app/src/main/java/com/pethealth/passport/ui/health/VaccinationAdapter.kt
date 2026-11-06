package com.pethealth.passport.ui.health

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.databinding.ItemVaccinationBinding
import com.pethealth.passport.ui.common.bindStatusPill
import com.pethealth.passport.ui.common.dueText
import com.pethealth.passport.util.Dates

class VaccinationAdapter(
    private val onClick: (Vaccination) -> Unit
) : ListAdapter<Vaccination, VaccinationAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemVaccinationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemVaccinationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val vaccination = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            txtEmoji.text = if (vaccination.kind == Vaccination.KIND_DEWORMER) "🪱" else "💉"
            txtName.text = vaccination.name
            txtSubtitle.text = buildString {
                append(Dates.format(vaccination.date))
                if (vaccination.clinic.isNotBlank()) append(" • ${vaccination.clinic}")
            }
            txtDue.text = dueText(context, vaccination.nextDueDate)
            bindStatusPill(txtStatus, vaccination)
            root.setOnClickListener { onClick(vaccination) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Vaccination>() {
            override fun areItemsTheSame(a: Vaccination, b: Vaccination) = a.id == b.id
            override fun areContentsTheSame(a: Vaccination, b: Vaccination) = a == b
        }
    }
}
