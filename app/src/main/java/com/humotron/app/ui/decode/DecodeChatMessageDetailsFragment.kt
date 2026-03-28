package com.humotron.app.ui.decode

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDecodeChatMessageDetailsBinding
import com.humotron.app.databinding.ItemAssessmentRowBinding
import com.humotron.app.domain.modal.response.ConversationData
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeChatMessageDetailsFragment : DialogFragment() {

    private var _binding: FragmentDecodeChatMessageDetailsBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DecodeViewModel by viewModels()

    override fun getTheme(): Int = R.style.FullScreenDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDecodeChatMessageDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val item = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_CONVERSATION, ConversationData::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_CONVERSATION)
        }
        
        item?.let { 
            setupUi(it)
            it.id?.let { id ->
                viewModel.getPromptContextByConversationId(id)
            }
        }
        
        setupObservers()
        
        binding.ivBack.setOnClickListener {
            dismiss()
        }
        
        binding.viewDismiss.setOnClickListener {
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            val screenHeight = displayMetrics.heightPixels
            
            val bottomNavHeight = (42 * resources.displayMetrics.density).toInt()
            
            setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                screenHeight - bottomNavHeight
            )
            setGravity(Gravity.TOP)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            
            // Allow covering status bar
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
            
            // Set status bar icons to white
            WindowCompat.getInsetsController(this, decorView).apply {
                isAppearanceLightStatusBars = false
            }
            
            setWindowAnimations(R.style.FragmentOpenAnimation)
        }
    }

    private fun setupObservers() {
        viewModel.promptContextData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.data?.let { data ->
                        binding.tvDataInfo.text = data.promptTitle
                        
                        // Populate Metric Data
                        data.promptContext?.metrics?.let { metrics ->
                                binding.tvMetricsDataTitle.visibility = View.VISIBLE
                                binding.layoutMetricName.visibility = View.VISIBLE
                                binding.tvMetricNameValue.text = metrics.metricName
                                
                                metrics.dateRange?.let { range ->
                                    binding.layoutDateRange.visibility = View.VISIBLE
                                    binding.tvDateRangeValue.text = formatDateRange(range.startDate, range.endDate)
                                }
                        }

                        // Populate Demographics
                        data.promptContext?.demographics?.let { demo ->
                            if (demo.available == true) {
                                binding.tvDemographicsTitle.visibility = View.VISIBLE
                                
                                demo.age?.let {
                                    binding.layoutAge.visibility = View.VISIBLE
                                    binding.tvAgeValue.text = it.toString()
                                }
                                
                                demo.gender?.let {
                                    binding.layoutGender.visibility = View.VISIBLE
                                    binding.tvGenderValue.text = it
                                }
                                
                                if (!demo.height.isNullOrEmpty()) {
                                    binding.layoutHeight.visibility = View.VISIBLE
                                    binding.tvHeightValue.text = "${demo.height} ${demo.heightUnit ?: ""}"
                                }
                                
                                if (!demo.weight.isNullOrEmpty()) {
                                    binding.layoutWeight.visibility = View.VISIBLE
                                    binding.tvWeightValue.text = "${demo.weight} ${demo.weightUnit ?: ""}"
                                }
                                
                                demo.bmi?.let {
                                    binding.layoutBmi.visibility = View.VISIBLE
                                    binding.tvBmiValue.text = String.format("%.2f", it)
                                }
                            }
                        }

                        // Populate Assessment
                        data.promptContext?.assessment?.let { assessment ->
                            if (assessment.available == true && !assessment.items.isNullOrEmpty()) {
                                binding.tvAssessmentResponsesTitle.visibility = View.VISIBLE
                                binding.layoutAssessmentItems.visibility = View.VISIBLE
                                
                                // Clear previous items if any
                                binding.layoutAssessmentItems.removeAllViews()
                                
                                assessment.items.forEach { item ->
                                    val rowBinding = ItemAssessmentRowBinding.inflate(layoutInflater, binding.layoutAssessmentItems, false)
                                    rowBinding.tvQuestion.text = "Q. ${item.assessmentQuestionName ?: ""}"
                                    rowBinding.tvAnswer.text = "A. ${item.assessmentQuestionAnswer?.joinToString(", ") ?: ""}"
                                    binding.layoutAssessmentItems.addView(rowBinding.root)
                                }
                            } else {
                                binding.tvAssessmentResponsesTitle.visibility = View.GONE
                                binding.layoutAssessmentItems.visibility = View.GONE
                            }
                        }
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    // Handle error if needed
                }
                Status.LOADING -> {
                    // Show loading if needed
                }
            }
        }
    }

    private fun formatDateRange(startDate: String?, endDate: String?): String {
        val start = formatDate(startDate)
        val end = formatDate(endDate)
        return if (start.isNotEmpty() && end.isNotEmpty()) "$start to $end" else ""
    }

    private fun formatDate(dateStr: String?): String {
        if (dateStr == null) return ""
        return try {
            val input = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
            input.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = input.parse(dateStr)
            val output = java.text.SimpleDateFormat("dd MMM yy", java.util.Locale.getDefault())
            output.format(date!!)
        } catch (e: Exception) {
            dateStr
        }
    }

    private fun setupUi(item: ConversationData) {
        binding.tvUserMsg.text = item.userMessage
        binding.tvDate.text = formatToTime(item.createdAt)
    }

    private fun formatToTime(dateStr: String?): String {
        if (dateStr.isNullOrEmpty()) return ""
        val inputFormats = arrayOf(
            "MMM dd, yyyy h:mm a",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'"
        )
        for (format in inputFormats) {
            try {
                val input = java.text.SimpleDateFormat(format, java.util.Locale.getDefault())
                if (format.endsWith("'Z'")) input.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = input.parse(dateStr)
                if (date != null) {
                    return java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(date)
                }
            } catch (e: Exception) { }
        }
        return dateStr
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CONVERSATION = "arg_conversation"

        fun newInstance(item: ConversationData): DecodeChatMessageDetailsFragment {
            return DecodeChatMessageDetailsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_CONVERSATION, item)
                }
            }
        }
    }
}
