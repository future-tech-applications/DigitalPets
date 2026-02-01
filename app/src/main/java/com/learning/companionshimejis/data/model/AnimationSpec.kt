package com.learning.companionshimejis.data.model

/**
 * Defines the animation specification for a single behavior.
 * @param row The row index in the sprite sheet (0-indexed).
 * @param frameCount The number of frames in this animation row.
 * @param frameDurationMs The duration of each frame in milliseconds.
 */
data class AnimationSpec(val row: Int, val frameCount: Int, val frameDurationMs: Long)
