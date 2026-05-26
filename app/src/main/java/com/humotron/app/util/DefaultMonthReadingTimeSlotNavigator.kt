package com.humotron.app.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class DefaultMonthReadingTimeSlotNavigator(
    override val maxTimeToGoBack: Int? = null,
    private var currentDate: LocalDate,
) : ReadingTimeSlotNavigator {

    override val rangeName = "MONTH"
    override val rangeUnitCount = 1 // 1 Month

    private val initialDate = currentDate

    override val startDate: LocalDateTime
        get() = currentDate.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay()

    override val endDate: LocalDateTime
        get() = currentDate.with(TemporalAdjusters.firstDayOfNextMonth()).atStartOfDay()

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
                ChronoUnit.MONTHS.between(currentDate, initialDate) < maxTimeToGoBack

    override val canGoNext: Boolean
        get() = currentDate.with(TemporalAdjusters.firstDayOfMonth())
            .isBefore(initialDate.with(TemporalAdjusters.firstDayOfMonth()))

    override fun getCurrentTimeRangeSlot(): String {
        return currentDate.format(DateTimeFormatter.ofPattern("MMM yyyy"))
    }

    override fun goToPreviousTimeRangeSlot(): String? {
        if (!canGoBack) return null
        currentDate = currentDate.minusMonths(1)
        return getCurrentTimeRangeSlot()
    }

    override fun goToNextTimeRangeSlot(): String? {
        if (!canGoNext) return null
        currentDate = currentDate.plusMonths(1)
        return getCurrentTimeRangeSlot()
    }
}
