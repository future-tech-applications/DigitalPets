package com.learning.companionshimejis.data

import com.learning.companionshimejis.R
import com.learning.companionshimejis.data.model.Pet

object PetRepository {
    val predefinedPets = listOf(
        Pet("cat_01", "Purple Cat", R.drawable.ic_pet_cat),
        Pet("dog_01", "Blue Dog", R.drawable.ic_pet_dog)
    )

    fun getPetById(id: String): Pet? {
        return predefinedPets.find { it.id == id }
    }
}
