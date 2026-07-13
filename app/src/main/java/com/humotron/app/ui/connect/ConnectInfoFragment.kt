package com.humotron.app.ui.connect

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentConnectInfoBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.ui.connect.adapter.DeviceInfo
import com.humotron.app.ui.navigation.NavKeys


class ConnectInfoFragment : Fragment(R.layout.fragment_connect_info) {

    private lateinit var binding: FragmentConnectInfoBinding
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mDeviceInfo: DeviceInfo? = null

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->

        if (!permissions.entries.map { it.value }.contains(false)) {
            isBluetoothEnabled()
        }
    }

    private val blueToothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                navigateToConnection()
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConnectInfoBinding.bind(view)

        mDeviceInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.DEVICE_INFO, DeviceInfo::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.DEVICE_INFO)
        }

        when (mDeviceInfo?.deviceType) {
            DeviceType.SMART_CUFF -> {
                binding.tvDeviceName.text =
                    getString(R.string.connect_your_bp_machine)
                binding.tvHeader.isVisible = false
                binding.tvContinue.visibility = View.INVISIBLE

                binding.tvDesc.text =
                    getString(R.string.smart_bp_machine_steps)
            }

            DeviceType.WEIGHT_MACHINE -> {
                binding.tvDeviceName.text =
                    getString(R.string.connect_your_humotron_smart_weight_machine)
                binding.tvHeader.isVisible = false
                binding.tvContinue.visibility = View.INVISIBLE

                binding.tvDesc.text =
                    getString(R.string.smart_scale_steps)
            }

            else -> {
                binding.tvHeader.text =
                    getString(R.string.smart_scale_steps)

                mDeviceInfo?.let {
                    binding.tvDeviceName.text = it.title
                    val stepsText = """
        ${getString(R.string.step_switch_bluetooth)}
        ${getString(R.string.step_power_on_device, it.title)}
        ${getString(R.string.step_detect_device, it.title)}
        ${getString(R.string.step_connect)}
        ${getString(R.string.step_successful)}
    """.trimIndent()
                    binding.tvDesc.text = stepsText
                }
            }
        }

        binding.btnSubmit.setOnClickListener {
            if (isBlueToothSupported()) {
                requestPermission()
            }
        }
    }

    private fun isBluetoothEnabled() {
        if (mBluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            blueToothLauncher.launch(enableBtIntent)
        } else {
            navigateToConnection()
        }
    }

    private fun isBlueToothSupported(): Boolean {
        if (!requireActivity().packageManager.hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE
            )
        ) {
            return false
        }

        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        return mBluetoothAdapter != null
    }

    private fun navigateToConnection() {
        when (mDeviceInfo?.deviceType) {
            DeviceType.RING -> {
                findNavController().navigate(R.id.fragmentRingConnection)
            }

            DeviceType.BAND -> {
                findNavController().navigate(R.id.fragmentBandConnection)
            }

            DeviceType.SMART_CUFF -> {
                findNavController().navigate(R.id.fragmentBPMachineConnection)
            }

            DeviceType.WEIGHT_MACHINE -> {
                findNavController().navigate(R.id.fragmentWeightMachineConnection)
            }

            else -> {

            }
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }
}
