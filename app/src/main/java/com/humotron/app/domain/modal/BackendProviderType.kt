package com.humotron.app.domain.modal

enum class BackendProviderType(val value: String) {
    GOOGLE_HEALTH("google_health"),
    OURA("oura"),
    POLAR("polar"),
    STRAVA("strava"),
    ULTRAHUMAN("ultrahuman"),
    WHOOP("whoop"),
    UNKNOWN("unknown");

    companion object {
        fun from(value: String?): BackendProviderType {
            return entries.find { it.value == value } ?: UNKNOWN
        }
    }
}
