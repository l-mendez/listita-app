package com.example.listitaapp.domain.model

enum class ThemePreference(val value: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromValue(value: String?): ThemePreference = when (value) {
            LIGHT.value -> LIGHT
            DARK.value -> DARK
            else -> SYSTEM
        }
    }
}
