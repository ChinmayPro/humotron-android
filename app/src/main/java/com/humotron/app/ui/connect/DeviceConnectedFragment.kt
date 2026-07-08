package com.humotron.app.ui.connect

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.core.App
import com.humotron.app.databinding.FragmentDeviceConnectedBinding
import com.humotron.app.domain.modal.DeviceType
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        viewModel.currBtMac = device?.device?.address ?: ""
        val metaData = args.deviceMetaData
        if (metaData != null) {
            viewModel.addDeviceMetaData(metaData)
        }
        val deviceType = args.deviceType
        if (deviceType != null) {
            when (deviceType) {
                DeviceType.WEIGHT_MACHINE -> {
                    //binding.ivDevice.setImageResource(R.drawable.ic_smart_scale_vector)
                    binding.tvTitle.text = getString(R.string.dvc_connected, "Smart Scale")
                    binding.tvDesc.text = getString(R.string.it_s_paired_and_feeding_track_scale)
                    setColor(
                        ContextCompat.getColor(requireActivity(), R.color.cool),
                        ContextCompat.getColor(requireActivity(), R.color.cool_40)
                    )
                }

                DeviceType.RING -> {
                    //binding.ivDevice.setImageResource(R.drawable.ic_ring_vector)
                    binding.tvTitle.text = getString(R.string.dvc_connected, "Ring")
                    binding.tvDesc.text = getString(R.string.it_s_paired_and_feeding_track_ring)
                    setColor(
                        ContextCompat.getColor(requireActivity(), R.color.lime),
                        ContextCompat.getColor(requireActivity(), R.color.lime_40)
                    )
                }

                DeviceType.BAND -> {
                    //binding.ivDevice.setImageResource(R.drawable.ic_band_vectr)
                    binding.tvTitle.text = getString(R.string.dvc_connected, "Wrist Band")
                    binding.tvDesc.text = getString(R.string.it_s_paired_and_feeding_track_band)
                    setColor(
                        ContextCompat.getColor(requireActivity(), R.color.good),
                        ContextCompat.getColor(requireActivity(), R.color.good_40)
                    )
                }

                DeviceType.BP_MACHINE -> {
                    //binding.ivDevice.setImageResource(R.drawable.ic_smart_cuff_vector)
                    binding.tvTitle.text = getString(R.string.dvc_connected, "Smart Cuff")
                    binding.tvDesc.text = getString(R.string.it_s_paired_and_feeding_track_scale)
                    setColor(
                        ContextCompat.getColor(requireActivity(), R.color.series),
                        ContextCompat.getColor(requireActivity(), R.color.series_40)
                    )
                }

                DeviceType.UNKNOWN -> {

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

    fun setColor(@ColorInt color: Int, @ColorInt strokeColor: Int) {
        binding.mcvSuccessIcon.strokeColor = strokeColor
        binding.ivDevice.imageTintList = ColorStateList.valueOf(color)
        binding.tvTitle.setTextColor(color)
    }
}