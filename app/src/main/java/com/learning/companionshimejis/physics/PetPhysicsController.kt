package com.learning.companionshimejis.physics

import com.learning.companionshimejis.data.model.EmoteType
import com.learning.companionshimejis.data.model.PetBehavior
import com.learning.companionshimejis.overlay.PetWindowManager
import com.learning.companionshimejis.persistence.PetState
import kotlin.random.Random

private const val TICK_MS = 16

/**
 * Responsible for updating the physics of the pets.
 * @param petWindowManager The window manager for the pets.
 */
class PetPhysicsController(private val petWindowManager: PetWindowManager) {

    private val bounds
        get() = petWindowManager.getUsableBounds()

    private val minX
        get() = bounds.left
    private val maxX
        get() = bounds.right
    private val minY
        get() = bounds.top
    private val maxY
        get() = bounds.bottom

    /**
     * Updates the physics of the pets.
     * @param pets The list of pets to update.
     * @param speed The speed of the pets.
     */
    fun updatePhysics(pets: List<PetState>, speed: Float) {

        // Reset per-tick decision locks
        pets.forEach {
            it.behaviorChangedThisTick = false
            it.behaviorTimer += TICK_MS
        }

        // Resolve behaviour when pet collides with other pets.
        resolveCollisions(pets, speed)

        pets.forEach { pet ->
            if (pet.isDragging || pet.isMenuOpen) return@forEach

            when (pet.behavior) {
                PetBehavior.FALL -> updateFall(pet, speed)
                PetBehavior.WALK_LEFT -> updateWalkLeft(pet, speed)
                PetBehavior.WALK_RIGHT -> updateWalkRight(pet, speed)
                PetBehavior.CLIMB_EDGE -> updateClimbEdge(pet, speed)
                PetBehavior.JUMP -> updateJump(pet, speed)
                PetBehavior.IDLE -> updateIdle(pet)
                PetBehavior.FLY -> updateFly(pet, speed)
                PetBehavior.NONE -> tryChange(pet, PetBehavior.FALL)
                else -> tryChange(pet, PetBehavior.FALL)
            }

            clampToBounds(pet)
            petWindowManager.updateViewLayout(pet.view, pet.params)
        }
    }

    // ────────────────────── BEHAVIOUR UPDATERS ──────────────────────

    private fun updateFall(pet: PetState, speed: Float) {
        pet.dx = 0
        pet.dy = (10 * speed).toInt()
        pet.y += pet.dy

        if (hitsFloor(pet)) {
            pet.y = maxY - pet.params.height
            tryChange(pet, PetBehavior.getRandomMovement())
        }
    }

    private fun updateWalkLeft(pet: PetState, speed: Float) {
        pet.dx = (-4 * speed).toInt()
        pet.dy = 0
        pet.x += pet.dx

        if (hitsLeftWall(pet)) {
            pet.x = minX
            tryChange(pet, if (chance(0.5f)) PetBehavior.CLIMB_EDGE else PetBehavior.WALK_RIGHT)
        } else if (pet.behaviorTimer > 3000 && chance(0.02f)) {
            tryChange(pet, PetBehavior.IDLE)
        }
    }

    private fun updateWalkRight(pet: PetState, speed: Float) {
        pet.dx = (4 * speed).toInt()
        pet.dy = 0
        pet.x += pet.dx

        if (hitsRightWall(pet)) {
            pet.x = maxX - pet.params.width
            tryChange(pet, if (chance(0.5f)) PetBehavior.CLIMB_EDGE else PetBehavior.WALK_LEFT)
        } else if (pet.behaviorTimer > 3000 && chance(0.02f)) {
            tryChange(pet, PetBehavior.IDLE)
        }
    }

    private fun updateClimbEdge(pet: PetState, speed: Float) {
        pet.dx = 0

        if (pet.dy == 0) {
            pet.dy = if (chance(0.25f)) (3 * speed).toInt() else (-3 * speed).toInt()
        }

        pet.y += pet.dy

        when {
            hitsCeiling(pet) -> {
                pet.y = minY
                tryChange(pet, PetBehavior.getRandomMovement())
            }
            hitsFloor(pet) -> {
                pet.y = maxY - pet.params.height
                tryChange(pet, PetBehavior.getRandomMovement())
            }
            pet.behaviorTimer > 2000 && chance(0.02f) -> {
                startJump(pet, speed)
            }
            pet.behaviorTimer > 4000 && chance(0.005f) -> {
                tryChange(pet, PetBehavior.FALL)
            }
        }
    }

