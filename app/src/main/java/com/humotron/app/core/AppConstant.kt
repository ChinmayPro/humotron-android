package com.humotron.app.core

object AppConstant {

    const val BASE_URL: String = "https://api.humotron.com:4200/api/v1/"


}

object ErrorCode {

    val EMPTY_ERROR: Int = 1001
    val INTERNET_ERROR: Int = 1002
    val SERVER_ERROR: Int = 1003
    val UNKNOWN_ERROR: Int = 5001


}


class Preference {
    companion object {
        const val PREF_NAME = "Humotron"
        const val AUTH_TOKEN = "auth_token"
        const val ONBOARD_PRIVACY = "onboard_privacy"
        const val LOGIN_USER_EMAIL = "login_user_email"
        const val LOGIN = "login"
        const val WEARABLE_RING = "wearable_ring"
        const val HARDWARE_DATA = "hardware_data"
        const val RECORD_DATE = "recorded_time"

    }
}