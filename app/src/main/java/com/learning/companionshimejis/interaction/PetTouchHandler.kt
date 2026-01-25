package com.learning.companionshimejis.interaction

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.overlay.PetWindowManager
import com.learning.companionshimejis.persistence.PetState
import kotlin.math.abs

class PetTouchHandler(
        context: Context,
        private val petState: PetState,
        private val petWindowManager: PetWindowManager,
        private val onShowMenu: () -> Unit
) : View.OnTouchListener {

    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private val gestureDetector =
            GestureDetector(
                    context,
                    object : GestureDetector.SimpleOnGestureListener() {
                        override fun onLongPress(e: MotionEvent) {
                            onShowMenu()
                        }

                        override fun onSingleTapUp(e: MotionEvent): Boolean {
                            return false
                        }
                    }
            )

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(event)) {
            return true
        }

        // If menu is open, swallow touch events
        if (petState.isMenuOpen) return true

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.rawX
                lastTouchY = event.rawY
                petState.isDragging = true
                petState.dx = 0
                petState.dy = 0
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = (event.rawX - lastTouchX).toInt()
                val deltaY = (event.rawY - lastTouchY).toInt()

                if (abs(deltaX) > 5 || abs(deltaY) > 5) {
                    val bounds = petWindowManager.getUsableBounds()

                    // Update and clamp positions
                    petState.x =
                            (petState.x + deltaX).coerceIn(
                                    bounds.left,
                                    bounds.right - petState.params.width
                            )
                    petState.y =
                            (petState.y + deltaY).coerceIn(
                                    bounds.top,
                                    bounds.bottom - petState.params.height
                            )

                    petState.params.x = petState.x
                    petState.params.y = petState.y

                    try {
                        petWindowManager.updateViewLayout(v, petState.params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    lastTouchX = event.rawX
                    lastTouchY = event.rawY
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                petState.isDragging = false
                if (!petState.isMenuOpen) {
                    val bounds = petWindowManager.getUsableBounds()
                    val snapDistance = 10 // Tolerance in pixels

                    // 1. Check Walls (Left/Right)
                    if (petState.x <= bounds.left + snapDistance) {
                        petState.x = bounds.left
                        petState.behavior = PetBehavior.CLIMB_EDGE
                    } else if (petState.x + petState.params.width >= bounds.right - snapDistance) {
                        petState.x = bounds.right - petState.params.width
                        petState.behavior = PetBehavior.CLIMB_EDGE
                    }
                    // 2. Check Ground (Bottom)
                    else if (petState.y + petState.params.height >= bounds.bottom - snapDistance) {
                        petState.y = bounds.bottom - petState.params.height
                        // Randomize direction to make it look active
                        petState.behavior =
                                if (kotlin.random.Random.nextBoolean()) PetBehavior.WALK_LEFT
                                else PetBehavior.WALK_RIGHT
                    }
                    // 3. Falling (Mid-air)
                    else {
                        petState.behavior = PetBehavior.FALL
                    }

                    petState.behaviorTimer = 0

                    // Apply snap updates immediately
                    try {
                        petState.params.x = petState.x
                        petState.params.y = petState.y
                        petWindowManager.updateViewLayout(v, petState.params)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                return true
            }
        }
        return false
    }
}
