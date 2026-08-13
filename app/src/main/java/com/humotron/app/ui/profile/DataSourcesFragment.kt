package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDataSourcesBinding
import com.humotron.app.databinding.ItemDataSourceBinding
import com.humotron.app.domain.modal.response.DataSourcesResponse
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DataSourcesFragment : BaseFragment(R.layout.fragment_data_sources) {

    private lateinit var binding: FragmentDataSourcesBinding
    private val viewModel: ProfileViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDataSourcesBinding.bind(view)

        setupInsets()

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

        // Observe API response & fetch fresh data sources so detail screen changes (e.g. Pause/Resume) reflect immediately
        setupObservers()
        viewModel.fetchDataSources()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val extraBottomPadding = (20 * resources.displayMetrics.density).toInt()
            binding.contentScrollView.updatePadding(bottom = systemBars.bottom + extraBottomPadding)
            insets
        }
    }

    private fun setupObservers() {
        viewModel.getDataSourcesLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    showShimmer(false)
                    resource.data?.data?.sections?.let { sections ->
                        bindDataSourcesFromApi(sections)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    showShimmer(false)
                }
                Status.LOADING -> {
                    showShimmer(true)
                }
            }
        }
    }

    private fun showShimmer(show: Boolean) {
        if (show) {
            binding.shimmerView.shimmerView.visibility = View.VISIBLE
            binding.shimmerView.shimmerView.startShimmer()
            binding.contentScrollView.visibility = View.GONE
        } else {
            binding.shimmerView.shimmerView.stopShimmer()
            binding.shimmerView.shimmerView.visibility = View.GONE
            binding.contentScrollView.visibility = View.VISIBLE
        }
    }

    private fun bindDataSourcesFromApi(sections: List<DataSourcesResponse.Section>) {
        val layoutInflater = LayoutInflater.from(requireContext())

        for (section in sections) {
            val container = when (section.section) {
                "EXTERNAL_DEVICE" -> binding.llExternalContainer
                "DIGITAL" -> binding.llDigitalContainer
                "IMPORTED" -> binding.llImportedContainer
                "ASSESSMENT" -> binding.llAssessmentContainer
                else -> null
            } ?: continue

            val sources = section.sources ?: emptyList()
            if (sources.isEmpty()) continue

            container.removeAllViews()

            sources.forEachIndexed { index, source ->
                val itemBinding = ItemDataSourceBinding.inflate(layoutInflater, container, false)

                val iconRes = getIconResForSourceKey(source.sourceKey)
                val colorHex = source.accentColor ?: "#5FB7C4"
                val title = source.name ?: ""
                val desc = source.description ?: ""

                val isPaused = source.isPaused == true || source.status.equals("Paused", ignoreCase = true)
                val isConnected = source.isConnected == true ||
                        source.status.equals("Connected", ignoreCase = true) ||
                        source.status.equals("Active", ignoreCase = true)

                val statusText = when {
                    isPaused -> "Paused"
                    !source.meta.isNullOrEmpty() -> source.meta
                    source.status != null -> source.status
                    isConnected -> "Connected"
                    else -> "Not connected"
                }

                val showDot = isConnected && !isPaused

                setupItem(itemBinding, iconRes, colorHex, title, desc, statusText, showDot, isPaused)

                // Dynamic click handler passing live source data
                itemBinding.root.setOnClickListener {
                    handleSourceClick(source)
                }

                container.addView(itemBinding.root)

                // Add 1dp divider line between items except after the last item
                if (index < sources.size - 1) {
                    val divider = View(requireContext()).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            (1 * resources.displayMetrics.density).toInt()
                        ).apply {
                            setMargins(
                                (16 * resources.displayMetrics.density).toInt(), 0,
                                (16 * resources.displayMetrics.density).toInt(), 0
                            )
                        }
                        setBackgroundColor(android.graphics.Color.parseColor("#1AFFFFFF"))
                    }
                    container.addView(divider)
                }
            }
        }
    }

    private fun handleSourceClick(source: DataSourcesResponse.Source) {
        viewModel.isReturningFromDetail = true

        val id = source.sourceKey ?: ""
        val name = source.name ?: ""
        val iconRes = getIconResForSourceKey(source.sourceKey)
        val desc = source.description ?: ""
        val color = source.accentColor ?: "#5FB7C4"
        val statusText = source.status ?: source.meta ?: "Connected"

        if (source.section == "ASSESSMENT" || id in listOf("lifestyle", "health_history", "goals_symptoms")) {
            val action = DataSourcesFragmentDirections.actionFragmentDataSourcesToFragmentAssessmentDetail(
                assessmentId = id,
                assessmentName = name,
                assessmentIcon = iconRes,
                assessmentDesc = desc,
                assessmentColor = color,
                assessmentDate = statusText
            )
            findNavController().navigate(action)
        } else {
            val action = DataSourcesFragmentDirections.actionFragmentDataSourcesToFragmentDataSourceDetail(
                sourceId = id,
                sourceName = name,
                sourceIcon = iconRes,
                sourceDesc = desc,
                sourceColor = color,
                sourceStatus = statusText
            )
            findNavController().navigate(action)
        }
    }

    private fun getIconResForSourceKey(sourceKey: String?): Int {
        val key = sourceKey?.lowercase() ?: return R.drawable.ic_spark
        return when {
            key.contains("apple") || key.contains("watch") -> R.drawable.ic_smart_band
            key.contains("whoop") -> R.drawable.ic_start_stress
            key.contains("garmin") -> R.drawable.ic_wrist_band
            key.contains("oura") || key.contains("ring") -> R.drawable.ic_wrist_band
            key.contains("polar") -> R.drawable.ic_onboard_heart
            key.contains("strava") -> R.drawable.ic_menu_24px
            key.contains("ultrahuman") -> R.drawable.ic_wrist_band
            key.contains("google") -> R.drawable.ic_scan_node
            key.contains("env") || key.contains("environment") -> R.drawable.ic_weather
            key.contains("calendar") || key.contains("workday") -> R.drawable.ic_appointments
            key.contains("report") -> R.drawable.ic_sheet_document
            key.contains("deep") || key.contains("scan") -> R.drawable.ic_spark
            key.contains("lifestyle") -> R.drawable.ic_menu_24px
            key.contains("health") || key.contains("history") -> R.drawable.ic_onboard_heart
            key.contains("goal") || key.contains("symptom") -> R.drawable.ic_target
            else -> R.drawable.ic_spark
        }
    }

    private fun setupItem(
        itemBinding: ItemDataSourceBinding,
        iconRes: Int,
        colorHex: String,
        title: String,
        desc: String,
        status: String,
        showDot: Boolean,
        isPaused: Boolean = false
    ) {
        val colorInt = try {
            android.graphics.Color.parseColor(colorHex)
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#5FB7C4")
        }
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
