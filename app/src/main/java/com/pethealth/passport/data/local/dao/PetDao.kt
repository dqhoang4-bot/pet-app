package com.pethealth.passport.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pethealth.passport.data.local.entity.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<Pet>>

    @Query("SELECT * FROM pets ORDER BY createdAt ASC")
    suspend fun getAll(): List<Pet>

    @Query("SELECT * FROM pets WHERE id = :id")
    fun observeById(id: Long): Flow<Pet?>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getById(id: Long): Pet?

    @Query("SELECT COUNT(*) FROM pets")
    suspend fun count(): Int

    @Insert
    suspend fun insert(pet: Pet): Long

    @Update
    suspend fun update(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Query("DELETE FROM pets")
    suspend fun deleteAll()
}
