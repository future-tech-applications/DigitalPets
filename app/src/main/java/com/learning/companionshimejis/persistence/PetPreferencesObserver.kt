package com.learning.companionshimejis.persistence

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * It basically observes and reacts to changes in the preferences of the user related to the pets.
 */
class PetPreferencesObserver(private val context: Context) {

    interface Callback {
        fun onScaleChanged(scale: Float)
        fun onOpacityChanged(opacity: Float)
        fun onSpeedChanged(speed: Float)
    }

    fun observeChanges(scope: CoroutineScope, callback: Callback) {
        // Scale Observer
        scope.launch {
            PreferencesManager.getPetScale(context).collect { scale ->
                callback.onScaleChanged(scale)
            }
        }

        // Opacity Observer
        scope.launch {
            PreferencesManager.getPetOpacity(context).collect { opacity ->
                callback.onOpacityChanged(opacity)
            }
        }

        // Speed Observer
        scope.launch {
            PreferencesManager.getAnimationSpeed(context).collect { speed ->
                callback.onSpeedChanged(speed)
            }
        }
    }
}
