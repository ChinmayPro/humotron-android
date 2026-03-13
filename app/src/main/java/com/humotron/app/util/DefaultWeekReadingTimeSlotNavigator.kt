package com.humotron.app.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DefaultWeekReadingTimeSlotNavigator(
    override val maxTimeToGoBack: Int? = null,
    private var currentDate: LocalDate,
) : ReadingTimeSlotNavigator {

    override val rangeName = "WEEK"
    override val rangeUnitCount = 7

    private val initialDate = currentDate

    private fun startOfWeek(): LocalDate =
        currentDate.with(DayOfWeek.MONDAY)

    override val startDate: LocalDateTime
        get() = startOfWeek().atStartOfDay()

    override val endDate: LocalDateTime
        get() = startOfWeek().plusWeeks(1).atStartOfDay()

    override val startDateStringUTC: String
        get() = startDate.toString()

    override val endDateStringUTC: String
        get() = endDate.toString()

    override val canGoBack: Boolean = true
    override val canGoNext: Boolean
        get() = startOfWeek().isBefore(initialDate)

    override fun getCurrentTimeRangeSlot(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM dd")
        return "${startOfWeek().format(formatter)} - ${
            startOfWeek().plusDays(6).format(formatter)
        }"
    }

    override fun goToPreviousTimeRangeSlot(): String? {
        currentDate = currentDate.minusWeeks(1)
        return getCurrentTimeRangeSlot()
    }

    override fun goToNextTimeRangeSlot(): String? {
        if (!canGoNext) return null
        currentDate = currentDate.plusWeeks(1)
        return getCurrentTimeRangeSlot()
    }
}