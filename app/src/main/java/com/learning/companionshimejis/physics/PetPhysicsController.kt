package com.learning.companionshimejis.physics

import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.overlay.PetWindowManager
import com.learning.companionshimejis.persistence.PetState
import kotlin.random.Random

/**
 * Handles the physics and behavior of the pets in the overlay manager and updates their positions.
 */
class PetPhysicsController(private val petWindowManager: PetWindowManager) {

    val bounds = petWindowManager.getUsableBounds()

    // Use absolute boundaries for screen awareness
    val minX = bounds.left
    val maxX = bounds.right
    val minY = bounds.top
    val maxY = bounds.bottom

    fun updatePhysics(activePets: List<PetState>, animationSpeedMultiplier: Float) {
        resolveCollisions(activePets, animationSpeedMultiplier)

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
                    } else if (Random.nextFloat() < 0.25f) { // 1/4th chance to Fly
                        pet.behavior = PetBehavior.FLY
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
                    } else if (Random.nextFloat() < 0.001f) { // Very rare chance to Fly
                        pet.behavior = PetBehavior.FLY
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
                PetBehavior.FLY -> {
                    pet.dx = 0 // Mostly vertical, maybe slight drift later
                    pet.dy = (-4 * animationSpeedMultiplier).toInt() // Float Up
                    pet.y += pet.dy

                    // Ceiling Collision (minY) -> Land on Ceiling
                    if (pet.y <= minY) {
                        pet.y = minY
                        pet.dy = 0
                        // Start Walking on Ceiling
                        pet.behavior =
                                if (Random.nextBoolean()) PetBehavior.WALK_LEFT
                                else PetBehavior.WALK_RIGHT
                        pet.behaviorTimer = 0
                    }
                    // Exhaustion: Random mid-air Fall
                    else if (pet.behaviorTimer > 2000 && Random.nextFloat() < 0.005f) {
                        pet.behavior = PetBehavior.FALL
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

    private fun resolveCollisions(pets: List<PetState>, animationSpeedMultiplier: Float) {
        if (pets.size < 2) return

        for (i in pets.indices) {
            val petA = pets[i]
            if (petA.isMenuOpen || petA.isDragging) continue

            for (j in i + 1 until pets.size) {
                val petB = pets[j]
                if (petB.isMenuOpen || petB.isDragging) continue

                // Check intersection
                if (android.graphics.Rect.intersects(
                                android.graphics.Rect(
                                        petA.x,
                                        petA.y,
                                        petA.x + petA.params.width,
                                        petA.y + petA.params.height
                                ),
                                android.graphics.Rect(
                                        petB.x,
                                        petB.y,
                                        petB.x + petB.params.width,
                                        petB.y + petB.params.height
                                )
                        )
                ) {
                    // Check if they are already reacting to avoid stickiness loops
                    if (petA.behavior == PetBehavior.COLLIDE || petB.behavior == PetBehavior.COLLIDE
                    )
                            continue

                    // Reaction Logic
                    val isClimbingCollision =
                            petA.behavior == PetBehavior.CLIMB_EDGE ||
                                    petB.behavior == PetBehavior.CLIMB_EDGE

                    if (isClimbingCollision) {
                        // WALL COLLISION: Jump or Reverse
                        // 50% chance to JUMP to other wall
                        if (Random.nextBoolean()) {
                            petA.behavior = PetBehavior.JUMP
                            petB.behavior = PetBehavior.JUMP

                            // Initial Jump Velocity (Out and Up)
                            val jumpOutPower = (15 * animationSpeedMultiplier).toInt()
                            val jumpUpPower = (-10 * animationSpeedMultiplier).toInt()

                            val midScreen = (maxX + minX) / 2

                            // Jump away from center? No, jump away from current wall.
                            // If pet X is < midScreen, it's likely on Left Wall -> Jump Right (+)
                            // If pet X > midScreen, it's likely on Right Wall -> Jump Left (-)
                            petA.dx = if (petA.x < midScreen) jumpOutPower else -jumpOutPower
                            petA.dy = jumpUpPower

                            petB.dx = if (petB.x < midScreen) jumpOutPower else -jumpOutPower
                            petB.dy = jumpUpPower
                        } else {
                            // 50% Polite Reverse (Climb down/up or stay? Reverse usually means swap
                            // Up/Down)
                            // Climbing reverse means changing dy.
                            petA.dy = -petA.dy
                            petB.dy = -petB.dy

                            // Separate vertically
                            if (petA.y < petB.y) {
                                petA.y -= 10
                                petB.y += 10
                            } else {
                                petA.y += 10
                                petB.y -= 10
                            }
                        }
                    } else {
                        // GROUND COLLISION: Polite Reverse
                        petA.behavior =
                                if (petA.behavior == PetBehavior.WALK_LEFT) PetBehavior.WALK_RIGHT
                                else PetBehavior.WALK_LEFT
                        petB.behavior =
                                if (petB.behavior == PetBehavior.WALK_LEFT) PetBehavior.WALK_RIGHT
                                else PetBehavior.WALK_LEFT

                        // Add a small nudge to separate them immediately
                        if (petA.x < petB.x) {
                            petA.x -= 10
                            petB.x += 10
                        } else {
                            petA.x += 10
                            petB.x -= 10
                        }
                    }
                }
            }
        }
    }
}
