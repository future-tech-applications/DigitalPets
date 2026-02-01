package com.learning.companionshimejis.animation

import android.graphics.Bitmap
import android.graphics.Color
import java.util.ArrayDeque

/**
 * Utility to process bitmpas for transparency. Specifically removes "checkerboard" patterns typical
 * of AI generation.
 */
object TransparencyHelper {

    /**
     * Removes checkerboard backgrounds using a 4-way flood fill from the edges. Only removes pixels
     * that are "near white" or "near grey" AND reachable from the corners.
     */
    fun removeCheckerboard(bitmap: Bitmap): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val width = result.width
        val height = result.height

        val queue = ArrayDeque<Pair<Int, Int>>()
        val visited = BooleanArray(width * height)

        // Seed from edges
        for (x in 0 until width) {
            queue.add(x to 0)
            queue.add(x to height - 1)
        }
        for (y in 0 until height) {
            queue.add(0 to y)
            queue.add(width - 1 to y)
        }

        while (queue.isNotEmpty()) {
            val (x, y) = queue.poll() ?: continue
            val idx = y * width + x
            if (visited[idx]) continue
            visited[idx] = true

            val color = result.getPixel(x, y)
            val a = (color shr 24) and 0xFF
            val r = (color shr 16) and 0xFF
            val g = (color shr 8) and 0xFF
            val b = color and 0xFF
            if (a > 0 && isBackground(r, g, b)) {
                result.setPixel(x, y, Color.TRANSPARENT)

                // 4-way Neighbors
                if (x > 0) queue.add(x - 1 to y)
                if (x < width - 1) queue.add(x + 1 to y)
                if (y > 0) queue.add(x to y - 1)
                if (y < height - 1) queue.add(x to y + 1)
            }
        }
        return result
    }

    private fun isBackground(r: Int, g: Int, b: Int): Boolean {
        val max = maxOf(r, maxOf(g, b))
        val min = minOf(r, minOf(g, b))
        val saturation = max - min

        // Match backgrounds/grid-lines with low saturation (greys, whites, blacks)
        if (saturation < 30) {
            val brightness = (r + g + b) / 3
            // AI checkerboards are usually in the 100-240 range.
            // We avoid 0-50 to protect potential dark eyes/details of the pet
            // unless they are very close to pure grey.
            if (brightness > 80) return true
            if (brightness < 30) return true // Also grid lines
        }

        return false
    }
}