    private fun updateJump(pet: PetState, speed: Float) {
        pet.x += pet.dx
        pet.dy += (1 * speed).toInt()
        pet.y += pet.dy

        when {
            hitsLeftWall(pet) -> stickToWall(pet, minX)
            hitsRightWall(pet) -> stickToWall(pet, maxX - pet.params.width)
            hitsFloor(pet) -> {
                pet.y = maxY - pet.params.height
                tryChange(pet, PetBehavior.getRandomMovement())
            }
            chance(0.005f) -> tryChange(pet, PetBehavior.FALL)
        }
    }

    private fun updateIdle(pet: PetState) {
        pet.dx = 0
        pet.dy = 0
        if (pet.behaviorTimer > 2000 && chance(0.05f)) {
            tryChange(pet, PetBehavior.getRandomMovement())
        }
    }

    private fun updateFly(pet: PetState, speed: Float) {
        pet.dx = 0
        pet.dy = (-4 * speed).toInt()
        pet.y += pet.dy

        if (hitsCeiling(pet)) {
            pet.y = minY
            tryChange(
                    pet,
                    if (Random.nextBoolean()) PetBehavior.WALK_LEFT else PetBehavior.WALK_RIGHT
            )
        } else if (pet.behaviorTimer > 2000 && chance(0.005f)) {
            tryChange(pet, PetBehavior.FALL)
        }
    }

    // ────────────────────── COLLISIONS ──────────────────────

    private fun resolveCollisions(pets: List<PetState>, speed: Float) {
        if (pets.size < 2) return

        for (i in pets.indices) {
            val a = pets[i]
            if (a.isDragging || a.isMenuOpen) continue

            for (j in i + 1 until pets.size) {
                val b = pets[j]
                if (b.isDragging || b.isMenuOpen) continue

                if (!intersects(a, b)) continue

                // Ceiling congestion → both fall
                if (a.y <= minY + 10 || b.y <= minY + 10) {
                    tryChange(a, PetBehavior.FALL)
                    tryChange(b, PetBehavior.FALL)
                    continue
                }

                // Climb collision → asymmetric jump
                if (a.behavior == PetBehavior.CLIMB_EDGE && b.behavior == PetBehavior.CLIMB_EDGE) {

                    startJump(a, speed)
                    tryChange(b, PetBehavior.FALL)
                }
            }
        }
    }

    // ────────────────────── HELPERS ──────────────────────

    private fun tryChange(pet: PetState, next: PetBehavior) {
        if (pet.behaviorChangedThisTick) return
        pet.behavior = next
        pet.behaviorTimer = 0
        pet.behaviorChangedThisTick = true

        // Trigger Emote on certain transitions
        if (next == PetBehavior.SLEEP) {
            triggerEmote(pet, EmoteType.SLEEPY)
        } else if (chance(0.05f)) {
            triggerRandomEmote(pet)
        }
    }

    private fun triggerEmote(pet: PetState, emote: EmoteType) {
        pet.currentEmote = emote
        pet.emoteTimer = 0
    }

    private fun triggerRandomEmote(pet: PetState) {
        val emotes = EmoteType.values().filter { it != EmoteType.NONE }
        triggerEmote(pet, emotes.random())
    }

    private fun startJump(pet: PetState, speed: Float) {
        tryChange(pet, PetBehavior.JUMP)
        pet.dx = if (pet.x < (maxX + minX) / 2) (15 * speed).toInt() else (-15 * speed).toInt()
        pet.dy = (-10 * speed).toInt()

        // Surprise emote on jump!
        if (chance(0.3f)) triggerEmote(pet, EmoteType.SURPRISED)
    }

    private fun stickToWall(pet: PetState, x: Int) {
        pet.x = x
        pet.dy = 0
        tryChange(pet, PetBehavior.CLIMB_EDGE)
    }

    private fun clampToBounds(pet: PetState) {
        pet.x = pet.x.coerceIn(minX, maxX - pet.params.width)
        pet.y = pet.y.coerceIn(minY, maxY - pet.params.height)
        pet.params.x = pet.x
        pet.params.y = pet.y
    }

    private fun intersects(a: PetState, b: PetState) =
            android.graphics.Rect.intersects(
                    android.graphics.Rect(a.x, a.y, a.x + a.params.width, a.y + a.params.height),
                    android.graphics.Rect(b.x, b.y, b.x + b.params.width, b.y + b.params.height)
            )

    private fun hitsLeftWall(p: PetState) = p.x <= minX
    private fun hitsRightWall(p: PetState) = p.x + p.params.width >= maxX
    private fun hitsCeiling(p: PetState) = p.y <= minY
    private fun hitsFloor(p: PetState) = p.y + p.params.height >= maxY

    private fun chance(p: Float) = Random.nextFloat() < p
}
