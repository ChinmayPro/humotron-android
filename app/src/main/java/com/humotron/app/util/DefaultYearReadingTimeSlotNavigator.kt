package com.humotron.app.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class DefaultYearReadingTimeSlotNavigator(
    override val maxTimeToGoBack: Int? = null,
    private var currentDate: LocalDate,
) : ReadingTimeSlotNavigator {

    override val rangeName = "YEAR"
    override val rangeUnitCount = 12 // 12 Months

    private val initialDate = currentDate

    override val startDate: LocalDateTime
        get() = currentDate.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay()

    override val endDate: LocalDateTime
        get() = currentDate.with(TemporalAdjusters.firstDayOfNextYear()).atStartOfDay()

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
                ChronoUnit.YEARS.between(currentDate, initialDate) < maxTimeToGoBack

    override val canGoNext: Boolean
        get() = currentDate.with(TemporalAdjusters.firstDayOfYear())
            .isBefore(initialDate.with(TemporalAdjusters.firstDayOfYear()))

    override fun getCurrentTimeRangeSlot(): String {
        return currentDate.format(DateTimeFormatter.ofPattern("yyyy"))
    }

    override fun goToPreviousTimeRangeSlot(): String? {
        if (!canGoBack) return null
        currentDate = currentDate.minusYears(1)
        return getCurrentTimeRangeSlot()
    }

    override fun goToNextTimeRangeSlot(): String? {
        if (!canGoNext) return null
        currentDate = currentDate.plusYears(1)
        return getCurrentTimeRangeSlot()
    }
}
