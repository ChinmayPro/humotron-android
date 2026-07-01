package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentWeatherOverviewBinding
import com.humotron.app.domain.modal.response.WeatherOverviewData
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeWeatherOverviewFragment : BaseFragment(R.layout.fragment_weather_overview) {

    private lateinit var binding: FragmentWeatherOverviewBinding
    private val viewModel: DecodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWeatherOverviewBinding.bind(view)

        initClicks()
        initObservers()
        
        viewModel.getWeatherOverview()
    }

    private fun initClicks() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initObservers() {
        viewModel.weatherOverviewData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.shimmerWeatherOverview.visibility = View.VISIBLE
                    binding.llPairings.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding.shimmerWeatherOverview.visibility = View.GONE
                    binding.llPairings.visibility = View.VISIBLE
                    resource.data?.data?.let { dataList ->
                        populatePairings(dataList)
                    }
                }
                else -> {
                    binding.shimmerWeatherOverview.visibility = View.GONE
                    binding.llPairings.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun populatePairings(pairings: List<WeatherOverviewData>) {
        val container = binding.llPairings
        container.removeAllViews()

        for ((index, pairing) in pairings.withIndex()) {
            val itemView = layoutInflater.inflate(R.layout.item_weather_pairing, container, false)
            
            val tvPair = itemView.findViewById<android.widget.TextView>(R.id.tvPair)
            val badgeScore = itemView.findViewById<android.widget.TextView>(R.id.badgeScore)
            val tvWeatherTitle = itemView.findViewById<android.widget.TextView>(R.id.tvWeatherTitle)
            val tvWeatherDesc = itemView.findViewById<android.widget.TextView>(R.id.tvWeatherDesc)
            
            val clActionReady = itemView.findViewById<View>(R.id.clActionReady)
            val btnGenerate = itemView.findViewById<View>(R.id.btnGenerate)
            val llCollectProgress = itemView.findViewById<View>(R.id.llCollectProgress)
            val tvActionNext = itemView.findViewById<View>(R.id.tvActionNext)
            
            val hasReports = !pairing.recentReports.isNullOrEmpty()
            val isEligible = pairing.isEligible == true
            val recentReport = pairing.recentReports?.firstOrNull()

            tvPair.text = pairing.title ?: pairing.key?.replace("_", " ")
            
            tvWeatherTitle.text = recentReport?.headline ?: pairing.subTitle ?: ""

            if (hasReports) {
                // State 2 and 3: Has past reports
                val fallbackScore = recentReport?.impactScore?.let { "$it%" } ?: "UPDATED"
                val fallbackZone = recentReport?.impactLabel ?: ""
                
                val scoreStr = recentReport?.score ?: fallbackScore
                val zoneStr = recentReport?.zone ?: fallbackZone
                val badgeText = if (zoneStr.isNotEmpty() && scoreStr != "UPDATED") "$scoreStr · $zoneStr" else scoreStr
                
                badgeScore.setBackgroundResource(R.drawable.bg_deep_dives_badge_ready)
                badgeScore.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.deep_dives_lime))
                badgeScore.text = badgeText
                
                val rawDate = recentReport?.createdAt ?: ""
                val formattedDate = try {
                    if (rawDate.contains("T")) {
                        val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                        val formatter = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                        val date = parser.parse(rawDate)
                        date?.let { formatter.format(it) } ?: rawDate
                    } else {
                        rawDate
                    }
                } catch (e: Exception) {
                    rawDate
                }
                tvWeatherDesc.text = if (formattedDate.isNotEmpty()) "Generated $formattedDate" else "Generated recently"
                
                clActionReady.visibility = View.VISIBLE
                itemView.findViewById<View>(R.id.tvActionView).visibility = View.VISIBLE
                btnGenerate.visibility = View.GONE
                
                if (isEligible) {
                    // State 3: Ready to generate next
                    tvActionNext.visibility = View.VISIBLE
                    llCollectProgress.visibility = View.GONE
                } else {
                    // State 2: Collecting for next
                    tvActionNext.visibility = View.GONE
                    llCollectProgress.visibility = View.VISIBLE
                    
                    val collected = pairing.validDayCount ?: 0
                    val total = pairing.totalDayCount ?: 180
                    val tvCollectCount = itemView.findViewById<android.widget.TextView>(R.id.tvCollectCount)
                    val vCollectProgress = itemView.findViewById<View>(R.id.vCollectProgress)
                    
                    tvCollectCount.text = "Next · $collected / $total days"
                    val pct = if (total > 0) (collected.toFloat() / total) * 100f else 0f
                    
                    vCollectProgress.post {
                        val parentView = vCollectProgress.parent as View
                        val parentWidth = parentView.width
                        val lp = vCollectProgress.layoutParams
                        lp.width = (parentWidth * (pct / 100f)).toInt()
                        vCollectProgress.layoutParams = lp
                    }
                }
            } else if (isEligible) {
                // State 1: Data ready (first time)
                badgeScore.setBackgroundResource(R.drawable.bg_deep_dives_badge_gen)
                badgeScore.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.deep_dives_watch))
                badgeScore.text = "DATA READY"
                
                tvWeatherDesc.text = "Enough paired data to compose your first report."
                
                clActionReady.visibility = View.GONE
                btnGenerate.visibility = View.VISIBLE
            } else {
                // State 4: Not ready (first time, collecting)
                badgeScore.setBackgroundResource(R.drawable.bg_deep_dives_badge_ready)
                badgeScore.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.deep_dives_lime))
                badgeScore.text = "COLLECTING"
                
                tvWeatherDesc.text = pairing.description
                
                clActionReady.visibility = View.GONE
                btnGenerate.visibility = View.GONE
            }

            // Click listener for the card/buttons
            val clickListener = View.OnClickListener {
                if (hasReports) {
                    val navArgs = Bundle().apply {
                        putString("weatherId", pairing.recentReports?.firstOrNull()?.id ?: "")
                    }
                    findNavController().navigate(R.id.action_fragmentWeatherOverview_to_fragmentWeatherDetail, navArgs)
                } else if (isEligible) {
                    android.widget.Toast.makeText(requireContext(), "Generating report...", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Still collecting data...", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            itemView.setOnClickListener(clickListener)
            itemView.findViewById<View>(R.id.tvActionView).setOnClickListener(clickListener)
            btnGenerate.setOnClickListener(clickListener)

            container.addView(itemView)
        }
    }
}
