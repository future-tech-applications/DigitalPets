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
 * @param context The context of the application.
 * @param petWindowManager The window manager for the pets.
 * @param callback The callback interface for the menu actions.
 */
class PetOptionsOverlayMenuManager(
        private val context: Context,
        private val petWindowManager: PetWindowManager,
        private val callback: Callback
) {

    /**
     * Callback interface for the menu actions.
     * This interface is implemented by the activity that uses this class.
     * It is used to communicate with the activity when a menu action is performed.
     */
    interface Callback {
        fun onDismissPet(petState: PetState)
        fun onSnooze()
        fun onOpenSettings()
        fun onStopApp()
    }

    /**
     * Shows the menu related to a pet.
     * @param petState The state of the pet.
     */
    fun showMenu(petState: PetState) {
        // on call of this method, we should set isMenuOpen to true of the selected pet
        // via PetSate object so we can track for which pet the menu is open.
        petState.isMenuOpen = true

        // Reset movement of the selected pet (dx and dy to 0)
        // because we are showing the menu and we don't want to move the pet.
        petState.dx = 0
        petState.dy = 0

        val inflater = LayoutInflater.from(context)
        val menuView = inflater.inflate(R.layout.overlay_menu, null)

        // Set the position of the menu relative to the pet.
        val params =
                petWindowManager.createMenuLayoutParams(
                        petState.params.x,
                        petState.params.y + petState.params.height
                )

        // Close the menu. This is done by removing the menu view from the window manager.
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

        // #### Below are the buttons of the menu ####

        // 1. Dismiss
        // Dismiss the pet by removing it from the window manager.
        menuView.findViewById<View>(R.id.btn_dismiss_pet).setOnClickListener {
            callback.onDismissPet(petState)
            try {
                petWindowManager.removeView(menuView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 2. Snooze
        // Snooze the pet by removing it from the window manager.
        // The snooze time is set to 30 minutes by default.
        menuView.findViewById<View>(R.id.btn_snooze).setOnClickListener {
            callback.onSnooze()
            try {
                petWindowManager.removeView(menuView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Settings
        // Open the settings by removing the menu view from the window manager.
        // This is done by calling the onOpenSettings method of the callback interface.
        menuView.findViewById<View>(R.id.btn_settings).setOnClickListener {
            callback.onOpenSettings()
            closeMenu()
        }

        // 4. Stop App
        // Stop the app by removing the menu view from the window manager.
        // This is done by calling the onStopApp method of the callback interface.
        menuView.findViewById<View>(R.id.btn_stop_app).setOnClickListener {
            try {
                petWindowManager.removeView(menuView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            callback.onStopApp()
        }

        // Outside Touch
        // If the user touches outside of the menu, the menu is closed.
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
