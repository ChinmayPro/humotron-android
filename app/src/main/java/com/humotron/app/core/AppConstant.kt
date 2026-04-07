package com.humotron.app.core

object AppConstant {

    const val BASE_URL: String = "https://api.humotron.com:4200/api/v1/"
    const val ASSESSMENT_ID = "assessmentId"
    const val ASSESSMENT = "assessment"


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

        /** Saved MAC for J-style smart band (BLE). */
        const val WEARABLE_BAND = "wearable_band"
        const val HARDWARE_DATA = "hardware_data"
        const val BAND_HARDWARE_DATA = "band_hardware_data"
        const val RECORD_DATE = "recorded_time"
        const val PREF_ASSESSMENT_ANSWERS = "assessment_answers"


    }
}

//var AssesmentTempId  = "67a29b40d4e4e815a8401275"
var AssesmentTempId = "67a24f1bd4e4e815a840102c"
//var AssesmentTempId  = "67a28001d4e4e815a8401147"
