package com.humotron.app.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DefaultDayReadingTimeSlotNavigator(
    override val maxTimeToGoBack: Int? = null,
    private var currentDate: LocalDate,
) : ReadingTimeSlotNavigator {

    override val rangeName = "DAY"
    override val rangeUnitCount = 1

    private val initialDate = currentDate

    override val startDate: LocalDateTime
        get() = currentDate.atStartOfDay()

    override val endDate: LocalDateTime
        get() = currentDate.plusDays(1).atStartOfDay()

    override val startDateStringUTC: String
        get() = startDate.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_DATE_TIME)

    override val endDateStringUTC: String
        get() = endDate.atZone(ZoneId.systemDefault())
            .withZoneSameInstant(ZoneOffset.UTC)
            .format(DateTimeFormatter.ISO_DATE_TIME)

    override val canGoBack: Boolean
        get() = maxTimeToGoBack == null ||
                ChronoUnit.DAYS.between(currentDate, initialDate) < maxTimeToGoBack

    override val canGoNext: Boolean
        get() = currentDate.isBefore(initialDate)

    override fun getCurrentTimeRangeSlot(): String {
        return currentDate.format(DateTimeFormatter.ofPattern("MMM dd yyyy"))
    }

    override fun goToPreviousTimeRangeSlot(): String? {
        if (!canGoBack) return null
        currentDate = currentDate.minusDays(1)
        return getCurrentTimeRangeSlot()
    }

    override fun goToNextTimeRangeSlot(): String? {
        if (!canGoNext) return null
        currentDate = currentDate.plusDays(1)
        return getCurrentTimeRangeSlot()
    }
}