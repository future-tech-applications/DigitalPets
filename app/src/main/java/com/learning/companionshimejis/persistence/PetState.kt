package com.learning.companionshimejis.persistence

import android.view.WindowManager
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.overlay.FloatingPetView

/** ## Character Part (State Model) ## */
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
        var behavior: PetBehavior = PetBehavior.NONE,
        var behaviorTimer: Long = 0,

        // Animation State
        var animationTimer: Long = 0,
        var currentFrameIndex: Int = 0,
        var lastBehavior: PetBehavior = PetBehavior.NONE,
        var currentAnimation: com.learning.companionshimejis.data.model.PetAnimation? = null
// We don't store the full PetAnimation object here to keep State serializable/lightweight if needed
// later.
// We will look it up in the Controller. But for runtime state, tracking the *ID* or just the frame
// data is enough.
// For now, let's keep it simple and just track the progress. The Controller will know *which*
// animation to play based on behavior.
// Actually, sometimes behavior != animation (e.g. idle variations).
// Let's add a visualState identifier if needed, but 'behavior' is usually 1:1.
)
