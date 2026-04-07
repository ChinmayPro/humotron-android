package com.humotron.app.util

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.humotron.app.core.Preference
import com.humotron.app.domain.modal.response.User
import com.humotron.app.domain.modal.response.UserHardware

class PrefUtils(private val sharedPreferences: SharedPreferences) {
    fun setAuthToken(string: String) {
        sharedPreferences.edit { putString(Preference.AUTH_TOKEN, string) }
    }

    fun getAuthToken(): String? {
        return sharedPreferences.getString(Preference.AUTH_TOKEN, "")
    }

    fun setLoginResponse(data: User) {
        sharedPreferences.edit { putString(Preference.LOGIN, Gson().toJson(data)) }
    }

    fun getLoginResponse(): User {
        val login = sharedPreferences.getString(Preference.LOGIN, "{}")
        return Gson().fromJson(login, User::class.java)

    }

    fun isLogin(): Boolean {
        return sharedPreferences.getString(Preference.LOGIN, null) != null
    }

    fun setString(key: String, value: String) {
        sharedPreferences.edit { putString(key, value) }
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun setBoolean(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, default)
    }

    fun setLong(key: String, value: Long) {
        sharedPreferences.edit { putLong(key, value) }
    }

    fun getLong(key: String): Long {
        return sharedPreferences.getLong(key, 0)
    }

    fun setHardwareData(data: UserHardware) {
        sharedPreferences.edit { putString(Preference.HARDWARE_DATA, Gson().toJson(data)) }
    }

    fun getHardwareId(): String? {
        val hardwareString = sharedPreferences.getString(Preference.HARDWARE_DATA, "{}")
        val data = Gson().fromJson(hardwareString, UserHardware::class.java)
        return data?.id
    }

    fun setBandHardwareData(data: UserHardware) {
        sharedPreferences.edit { putString(Preference.BAND_HARDWARE_DATA, Gson().toJson(data)) }
    }

    fun getBandHardwareType(): String? {
        val hardwareString = sharedPreferences.getString(Preference.BAND_HARDWARE_DATA, "{}")
        val data = Gson().fromJson(hardwareString, UserHardware::class.java)
        return data?.hardwareType
    }

    fun getBandHardwareId(): String? {
        val hardwareString = sharedPreferences.getString(Preference.BAND_HARDWARE_DATA, "{}")
        val data = Gson().fromJson(hardwareString, UserHardware::class.java)
        return data?.id
    }

    fun clear() {
        sharedPreferences.edit { clear() }
    }

    fun saveAssessmentAnswers(assessmentId: String, answersJson: String) {
        sharedPreferences.edit()
            .putString(Preference.PREF_ASSESSMENT_ANSWERS + assessmentId, answersJson).apply()
    }

    fun getAssessmentAnswers(assessmentId: String): String? {
        return sharedPreferences.getString(Preference.PREF_ASSESSMENT_ANSWERS + assessmentId, null)
    }

    fun clearAssessmentAnswers(assessmentId: String) {
        sharedPreferences.edit().remove(Preference.PREF_ASSESSMENT_ANSWERS + assessmentId).apply()
    }
}