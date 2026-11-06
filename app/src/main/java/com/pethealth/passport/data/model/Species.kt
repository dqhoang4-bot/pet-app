package com.pethealth.passport.data.model

import androidx.annotation.StringRes
import com.pethealth.passport.R

data class SpeciesOption(
    val key: String,
    val emoji: String,
    val labelVi: String,
    val labelEn: String,
    @StringRes val labelRes: Int
)

object Species {
    val all = listOf(
        SpeciesOption("dog", "🐶", "Chó", "Dog", R.string.species_dog),
        SpeciesOption("cat", "🐱", "Mèo", "Cat", R.string.species_cat),
        SpeciesOption("bird", "🐦", "Chim", "Bird", R.string.species_bird),
        SpeciesOption("rabbit", "🐰", "Thỏ", "Rabbit", R.string.species_rabbit),
        SpeciesOption("other", "🐾", "Khác", "Other", R.string.species_other)
    )

    fun option(key: String): SpeciesOption = all.firstOrNull { it.key == key } ?: all.last()
    fun label(key: String, english: Boolean): String =
        if (english) option(key).labelEn else option(key).labelVi
    fun emoji(key: String): String = option(key).emoji
}
