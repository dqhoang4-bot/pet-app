package com.pethealth.passport.ui.content

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.R
import com.pethealth.passport.data.repository.Clinic
import com.pethealth.passport.databinding.ItemClinicBinding

class ClinicAdapter(
    private val english: Boolean,
    private val onCall: (Clinic) -> Unit,
    private val onMaps: (Clinic) -> Unit
) : ListAdapter<Clinic, ClinicAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemClinicBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemClinicBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val clinic = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            txtName.text = clinic.name
            txtAddress.text = clinic.address
            txtHours.text = context.getString(
                R.string.clinics_hours_value,
                clinic.hours(english)
            )
            btnCall.setOnClickListener { onCall(clinic) }
            btnMaps.setOnClickListener { onMaps(clinic) }
            root.setOnClickListener { onMaps(clinic) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Clinic>() {
            override fun areItemsTheSame(oldItem: Clinic, newItem: Clinic) =
                oldItem.name == newItem.name && oldItem.address == newItem.address

            override fun areContentsTheSame(oldItem: Clinic, newItem: Clinic) =
                oldItem == newItem
        }
    }
}
