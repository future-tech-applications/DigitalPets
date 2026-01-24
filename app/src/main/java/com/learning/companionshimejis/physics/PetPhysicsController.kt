package com.learning.companionshimejis.physics

import com.learning.companionshimejis.overlay.PetWindowManager
import com.learning.companionshimejis.persistence.PetState

/**
 * Handles the physics of the pets in the overlay manager and updates their positions.
 */
class PetPhysicsController(private val petWindowManager: PetWindowManager) {

    fun updatePhysics(activePets: List<PetState>, animationSpeedMultiplier: Float) {
        val bounds = petWindowManager.getUsableBounds()
        val screenWidth = bounds.width()
        val screenHeight = bounds.height()

        activePets.forEach { pet ->
            val moveX = (pet.dx * animationSpeedMultiplier).toInt()
            val moveY = (pet.dy * animationSpeedMultiplier).toInt()

            val finalDx =
                    if (moveX == 0 && pet.dx != 0 && animationSpeedMultiplier > 0) {
                        if (pet.dx > 0) 1 else -1
                    } else moveX
            val finalDy =
                    if (moveY == 0 && pet.dy != 0 && animationSpeedMultiplier > 0) {
                        if (pet.dy > 0) 1 else -1
                    } else moveY

            pet.x += finalDx
            pet.y += finalDy

            val petWidth = pet.params.width
            val petHeight = pet.params.height

            // Bounce logic
            if (pet.x <= 0) {
                pet.x = 0
                pet.dx *= -1
            } else if (pet.x + petWidth >= screenWidth) {
                pet.x = screenWidth - petWidth
                pet.dx *= -1
            }

            if (pet.y <= 0) {
                pet.y = 0
                pet.dy *= -1
            } else if (pet.y + petHeight >= screenHeight) {
                pet.y = screenHeight - petHeight
                pet.dy *= -1
            }

            pet.params.x = pet.x
            pet.params.y = pet.y

            try {
                petWindowManager.updateViewLayout(pet.view, pet.params)
            } catch (e: Exception) {
                // View might be removed
            }
        }
    }
}
