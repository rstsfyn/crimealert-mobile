package com.restusofyan.crimealert_mobile.utils

enum class SensitivityLevel(val threshold: Double, val displayName: String) {
    LOW(0.9, "Low"),
    MID(0.8, "Mid"),
    HIGH(0.6, "High");

    companion object {

        fun fromString(name: String): SensitivityLevel {
            return values().find { it.displayName.equals(name, ignoreCase = true) } ?: LOW
        }
    }
}