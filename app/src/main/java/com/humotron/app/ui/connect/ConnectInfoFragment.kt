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
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentConnectInfoBinding
import com.humotron.app.ui.connect.adapter.DeviceInfo


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
            arguments?.getParcelable("deviceInfo", DeviceInfo::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("deviceInfo")
        }

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
        if (mDeviceInfo?.title == "Humotron Smart Ring") {
            findNavController().navigate(R.id.fragmentDeviceConnection)
        } else {
            findNavController().navigate(R.id.fragmentBandConnection)
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
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
