package com.learning.companionshimejis.persistence

import android.view.WindowManager
import com.learning.companionshimejis.data.model.EmoteType
import com.learning.companionshimejis.data.model.PetAnimation
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.overlay.FloatingPetView

/**
 * Represents the state of a pet in the overlay.
 * @param id The unique identifier of the pet.
 * @param view The view of the pet.
 * @param params The layout parameters of the pet.
 * @param x The x-coordinate of the pet.
 * @param y The y-coordinate of the pet.
 * @param dx The x-velocity of the pet.
 * @param dy The y-velocity of the pet.
 * @param isMenuOpen Whether the pet's menu is open.
 * @param isDragging Whether the pet is being dragged.
 * @param behaviorChangedThisTick Whether the pet's behavior has changed this tick.
 * @param behavior The current behavior of the pet. Defaults to [PetBehavior.NONE].
 * @param behaviorTimer The timer for the current behavior which indicates how long the pet has been
 * in the current behavior. Defaults to 0.
 * @param animationTimer The timer for the current animation which indicates how long the pet has
 * been in the current animation. Defaults to 0.
 * @param currentFrameIndex The index of the current frame in the animation. Defaults to 0.
 * @param lastBehavior The last behavior the pet was in. Defaults to [PetBehavior.NONE].
 * @param currentAnimation The current animation of the pet. Defaults to null.
 */
data class PetState(
        val id: String,
        val view: FloatingPetView,
        val params: WindowManager.LayoutParams,
        var x: Int,
        var y: Int,
        var dx: Int,
        var dy: Int,
        var isMenuOpen: Boolean = false,
        var isDragging: Boolean = false,

        // Behavior State
        var behaviorChangedThisTick: Boolean = false,
        var behavior: PetBehavior = PetBehavior.NONE,
        var behaviorTimer: Long = 0,

        // Animation State
        var animationTimer: Long = 0,
        var currentFrameIndex: Int = 0,
        var lastBehavior: PetBehavior = PetBehavior.NONE,
        var currentAnimation: PetAnimation? = null,

        // Emote State
        var currentEmote: EmoteType = EmoteType.NONE,
        var emoteTimer: Long = 0
)
