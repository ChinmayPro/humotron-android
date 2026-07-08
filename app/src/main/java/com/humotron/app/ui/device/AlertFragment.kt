package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentAlertBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlertFragment :
    BaseFragment(R.layout.fragment_alert),
    View.OnClickListener {

    private lateinit var binding: FragmentAlertBinding
    private var userDevice: UserDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAlertBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@AlertFragment)
        binding.header.tvTitle.text = getString(R.string.alerts)
    }

    private fun initViews() = with(binding) {
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        userDevice?.let {
            binding.tvSubTitle.text =
                getString(R.string.choose_which_readings_from_humotron_, it.deviceFacingName)

            val deviceType = DeviceType.from(userDevice?.deviceName)
            when (deviceType) {
                DeviceType.BAND -> {
                    manageVisibility(binding.llBand)
                }

                DeviceType.RING -> {
                    manageVisibility(binding.llRing)
                }

                DeviceType.BP_MACHINE -> {
                    manageVisibility(binding.llSmartCuff)
                }

                DeviceType.WEIGHT_MACHINE -> {
                    manageVisibility(binding.llSmartScale)
                }

                DeviceType.UNKNOWN -> {

                }
            }
        }
    }

    fun manageVisibility(view: View) {
        binding.llBand.isVisible = false
        binding.llRing.isVisible = false
        binding.llSmartScale.isVisible = false
        binding.llSmartCuff.isVisible = false

        view.isVisible = true
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
        }
    }
}
