package com.pethealth.passport.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.repository.CommunityRepository
import com.pethealth.passport.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CreatePostViewModel(
    private val communityRepository: CommunityRepository,
    petRepository: PetRepository
) : ViewModel() {

    val pets: StateFlow<List<Pet>> = petRepository.pets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    suspend fun createPost(
        content: String,
        speciesTag: String,
        petName: String,
        imageUri: String?
    ): Long = communityRepository.createPost(content, speciesTag, petName, imageUri)
}
