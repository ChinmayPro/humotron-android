package com.humotron.app.ui.decode.data

import java.util.Calendar
import java.util.Date

object DeepDivesMockData {

    // Generates 14 days of workday data matching the HTML prototype
    val WORKDAY_DAYS: List<WorkdayDay> by lazy { buildDays() }

    private fun buildDays(): List<WorkdayDay> {
        val out = mutableListOf<WorkdayDay>()
        var seed = 13
        fun rnd(): Double {
            seed = (seed * 9301 + 49297) % 233280
            return seed / 233280.0
        }

        for (i in 13 downTo 0) {
            val cal = Calendar.getInstance().apply {
                set(2026, Calendar.APRIL, 8)
                add(Calendar.DAY_OF_MONTH, -i)
            }
            val dow = cal.get(Calendar.DAY_OF_WEEK)
            val isWeekend = dow == Calendar.SUNDAY || dow == Calendar.SATURDAY
            val base = if (isWeekend) 57 else 47
            val score = maxOf(34, minOf(70, (base + rnd() * 18 - 5).toInt()))
            val zone = StressZone.fromScore(score)

            val hours = (9..17).map { h ->
                val t = (h - 9) / 8.0
                val shape = Math.sin(t * Math.PI) * -8 + if (h < 11) 6.0 else 0.0
                val v = maxOf(22, minOf(82, (score + shape + rnd() * 20 - 10).toInt()))
                HourData(h, v)
            }

            out.add(
                WorkdayDay(
                    date = cal.time,
                    dayOfWeek = dow,
                    score = score,
                    zone = zone,
                    hours = hours
                )
            )
        }
        return out
    }

    fun zoneDist(day: WorkdayDay): List<ZoneDistribution> {
        val counts = mutableMapOf<StressZone, Int>()
        StressZone.entries.forEach { counts[it] = 0 }
        day.hours.forEach { h ->
            val z = StressZone.fromScore(h.stressValue)
            counts[z] = (counts[z] ?: 0) + 1
        }
        val total = day.hours.size
        return StressZone.entries.map { z ->
            ZoneDistribution(
                name = z.label,
                colorHex = z.colorHex,
                percentage = if (total > 0) ((counts[z] ?: 0) * 100) / total else 0
            )
        }
    }

    val WORKDAY_REPORTS = listOf(
        WorkdayReport(
            score = "51.4",
            zone = "Alert",
            date = "Mar 24 – Apr 08",
            name = "Avg Workday Stress",
            target = "workday_details:a"
        ),
        WorkdayReport(
            score = "46.2",
            zone = "Balanced",
            date = "Mar 09 – Mar 23",
            name = "Avg Workday Stress",
            target = "workday_details:b"
        ),
        WorkdayReport(
            score = "57.8",
            zone = "Challenged",
            date = "Feb 23 – Mar 08",
            name = "Avg Workday Stress",
            target = "workday_details:c"
        )
    )

    val WEATHER_REPORTS = listOf(
        WeatherReport(
            score = "47.3%",
            zone = "Moderate",
            date = "09 Apr 2026",
            name = "Weather Resilience",
            target = "weather_detail:a"
        ),
        WeatherReport(
            score = "41.0%",
            zone = "Sensitive",
            date = "12 Oct 2025",
            name = "Weather Resilience",
            target = "weather_detail:a"
        )
    )

    val WEATHER_PAIRINGS = listOf(
        WeatherPairing("a", "HRV vs Temperature", "Cooler days boost your recovery", "Generated 09 Apr 2026", "47%", "Moderate", 3),
        WeatherPairing("b", "Humidity vs Sleep Efficiency", "Humidity moderately affects your sleep", "Generated 11 Apr 2026", "41%", "Sensitive", 2, 79, "142 / 180 days"),
        WeatherPairing("c", "Pressure vs Systolic BP", "Pressure drops nudge your BP up", null, null, null, 1),
        WeatherPairing("d", "Temperature vs Resting HR", "Warmer days lift your resting heart rate", "Generated 14 Apr 2026", "52%", "Moderate", 2, 64, "116 / 180 days")
    )

