package com.humotron.app.util

import java.time.*
import java.time.format.DateTimeFormatter

class DefaultHourReadingTimeSlotNavigator(
    override val maxTimeToGoBack: Int? = null,
    currentDate: LocalDateTime
) : ReadingTimeSlotNavigator {

    override val rangeName: String = "HOUR"
    override val rangeUnitCount: Int = 1

    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    private var baseDate: LocalDateTime =
        currentDate.toLocalDate().atStartOfDay()

    private var currentHourIndex: Int = currentDate.hour

    private val initialDate = baseDate

    override val startDate: LocalDateTime
        get() = baseDate.plusHours(currentHourIndex.toLong())

    override val endDate: LocalDateTime
        get() = startDate.plusHours(1)

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
                Duration.between(startDate, initialDate).toHours() < maxTimeToGoBack

    override val canGoNext: Boolean
        get() = startDate.isBefore(LocalDateTime.now())

    override fun getCurrentTimeRangeSlot(): String {
        return "${startDate.format(formatter)} to ${endDate.format(formatter)}"
    }

    override fun goToPreviousTimeRangeSlot(): String? {
        if (!canGoBack) return null
        currentHourIndex--
        if (currentHourIndex < 0) {
            baseDate = baseDate.minusDays(1)
            currentHourIndex = 23
        }
        return getCurrentTimeRangeSlot()
    }

    override fun goToNextTimeRangeSlot(): String? {
        if (!canGoNext) return null
        currentHourIndex++
        if (currentHourIndex > 23) {
            baseDate = baseDate.plusDays(1)
            currentHourIndex = 0
        }
        return getCurrentTimeRangeSlot()
    }
}
