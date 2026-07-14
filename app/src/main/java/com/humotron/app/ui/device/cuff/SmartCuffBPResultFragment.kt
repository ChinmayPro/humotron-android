package com.humotron.app.ui.device.cuff

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSmartCuffBpResultBinding
import com.humotron.app.domain.modal.BPMachineReadingType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SmartCuffBPResultFragment :
    BaseFragment(R.layout.fragment_smart_cuff_bp_result),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartCuffBpResultBinding
    private var userDevice: UserDevice? = null
    private var readingType: BPMachineReadingType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartCuffBpResultBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@SmartCuffBPResultFragment)
        binding.btnDone.setOnClickListener(this@SmartCuffBPResultFragment)
    }

    private fun initViews() = with(binding) {
        header.tvTitle.text = getString(R.string.take_a_reading)
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }
        readingType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.READING_TYPE, BPMachineReadingType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.READING_TYPE)
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
            binding.btnDone -> findNavController().popBackStack(R.id.fragmentTrack, false)
        }
    }
}
