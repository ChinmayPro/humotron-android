package com.humotron.app.util

import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

class DefaultQuarterReadingTimeSlotNavigator(
    override val maxTimeToGoBack: Int? = null,
    private var currentDate: LocalDate,
) : ReadingTimeSlotNavigator {

    override val rangeName = "QUARTER"
    override val rangeUnitCount = 3 // 3 Months

    private val initialDate = currentDate

    private fun startOfQuarter(): LocalDate {
        val month = currentDate.monthValue
        val quarterStartMonth = ((month - 1) / 3) * 3 + 1
        return currentDate.withMonth(quarterStartMonth).with(TemporalAdjusters.firstDayOfMonth())
    }

    override val startDate: LocalDateTime
        get() = startOfQuarter().atStartOfDay()

    override val endDate: LocalDateTime
        get() = startOfQuarter().plusMonths(3).atStartOfDay()

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
                ChronoUnit.MONTHS.between(startOfQuarter(), startOfInitialQuarter()) < (maxTimeToGoBack * 3)

    override val canGoNext: Boolean
        get() = startOfQuarter().isBefore(startOfInitialQuarter())

    private fun startOfInitialQuarter(): LocalDate {
        val month = initialDate.monthValue
        val quarterStartMonth = ((month - 1) / 3) * 3 + 1
        return initialDate.withMonth(quarterStartMonth).with(TemporalAdjusters.firstDayOfMonth())
    }

    override fun getCurrentTimeRangeSlot(): String {
        val start = startOfQuarter()
        val end = start.plusMonths(2)
        val formatter = DateTimeFormatter.ofPattern("MMM")
        val yearFormatter = DateTimeFormatter.ofPattern("yyyy")
        return "${start.format(formatter)} - ${end.format(formatter)} ${start.format(yearFormatter)}"
    }

    override fun goToPreviousTimeRangeSlot(): String? {
        if (!canGoBack) return null
        currentDate = currentDate.minusMonths(3)
        return getCurrentTimeRangeSlot()
    }

    override fun goToNextTimeRangeSlot(): String? {
        if (!canGoNext) return null
        currentDate = currentDate.plusMonths(3)
        return getCurrentTimeRangeSlot()
    }
}
