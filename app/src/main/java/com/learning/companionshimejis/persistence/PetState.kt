package com.learning.companionshimejis.persistence

import android.view.WindowManager
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
    )