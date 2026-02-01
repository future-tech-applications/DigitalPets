package com.learning.companionshimejis.animation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.learning.companionshimejis.data.model.AnimationSpec
import com.learning.companionshimejis.data.model.EmoteType
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.persistence.PetState

/**
 * Controls the animation state of all active pets. Maps behavior states to sprite frames and
 * updates the view.
 */
class PetAnimationController(private val context: Context) {

    // ─────────────────────── ANIMATION METADATA ───────────────────────
    // Single source of truth for behavior → animation mapping.
    // Spec: 64x64 frames, 8 columns. We currently use 6 frames for animation, but the grid allows
    // 8.
    private val animationMap =
            mapOf(
                    PetBehavior.WALK_LEFT to
                            AnimationSpec(row = 0, frameCount = 6, frameDurationMs = 100),
                    PetBehavior.WALK_RIGHT to
                            AnimationSpec(row = 1, frameCount = 6, frameDurationMs = 100),
                    PetBehavior.CLIMB_EDGE to
                            AnimationSpec(row = 2, frameCount = 6, frameDurationMs = 120),
                    PetBehavior.JUMP to
                            AnimationSpec(row = 3, frameCount = 6, frameDurationMs = 80),
                    PetBehavior.IDLE to
                            AnimationSpec(row = 4, frameCount = 6, frameDurationMs = 250),
                    PetBehavior.FALL to
                            AnimationSpec(row = 5, frameCount = 6, frameDurationMs = 120),
                    PetBehavior.FLY to
                            AnimationSpec(row = 6, frameCount = 6, frameDurationMs = 150),
                    // Row 7 is available for future use (e.g. SLEEP)
                    // Fallback for any unmapped behaviors
                    PetBehavior.NONE to
                            AnimationSpec(row = 4, frameCount = 6, frameDurationMs = 250)
            )

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
        val petData = com.learning.companionshimejis.data.PetRepository.getPetById(pet.id) ?: return
        val spriteSheet = loadSpriteSheet(petData.resId) ?: return

        // 1. Get Animation Spec (Dynamically based on Pet Config)
        // We use the Pet's specific configuration to determine the animation metadata
        val baseAnim =
                animationMap[pet.behavior]
                        ?: animationMap[PetBehavior.IDLE]!! // Timing comes from default map for now

        // 2. Resolve final Row/Columns based on Layout Mapping
        val (finalRow, frameCount) = resolveAnimationData(petData, pet.behavior, baseAnim)

        // 3. Detect State Change → Reset Animation
        if (pet.behavior != pet.lastBehavior) {
            pet.lastBehavior = pet.behavior
            pet.animationTimer = 0
            pet.currentFrameIndex = 0
        }

        // 4. Advance Animation Timer
        pet.animationTimer += (16 * speed).toLong()
        if (pet.animationTimer >= baseAnim.frameDurationMs) {
            pet.animationTimer = 0
            pet.currentFrameIndex = (pet.currentFrameIndex + 1) % frameCount
        }

        // 5. Calculate Coordinates (Per-Pet Layout)
        // Use the explicit row/col count from the Pet config
        val frameWidth = spriteSheet.width / petData.cols
        val frameHeight = spriteSheet.height / petData.rows

        val frameX = pet.currentFrameIndex * frameWidth
        val frameY = finalRow * frameHeight

        // 6. Update View
        pet.view.updateFrame(spriteSheet, frameX, frameY, frameWidth, frameHeight)

        // 7. Handle Emotes
        updatePetEmote(pet, speed)
    }

    /**
     * Resolves the correct Row Index and Frame Count for a specific pet's layout.
     * @return Pair(RowIndex, FrameCount)
     */
    private fun resolveAnimationData(
            pet: com.learning.companionshimejis.data.model.Pet,
            behavior: PetBehavior,
            spec: AnimationSpec
    ): Pair<Int, Int> {
        val row =
                when (pet.behaviorMapId) {
                    "LEGACY_4ROW" -> mapToLegacy4Row(behavior)
                    "LITTLE_MAN_7ROW" -> mapToLittleMan7Row(behavior)
                    else -> spec.row // Fallback to STANDARD_8ROW (1:1 mapping) or undefined
                }

        // Use the configured columns as the max frame count for now,
        // or default to 6 if we want to stick to the active animation length.
        // For Little Man (4 cols), max frames is 4.
        // For Legacy (4 cols), max frames is 4.
        // For Standard (8 cols), max frames is 6 (from Spec).

        val frames = if (pet.cols < spec.frameCount) pet.cols else spec.frameCount
        return Pair(row, frames)
    }

    // Mapping Logic for Legacy 4-Row Sheets (Walk L/R, Climb, Idle/All-Else)
    private fun mapToLegacy4Row(behavior: PetBehavior): Int {
        return when (behavior) {
            PetBehavior.WALK_LEFT -> 0
            PetBehavior.WALK_RIGHT -> 1
            PetBehavior.CLIMB_EDGE -> 2
            PetBehavior.JUMP -> 3
            PetBehavior.IDLE -> 3
            PetBehavior.FALL -> 3
            PetBehavior.FLY -> 2 // reusing climb? or 3?
            else -> 3
        }
    }

    // Mapping Logic for Little Man (7 Rows) - Assuming specific layout
    // Row 0: Walk Left, 1: Walk Right, 2: Climb, 3: Jump, 4: Idle, 5: Fall, 6: Fly
    // (This matches our 'AnimationSpec' defaults exactly, so we can just return the spec row)
    private fun mapToLittleMan7Row(behavior: PetBehavior): Int {
        // If Little Man follows the standard "Behavior -> Row (0-6)" pattern:
        return when (behavior) {
            PetBehavior.WALK_LEFT -> 0
            PetBehavior.WALK_RIGHT -> 1
            PetBehavior.CLIMB_EDGE -> 2
            PetBehavior.JUMP -> 3
            PetBehavior.IDLE -> 4
            PetBehavior.FALL -> 5
            PetBehavior.FLY -> 6
            else -> 4
        }
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
