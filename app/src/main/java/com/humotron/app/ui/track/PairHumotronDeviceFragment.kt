package com.humotron.app.ui.track

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
            openConnectInfo(
                DeviceInfo(
                    R.drawable.ic_ring,
                    getString(R.string.device_ring),
                    getString(R.string.pair_humotron_ring_desc),
                    DeviceType.RING
                )
            )
        }

        binding.mcvBand.setOnClickListener {
            openConnectInfo(
                DeviceInfo(
                    R.drawable.ic_smart_band,
                    getString(R.string.device_band),
                    getString(R.string.pair_humotron_band_desc),
                    DeviceType.BAND
                )
            )
        }

        binding.mcvScale.setOnClickListener {
            openConnectInfo(
                DeviceInfo(
                    R.drawable.ic_weight_scale,
                    getString(R.string.pair_humotron_scale_title),
                    getString(R.string.pair_humotron_scale_desc),
                    DeviceType.WEIGHT_MACHINE
                )
            )
        }

        binding.mcvCuff.setOnClickListener {
            openConnectInfo(
                DeviceInfo(
                    R.drawable.ic_smart_cuff,
                    getString(R.string.pair_humotron_cuff_title),
                    getString(R.string.pair_humotron_cuff_desc),
                    DeviceType.BP_MACHINE
                )
            )
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
