package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDataSourcesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataSourcesFragment : BaseFragment(R.layout.fragment_data_sources) {

    private lateinit var binding: FragmentDataSourcesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDataSourcesBinding.bind(view)

        // Header Title
        binding.header.title.text = "Data Sources"

        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Bold the assure bar text to match HTML
        binding.tvAssureText.text = android.text.Html.fromHtml(
            "Your data may live in the cloud, but the controls live with you. <b>Pause, exclude or delete</b> anything, any time — and we\'ll never share it.",
            android.text.Html.FROM_HTML_MODE_LEGACY
        )

        // Setup individual items based on HTML mockup data
        
        // External
        setupItem(binding.dsAppleWatch, R.drawable.ic_alarm_24px, "#EE4D3D", "Apple Watch", "Activity, heart rate & workouts", "Connected", true)
        setupItem(binding.dsWhoop, R.drawable.ic_start_stress, "#5FB7C4", "Whoop", "Strain, recovery & sleep", "Connected", true)
        setupItem(binding.dsGarmin, R.drawable.ic_wrist_band, "#9b87f5", "Garmin", "Training load & GPS activity", "Connected", true)

        // Digital
        setupItem(binding.dsEnv, R.drawable.ic_weather, "#5FB7C4", "Environmental Context", "Weather & environmental correlations", "Active", true)
        setupItem(binding.dsCalendar, R.drawable.ic_appointments, "#9b87f5", "Calendar & Workday", "Schedule-based stress & recovery", "Connected", true)

        // Imported
        setupItem(binding.dsReports, R.drawable.ic_sheet_document, "#9b87f5", "Uploaded Reports", "PDFs, scans & imported records", "12 reports", false)
        setupItem(binding.dsDeepScan, R.drawable.ic_spark, "#C4F23E", "Deep Scan", "Full-body scan results & history", "2 scans", false)

        // Assessments
        setupItem(binding.dsLifestyle, R.drawable.ic_menu_24px, "#5FB7C4", "Lifestyle Assessment", "Diet, sleep, activity & habits", "Updated 12 Jun", false)
        setupItem(binding.dsHealth, R.drawable.ic_onboard_heart, "#EE4D3D", "Health History", "Conditions, medications & family history", "Updated 2 May", false)
        setupItem(binding.dsGoals, R.drawable.ic_target, "#C4F23E", "Goals & Symptoms", "What you're tracking and why", "Updated 18 Jun", false)

        // Add Click listeners for navigation to detail screens
        val navigateToSourceDetail: (String, String, Int, String, String, String) -> Unit = { id, name, iconRes, desc, color, status ->
            val action = DataSourcesFragmentDirections.actionFragmentDataSourcesToFragmentDataSourceDetail(
                sourceId = id,
                sourceName = name,
                sourceIcon = iconRes,
                sourceDesc = desc,
                sourceColor = color,
                sourceStatus = status
            )
            findNavController().navigate(action)
        }

        binding.dsAppleWatch.root.setOnClickListener {
            navigateToSourceDetail("applewatch", "Apple Watch", R.drawable.ic_alarm_24px, "Activity, heart rate & workouts", "#EE4D3D", "Connected")
        }
        binding.dsWhoop.root.setOnClickListener {
            navigateToSourceDetail("whoop", "Whoop", R.drawable.ic_start_stress, "Strain, recovery & sleep", "#5FB7C4", "Connected")
        }
        binding.dsGarmin.root.setOnClickListener {
            navigateToSourceDetail("garmin", "Garmin", R.drawable.ic_wrist_band, "Training load & GPS activity", "#9b87f5", "Connected")
        }
        binding.dsEnv.root.setOnClickListener {
            navigateToSourceDetail("environment", "Environmental Context", R.drawable.ic_weather, "Weather & environmental correlations", "#5FB7C4", "Active")
        }
        binding.dsCalendar.root.setOnClickListener {
            navigateToSourceDetail("calendar", "Calendar & Workday", R.drawable.ic_appointments, "Schedule-based stress & recovery", "#9b87f5", "Connected")
        }
        binding.dsDeepScan.root.setOnClickListener {
            navigateToSourceDetail("deepscan", "Deep Scan", R.drawable.ic_spark, "Full-body scan results & history", "#C4F23E", "2 scans")
        }

        binding.dsReports.root.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentDataSources_to_fragmentUploadedReports)
        }

        val navigateToAssessmentDetail: (String, String, Int, String, String, String) -> Unit = { id, name, iconRes, desc, color, status ->
            val action = DataSourcesFragmentDirections.actionFragmentDataSourcesToFragmentAssessmentDetail(
                assessmentId = id,
                assessmentName = name,
                assessmentIcon = iconRes,
                assessmentDesc = desc,
                assessmentColor = color,
                assessmentDate = status
            )
            findNavController().navigate(action)
        }

        binding.dsLifestyle.root.setOnClickListener {
            navigateToAssessmentDetail("lifestyle", "Lifestyle Assessment", R.drawable.ic_menu_24px, "Diet, sleep, activity & habits", "#5FB7C4", "Updated 12 Jun")
        }
        binding.dsHealth.root.setOnClickListener {
            navigateToAssessmentDetail("health_history", "Health History", R.drawable.ic_onboard_heart, "Conditions, medications & family history", "#EE4D3D", "Updated 2 May")
        }
        binding.dsGoals.root.setOnClickListener {
            navigateToAssessmentDetail("goals_symptoms", "Goals & Symptoms", R.drawable.ic_target, "What you're tracking and why", "#C4F23E", "Updated 18 Jun")
        }
    }

    private fun setupItem(
        itemBinding: com.humotron.app.databinding.ItemDataSourceBinding,
        iconRes: Int,
        colorHex: String,
        title: String,
        desc: String,
        status: String,
        showDot: Boolean
    ) {
        val colorInt = android.graphics.Color.parseColor(colorHex)
        itemBinding.ivDeviceIcon.setImageResource(iconRes)
        itemBinding.ivDeviceIcon.imageTintList = android.content.res.ColorStateList.valueOf(colorInt)
        
        // 22% opacity background for the icon box
        val bgTint = android.graphics.Color.argb(
            (255 * 0.22).toInt(),
            android.graphics.Color.red(colorInt),
            android.graphics.Color.green(colorInt),
            android.graphics.Color.blue(colorInt)
        )
        itemBinding.llDeviceIcon.backgroundTintList = android.content.res.ColorStateList.valueOf(bgTint)

        itemBinding.tvDeviceName.text = title
        itemBinding.tvDeviceDesc.text = desc
        itemBinding.tvStatus.text = status

        if (showDot) {
            itemBinding.vStatusDot.visibility = View.VISIBLE
            itemBinding.tvStatus.setTextColor(android.graphics.Color.parseColor("#C4F23E")) // lime_green
        } else {
            itemBinding.vStatusDot.visibility = View.GONE
            itemBinding.tvStatus.setTextColor(android.graphics.Color.parseColor("#A0B3AF")) // grey
        }
    }
}
