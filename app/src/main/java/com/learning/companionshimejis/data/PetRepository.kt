package com.learning.companionshimejis.data

import com.learning.companionshimejis.R
import com.learning.companionshimejis.data.model.Pet

object PetRepository {
    val predefinedPets =
            listOf(
                    Pet("cat_01", "Purple Cat", R.drawable.pet_sprite_purple_cat),
                    Pet("dog_01", "Blue Dog", R.drawable.pet_sprite_blue_dog)
            )

    fun getPetById(id: String): Pet? {
        return predefinedPets.find { it.id == id }
    }
}
