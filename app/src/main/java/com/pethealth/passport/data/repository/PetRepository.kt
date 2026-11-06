package com.pethealth.passport.data.repository

import com.pethealth.passport.data.local.dao.PetDao
import com.pethealth.passport.data.local.entity.Pet
import kotlinx.coroutines.flow.Flow

class PetRepository(private val dao: PetDao) {
    val pets: Flow<List<Pet>> = dao.observeAll()

    fun observePet(id: Long): Flow<Pet?> = dao.observeById(id)

    suspend fun getPet(id: Long): Pet? = dao.getById(id)

    suspend fun getAll(): List<Pet> = dao.getAll()

    suspend fun save(pet: Pet): Long =
        if (pet.id == 0L) dao.insert(pet) else {
            dao.update(pet)
            pet.id
        }

    suspend fun delete(pet: Pet) = dao.delete(pet)

    suspend fun count(): Int = dao.count()
}
