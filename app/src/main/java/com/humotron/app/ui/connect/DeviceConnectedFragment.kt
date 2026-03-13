package com.humotron.app.ui.connect

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.BleDevice
import com.humotron.app.core.App
import com.humotron.app.databinding.FragmentDeviceConnectedBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DeviceConnectedFragment : Fragment(R.layout.fragment_device_connected) {

    private lateinit var binding: FragmentDeviceConnectedBinding
    private val viewModel by viewModels<HomeViewModel>()
    private val app by lazy { requireActivity().application as App }


    companion object {
        private const val TAG = "DCF"
        var device: BleDevice? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeviceConnectedBinding.bind(view)
        viewModel.currBtMac = device?.device?.address ?: ""
//        viewModel.loadDateData()
        binding.btnSubmit.setOnClickListener {
            findNavController().navigate(R.id.fragmentDeviceData)
        }
    }
}