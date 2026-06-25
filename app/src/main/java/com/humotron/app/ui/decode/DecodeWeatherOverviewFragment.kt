package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentWeatherOverviewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeWeatherOverviewFragment : BaseFragment(R.layout.fragment_weather_overview) {

    private lateinit var binding: FragmentWeatherOverviewBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWeatherOverviewBinding.bind(view)

        initClicks()
        populatePairings()
    }

    private fun initClicks() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun populatePairings() {
        val container = binding.llPairings
        container.removeAllViews()

        val pairings = com.humotron.app.ui.decode.data.DeepDivesMockData.WEATHER_PAIRINGS

        for (pairing in pairings) {
            val itemView = layoutInflater.inflate(R.layout.item_weather_pairing, container, false)
            
            val tvPair = itemView.findViewById<android.widget.TextView>(R.id.tvPair)
            val badgeScore = itemView.findViewById<android.widget.TextView>(R.id.badgeScore)
            val tvWeatherTitle = itemView.findViewById<android.widget.TextView>(R.id.tvWeatherTitle)
            val tvWeatherDesc = itemView.findViewById<android.widget.TextView>(R.id.tvWeatherDesc)
            
            val clActionReady = itemView.findViewById<View>(R.id.clActionReady)
            val btnGenerate = itemView.findViewById<View>(R.id.btnGenerate)
            val llCollectProgress = itemView.findViewById<View>(R.id.llCollectProgress)
            val tvActionNext = itemView.findViewById<View>(R.id.tvActionNext)
            val tvCollectCount = itemView.findViewById<android.widget.TextView>(R.id.tvCollectCount)
            val vCollectProgress = itemView.findViewById<View>(R.id.vCollectProgress)

            tvPair.text = pairing.pair
            tvWeatherTitle.text = pairing.title

            when (pairing.cstate) {
                1 -> {
                    // Generate state
                    badgeScore.setBackgroundResource(R.drawable.bg_deep_dives_badge_gen)
                    badgeScore.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.deep_dives_watch))
                    badgeScore.text = "DATA READY"
                    
                    tvWeatherDesc.text = "Enough paired data to compose your first report."
                    
                    clActionReady.visibility = View.GONE
                    btnGenerate.visibility = View.VISIBLE
                }
                2 -> {
                    // Collecting state
                    badgeScore.setBackgroundResource(R.drawable.bg_deep_dives_badge_ready)
                    badgeScore.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.deep_dives_lime))
                    badgeScore.text = "${pairing.score} · ${pairing.zone}"
                    
                    tvWeatherDesc.text = pairing.date
                    
                    clActionReady.visibility = View.VISIBLE
                    btnGenerate.visibility = View.GONE
                    tvActionNext.visibility = View.GONE
                    llCollectProgress.visibility = View.VISIBLE
                    
                    tvCollectCount.text = "Next · ${pairing.collectCount}"
                    
                    val pct = pairing.collectPct ?: 0
                    vCollectProgress.post {
                        val parent = vCollectProgress.parent as android.widget.FrameLayout
                        val lp = vCollectProgress.layoutParams
                        lp.width = (parent.width * (pct / 100f)).toInt()
                        vCollectProgress.layoutParams = lp
                    }
                }
                3 -> {
                    // Ready state
                    badgeScore.setBackgroundResource(R.drawable.bg_deep_dives_badge_ready)
                    badgeScore.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.deep_dives_lime))
                    badgeScore.text = "${pairing.score} · ${pairing.zone}"
                    
                    tvWeatherDesc.text = pairing.date
                    
                    clActionReady.visibility = View.VISIBLE
                    btnGenerate.visibility = View.GONE
                    tvActionNext.visibility = View.VISIBLE
                    llCollectProgress.visibility = View.GONE
                }
            }

            // Click listener for the card/buttons
            val navArgs = Bundle().apply {
                putString("weatherId", pairing.id)
            }
            val clickListener = View.OnClickListener {
                findNavController().navigate(R.id.action_fragmentWeatherOverview_to_fragmentWeatherDetail, navArgs)
            }

            itemView.setOnClickListener(clickListener)
            itemView.findViewById<View>(R.id.tvActionView).setOnClickListener(clickListener)
            btnGenerate.setOnClickListener(clickListener)

            container.addView(itemView)
        }
    }
}
