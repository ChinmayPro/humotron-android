package com.humotron.app.ui.shop

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentChooseDateTimeBinding
import com.humotron.app.ui.shop.adapter.DayCard
import com.humotron.app.ui.shop.adapter.DayCardAdapter
import com.humotron.app.ui.shop.adapter.LabTimeSlotAdapter
import com.humotron.app.ui.shop.adapter.TimeWindow
import com.humotron.app.ui.shop.adapter.TimeWindowAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class ChooseDateTimeFragment : BaseFragment(R.layout.fragment_choose_date_time) {

    private lateinit var binding: FragmentChooseDateTimeBinding
    private val viewModel: ShopViewModel by activityViewModels()

    private lateinit var dayCardAdapter: DayCardAdapter
    private lateinit var timeWindowAdapter: TimeWindowAdapter
    private lateinit var labTimeSlotAdapter: LabTimeSlotAdapter

    private var currentStep = 1 // 1: Date selection, 2: Time selection
    private var selectedDayCard: DayCard? = null
    private var selectedWindow: TimeWindow? = null
    private var selectedLabSlotTime: String? = "09:30"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentChooseDateTimeBinding.bind(view)

        activity?.window?.statusBarColor = Color.TRANSPARENT

        setupInsets()
        setupAdapters()
        initViews()
        renderStep()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            binding.layoutFooter.updatePadding(bottom = systemBars.bottom + (16 * density).toInt())
            insets
        }
    }

    private fun setupAdapters() {
        // Day cards adapter
        dayCardAdapter = DayCardAdapter { dayCard ->
            selectedDayCard = dayCard
            binding.tvSelectedDateVal.text = dayCard.fullFormatted
            binding.btnContinue.isEnabled = true
            binding.btnContinue.alpha = 1.0f
        }
        binding.rvDays.adapter = dayCardAdapter

        val days = DayCardAdapter.generateNextDays(14)
        dayCardAdapter.setData(days)

        if (days.isNotEmpty()) {
            selectedDayCard = days[0]
            binding.tvSelectedDateVal.text = days[0].fullFormatted
            val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.ENGLISH)
            binding.tvMonthLabel.text = monthFormat.format(days[0].calendar.time)
        }

        // Home service time windows adapter
        timeWindowAdapter = TimeWindowAdapter { window ->
            selectedWindow = window
            binding.btnContinue.isEnabled = true
            binding.btnContinue.alpha = 1.0f
        }
        binding.rvTimeWindows.adapter = timeWindowAdapter
        timeWindowAdapter.setData(TimeWindowAdapter.getDefaultWindows())

        // Lab visit 3-column time slot adapter
        labTimeSlotAdapter = LabTimeSlotAdapter { slotTime ->
            selectedLabSlotTime = slotTime
            binding.btnContinue.isEnabled = true
            binding.btnContinue.alpha = 1.0f
        }
        val gridLayoutManager = GridLayoutManager(requireContext(), 3)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (labTimeSlotAdapter.getItemViewType(position) == 0) 3 else 1
            }
        }
        binding.rvTimeSlotsGrid.layoutManager = gridLayoutManager
        binding.rvTimeSlotsGrid.adapter = labTimeSlotAdapter
        labTimeSlotAdapter.setData(LabTimeSlotAdapter.getDefaultLabSlots(), "09:30")
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentBookingType, false)
        }

        binding.btnContinue.setOnClickListener {
            if (currentStep == 1) {
                currentStep = 2
                renderStep()
            } else {
                onFinalContinue()
            }
        }
    }

    private fun onBackPressed() {
        if (currentStep == 2) {
            currentStep = 1
            renderStep()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun renderStep() {
        val titleLower = viewModel.getSelectedBookingType()?.title?.lowercase() ?: ""
        val isLab = titleLower.contains("lab")
        val isHome = titleLower.contains("home")
        val dateText = selectedDayCard?.fullFormatted ?: "Wed, 18 Jun"

        if (currentStep == 1) {
            // STEP 1: CHOOSE DATE
            binding.tvMonthLabel.visibility = View.VISIBLE
            binding.rvDays.visibility = View.VISIBLE
            binding.cvSelectedDate.visibility = View.VISIBLE
            binding.llTimeSection.visibility = View.GONE

            if (isLab) {
                updateStepProgressBar(totalSteps = 6, currentStepIndex = 3, stepName = "CHOOSE A DATE")
                binding.tvSelectHeading.text = "Pick a date to visit"
                val labName = viewModel.getSelectedLab()?.labName ?: "Canary Wharf Lab"
                binding.tvSelectSubtitle.text = labName
            } else if (isHome) {
                updateStepProgressBar(totalSteps = 5, currentStepIndex = 1, stepName = "CHOOSE A DATE")
                binding.tvSelectHeading.text = "Pick a date for your visit"
                binding.tvSelectSubtitle.text = "A trained professional will come to you."
            } else {
                updateStepProgressBar(totalSteps = 3, currentStepIndex = 1, stepName = "CHOOSE A DATE")
                binding.tvSelectHeading.text = "Pick a date"
                binding.tvSelectSubtitle.text = "Select your preferred date."
            }

            binding.btnContinue.isEnabled = selectedDayCard != null
            binding.btnContinue.alpha = if (selectedDayCard != null) 1.0f else 0.5f
            binding.btnContinue.text = "Continue"

        } else {
            // STEP 2: CHOOSE TIME
            binding.tvMonthLabel.visibility = View.GONE
            binding.rvDays.visibility = View.GONE
            binding.cvSelectedDate.visibility = View.GONE
            binding.llTimeSection.visibility = View.VISIBLE

            if (isLab) {
                // LAB VISIT: 3-Column Time Slot Grid with Morning/Afternoon/Evening sections (Prototype S.time_slots)
                updateStepProgressBar(totalSteps = 6, currentStepIndex = 4, stepName = "CHOOSE A TIME")
                binding.tvSelectHeading.text = "Choose a time slot"
                val labName = viewModel.getSelectedLab()?.labName ?: "Canary Wharf"
                binding.tvSelectSubtitle.text = "$dateText · $labName"

                binding.tvTimeSectionHeading.visibility = View.GONE
                binding.tvTimeSectionSubtitle.visibility = View.GONE
                binding.rvTimeWindows.visibility = View.GONE
                binding.rvTimeSlotsGrid.visibility = View.VISIBLE

                binding.btnContinue.isEnabled = selectedLabSlotTime != null
                binding.btnContinue.alpha = if (selectedLabSlotTime != null) 1.0f else 0.5f

            } else {
                // AT-HOME SERVICE: Time Windows (Prototype S.time_windows)
                updateStepProgressBar(totalSteps = 5, currentStepIndex = 2, stepName = "CHOOSE A TIME")
                binding.tvSelectHeading.text = "Choose a time window"
                binding.tvSelectSubtitle.text = "$dateText · we'll confirm a tighter arrival time the day before."

                binding.tvTimeSectionHeading.visibility = View.VISIBLE
                binding.tvTimeSectionSubtitle.visibility = View.VISIBLE
                binding.rvTimeWindows.visibility = View.VISIBLE
                binding.rvTimeSlotsGrid.visibility = View.GONE

                binding.btnContinue.isEnabled = selectedWindow != null
                binding.btnContinue.alpha = if (selectedWindow != null) 1.0f else 0.5f
            }

            binding.btnContinue.text = "Continue"
        }
    }

    private fun updateStepProgressBar(totalSteps: Int, currentStepIndex: Int, stepName: String) {
        binding.llProgressBar.removeAllViews()
        val density = resources.displayMetrics.density
        for (i in 0 until totalSteps) {
            val segment = View(requireContext())
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            if (i < totalSteps - 1) {
                params.marginEnd = (6 * density).toInt()
            }
            segment.layoutParams = params

            val bgDrawable = GradientDrawable().apply {
                cornerRadius = 2 * density
                if (i <= currentStepIndex) {
                    setColor(Color.parseColor("#5FB7C4"))
                } else {
                    setColor(Color.parseColor("#1AFFFFFF"))
                }
            }
            segment.background = bgDrawable
            binding.llProgressBar.addView(segment)
        }

        val stepNumber = currentStepIndex + 1
        binding.tvStepEyebrow.text = "STEP $stepNumber OF $totalSteps · $stepName"
    }

    private fun onFinalContinue() {
        val titleLower = viewModel.getSelectedBookingType()?.title?.lowercase() ?: ""
        val dateCal = selectedDayCard?.calendar

        if (titleLower.contains("lab")) {
            viewModel.setSelectedDateTime(dateCal, selectedLabSlotTime)
            findNavController().navigate(R.id.action_fragmentChooseDateTime_to_fragmentVerifyBooking)
        } else {
            val timeStr = selectedWindow?.let { "${it.name} (${it.timeRange})" }
            viewModel.setSelectedDateTime(dateCal, timeStr)
            findNavController().navigate(R.id.action_fragmentChooseDateTime_to_fragmentSelectAddress)
        }
    }
}
