package com.learning.companionshimejis.animation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.learning.companionshimejis.data.model.EmoteType
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.persistence.PetState

/**
 * Controls the animation state of all active pets. Maps behavior states to sprite frames and
 * updates the view.
 */
class PetAnimationController(private val context: Context) {

    // Cache for loaded bitmaps to avoid decoding every time
    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    // Set to track which bitmaps are currently being processed
    private val loadingBitmaps = mutableSetOf<Int>()

    /**
     * Whether the programmatic transparency filter (checkerboard removal) is enabled. Defaults to
     * false as it is computationally expensive (~8-10s per asset).
     */
    var applyRemoveCheckerboardAlgo: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                synchronized(bitmapCache) {
                    bitmapCache.clear() // Invalidate cache to apply/remove filter
                }
            }
        }

    /** Updates the animation state for all active pets. Should be called in the main game loop. */
    fun updateAnimations(activePets: List<PetState>, speed: Float) {
        activePets.forEach { pet -> updatePetAnimation(pet, speed) }
    }

    private fun updatePetAnimation(pet: PetState, speed: Float) {
        // 1. Detect State Change
        if (pet.behavior != pet.lastBehavior) {
            pet.lastBehavior = pet.behavior
            pet.animationTimer = 0
            pet.currentFrameIndex = 0
        }

        // 2. Load Sprite Sheet
        val petData = com.learning.companionshimejis.data.PetRepository.getPetById(pet.id) ?: return
        val spriteSheet = loadSpriteSheet(petData.resId) ?: return // Wait for background processing

        // 3. Determine Row and Frame Count
        val (row, frameCount) = getAnimationData(pet.behavior)

        // 4. Update Timer and Frame Index
        // 16ms is the fixed tick rate from MainService/Engine
        pet.animationTimer += (16 * speed).toLong()

        // Speed control: update frame every ~150ms
        val frameDurationMs = 150

        if (pet.animationTimer >= frameDurationMs) {
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

        // 7. Handle Emotes
        updatePetEmote(pet, speed)
    }

    private fun updatePetEmote(pet: PetState, speed: Float) {
        if (pet.currentEmote != EmoteType.NONE) {
            pet.emoteTimer += (16 * speed).toLong()
            if (pet.emoteTimer > 3000) { // Show emotes for 3 seconds
                pet.currentEmote = EmoteType.NONE
                pet.emoteTimer = 0
            }
        }
        pet.view.setEmote(pet.currentEmote)
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
    fun loadSpriteSheet(resId: Int): Bitmap? {
        synchronized(bitmapCache) {
            val cached = bitmapCache[resId]
            if (cached != null) return cached
        }

        if (!applyRemoveCheckerboardAlgo) {
            // Instant load if filter is disabled
            val bitmap = BitmapFactory.decodeResource(context.resources, resId)
            synchronized(bitmapCache) { bitmapCache[resId] = bitmap }
            return bitmap
        }

        if (!loadingBitmaps.contains(resId)) {
            loadingBitmaps.add(resId)
            // Start background processing
            Thread {
                        try {
                            val original = BitmapFactory.decodeResource(context.resources, resId)
                            val filtered = TransparencyHelper.removeCheckerboard(original)
                            synchronized(bitmapCache) { bitmapCache[resId] = filtered }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            loadingBitmaps.remove(resId)
                        }
                    }
                    .start()
        }
        return null // Return null while loading to avoid blocking UI thread
    }
}
