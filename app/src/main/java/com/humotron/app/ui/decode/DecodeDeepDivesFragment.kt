package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentDecodeDeepDivesBinding
import dagger.hilt.android.AndroidEntryPoint

import android.graphics.Color
import androidx.fragment.app.viewModels
import com.humotron.app.data.network.Status
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel

@AndroidEntryPoint
class DecodeDeepDivesFragment : BaseFragment(R.layout.fragment_decode_deep_dives) {

    private lateinit var binding: FragmentDecodeDeepDivesBinding
    private val viewModel: DecodeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeDeepDivesBinding.bind(view)

        initClicks()
        initViews()
        initObservers()
    }

    private fun initViews() {
        viewModel.getWorkDayStress()
        viewModel.getWeatherResilienceOverview()
    }

    private fun initClicks() {
        binding.cardWorkdayStress.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentDecode_to_fragmentWorkdayReport)
        }

        binding.cardWeatherResilience.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentDecode_to_fragmentWeatherOverview)
        }
    }

    private var isWorkdayStressLoading = true
    private var isWeatherResilienceLoading = true

    private fun checkLoadingState() {
        if (isWorkdayStressLoading || isWeatherResilienceLoading) {
            binding.shimmerDeepDives.visibility = View.VISIBLE
            binding.llContent.visibility = View.GONE
        } else {
            binding.shimmerDeepDives.visibility = View.GONE
            binding.llContent.visibility = View.VISIBLE
        }
    }

    private fun initObservers() {
        viewModel.workDayStressData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    isWorkdayStressLoading = true
                    checkLoadingState()
                }
                Status.SUCCESS -> {
                    isWorkdayStressLoading = false
                    checkLoadingState()
                    resource.data?.data?.let { data ->
                        binding.tvWorkdayLevel.text = data.workdayStressLabel ?: ""
                        data.workdayStressColor?.let { colorStr ->
                            try {
                                binding.tvWorkdayLevel.setTextColor(Color.parseColor(colorStr))
                            } catch (e: Exception) {
                                // Ignore if invalid color
                            }
                        }
                        
                        val timeStr = data.peakStress?.time12h ?: ""
                        val meetingStr = data.peakStress?.duringMeeting ?: ""
                        val desc = "⏱ Peak stress $timeStr · \uD83D\uDCC5 $meetingStr"
                        binding.tvWorkdayDesc.text = desc
                    }
                }
                else -> {
                    isWorkdayStressLoading = false
                    checkLoadingState()
                }
            }
        }

        viewModel.weatherResilienceData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    isWeatherResilienceLoading = true
                    checkLoadingState()
                }
                Status.SUCCESS -> {
                    isWeatherResilienceLoading = false
                    checkLoadingState()
                    resource.data?.data?.let { data ->
                        binding.tvWeatherScore.text = "${data.averageScore ?: 0.0}%"
                        if (data.selectedDate != null) {
                            binding.tvWeatherDesc.text = "Generated ${data.selectedDate} · next report collecting data"
                        }
                    }
                }
                else -> {
                    isWeatherResilienceLoading = false
                    checkLoadingState()
                }
            }
        }
    }
}
