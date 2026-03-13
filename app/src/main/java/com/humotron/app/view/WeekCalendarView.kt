package com.humotron.app.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.humotron.app.R
import com.humotron.app.util.parseIsoToLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class WeekCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : LinearLayout(context, attrs) {

    private var today = LocalDate.now()
    private var currentWeekStart = today.with(DayOfWeek.MONDAY)
    private var selectedDate = today

    private val disabledDates = mutableSetOf<LocalDate>()

    private var dateListener: ((LocalDate) -> Unit)? = null

    private var weekChangeListener: ((LocalDate, LocalDate, LocalDate) -> Unit)? = null

    private val btnPrev
        get() = findViewById<MaterialButton>(R.id.btnPrev)

    private val btnNext
        get() = findViewById<MaterialButton>(R.id.btnNext)

    private val container
        get() = findViewById<LinearLayout>(R.id.weekContainer)

    init {

        orientation = HORIZONTAL

        LayoutInflater.from(context)
            .inflate(R.layout.view_week_calendar, this, true)

        setupArrows()

        renderWeek()
    }

    private fun setupArrows() {
        btnPrev.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            renderWeek()
            notifyWeekChanged()
        }

        btnNext.setOnClickListener {
            val nextWeek = currentWeekStart.plusWeeks(1)
            if (!nextWeek.isAfter(today)) {
                currentWeekStart = nextWeek
                renderWeek()
                notifyWeekChanged()
            }
        }
    }

    private fun renderWeek() {
        container.removeAllViews()
        for (i in 0..6) {

            val date = currentWeekStart.plusDays(i.toLong())

            val view = LayoutInflater.from(context)
                .inflate(R.layout.item_week_day, container, false)

            val btn = view.findViewById<MaterialButton>(R.id.btnDate)
            val tv = view.findViewById<android.widget.TextView>(R.id.tvDay)

            btn.text = date.dayOfMonth.toString()

            tv.text = date.dayOfWeek
                .getDisplayName(TextStyle.SHORT, Locale.getDefault())
                .uppercase()

            val isFuture = date.isAfter(today)
            val isDisabled = disabledDates.contains(date)
            val disabled = isFuture || isDisabled

            btn.isEnabled = !disabled

            if (disabled) {
                btn.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.week_day_back_disabled)

                btn.setTextColor(
                    ContextCompat.getColor(context, R.color.d100)
                )
                tv.setTextColor(
                    ContextCompat.getColor(context, R.color.d100)
                )
            } else if (date == selectedDate) {
                btn.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.green_2)
                val strokeWidth = (2 * resources.displayMetrics.density).toInt()
                btn.strokeWidth = strokeWidth
            } else {
                btn.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.week_day_back)
                btn.strokeWidth = 0
            }

            btn.setOnClickListener {
                selectedDate = date
                dateListener?.invoke(date)
                renderWeek()
            }
            container.addView(view)
        }
        updateArrowState()
    }

    private fun updateArrowState() {
        val nextWeek = currentWeekStart.plusWeeks(1)
        btnNext.isEnabled = !nextWeek.isAfter(today)
    }

    fun setDisabledDates(dates: List<LocalDate>) {
        disabledDates.clear()
        disabledDates.addAll(dates)
        renderWeek()
    }

    fun setOnDateSelected(listener: (LocalDate) -> Unit) {
        dateListener = listener
    }

    fun setOnWeekChanged(listener: (LocalDate, LocalDate, LocalDate) -> Unit) {
        weekChangeListener = listener
    }

    private fun notifyWeekChanged() {
        val start = currentWeekStart
        val end = currentWeekStart.plusDays(6)

        weekChangeListener?.invoke(selectedDate, start, end)
    }

    fun getSelectedDate(): LocalDate {
        return selectedDate
    }

    fun getWeekStartDate(): LocalDate {
        return currentWeekStart
    }

    fun getWeekEndDate(): LocalDate {
        return currentWeekStart.plusDays(6)
    }

    fun getWeekRange(): Pair<LocalDate, LocalDate> {
        val start = currentWeekStart
        val end = currentWeekStart.plusDays(6)
        return Pair(start, end)
    }

    fun showCurrentWeek() {
        currentWeekStart = today.with(DayOfWeek.MONDAY)
        renderWeek()
    }

    fun setDataSyncDate(dateString: String) {
        today = parseIsoToLocalDate(dateString)
        currentWeekStart = today.with(DayOfWeek.MONDAY)
        selectedDate = today
        renderWeek()
    }
}