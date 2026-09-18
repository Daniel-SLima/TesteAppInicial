package com.danielslima.testeappinicial

enum class ViraSection {
    HOME,
    MOVEMENTS,
    PLANNING,
    SETTINGS;

    companion object {
        fun initial(): ViraSection = HOME
    }
}
