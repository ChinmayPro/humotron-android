package com.humotron.app.ui.connect

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.bt.band.STATE_BAND_CONNECTED
import com.humotron.app.core.Preference
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBandConnectionBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.param.DeviceMetaDataParam
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.util.PrefUtils
import com.jstyle.blesdk2208a.callback.OnScanResults
import com.jstyle.blesdk2208a.model.Device
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BandConnectionFragment : Fragment(R.layout.fragment_band_connection) {

    private lateinit var binding: FragmentBandConnectionBinding
    private val viewModel: RingConnectionViewModel by viewModels()

    @Inject
    lateinit var prefUtils: PrefUtils

    @Inject
    lateinit var bandBleManager: BandBleManager

    private var discoveredDevice: Device? = null
    private var dialog: LoadingDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBandConnectionBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initData()
        observeData()
    }

    private fun initClicks() {
        binding.cbDeviceChecked.setOnCheckedChangeListener { _, isChecked ->
            setBtnEnabled(isChecked)
        }
        binding.btnSubmit.setOnClickListener {
            val device = discoveredDevice
            val mac = device?.mac ?: prefUtils.getString(Preference.WEARABLE_BAND)
            ?: return@setOnClickListener

            binding.scanAnimationView.setProgress(0.75f, animate = true, duration = 300)

            binding.tvSubtitleStatus.text = getString(R.string.pairing_securely)

            with(binding) {
                mcvDeviceBox.isVisible = false
                btnSubmit.isVisible = false
                footerDisclaimerTextView.isVisible = false
                tvScanAgain.isVisible = false
                tvDeviceStatus.isVisible = false

                llAnimationView.isVisible = true
            }
            showProgress()
            bandBleManager.connectDevice(mac)
        }

        binding.tvScanAgain.setOnClickListener { v ->
            startScan()
        }

        binding.mcvDeviceBox.setOnClickListener {
            binding.cbDeviceChecked.isChecked = true
        }
    }

    private fun initData() {
        binding.scanAnimationView.color =ContextCompat.getColor(requireContext(), R.color.good)
        binding.header.tvTitle.text = getString(R.string.connect_device)
        // Stop scanning if we don't find a device within the timeout.
        viewLifecycleOwner.lifecycleScope.launch {
            delay(30_000)
            bandBleManager.stopDeviceScan()
        }
        startScan()
    }

    private fun observeData() {
        // When the band connects successfully, store hardware and navigate.
        viewLifecycleOwner.lifecycleScope.launch {
            bandBleManager.connectionState.collect { connected ->
                if (connected != STATE_BAND_CONNECTED) return@collect

                binding.tvSubtitleStatus.text = getString(R.string.connected)

                // Second time: `MainActivity` may already have connected using the saved MAC,
                // so scan result may never arrive and `discoveredDevice` can stay null.
                val mac = discoveredDevice?.mac ?: prefUtils.getString(Preference.WEARABLE_BAND)
                ?: return@collect
                prefUtils.setString(Preference.WEARABLE_BAND, mac)

                val hardwareType = prefUtils.getBandHardwareType()
                if (hardwareType.isNullOrEmpty() || hardwareType != DeviceType.BAND.value) {
                    viewModel.addHardwareInProfile(AddHardware(DeviceType.BAND.value, mac))
                } else {
                    hideProgress()
                    val userHardware = prefUtils.getBandHardware()
                    val deviceId = prefUtils.getBandHardware()?.id

                    val bundle = Bundle()
                    bundle.putSerializable("deviceType", DeviceType.BAND)
                    if (userHardware != null && discoveredDevice != null) {
                        bundle.putParcelable(
                            "deviceMetaData",
                            DeviceMetaDataParam.from(discoveredDevice!!, userHardware, deviceId)
                        )
                    }
                    findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
                }
            }
        }

        viewModel.addHardwareData().observe(viewLifecycleOwner) { networkState ->
            when (networkState.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val userHardware = networkState.data?.data?.userHardware
                    val deviceId = networkState.data?.data?.deviceDetails?.id
                    val bundle = Bundle()
                    if (userHardware != null && discoveredDevice != null && deviceId != null) {
                        prefUtils.setBandHardwareData(userHardware)
                        bundle.putParcelable(
                            "deviceMetaData",
                            DeviceMetaDataParam.from(
                                discoveredDevice!!,
                                userHardware,
                                deviceId
                            )
                        )
                        bundle.putSerializable("deviceType", DeviceType.BAND)
                    }
                    findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val bundle = Bundle()
                    bundle.putSerializable("deviceType", DeviceType.BAND)
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }

                Status.LOADING -> showProgress()
            }
        }
    }

    private fun startScan() {
        discoveredDevice = null
        binding.scanAnimationView.setProgress(0.25f, animate = true, duration = 300)

        binding.tvSubtitleStatus.text = getString(R.string.searching_for_your_band)
        //adapter.clearData()
        with(binding) {
            mcvDeviceBox.isVisible = false
            btnSubmit.isVisible = false
            footerDisclaimerTextView.isVisible = false
            tvScanAgain.isVisible = false
            tvDeviceStatus.isVisible = false

            llAnimationView.isVisible = true
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

                    binding.tvDeviceModel.text = device.name
                    binding.tvDeviceColor.text = ""
                    binding.tvDeviceGeneration.text = ""
                    binding.tvDeviceSize.text = ""

                    with(binding) {
                        binding.tvSubtitleStatus.text = getString(R.string.band_found)
                        binding.scanAnimationView.setProgress(0.5f, animate = true, duration = 300)

                        binding.llAnimationView.postDelayed({
                            mcvDeviceBox.isVisible = true
                            btnSubmit.isVisible = true
                            footerDisclaimerTextView.isVisible = true
                            tvScanAgain.isVisible = true
                            tvDeviceStatus.isVisible = true

                            binding.llAnimationView.isVisible = false
                        }, 1000)
                    }

                    bandBleManager.stopDeviceScan()
                }

                override fun Fail(code: Int) {
                    // No-op: user can retry by returning to this screen.
                }
            },
        )
    }

    fun setBtnEnabled(enabled: Boolean) {
        binding.btnSubmit.apply {
            isEnabled = enabled

            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    if (enabled) R.color.lime else R.color.white05
                )
            )

            setTextColor(
                ContextCompat.getColor(
                    context,
                    if (enabled) android.R.color.black else R.color.ink2
                )
            )
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

