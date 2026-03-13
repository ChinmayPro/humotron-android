package com.humotron.app.data.local.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import lib.linktop.nexring.api.SleepStage
import lib.linktop.nexring.api.SleepState


class SleepConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromSleepStages(value: List<SleepStage>): String = gson.toJson(value)

    @TypeConverter
    fun toSleepStages(value: String): List<SleepStage> =
        gson.fromJson(value, object : TypeToken<List<SleepStage>>() {}.type)

    @TypeConverter
    fun fromSleepStates(value: List<SleepState>): String = gson.toJson(value)

    @TypeConverter
    fun toSleepStates(value: String): List<SleepState> =
        gson.fromJson(value, object : TypeToken<List<SleepState>>() {}.type)
}