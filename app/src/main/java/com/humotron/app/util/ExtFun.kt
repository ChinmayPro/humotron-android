package com.humotron.app.util

fun String.toTitleFromCamelCase(): String {
    return try {
        this.replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        this
    }
}