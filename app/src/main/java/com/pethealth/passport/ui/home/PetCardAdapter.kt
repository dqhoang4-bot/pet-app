package com.pethealth.passport.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.R
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.databinding.ItemPetCardBinding
import com.pethealth.passport.ui.common.ageLabel
import com.pethealth.passport.ui.common.bindPetAvatar
import com.pethealth.passport.ui.common.speciesLabel

class PetCardAdapter(
    private val onClick: (Pet) -> Unit
) : ListAdapter<Pet, PetCardAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemPetCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemPetCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pet = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            txtName.text = pet.name
            txtBreed.text = listOf(pet.speciesLabel(context), pet.breed)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
            txtAge.text = pet.ageLabel(context)
            txtWeight.text = context.getString(R.string.common_kg, pet.weightKg)
            bindPetAvatar(imgPhoto, txtEmoji, pet)
            root.setOnClickListener { onClick(pet) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Pet>() {
            override fun areItemsTheSame(oldItem: Pet, newItem: Pet) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Pet, newItem: Pet) = oldItem == newItem
        }
    }
}
