package com.humotron.app.ui.device.cuff

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSmartCuffReadingTypesBinding
import com.humotron.app.domain.modal.BPMachineReadingType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmartCuffReadingTypesFragment :
    BaseFragment(R.layout.fragment_smart_cuff_reading_types),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartCuffReadingTypesBinding
    private var userDevice: UserDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartCuffReadingTypesBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@SmartCuffReadingTypesFragment)
        binding.mcvBP.setOnClickListener(this@SmartCuffReadingTypesFragment)
        binding.mcvEcg.setOnClickListener(this@SmartCuffReadingTypesFragment)
    }

    private fun initViews() = with(binding) {
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        header.tvTitle.text = getString(R.string.take_a_reading)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
            binding.mcvBP -> {
                findNavController().navigate(
                    R.id.fragmentSmartCuffReadingInstructions,
                    Bundle().apply {
                        putParcelable(NavKeys.WEARABLE, userDevice)
                        putParcelable(NavKeys.READING_TYPE, BPMachineReadingType.BLOOD_PRESSURE)
                    }
                )
            }

            binding.mcvEcg -> {
                findNavController().navigate(
                    R.id.fragmentSmartCuffReadingInstructions,
                    Bundle().apply {
                        putParcelable(NavKeys.WEARABLE, userDevice)
                        putParcelable(NavKeys.READING_TYPE, BPMachineReadingType.ECG)
                    }
                )
            }
        }
    }
}
