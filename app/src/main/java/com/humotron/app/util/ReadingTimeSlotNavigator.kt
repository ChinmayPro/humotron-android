package com.humotron.app.util

import java.time.LocalDateTime

interface ReadingTimeSlotNavigator {

    fun getCurrentTimeRangeSlot(): String

    fun goToPreviousTimeRangeSlot(): String?
    fun goToNextTimeRangeSlot(): String?

    val maxTimeToGoBack: Int?

    val startDate: LocalDateTime
    val endDate: LocalDateTime

    val startDateStringUTC: String
    val endDateStringUTC: String

    val canGoBack: Boolean
    val canGoNext: Boolean

    val rangeName: String
    val rangeUnitCount: Int
}