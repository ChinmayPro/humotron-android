package com.humotron.app.ui.connect

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.input.key.Key.Companion.Pairing
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.ring.OnBleScanCallback
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.bt.ring.isCharging
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentRingConnectionBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.param.DeviceMetaDataParam
import com.humotron.app.ui.connect.adapter.RingDeviceAdapter
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.util.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RingConnectionFragment : Fragment(R.layout.fragment_ring_connection) {

    private lateinit var binding: FragmentRingConnectionBinding
    private val viewModel: RingConnectionViewModel by viewModels()

    @Inject
    lateinit var prefUtils: PrefUtils

    private val app by lazy { requireActivity().application as App }

    //private var device: RingBleDevice? = null
    var dialog: LoadingDialog? = null

    companion object {
        private const val TAG = "DCF"
        var device: RingBleDevice? = null
    }

    private val adapter by lazy {
        RingDeviceAdapter { selected ->
            device = selected
            val enabled = selected != null
            setBtnEnabled(enabled)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRingConnectionBinding.bind(view)
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
        binding.btnSubmit.setOnClickListener { v ->
            device?.let {
                app.ringDeviceManager.connect(it.device.address)
                binding.scanAnimationView.setProgress(0.75f, animate = true, duration = 300)

                binding.tvSubtitleStatus.text = getString(R.string.pairing_securely)

                with(binding) {
                    rvDevices.isVisible = false
                    btnSubmit.isVisible = false
                    footerDisclaimerTextView.isVisible = false
                    tvScanAgain.isVisible = false
                    tvDeviceStatus.isVisible = false

                    llAnimationView.isVisible = true
                }
                showProgress()
            }
        }
        binding.tvScanAgain.setOnClickListener { v ->
            startScan()
        }
    }

    private fun initData() {
        binding.header.tvTitle.text = getString(R.string.connect_device)
        binding.tvDeviceTitle.text = resources.getString(R.string.humotron_smart_ring)
        binding.scanAnimationView.color =ContextCompat.getColor(requireContext(), R.color.lime)
        app.ringDeviceManager.registerCb()
        binding.rvDevices.adapter = adapter
        startScan()
    }

    private fun observeData() {
        app.ringDeviceManager.connected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected && device != null) {
                binding.tvSubtitleStatus.text = getString(R.string.connected)
                DeviceConnectedFragment.device = device
                device?.device?.address?.let {
                    prefUtils.setString(Preference.WEARABLE_RING, it)
                }
                if (prefUtils.getRingHardwareId().isNullOrEmpty()) {
                    viewModel.addHardwareInProfile(
                        AddHardware(
                            "HumotronRing",
                            device!!.device.address
                        )
                    )
                } else {
                    hideProgress()
                    val userHardware = prefUtils.getRingHardwareData()
                    val deviceId = prefUtils.getBandHardware()?.id

                    val bundle = Bundle()
                    if (userHardware != null && device != null) {
                        bundle.putParcelable(
                            "deviceMetaData",
                            DeviceMetaDataParam.from(
                                device!!,
                                userHardware,
                                deviceId,
                                requireContext()
                            )
                        )
                        bundle.putSerializable(
                            "deviceType",
                            com.humotron.app.domain.modal.DeviceType.RING
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
                    if (userHardware != null && device != null) {
                        prefUtils.setRingHardwareData(userHardware)
                        bundle.putParcelable(
                            "deviceMetaData",
                            DeviceMetaDataParam.from(
                                device!!,
                                userHardware,
                                deviceId,
                                requireContext()
                            )
                        )
                        bundle.putSerializable(
                            "deviceType",
                            DeviceType.RING
                        )
                    }
                    findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val bundle = Bundle()
                    bundle.putSerializable("deviceType", DeviceType.RING)
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    private fun startScan() {
        binding.scanAnimationView.setProgress(0.25f, animate = true, duration = 300)

        binding.tvSubtitleStatus.text = getString(R.string.searching_for_your_ring)
        adapter.clearData()
        with(binding) {
            rvDevices.isVisible = false
            btnSubmit.isVisible = false
            footerDisclaimerTextView.isVisible = false
            tvScanAgain.isVisible = false
            tvDeviceStatus.isVisible = false

            llAnimationView.isVisible = true
        }

        app.ringBleManager.startScan(30000, object : OnBleScanCallback {
            @SuppressLint("MissingPermission")
            override fun onScanning(result: RingBleDevice) {
                if (result.generation != null && result.batteryLevel != null && result.isCharging) {
                    adapter.addDevice(result)
                    with(binding) {
                        binding.tvSubtitleStatus.text = getString(R.string.ring_found)
                        binding.scanAnimationView.setProgress(0.5f, animate = true, duration = 300)

                        binding.llAnimationView.postDelayed({
                            rvDevices.isVisible = true
                            btnSubmit.isVisible = true
                            footerDisclaimerTextView.isVisible = true
                            tvScanAgain.isVisible = true
                            tvDeviceStatus.isVisible = true

                            binding.llAnimationView.isVisible = false
                        }, 1000)
                    }
                }
            }

            override fun onScanFinished() {

            }
        })
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

    fun showProgress() {
        if (dialog == null) {
            initDialog()
        } else {
            if (dialog?.isShowing == true) {
                dialog?.dismiss()
                initDialog()
            } else {
                initDialog()
            }
        }
    }

    private fun initDialog() {
        dialog = LoadingDialog(requireContext())
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.show()
        dialog?.loadingMessage = getString(R.string.connecting)
    }

    fun hideProgress() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }
}