package com.humotron.app.ui.device.scale

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSmartScaleReadingInstructionsBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmartScaleReadingInstructionsFragment :
    BaseFragment(R.layout.fragment_smart_scale_reading_instructions),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartScaleReadingInstructionsBinding
    private var userDevice: UserDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartScaleReadingInstructionsBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.tvTitle.text = getString(R.string.take_a_reading)
        binding.header.btnBack.setOnClickListener(this@SmartScaleReadingInstructionsFragment)
        binding.btnPastScans.setOnClickListener(this@SmartScaleReadingInstructionsFragment)
        binding.btnStartReading.setOnClickListener(this@SmartScaleReadingInstructionsFragment)
    }

    private fun initViews() = with(binding) {
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }
        val deviceType = DeviceType.from(userDevice?.deviceName)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
            binding.btnPastScans -> {
                /*findNavController().navigate(
                    R.id.fragmentPastScans,
                    bundleOf(
                        NavKeys.HEALTH_SCAN_ITEM to healthScanItem,
                        NavKeys.WEARABLE to userDevice
                    )
                )*/
            }

            binding.btnStartReading -> {
                findNavController().navigate(
                    R.id.action_fragmentSmartScaleReadingInstructions_to_fragmentWeightScaleReading,
                    bundleOf(NavKeys.WEARABLE to userDevice)
                )
            }
        }
    }
}
