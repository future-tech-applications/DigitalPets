package com.learning.companionshimejis.data.model

data class Pet(
        val id: String,
        val name: String,
        val resId: Int,
        // Default to legacy 4x4 if not specified
        val cols: Int = 4,
        val rows: Int = 4,
        val behaviorMapId: String =
                "LEGACY_4ROW" // "LEGACY_4ROW" or "STANDARD_8ROW" or "LITTLE_MAN_7ROW"
)
