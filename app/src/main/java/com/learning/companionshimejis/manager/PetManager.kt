package com.learning.companionshimejis.manager

import android.content.Context
import android.view.Gravity
import android.view.View
import com.learning.companionshimejis.data.model.Pet
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.interaction.PetTouchHandler
import com.learning.companionshimejis.overlay.FloatingPetView
import com.learning.companionshimejis.overlay.PetOptionsOverlayMenuManager
import com.learning.companionshimejis.overlay.PetWindowManager
import com.learning.companionshimejis.persistence.PetState
import kotlin.random.Random

/** Helps to manage the pets to add, remove, and update them in the overlay. */
class PetManager(
        private val context: Context,
        private val petWindowManager: PetWindowManager,
        private val menuManager: PetOptionsOverlayMenuManager,
        private val onPetCountChanged: (Boolean) -> Unit,
        private val onPetRemoved: (PetState) -> Unit
) {

    val activePets = mutableListOf<PetState>()

    fun addPet(pet: Pet, position: Pair<Int, Int>?, currentScale: Float, currentOpacity: Float) {
        val petView = FloatingPetView(context)
        // Initialize with default frame (Row 0, Col 0)
        val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, pet.resId)
        val frameWidth = bitmap.width / 4
        val frameHeight = bitmap.height / 4
        petView.updateFrame(bitmap, 0, 0, frameWidth, frameHeight)

        petView.updateAlpha(currentOpacity)

        val density = context.resources.displayMetrics.density
        val baseSize = (64 * density).toInt()
        val size = (baseSize * currentScale).toInt()

        val params = petWindowManager.createPetLayoutParams(size)
        params.gravity = Gravity.TOP or Gravity.START

        val bounds = petWindowManager.getUsableBounds()

        val startX: Int
        val startY: Int

        if (position != null) {
            startX = position.first.coerceIn(0, (bounds.width() - size).coerceAtLeast(0))
            startY = position.second.coerceIn(0, (bounds.height() - size).coerceAtLeast(0))
        } else {
            startX = Random.nextInt(0, (bounds.width() - size).coerceAtLeast(1))
            startY = Random.nextInt(0, (bounds.height() - size).coerceAtLeast(1))
        }

        params.x = startX
        params.y = startY

        val petState =
                PetState(
                        id = pet.id,
                        view = petView,
                        params = params,
                        x = startX,
                        y = startY,
                        dx = 0,
                        dy = 0,
                        behavior = PetBehavior.NONE,
                        behaviorTimer = 0
                )

        petView.setOnTouchListener(
                PetTouchHandler(context, petState, petWindowManager) {
                    menuManager.showMenu(petState)
                }
        )

        try {
            petWindowManager.addView(petView, params)
            activePets.add(petState)
            onPetCountChanged(activePets.isNotEmpty())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removePet(petState: PetState) {
        try {
            petWindowManager.removeView(petState.view)
            activePets.remove(petState)
            onPetRemoved(petState) // Trigger persistence sync
            onPetCountChanged(activePets.isNotEmpty())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun removeAllPets() {
        activePets.toList().forEach { pet ->
            try {
                petWindowManager.removeView(pet.view)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        activePets.clear()
        // No persistence sync needed here usually, or handled differently
        onPetCountChanged(false)
    }

    fun setPetsVisible(visible: Boolean) {
        val visibility = if (visible) View.VISIBLE else View.GONE
        activePets.forEach { pet -> pet.view.visibility = visibility }
    }

    fun updatePetsScale(scale: Float) {
        val density = context.resources.displayMetrics.density
        val baseSize = (64 * density).toInt()
        val newSize = (baseSize * scale).toInt()

        activePets.forEach { pet ->
            pet.params.width = newSize
            pet.params.height = newSize
            try {
                petWindowManager.updateViewLayout(pet.view, pet.params)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePetsOpacity(opacity: Float) {
        activePets.forEach { pet -> pet.view.updateAlpha(opacity) }
    }
}
