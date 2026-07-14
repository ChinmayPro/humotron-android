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
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentHumotronDeviceConnectInstructionBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HumotronDeviceConnectInstructionFragment :
    BaseFragment(R.layout.fragment_humotron_device_connect_instruction),
    View.OnClickListener {

    private lateinit var binding: FragmentHumotronDeviceConnectInstructionBinding
    private var deviceType: DeviceType? = null

    private var mBluetoothAdapter: BluetoothAdapter? = null

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
        binding = FragmentHumotronDeviceConnectInstructionBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener(this@HumotronDeviceConnectInstructionFragment)
        binding.btnStartPairing.setOnClickListener(this@HumotronDeviceConnectInstructionFragment)
    }

    private fun initViews() = with(binding) {
        deviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable(NavKeys.KEY_DEVICE_TYPE) as? DeviceType
        }

        when (deviceType) {
            DeviceType.RING -> {
                setColorIcons(R.color.lime, R.color.lime_10, R.drawable.ic_ring_vector)
                binding.header.tvTitle.text = getString(R.string.pair_humotron_ring_title)
                tvDeviceTitle.setText(R.string.device_title_ring)
                tvSubtitle.setText(R.string.device_subtitle_ring)
                tvStep1Text.setText(R.string.device_step1_ring)
                tvStep2Text.setText(R.string.device_step2_ring)
                tvStep3Text.setText(R.string.device_step3_ring)
            }

            DeviceType.BAND -> {
                setColorIcons(R.color.good, R.color.good_14, R.drawable.ic_band_vectr)
                binding.header.tvTitle.text = getString(R.string.pair_humotron_band_title)
                tvDeviceTitle.setText(R.string.device_title_band)
                tvSubtitle.setText(R.string.device_subtitle_band)
                tvStep1Text.setText(R.string.device_step1_band)
                tvStep2Text.setText(R.string.device_step2_band)
                tvStep3Text.setText(R.string.device_step3_band)
            }

            DeviceType.SMART_CUFF -> {
                setColorIcons(R.color.cool, R.color.cool_14, R.drawable.ic_smart_cuff_vector)
                binding.header.tvTitle.text = getString(R.string.pair_humotron_cuff_title)
                tvDeviceTitle.setText(R.string.device_title_cuff)
                tvSubtitle.setText(R.string.device_subtitle_cuff)
                tvStep1Text.setText(R.string.device_step1_cuff)
                tvStep2Text.setText(R.string.device_step2_cuff)
                tvStep3Text.setText(R.string.device_step3_cuff)
            }

            DeviceType.WEIGHT_MACHINE -> {
                setColorIcons(R.color.series, R.color.series_14, R.drawable.ic_smart_scale_vector)
                binding.header.tvTitle.text = getString(R.string.pair_humotron_scale_title)
                tvDeviceTitle.setText(R.string.device_title_scale)
                tvSubtitle.setText(R.string.device_subtitle_scale)
                tvStep1Text.setText(R.string.device_step1_scale)
                tvStep2Text.setText(R.string.device_step2_scale)
                tvStep3Text.setText(R.string.device_step3_scale)
            }

            DeviceType.UNKNOWN, null -> {
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
        when (deviceType) {
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

    fun setColorIcons(mColor: Int, mColor14: Int, drawable: Int) {
        binding.ivRingIcon.setImageResource(drawable)
        binding.ivRingIcon.setColorFilter(ContextCompat.getColor(requireActivity(), mColor))
        binding.cvRingIcon.setCardBackgroundColor(
            ContextCompat.getColor(requireActivity(), mColor14)
        )
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.header.btnBack -> findNavController().popBackStack()
            binding.btnStartPairing -> {
                if (isBlueToothSupported()) {
                    requestPermission()
                }
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
