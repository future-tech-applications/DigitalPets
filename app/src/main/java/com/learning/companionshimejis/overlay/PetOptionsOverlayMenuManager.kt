package com.learning.companionshimejis.overlay

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import com.learning.companionshimejis.R
import com.learning.companionshimejis.persistence.PetState
import kotlin.random.Random

/**
 * Responsible for showing and dismissing the
 * menu related to a pet whenever it is pressed and hold from anywhere on the screen.
 * It helps to allow for actions such as dismissing the
 * pet or stopping the service.
 */
class PetOptionsOverlayMenuManager(
        private val context: Context,
        private val petWindowManager: PetWindowManager,
        private val callback: Callback
) {

    interface Callback {
        fun onDismissPet(petState: PetState)
        fun onSnooze()
        fun onOpenSettings()
        fun onStopApp()
    }

    fun showMenu(petState: PetState) {
        petState.isMenuOpen = true
        petState.dx = 0
        petState.dy = 0

        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.overlay_menu, null)

        val params =
                petWindowManager.createMenuLayoutParams(
                        petState.params.x,
                        petState.params.y + petState.params.height
                )

        val closeMenu = {
            try {
                petWindowManager.removeView(menuView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            petState.isMenuOpen = false
            petState.dx = if (Random.nextBoolean()) 5 else -5
            petState.dy = if (Random.nextBoolean()) 5 else -5
        }

        // Dismiss
        menuView.findViewById<View>(R.id.btn_dismiss_pet).setOnClickListener {
            callback.onDismissPet(petState)
            try {
                petWindowManager.removeView(menuView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Snooze
        menuView.findViewById<View>(R.id.btn_snooze).setOnClickListener {
            callback.onSnooze()
            try {
                petWindowManager.removeView(menuView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Settings
        menuView.findViewById<View>(R.id.btn_settings).setOnClickListener {
            callback.onOpenSettings()
            closeMenu()
        }

        // Stop App
        menuView.findViewById<View>(R.id.btn_stop_app).setOnClickListener {
            try {
                petWindowManager.removeView(menuView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            callback.onStopApp()
        }

        // Outside Touch
        menuView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE) {
                closeMenu()
                true
            } else {
                false
            }
        }

        try {
            petWindowManager.addView(menuView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
