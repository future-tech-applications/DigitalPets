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
        var behaviorTimer: Long = 0
)
