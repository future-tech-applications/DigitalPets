package com.learning.companionshimejis.data.model

sealed class PetBehavior(val name: String) {
    object NONE : PetBehavior("NONE")
    object IDLE : PetBehavior("IDLE")
    object WALK_LEFT : PetBehavior("WALK_LEFT")
    object WALK_RIGHT : PetBehavior("WALK_RIGHT")
    object JUMP : PetBehavior("JUMP")
    object FALL : PetBehavior("FALL")
    object CLIMB_EDGE : PetBehavior("CLIMB_EDGE")
    object SLEEP : PetBehavior("SLEEP")
    object INTERACT : PetBehavior("INTERACT")
    object COLLIDE : PetBehavior("COLLIDE")

    // Duration in milliseconds before considering a transition
    var duration: Long = 0

    companion object {
        fun getRandomMovement(): PetBehavior {
            return when ((0..2).random()) {
                0 -> IDLE
                1 -> WALK_LEFT
                else -> WALK_RIGHT
            }
        }
    }
}
