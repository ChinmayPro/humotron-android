package com.humotron.app.ui.connect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.core.App
import com.humotron.app.databinding.FragmentDeviceConnectedBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.param.DeviceMetaDataParam
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeviceConnectedFragment : Fragment(R.layout.fragment_device_connected) {

    private lateinit var binding: FragmentDeviceConnectedBinding
    private val viewModel by activityViewModels<HomeViewModel>()
    private val args: DeviceConnectedFragmentArgs by navArgs()

    private val app by lazy { requireActivity().application as App }

    companion object {
        private const val TAG = "DCF"
        var device: RingBleDevice? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeviceConnectedBinding.bind(view)

        viewModel.currBtMac = device?.device?.address ?: ""
        val metaData = args.deviceMetaData
        if (metaData != null) {
            viewModel.addDeviceMetaData(metaData)
        }
        val deviceType = args.deviceType
        if (deviceType != null) {
            // Set the correct icon based on device type
            when (deviceType) {
                DeviceType.WEIGHT_MACHINE -> {
                    binding.ivDevice.setImageResource(R.drawable.ic_connect_weightscale_logo)
                }
                DeviceType.RING -> {
                    binding.ivDevice.setImageResource(R.drawable.ic_ring_horizontal_2x)
                }
                DeviceType.BAND -> {
                    binding.ivDevice.setImageResource(R.drawable.ic_smart_band)
                }
                else -> {
                    binding.ivDevice.setImageResource(R.drawable.ic_ring_horizontal_2x)
                }
            }
        }

        //viewModel.currBtMac = metaData?.data?.mac ?: device?.device?.address ?: ""


        //viewModel.loadDateData()
        binding.btnSubmit.setOnClickListener {
            //findNavController().navigate(R.id.fragmentDeviceData)
            //This removes everything including TrackFragment, Then it recreates TrackFragment fresh
            /*findNavController().navigate(
                R.id.fragmentTrack,
                null,
                NavOptions.Builder()
                    .setPopUpTo(R.id.fragmentTrack, true) // clear everything up to TrackFragment
                    .build()
            )*/
            //If you want to go back to the existing TrackFragment (not recreate it)
            findNavController().popBackStack(R.id.fragmentTrack, false)
        }
    }
}