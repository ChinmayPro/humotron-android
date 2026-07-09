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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.bp.BpConnectionState
import com.humotron.app.bt.bp.BpDiscoveredDevice
import com.humotron.app.bt.bp.BpError
import com.humotron.app.bt.bp.BpMachineEvent
import com.humotron.app.bt.bp.BpMachineViewModel
import com.humotron.app.bt.bp.BpScanState
import com.humotron.app.bt.bp.BpSdkState
import com.humotron.app.core.Preference
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBpMachineConnectionBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.param.DeviceMetaDataParam
import com.humotron.app.ui.connect.adapter.BpMachineDeviceAdapter
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.ToastUtils
import com.lepu.blepro.constants.Ble
import com.lepu.blepro.observer.BIOL
import com.lepu.blepro.observer.BleChangeObserver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BPMachineConnectionFragment : Fragment(R.layout.fragment_bp_machine_connection),
    BleChangeObserver {

    private lateinit var binding: FragmentBpMachineConnectionBinding
    private val viewModel: BpMachineViewModel by activityViewModels()

    @Inject
    lateinit var prefUtils: PrefUtils

    private var dialog: LoadingDialog? = null
    private var selectedDevice: BpDiscoveredDevice? = null
    private var biol: BIOL? = null
    private var connectionHandled = false

    private val adapter by lazy {
        BpMachineDeviceAdapter { device ->
            selectedDevice = device
            val enabled = device != null
            setBtnEnabled(enabled)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBpMachineConnectionBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeData()
    }

    private fun initClicks() {
        binding.btnSubmit.setOnClickListener {
            val device = selectedDevice ?: return@setOnClickListener
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
            connectionHandled = false
            showProgress()
            attachBleInterface(device.model)
            viewModel.connect(device)
        }
        binding.tvScanAgain.setOnClickListener { v ->
            startScan()
        }
    }

    private fun initViews() {
        binding.scanAnimationView.color =ContextCompat.getColor(requireContext(), R.color.series)
        binding.header.tvTitle.text = getString(R.string.connect_device)
        binding.btnSubmit.isEnabled = false
        binding.rvDevices.adapter = adapter
        viewModel.initialize()
    }

    private fun observeData() {
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.sdkState.observe(viewLifecycleOwner) { state ->
            when (state) {
                BpSdkState.Ready -> {
                    startScan()
                }
                BpSdkState.Initializing -> {

                }

                is BpSdkState.Failed -> {
                    ToastUtils.showShort(requireContext(), state.message)
                }

                BpSdkState.Uninitialized -> Unit
            }
        }

        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                BpScanState.Starting,
                BpScanState.Scanning,
                    -> {
                    binding.llAnimationView.isVisible = true
                }

                BpScanState.Idle,
                BpScanState.Stopping,
                    -> {
                }

                is BpScanState.Failed -> {
                    binding.llAnimationView.isVisible = false
                    ToastUtils.showShort(requireContext(), state.message)
                }
            }
        }

        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            adapter.submitList(devices)
            val hasDevices = devices.isNotEmpty()
            if (hasDevices) {
                with(binding) {
                    binding.tvSubtitleStatus.text = getString(R.string.cuff_found)
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

        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                BpConnectionState.Idle -> hideProgress()
                is BpConnectionState.Connecting -> {
                    showProgress()
                }

                is BpConnectionState.Connected -> {
                    hideProgress()
                }

                is BpConnectionState.Disconnecting -> {
                }

                is BpConnectionState.Disconnected -> {
                    hideProgress()
                }

                is BpConnectionState.Reconnecting -> {

                }

                is BpConnectionState.Failed -> {
                    hideProgress()
                    ToastUtils.showShort(requireContext(), state.message)
                }
            }
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is BpMachineEvent.DeviceDiscovered -> {
                }

                is BpMachineEvent.ConnectionReady -> {
                    handleConnectionReady()
                }

                is BpMachineEvent.Bp2.SyncTime -> {
                    if (event.success) handleConnectionReady()
                }

                is BpMachineEvent.Bp2W.SyncTime -> {
                    if (event.success) handleConnectionReady()
                }

                is BpMachineEvent.Bp3.SetTime -> {
                    if (event.success) handleConnectionReady()
                }

                is BpMachineEvent.AirBp.SetTime -> {
                    if (event.success) handleConnectionReady()
                }

                is BpMachineEvent.RawResult -> {
                    if (event.code != 0 && event.message.isNotBlank()) {
                        ToastUtils.showShort(requireContext(), event.message)
                    }
                }

                else -> Unit
            }
        }

        viewModel.errors.observe(viewLifecycleOwner) { error ->
            val message = when (error) {
                is BpError.Connection -> error.message
                is BpError.Initialization -> error.message
                is BpError.Scan -> error.message
                is BpError.Command -> error.message
                is BpError.Unknown -> error.message
            }
            ToastUtils.showShort(requireContext(), message)
        }

        viewModel.addHardwareData().observe(viewLifecycleOwner) { networkState ->
            when (networkState.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val userHardware = networkState.data?.data?.userHardware
                    val deviceId = networkState.data?.data?.deviceDetails?.id
                    val bundle = Bundle()
                    val device = selectedDevice
                    if (userHardware != null && device != null) {
                        prefUtils.setBpHardwareData(userHardware)
                        bundle.putParcelable(
                            "deviceMetaData",
                            DeviceMetaDataParam.from(
                                device,
                                userHardware,
                                deviceId
                            )
                        )
                        bundle.putSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType.BP_MACHINE)
                    }
                    findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val bundle = Bundle()
                    bundle.putSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType.BP_MACHINE)
                    findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    private fun startScan() {
        binding.scanAnimationView.setProgress(0.25f, animate = true, duration = 300)

        binding.tvSubtitleStatus.text = getString(R.string.searching_for_your_smart_cuff)
        adapter.clearData()
        with(binding) {
            rvDevices.isVisible = false
            btnSubmit.isVisible = false
            footerDisclaimerTextView.isVisible = false
            tvScanAgain.isVisible = false
            tvDeviceStatus.isVisible = false

            llAnimationView.isVisible = true
        }
        viewModel.startScan()
    }

    private fun showProgress() {
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

    private fun hideProgress() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }

    private fun handleConnectionReady() {
        if (connectionHandled) return
        connectionHandled = true

        val device = selectedDevice ?: return
        prefUtils.setString(Preference.BP_MACHINE, device.macAddress)

        if (prefUtils.getBpHardwareId().isNullOrEmpty()) {
            viewModel.addHardwareInProfile(
                AddHardware(
                    DeviceType.BP_MACHINE.value,
                    device.macAddress
                )
            )
        } else {
            hideProgress()
            val userHardware = prefUtils.getBpHardware()
            val deviceId = userHardware?.id

            val bundle = Bundle()
            if (userHardware != null) {
                bundle.putParcelable(
                    "deviceMetaData",
                    DeviceMetaDataParam.from(
                        device,
                        userHardware,
                        deviceId
                    )
                )
                bundle.putSerializable(NavKeys.KEY_DEVICE_TYPE, DeviceType.BP_MACHINE)
            }
            findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
        }
    }

    private fun attachBleInterface(model: Int) {
        biol?.let { lifecycle.removeObserver(it) }
        biol = BIOL(this, intArrayOf(model)).also { lifecycle.addObserver(it) }
    }

    override fun onBleStateChanged(model: Int, state: Int) {
        if (state == Ble.State.CONNECTED) {
            hideProgress()
        }
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

    override fun onDestroyView() {
        biol?.let { lifecycle.removeObserver(it) }
        biol = null
        hideProgress()
        viewModel.stopScan()
        selectedDevice = null
        super.onDestroyView()
    }
}
