package com.pethealth.passport.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Vaccination
import com.pethealth.passport.data.local.model.UpcomingVaccination
import com.pethealth.passport.databinding.ItemUpcomingBinding
import com.pethealth.passport.ui.common.bindStatusPill
import com.pethealth.passport.ui.common.dueText

class UpcomingAdapter(
    private val onClick: (UpcomingVaccination) -> Unit
) : ListAdapter<UpcomingVaccination, UpcomingAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemUpcomingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemUpcomingBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        val context = holder.binding.root.context
        val vaccination = item.vaccination
        with(holder.binding) {
            txtEmoji.text = if (vaccination.kind == Vaccination.KIND_DEWORMER) "🪱" else "💉"
            txtTitle.text = "${item.petName} • ${vaccination.name}"
            txtSubtitle.text = context.getString(
                if (vaccination.kind == Vaccination.KIND_DEWORMER) R.string.kind_dewormer
                else R.string.kind_vaccine
            )
            txtDue.text = dueText(context, vaccination.nextDueDate)
            bindStatusPill(txtStatus, vaccination)
            root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<UpcomingVaccination>() {
            override fun areItemsTheSame(a: UpcomingVaccination, b: UpcomingVaccination) =
                a.vaccination.id == b.vaccination.id

            override fun areContentsTheSame(a: UpcomingVaccination, b: UpcomingVaccination) =
                a == b
        }
    }
}
