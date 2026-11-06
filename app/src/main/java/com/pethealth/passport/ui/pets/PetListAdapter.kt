package com.pethealth.passport.ui.pets

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.databinding.ItemPetRowBinding
import com.pethealth.passport.ui.common.ageLabel
import com.pethealth.passport.ui.common.bindPetAvatar
import com.pethealth.passport.ui.common.speciesLabel

class PetListAdapter(
    private val onClick: (Pet) -> Unit
) : ListAdapter<Pet, PetListAdapter.VH>(DIFF) {

    inner class VH(val binding: ItemPetRowBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemPetRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val pet = getItem(position)
        val context = holder.binding.root.context
        with(holder.binding) {
            txtName.text = pet.name
            txtBreed.text = listOf(pet.speciesLabel(context), pet.breed)
                .filter { it.isNotBlank() }
                .joinToString(" • ")
            txtAge.text = pet.ageLabel(context)
            bindPetAvatar(imgPhoto, txtEmoji, pet)
            root.setOnClickListener { onClick(pet) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Pet>() {
            override fun areItemsTheSame(a: Pet, b: Pet) = a.id == b.id
            override fun areContentsTheSame(a: Pet, b: Pet) = a == b
        }
    }
}
