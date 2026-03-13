package com.humotron.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

fun utcToLocalTime(
    utcTime: String?,
    outputPattern: String = "dd-MMM-yyyy hh:mm:ss a",
): String {
    return try {
        val instant = Instant.parse(utcTime)
        val formatter = DateTimeFormatter
            .ofPattern(outputPattern)
            .withZone(ZoneId.systemDefault())

        formatter.format(instant)
    } catch (e: Exception) {
        ""
    }
}


fun utcOffsetToLocalTime1(
    utcTime: String?,
    outputPattern: String = "dd-MMM-yyyy hh:mm:ss a",
): String {
    return try {
        if (utcTime.isNullOrBlank()) return ""

        val inputFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")

        val offsetDateTime =
            java.time.OffsetDateTime.parse(utcTime, inputFormatter)

        offsetDateTime
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(outputPattern))

    } catch (e: Exception) {
        ""
    }
}

fun utcOffsetToLocalTime(
    utcTime: String?,
    outputPattern: String = "dd-MMM-yyyy hh:mm:ss a"
): String {
    if (utcTime.isNullOrBlank()) return ""
    return try {
        val offsetDateTime = runCatching {
            OffsetDateTime.parse(utcTime) // works for Z
        }.recoverCatching {

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            OffsetDateTime.parse(utcTime, formatter) // works for +0000
        }.getOrNull() ?: return ""

        offsetDateTime
            .atZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern(outputPattern))

    } catch (e: Exception) {
        ""
    }
}

fun utcOffsetToOrdinalDate(
    utcTime: String?
): String {
    return try {
        if (utcTime.isNullOrBlank()) return ""

        val offsetDateTime = OffsetDateTime.parse(utcTime)

        val localDate = offsetDateTime
            .atZoneSameInstant(ZoneId.systemDefault())
            .toLocalDate()

        val day = localDate.dayOfMonth

        val suffix = if (day in 11..13) {
            "th"
        } else {
            when (day % 10) {
                1 -> "st"
                2 -> "nd"
                3 -> "rd"
                else -> "th"
            }
        }

        val monthYear = localDate.format(DateTimeFormatter.ofPattern("MMM, yyyy"))

        "$day$suffix $monthYear"

    } catch (e: Exception) {
        ""
    }
}

fun formatDateToMMMddyyyy(inputDate: String?): String? {
    return try {
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

        val date = inputFormat.parse(inputDate)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        inputDate // return original if parsing fails
    }
}

fun convertDecimalHours(time: String?): Pair<Int, Int> {
    val totalHours = time?.toDoubleOrNull() ?: return 0 to 0
    val hours = totalHours.toInt()
    val minutes = ((totalHours - hours) * 60).roundToInt()
    return hours to minutes
}

fun formatLocalDateToIso(date: LocalDate): String {
    return date.atStartOfDay(ZoneOffset.UTC)
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
}

fun parseIsoToLocalDate(isoDate: String): LocalDate {
    return try {
        Instant.parse(isoDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
    } catch (e: Exception) {
        LocalDate.now()
    }
}
