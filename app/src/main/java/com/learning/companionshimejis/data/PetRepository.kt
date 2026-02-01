package com.learning.companionshimejis.data

import com.learning.companionshimejis.R
import com.learning.companionshimejis.data.model.Pet

object PetRepository {
    val predefinedPets =
            listOf(
                    Pet(
                            "cat_01",
                            "Purple Cat",
                            R.drawable.pet_sprite_purple_cat,
                            cols = 4,
                            rows = 4,
                            behaviorMapId = "LEGACY_4ROW"
                    ),
                    Pet(
                            "dog_01",
                            "Blue Dog",
                            R.drawable.pet_sprite_blue_dog,
                            cols = 4,
                            rows = 4,
                            behaviorMapId = "LEGACY_4ROW"
                    ),
                    Pet(
                            "man_01",
                            "Little Man",
                            R.drawable.sprite_sheet_little_man,
                            cols = 4,
                            rows = 7,
                            behaviorMapId = "LITTLE_MAN_7ROW"
                    )
            )

    fun getPetById(id: String): Pet? {
        return predefinedPets.find { it.id == id }
    }
}
