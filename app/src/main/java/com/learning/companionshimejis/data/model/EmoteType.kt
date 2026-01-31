package com.learning.companionshimejis.data.model

/** Defines the type of emotional expression (emote) a pet can show. */
sealed class EmoteType {
    data object NONE : EmoteType()
    data object HAPPY : EmoteType() // ❤️
    data object SURPRISED : EmoteType() // !
    data object THINKING : EmoteType() // ?
    data object SLEEPY : EmoteType() // zZz
    data object ANGRY : EmoteType() // #
    companion object {
        fun values(): Array<EmoteType> {
            return arrayOf(NONE, HAPPY, SURPRISED, THINKING, SLEEPY, ANGRY)
        }

        fun valueOf(value: String): EmoteType {
            return when (value) {
                "NONE" -> NONE
                "HAPPY" -> HAPPY
                "SURPRISED" -> SURPRISED
                "THINKING" -> THINKING
                "SLEEPY" -> SLEEPY
                "ANGRY" -> ANGRY
                else -> throw IllegalArgumentException("No object com.learning.companionshimejis.data.model.EmoteType.$value")
            }
        }
    }
}
