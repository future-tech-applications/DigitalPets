package com.learning.companionshimejis.animation

import android.os.Handler
import android.os.Looper

/** Manages the animation loop for the pets. Uses a Handler to post runnables at ~60 FPS.
 * @param onUpdate Callback to be called on each animation frame. Should update the pet state.
 */
class PetAnimationEngine(val onUpdate: () -> Unit) {
    // Handler to post runnables at 60 FPS. Handlers helps with scheduling tasks.
    // Here we use it to schedule the animation loop. Handler runs on the UI thread.
    private val animationHandler = Handler(Looper.getMainLooper())

    // Flag to track if the animation is running. Used to prevent multiple runs.
    private var isRunning = false

    // Runnable to be posted to the animationHandler. Runnables are executed on the UI thread.
    // Generally Runnables are used to schedule tasks that need to be executed on the UI thread.
    private val animationRunnable =
            object : Runnable {
                // Called on each animation frame.
                override fun run() {
                    if (!isRunning) return

                    // Update the pet state. This is where the magic happens.
                    onUpdate()

                    // Schedule the next frame. This is the heart of the animation loop.
                    animationHandler.postDelayed(this, 16) // roughly 60fps
                }
            }

    // Starts the animation loop. It sets isRunning to true and posts the animationRunnable.
    fun start() {
        if (!isRunning) {
            isRunning = true
            animationHandler.post(animationRunnable)
        }
    }

    // Stops the animation loop. This should be called when the service is stopped.
    // It sets isRunning to false and removes the animationRunnable from the animationHandler.
    fun stop() {
        isRunning = false
        animationHandler.removeCallbacks(animationRunnable)
    }

    // Provides a way to check if the animation is running.
    fun isRunning() = isRunning
}