    val WEATHER_DETAILS = mapOf(
        "a" to WeatherDetail(
            title = "HRV vs Temperature",
            range = "Feb 18 – Apr 08 · 180 paired days",
            a = "HRV",
            b = "Feels like",
            impact = 45,
            level = "40–59",
            summary = "Your HRV thrives on cooler days. As temperatures climb, recovery isn't as strong — your body prefers a chillier environment to fully reset.",
            impactNote = "Feels-like temperature has a moderate impact on your HRV.",
            aPts = listOf(44, 48, 52, 46, 55, 49, 58, 51),
            bPts = listOf(40, 42, 38, 44, 36, 43, 34, 41),
            ins = listOf(
                WeatherInsight("2026-03-21", "HRV peaks on cooler days", "Your highest HRV of 150.3 landed on a day that felt like 2.6°C."),
                WeatherInsight("2026-04-08", "Lower HRV on warmer days", "On warmer days your HRV settled to 43.5.")
            ),
            plan = listOf(
                WeatherPlan("A", "Schedule recovery on cooler days", "Plan deload sessions when it feels below 3°C.", "lime"),
                WeatherPlan("B", "Cool the bedroom on warm nights", "Aim for a cooler sleep surface above 8°C.", "cool"),
                WeatherPlan("C", "Mind the warm-up window", "Add breath work on warmer afternoons.", "watch"),
                WeatherPlan("D", "Keep tracking sensitivity", "Confirm the pattern as the season turns.", "attention")
            )
        ),
        "b" to WeatherDetail(
            title = "Humidity vs Sleep Efficiency",
            range = "Feb 18 – Apr 08 · 180 paired days",
            a = "Sleep eff.",
            b = "Humidity",
            impact = 38,
            level = "20–39",
            summary = "Higher humidity nudges your sleep efficiency down. Drier nights line up with your most restorative sleep.",
            impactNote = "Humidity has a mild-to-moderate impact on your sleep efficiency.",
            aPts = listOf(88, 86, 90, 84, 82, 87, 80, 85),
            bPts = listOf(55, 60, 52, 68, 72, 58, 75, 63),
            ins = listOf(
                WeatherInsight("2026-03-12", "Best sleep on dry nights", "Your top efficiency of 94% came on a 48% humidity night."),
                WeatherInsight("2026-03-29", "Humid nights cost efficiency", "At 78% humidity, efficiency dipped to 80%.")
            ),
            plan = listOf(
                WeatherPlan("A", "Dehumidify before bed", "Target 45–55% in the bedroom.", "lime"),
                WeatherPlan("B", "Ventilate on humid nights", "Move air to ease the load.", "cool"),
                WeatherPlan("C", "Lighten late meals", "Reduce overnight thermal load.", "watch"),
                WeatherPlan("D", "Keep tracking sensitivity", "Confirm the pattern across seasons.", "attention")
            )
        ),
        "c" to WeatherDetail(
            title = "Pressure vs Systolic BP",
            range = "Feb 18 – Apr 08 · 180 paired days",
            a = "Systolic BP",
            b = "Pressure",
            impact = 52,
            level = "40–59",
            summary = "Falling barometric pressure tends to push your systolic BP up. Stable, high-pressure days read calmer.",
            impactNote = "Barometric pressure has a moderate impact on your systolic BP.",
            aPts = listOf(118, 121, 117, 124, 126, 120, 128, 122),
            bPts = listOf(1018, 1012, 1020, 1006, 1002, 1014, 999, 1010),
            ins = listOf(
                WeatherInsight("2026-03-30", "BP rises as pressure drops", "Your highest reading of 128 mmHg came on a low-pressure day."),
                WeatherInsight("2026-03-18", "Calmer on stable days", "On a high-pressure day BP settled to 117 mmHg.")
            ),
            plan = listOf(
                WeatherPlan("A", "Ease load on low-pressure days", "Plan lighter days when fronts move in.", "lime"),
                WeatherPlan("B", "Hold your routine", "Hydration and sleep buffer the swing.", "cool"),
                WeatherPlan("C", "Flag big drops", "Note readings around weather changes.", "watch"),
                WeatherPlan("D", "Keep tracking sensitivity", "Confirm the pattern over time.", "attention")
            )
        ),
        "d" to WeatherDetail(
            title = "Temperature vs Resting HR",
            range = "Feb 18 – Apr 08 · 180 paired days",
            a = "Resting HR",
            b = "Feels like",
            impact = 41,
            level = "40–59",
            summary = "Warmer days lift your resting heart rate a little. Cooler days line up with your lowest resting readings.",
            impactNote = "Feels-like temperature has a moderate impact on your resting heart rate.",
            aPts = listOf(54, 56, 53, 58, 60, 55, 62, 57),
            bPts = listOf(6, 9, 5, 12, 15, 8, 18, 11),
            ins = listOf(
                WeatherInsight("2026-04-05", "Resting HR climbs on warm days", "Your highest resting HR of 62 bpm came on an 18°C day."),
                WeatherInsight("2026-03-19", "Lowest on cool days", "On a 5°C day your resting HR dropped to 53 bpm.")
            ),
            plan = listOf(
                WeatherPlan("A", "Cool down on warm days", "Pre-cool before sleep when it is warm.", "lime"),
                WeatherPlan("B", "Hydrate in the heat", "Support circulation as temps rise.", "cool"),
                WeatherPlan("C", "Time training", "Favour cooler parts of warm days.", "watch"),
                WeatherPlan("D", "Keep tracking sensitivity", "Confirm the pattern through summer.", "attention")
            )
        )
    )

    val MONTHLY_REPORTS = listOf(
        MonthlyReportItem(
            month = "April 2026",
            state = "ready",
            score = "51.4",
            zone = "Alert",
            range = "Mar 24 - Apr 08 · 13 workday days"
        ),
        MonthlyReportItem(
            month = "March 2026",
            state = "generate",
            range = "Mar 01 - Mar 28 · 21 workday days",
            note = "Enough data collected — ready to compose."
        ),
        MonthlyReportItem(
            month = "February 2026",
            state = "collecting",
            range = "Feb 01 - Feb 27",
            note = "14 / 20 workday days",
            pct = 70
        )
    )
}
