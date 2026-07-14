package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentReadingTypesRingBandBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.device.adapter.HealthScanAdapter
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReadingTypesRingBandFragment :
    BaseFragment(R.layout.fragment_reading_types_ring_band),
    View.OnClickListener {

    private lateinit var binding: FragmentReadingTypesRingBandBinding
    private var userDevice: UserDevice? = null
    private lateinit var healthScanAdapter: HealthScanAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentReadingTypesRingBandBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@ReadingTypesRingBandFragment)
    }

    private fun initViews() = with(binding) {
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        header.tvTitle.text = getString(R.string.take_a_reading)

        val resolvedType = DeviceType.from( userDevice?.deviceName)
        val deviceLabel = if (resolvedType == DeviceType.RING) {
            getString(R.string.take_reading_device_ring)
        } else {
            getString(R.string.take_reading_device_band)
        }

        tvDeviceMeta.text = getString(R.string.take_reading_on_device_meta, deviceLabel)
        tvTitle2.text = getString(R.string.take_reading_check_title)
        tvSubtitle.text = getString(R.string.take_reading_ring_band_subtitle)

        setupHealthScanAdapter()
    }

    private fun setupHealthScanAdapter() {
        healthScanAdapter = HealthScanAdapter(
            onScanNowClick = { item ->
                findNavController().navigate(
                    R.id.fragmentReadingInstructionsRingBand,
                    bundleOf(
                        NavKeys.HEALTH_SCAN_ITEM to item,
                        NavKeys.WEARABLE to userDevice
                    )
                )
            },
            onPastScansClick = { item ->
                findNavController().navigate(
                    R.id.fragmentPastScans,
                    bundleOf(
                        NavKeys.HEALTH_SCAN_ITEM to item,
                        NavKeys.WEARABLE to userDevice
                    )
                )
            }
        )
        binding.rvScans.adapter = healthScanAdapter

        val healthScans = listOf(
            HealthScanItem(
                "Stress Scan",
                "Stress",
                "How wound up are you, right now?",
                HealthScanType.HRV,
                "Stress"
            ),
            HealthScanItem(
                "Body Load Scan",
                "Body Load",
                "Is your body working harder than it should, right now?",
                HealthScanType.HR,
                "Heart Rate"
            ),
            HealthScanItem(
                "Oxygen Check",
                "Oxygen",
                "Is your blood well-oxygenated, right now?",
                HealthScanType.SPO2,
                "Oxygen"
            ),
            HealthScanItem(
                "Thermal Scan",
                "Thermal",
                "Is your body temperature drifting from your normal?",
                HealthScanType.TEMPERATURE,
                "Temperature"
            )
        )
        healthScanAdapter.submitList(healthScans)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
        }
    }
}
