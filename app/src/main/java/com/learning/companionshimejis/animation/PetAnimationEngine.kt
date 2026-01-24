package com.learning.companionshimejis.animation

import android.os.Handler
import android.os.Looper

/** Manages the animation loop for the pets. Uses a Handler to post runnables at ~60 FPS. */
class PetAnimationEngine(val onUpdate: () -> Unit) {
    private val animationHandler = Handler(Looper.getMainLooper())
    private var isRunning = false

    private val animationRunnable =
            object : Runnable {
                override fun run() {
                    if (!isRunning) return

                    onUpdate()

                    animationHandler.postDelayed(this, 16) // roughly 60fps
                }
            }

    fun start() {
        if (!isRunning) {
            isRunning = true
            animationHandler.post(animationRunnable)
        }
    }

    fun stop() {
        isRunning = false
        animationHandler.removeCallbacks(animationRunnable)
    }

    fun isRunning() = isRunning
}
