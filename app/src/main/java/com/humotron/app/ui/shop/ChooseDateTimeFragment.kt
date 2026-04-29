package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentChooseDateTimeBinding
import com.humotron.app.ui.shop.adapter.CalendarAdapter
import com.humotron.app.ui.shop.adapter.TimeSlotAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class ChooseDateTimeFragment : BaseFragment(R.layout.fragment_choose_date_time) {

    private lateinit var binding: FragmentChooseDateTimeBinding
    private val viewModel: ShopViewModel by activityViewModels()
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var timeSlotAdapter: TimeSlotAdapter
    
    private var currentMonthCalendar = Calendar.getInstance()
    private var selectedDate = Calendar.getInstance()
    private var selectedTime: String? = "08:30" // Default selection
    private val today = Calendar.getInstance()

    private var isPickerVisible = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChooseDateTimeBinding.bind(view)

        setupInsets()
        initViews()
        setupCalendar()
        setupPickers()
        setupTimeSlots()
        
        // Show summary by default for the pre-selected time
        showSummary()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            binding.btnContinue.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + dpToPx(24)
            }
            
            insets
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivPrevMonth.setOnClickListener {
            if (!isPickerVisible && canGoToPrevMonth()) {
                currentMonthCalendar.add(Calendar.MONTH, -1)
                updateCalendar()
            }
        }

        binding.ivNextMonth.setOnClickListener {
            if (!isPickerVisible) {
                currentMonthCalendar.add(Calendar.MONTH, 1)
                updateCalendar()
            }
        }

        binding.llHeader.setOnClickListener {
            togglePicker()
        }

        binding.ivEdit.setOnClickListener {
            // Edit logic here
        }

        binding.btnContinue.setOnClickListener {
            viewModel.setSelectedDateTime(selectedDate, selectedTime)
            findNavController().navigate(R.id.action_fragmentChooseDateTime_to_fragmentVerifyBooking)
        }
    }

    private fun setupPickers() {
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        
        binding.monthPicker.apply {
            minValue = 0
            maxValue = months.size - 1
            displayedValues = months
            value = currentMonthCalendar.get(Calendar.MONTH)
            setOnValueChangedListener { _, _, newVal ->
                currentMonthCalendar.set(Calendar.MONTH, newVal)
                updateCalendar()
            }
        }

        val currentYear = today.get(Calendar.YEAR)
        binding.yearPicker.apply {
            minValue = currentYear
            maxValue = currentYear + 10
            value = currentMonthCalendar.get(Calendar.YEAR)
            setOnValueChangedListener { _, _, newVal ->
                currentMonthCalendar.set(Calendar.YEAR, newVal)
                updateCalendar()
            }
        }
    }

    private fun setupTimeSlots() {
        timeSlotAdapter = TimeSlotAdapter { time ->
            selectedTime = time
            showSummary()
        }
        binding.rvTimeSlots.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = timeSlotAdapter
        }
        
        val dummySlots = listOf(
            "08:30", "09:00", "09:30",
            "10:00", "10:30", "11:00",
            "11:30", "12:30", "13:00",
            "13:30"
        )
        timeSlotAdapter.setData(dummySlots)
        timeSlotAdapter.setSelectedTime("08:30") // Pre-select first position
    }

    private fun showSummary() {
        binding.llSummary.isVisible = true
        
        val sdf = SimpleDateFormat("EEEE, MMM dd' 'yyyy", Locale.getDefault())
        val dateStr = sdf.format(selectedDate.time)
        val timeStr = formatTime(selectedTime ?: "08:30")
        
        binding.tvSelectedDateTime.text = "$dateStr | $timeStr"
    }

    private fun formatTime(time: String): String {
        return try {
            val sdf24 = SimpleDateFormat("HH:mm", Locale.getDefault())
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = sdf24.parse(time)
            sdf12.format(date!!).uppercase()
        } catch (e: Exception) {
            time
        }
    }

    private fun togglePicker() {
        isPickerVisible = !isPickerVisible
        
        val rotationAngle = if (isPickerVisible) 90f else 0f
        binding.ivTitleChevron.animate().rotation(rotationAngle).setDuration(200).start()

        val titleColor = if (isPickerVisible) R.color.lime_green else R.color.white
        binding.tvMonthYear.setTextColor(ContextCompat.getColor(requireContext(), titleColor))

        binding.llWeekDays.isVisible = !isPickerVisible
        binding.rvCalendar.isVisible = !isPickerVisible
        binding.clWheelPicker.isVisible = isPickerVisible
        
        binding.ivPrevMonth.isVisible = !isPickerVisible
        binding.ivNextMonth.isVisible = !isPickerVisible

        if (isPickerVisible) {
            binding.monthPicker.value = currentMonthCalendar.get(Calendar.MONTH)
            binding.yearPicker.value = currentMonthCalendar.get(Calendar.YEAR)
        }
    }

    private fun canGoToPrevMonth(): Boolean {
        return currentMonthCalendar.get(Calendar.YEAR) > today.get(Calendar.YEAR) ||
                (currentMonthCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        currentMonthCalendar.get(Calendar.MONTH) > today.get(Calendar.MONTH))
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter { date ->
            selectedDate = date
            updateCalendar()
            showSummary()
        }
        binding.rvCalendar.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
        }
        updateCalendar()
    }

    private fun updateCalendar() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.tvMonthYear.text = sdf.format(currentMonthCalendar.time)

        val canGoBack = canGoToPrevMonth()
        binding.ivPrevMonth.isEnabled = canGoBack
        binding.ivPrevMonth.alpha = if (canGoBack) 1.0f else 0.3f

        val days = mutableListOf<Calendar?>()
        val calendar = currentMonthCalendar.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
        
        for (i in 0 until firstDayOfWeek) {
            days.add(null)
        }

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 1..daysInMonth) {
            val dayCalendar = calendar.clone() as Calendar
            dayCalendar.set(Calendar.DAY_OF_MONTH, i)
            days.add(dayCalendar)
        }

        calendarAdapter.setData(days, selectedDate)
    }
}
