package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentRealTimeScanRingBandBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RealTimeScanRingBandFragment :
    BaseFragment(R.layout.fragment_real_time_scan_ring_band),
    View.OnClickListener {

    private lateinit var binding: FragmentRealTimeScanRingBandBinding
    private var userDevice: UserDevice? = null
    private var healthScanItem: HealthScanItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRealTimeScanRingBandBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.btnStopScan.setOnClickListener(this@RealTimeScanRingBandFragment)


    }

    private fun initViews() = with(binding) {
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        healthScanItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM, HealthScanItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM)
        }

        healthScanItem?.let {
            when (it.type) {
                HealthScanType.HRV -> {
                    binding.scanAnimationView.setScanData("0", "ms", "HRV")
                    binding.scanAnimationView.setScanColor(R.color.series)
                }

                HealthScanType.HR -> {
                    binding.scanAnimationView.setScanData("0", "bpm", "HEART RATE")
                    binding.scanAnimationView.setScanColor(R.color.watch)
                }

                HealthScanType.SPO2 -> {
                    binding.scanAnimationView.setScanData("0", "%", "SPO2")
                    binding.scanAnimationView.setScanColor(R.color.cool)
                }

                HealthScanType.TEMPERATURE -> {
                    binding.scanAnimationView.setScanData("0", "°C", "TEMPERATURE")
                    binding.scanAnimationView.setScanColor(R.color.series)
                }
            }
        }

        val deviceType = DeviceType.from(userDevice?.deviceName)
        //startOneMinuteScan()

        // Start a continuous 60-second (60,000 milliseconds) animation to 100%
        binding.scanAnimationView.animateProgressContinuously(durationMillis = 60_000L)
    }

    override fun onClick(v: View?) {
        when (v) {
            //binding.btnStopScan -> findNavController().popBackStack()
            binding.btnStopScan -> {
                findNavController().navigate(
                    R.id.fragmentScanReportRingBand,
                    bundleOf(
                        NavKeys.HEALTH_SCAN_ITEM to healthScanItem,
                        NavKeys.WEARABLE to userDevice
                    )
                )
            }
        }
    }
}
