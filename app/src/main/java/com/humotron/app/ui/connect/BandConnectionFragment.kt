package com.humotron.app.ui.connect

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.core.Preference
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBandConnectionBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.util.PrefUtils
import com.jstyle.blesdk2208a.callback.OnScanResults
import com.jstyle.blesdk2208a.model.Device
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BandConnectionFragment : Fragment(R.layout.fragment_band_connection) {

    private lateinit var binding: FragmentBandConnectionBinding
    private val viewModel: DeviceConnectionViewModel by viewModels()

    @Inject
    lateinit var prefUtils: PrefUtils

    @Inject
    lateinit var bandBleManager: BandBleManager

    private var discoveredDevice: Device? = null
    private var dialog: LoadingDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBandConnectionBinding.bind(view)

        binding.ringsView.startAnimation()

        // Stop scanning if we don't find a device within the timeout.
        viewLifecycleOwner.lifecycleScope.launch {
            delay(30_000)
            bandBleManager.stopDeviceScan()
            binding.ringsView.stopAnimation()
        }

        // We expect the advertised name to contain "WristBand".
        bandBleManager.startDeviceScan(
            deviceNameTokens = arrayOf("J2208A", "J1657"),
            onScanResults = object : OnScanResults {
                override fun Success(device: Device) {
                    // Only keep the first matching device.
                    if (discoveredDevice != null) return
                    if (device.mac.isBlank()) return

                    discoveredDevice = device
                    binding.tvNoDeviceFound.isVisible = false
                    binding.tvDeviceDiscoverStatus.setText(R.string.showing_devices)
                    binding.clDevice.isVisible = true

                    binding.tvDeviceModel.text = device.name
                    binding.tvDeviceColor.text = ""
                    binding.tvDeviceGeneration.text = ""
                    binding.tvDeviceSize.text = ""

                    binding.ringsView.stopAnimation()
                    bandBleManager.stopDeviceScan()
                }

                override fun Fail(code: Int) {
                    // No-op: user can retry by returning to this screen.
                }
            },
        )

        binding.cbDeviceChecked.setOnCheckedChangeListener { _, isChecked ->
            binding.btnSubmit.isEnabled = isChecked
        }

        binding.btnSubmit.setOnClickListener {
            val device = discoveredDevice
            val mac = device?.mac ?: prefUtils.getString(Preference.WEARABLE_BAND) ?: return@setOnClickListener
            showProgress()
            bandBleManager.connectDevice(mac)
        }

        binding.clDevice.setOnClickListener {
            binding.cbDeviceChecked.isChecked = true
        }

        // When the band connects successfully, store hardware and navigate.
        viewLifecycleOwner.lifecycleScope.launch {
            bandBleManager.connectionState.collect { connected ->
                if (!connected) return@collect

                // Second time: `MainActivity` may already have connected using the saved MAC,
                // so scan result may never arrive and `discoveredDevice` can stay null.
                val mac = discoveredDevice?.mac ?: prefUtils.getString(Preference.WEARABLE_BAND) ?: return@collect
                prefUtils.setString(Preference.WEARABLE_BAND, mac)

                val hardwareType = prefUtils.getBandHardwareType()
                if (hardwareType.isNullOrEmpty() || hardwareType != DeviceType.BAND.value) {
                    viewModel.addHardwareInProfile(AddHardware(DeviceType.BAND.value, mac))
                } else {
                    hideProgress()
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }
            }
        }

        viewModel.addHardwareData().observe(viewLifecycleOwner) { networkState ->
            when (networkState.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    networkState.data?.data?.userHardware?.let { prefUtils.setBandHardwareData(it) }
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }

                Status.LOADING -> showProgress()
            }
        }
    }

    private fun showProgress() {
        if (dialog == null) {
            initDialog()
            return
        }
        if (dialog?.isShowing == true) dialog?.dismiss()
        initDialog()
    }

    private fun initDialog() {
        dialog = LoadingDialog(requireContext())
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        dialog?.show()
        dialog?.loadingMessage = getString(R.string.connecting)
    }

    private fun hideProgress() {
        if (dialog?.isShowing == true) dialog?.dismiss()
    }
}

