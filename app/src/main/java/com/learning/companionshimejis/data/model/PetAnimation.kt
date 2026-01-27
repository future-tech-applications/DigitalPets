package com.learning.companionshimejis.data.model

import android.graphics.Bitmap

/**
 * Defines a specific animation sequence (e.g., WALK_LEFT, JUMP).
 *
 * @param spriteSheet The full bitmap containing all frames for this animation.
 * @param frameWidth The width of a single frame in pixels.
 * @param frameHeight The height of a single frame in pixels.
 * @param frameCount Total number of frames in this animation.
 * @param frameDurationMs How long each frame should be displayed (milliseconds).
 * @param loop Whether the animation should loop (true) or play once (false).
 */
data class PetAnimation(
        val spriteSheet: Bitmap,
        val frameWidth: Int,
        val frameHeight: Int,
        val frameCount: Int,
        val frameDurationMs: Long,
        val loop: Boolean = true
)
