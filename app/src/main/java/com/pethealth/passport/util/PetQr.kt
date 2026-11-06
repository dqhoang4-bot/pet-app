package com.pethealth.passport.util

import com.pethealth.passport.data.local.entity.Pet
import com.pethealth.passport.data.local.entity.Vaccination
import org.json.JSONObject

data class QrPetProfile(
    val id: Long,
    val name: String,
    val species: String,
    val breed: String,
    val birthDate: Long,
    val weightKg: Double,
    val ownerName: String,
    val ownerPhone: String,
    val microchip: String,
    val latestVaccine: String,
    val nextDue: Long?
)

object PetQr {
    const val TYPE = "pet_health_passport"

    fun encode(pet: Pet, latestVaccination: Vaccination?): String {
        val json = JSONObject()
        json.put("type", TYPE)
        json.put("id", pet.id)
        json.put("name", pet.name)
        json.put("species", pet.species)
        json.put("breed", pet.breed)
        json.put("birthDate", pet.birthDate)
        json.put("weightKg", pet.weightKg)
        json.put("ownerName", pet.ownerName)
        json.put("ownerPhone", pet.ownerPhone)
        json.put("microchip", pet.microchip)
        json.put("latestVaccine", latestVaccination?.name ?: "")
        if (latestVaccination?.nextDueDate != null) {
            json.put("nextDue", latestVaccination.nextDueDate)
        }
        return json.toString()
    }

    fun decode(raw: String): QrPetProfile? = runCatching {
        val json = JSONObject(raw)
        if (json.optString("type") != TYPE) return null
        QrPetProfile(
            id = json.optLong("id"),
            name = json.optString("name"),
            species = json.optString("species", "other"),
            breed = json.optString("breed"),
            birthDate = json.optLong("birthDate"),
            weightKg = json.optDouble("weightKg", 0.0),
            ownerName = json.optString("ownerName"),
            ownerPhone = json.optString("ownerPhone"),
            microchip = json.optString("microchip"),
            latestVaccine = json.optString("latestVaccine"),
            nextDue = if (json.has("nextDue")) json.optLong("nextDue") else null
        )
    }.getOrNull()
}
