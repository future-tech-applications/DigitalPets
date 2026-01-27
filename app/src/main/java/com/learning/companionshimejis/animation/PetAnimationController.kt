package com.learning.companionshimejis.animation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.learning.companionshimejis.data.model.PetAnimation
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.persistence.PetState

/**
 * Controls the animation state of all active pets. Maps behavior states to sprite frames and
 * updates the view.
 */
class PetAnimationController(private val context: Context) {

    // Cache for loaded bitmaps to avoid decoding every time
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    // Registry of animations for each pet type (ResID -> Map<Behavior, Animation>)
    private val animationRegistry = mutableMapOf<Int, Map<PetBehavior, PetAnimation>>()

    /** Updates the animation state for all active pets. Should be called in the main game loop. */
    fun updateAnimations(activePets: List<PetState>) {
        activePets.forEach { pet -> updatePetAnimation(pet) }
    }

    private fun updatePetAnimation(pet: PetState) {
        // 1. Detect State Change
        if (pet.behavior != pet.lastBehavior) {
            pet.lastBehavior = pet.behavior
            pet.animationTimer = 0
            pet.currentFrameIndex = 0
        }

        // 2. Load Sprite Sheet
        // Assuming pet.id maps to a resource, or we use a lookup.
        // For now, let's look up the resource ID from the Repository directly if possible,
        // OR assume PetState has it.
        // Since PetState doesn't have resId, we can use PetRepository to get it from pet.id
        val petData = com.learning.companionshimejis.data.PetRepository.getPetById(pet.id) ?: return
        val spriteSheet = loadSpriteSheet(petData.resId)

        // 3. Determine Row and Frame Count
        val (row, frameCount) = getAnimationData(pet.behavior)

        // 4. Update Timer and Frame Index
        pet.animationTimer++
        // Speed control: update frame every X ticks.
        // 60 FPS loop. 10 ticks = 6 updates/sec.
        val ticksPerFrame = 10

        if (pet.animationTimer >= ticksPerFrame) {
            pet.animationTimer = 0
            pet.currentFrameIndex = (pet.currentFrameIndex + 1) % frameCount
        }

        // 5. Calculate Coordinates Dynamically
        val frameWidth = spriteSheet.width / 4
        val frameHeight = spriteSheet.height / 4
        val frameX = pet.currentFrameIndex * frameWidth
        val frameY = row * frameHeight

        // 6. Update View
        pet.view.updateFrame(spriteSheet, frameX, frameY, frameWidth, frameHeight)
    }

    private fun getAnimationData(behavior: PetBehavior): Pair<Int, Int> {
        // Returns Pair(RowIndex, FrameCount)
        return when (behavior) {
            PetBehavior.WALK_LEFT -> Pair(0, 4)
            PetBehavior.WALK_RIGHT -> Pair(1, 4)
            PetBehavior.CLIMB_EDGE -> Pair(2, 4)
            // Map others to Row 3 or reuse
            PetBehavior.IDLE -> Pair(3, 4)
            PetBehavior.FALL -> Pair(3, 4) // Reuse IDLE/Row 3 for now
            PetBehavior.FLY -> Pair(3, 4)
            PetBehavior.JUMP -> Pair(2, 4) // Reuse Climb
            else -> Pair(3, 4)
        }
    }

    // Helper to load bitmap
    fun loadSpriteSheet(resId: Int): Bitmap {
        return bitmapCache.getOrPut(resId) {
            BitmapFactory.decodeResource(context.resources, resId)
        }
    }
}
