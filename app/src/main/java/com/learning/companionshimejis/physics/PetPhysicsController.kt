package com.learning.companionshimejis.physics

import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.overlay.PetWindowManager
import com.learning.companionshimejis.persistence.PetState
import kotlin.random.Random

/**
 * Handles the physics and behavior of the pets in the overlay manager and updates their positions.
 */
class PetPhysicsController(private val petWindowManager: PetWindowManager) {

    fun updatePhysics(activePets: List<PetState>, animationSpeedMultiplier: Float) {
        val bounds = petWindowManager.getUsableBounds()

        // Use absolute boundaries for screen awareness
        val minX = bounds.left
        val maxX = bounds.right
        val minY = bounds.top
        val maxY = bounds.bottom

        activePets.forEach { pet ->
            if (pet.isMenuOpen || pet.isDragging) return@forEach

            // Increment behavioral timer
            pet.behaviorTimer += 16 // Approx ms per tick

            // State Logic
            when (pet.behavior) {
                PetBehavior.FALL -> {
                    pet.dy = (10 * animationSpeedMultiplier).toInt()
                    pet.dx = 0
                    pet.y += pet.dy

                    // Ground collision (maxY)
                    if (pet.y + pet.params.height >= maxY) {
                        pet.y = maxY - pet.params.height
                        pet.behavior = PetBehavior.getRandomMovement()
                        pet.behaviorTimer = 0
                    }
                }
                PetBehavior.WALK_LEFT -> {
                    pet.dx = (-4 * animationSpeedMultiplier).toInt()
                    pet.dy = 0
                    pet.x += pet.dx

                    // Left wall collision
                    if (pet.x <= minX) {
                        pet.x = minX
                        // 30% chance to climb, otherwise turn back
                        if (Random.nextFloat() < 0.5f) {
                            pet.behavior = PetBehavior.CLIMB_EDGE
                        } else {
                            pet.behavior = PetBehavior.WALK_RIGHT
                        }
                        pet.behaviorTimer = 0
                    } else if (pet.behaviorTimer > 3000 && Random.nextFloat() < 0.02f) {
                        pet.behavior = PetBehavior.IDLE
                        pet.behaviorTimer = 0
                    }
                }
                PetBehavior.WALK_RIGHT -> {
                    pet.dx = (4 * animationSpeedMultiplier).toInt()
                    pet.dy = 0
                    pet.x += pet.dx

                    // Right wall collision
                    if (pet.x + pet.params.width >= maxX) {
                        pet.x = maxX - pet.params.width
                        // 30% chance to climb, otherwise turn back
                        if (Random.nextFloat() < 0.5f) {
                            pet.behavior = PetBehavior.CLIMB_EDGE
                        } else {
                            pet.behavior = PetBehavior.WALK_LEFT
                        }
                        pet.behaviorTimer = 0
                    } else if (pet.behaviorTimer > 3000 && Random.nextFloat() < 0.02f) {
                        pet.behavior = PetBehavior.IDLE
                        pet.behaviorTimer = 0
                    }
                }
                PetBehavior.CLIMB_EDGE -> {
                    pet.dx = 0

                    if (pet.dy == 0) {
                        pet.dy =
                                if (Random.nextFloat() < 0.25f) {
                                    (3 * animationSpeedMultiplier).toInt() // DOWN
                                } else {
                                    (-3 * animationSpeedMultiplier).toInt() // UP
                                }
                    }

                    pet.y += pet.dy

                    // Boundary checks
                    if (pet.y <= minY) {
                        // Reached TOP
                        pet.y = minY
                        pet.behavior = PetBehavior.getRandomMovement()
                        pet.behaviorTimer = 0
                    } else if (pet.y + pet.params.height >= maxY) {
                        // Reached BOTTOM (climbed down)
                        pet.y = maxY - pet.params.height
                        pet.behavior = PetBehavior.getRandomMovement()
                        pet.behaviorTimer = 0
                    } else {
                        // Mid-climb events
                        if (pet.behaviorTimer > 2000 && Random.nextFloat() < 0.02f) {
                            // Trigger JUMP to opposite wall
                            pet.behavior = PetBehavior.JUMP
                            // Jump away from the wall
                            pet.dx =
                                    if (pet.x <= minX) (15 * animationSpeedMultiplier).toInt()
                                    else (-15 * animationSpeedMultiplier).toInt()
                            pet.dy = (-10 * animationSpeedMultiplier).toInt() // Initial upward arc
                            pet.behaviorTimer = 0
                        } else if (pet.behaviorTimer > 4000 && Random.nextFloat() < 0.005f) {
                            // Fatigue -> Fall
                            pet.behavior = PetBehavior.FALL
                            pet.behaviorTimer = 0
                        }
                    }
                }
                PetBehavior.JUMP -> {
                    // Apply horizontal velocity
                    pet.x += pet.dx
                    // Apply gravity to vertical velocity (Arc)
                    pet.dy += (1 * animationSpeedMultiplier).toInt()
                    pet.y += pet.dy

                    // Collision: Opposite Wall
                    if (pet.x <= minX) {
                        pet.x = minX
                        pet.behavior = PetBehavior.CLIMB_EDGE // Stick to wall
                        pet.dy = 0 // Stop vertical momentum
                        pet.behaviorTimer = 0
                    } else if (pet.x + pet.params.width >= maxX) {
                        pet.x = maxX - pet.params.width
                        pet.behavior = PetBehavior.CLIMB_EDGE // Stick to wall
                        pet.dy = 0
                        pet.behaviorTimer = 0
                    }
                    // Collision: Floor
                    else if (pet.y + pet.params.height >= maxY) {
                        pet.y = maxY - pet.params.height
                        pet.behavior = PetBehavior.getRandomMovement() // Landed
                        pet.behaviorTimer = 0
                    }
                    // Low chance to "miss" and fall
                    else if (Random.nextFloat() < 0.005f) {
                        pet.behavior = PetBehavior.FALL
                        pet.behaviorTimer = 0
                    }
                }
                PetBehavior.IDLE -> {
                    pet.dx = 0
                    pet.dy = 0
                    // Transition to movement after random time
                    if (pet.behaviorTimer > 2000 && Random.nextFloat() < 0.05f) {
                        pet.behavior = PetBehavior.getRandomMovement()
                        pet.behaviorTimer = 0
                    }
                }
                PetBehavior.NONE -> {
                    pet.dx = 0
                    pet.dy = 0
                    // Quickly transition to active life
                    if (pet.behaviorTimer > 500) {
                        pet.behavior = PetBehavior.getRandomMovement()
                        pet.behaviorTimer = 0
                    }
                }
                else -> {
                    // Start falling if state is unsupported
                    pet.behavior = PetBehavior.FALL
                }
            }

            pet.params.x = pet.x
            pet.params.y = pet.y

            try {
                petWindowManager.updateViewLayout(pet.view, pet.params)
            } catch (e: Exception) {
                // View might be removed
            }
        }
    }
}
