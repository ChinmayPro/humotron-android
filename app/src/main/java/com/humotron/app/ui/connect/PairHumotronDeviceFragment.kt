package com.humotron.app.ui.connect

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentPairHumotronDeviceBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.ui.connect.adapter.DeviceInfo
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PairHumotronDeviceFragment : BaseFragment(R.layout.fragment_pair_humotron_device) {

    private lateinit var binding: FragmentPairHumotronDeviceBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPairHumotronDeviceBinding.bind(view)
        initViews()
        initClicks()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.pair_humotron_devices_title)
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.mcvRing.setOnClickListener {
            findNavController().navigate(
                R.id.fragmentHumotronDeviceConnectInstruction,
                Bundle().apply {
                    putSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType.RING)
                }
            )
        }

        binding.mcvBand.setOnClickListener {
            findNavController().navigate(
                R.id.fragmentHumotronDeviceConnectInstruction,
                Bundle().apply {
                    putSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType.BAND)
                }
            )
        }

        binding.mcvScale.setOnClickListener {
            findNavController().navigate(
                R.id.fragmentHumotronDeviceConnectInstruction,
                Bundle().apply {
                    putSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType.WEIGHT_MACHINE)
                }
            )
        }

        binding.mcvCuff.setOnClickListener {
            findNavController().navigate(
                R.id.fragmentHumotronDeviceConnectInstruction,
                Bundle().apply {
                    putSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType.SMART_CUFF)
                }
            )
            /*openConnectInfo(
                DeviceInfo(
                    R.drawable.ic_smart_cuff,
                    getString(R.string.pair_humotron_cuff_title),
                    getString(R.string.pair_humotron_cuff_desc),
                    DeviceType.BP_MACHINE
                )
            )*/
        }
    }

    private fun openConnectInfo(deviceInfo: DeviceInfo) {
        findNavController().navigate(
            R.id.fragmentConnectInfo,
            Bundle().apply {
                putParcelable(NavKeys.DEVICE_INFO, deviceInfo)
            }
        )
    }
}
