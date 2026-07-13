package com.humotron.app.ui.device.cuff

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.bp.BpConnectionState
import com.humotron.app.bt.bp.BpError
import com.humotron.app.bt.bp.BpMachineEvent
import com.humotron.app.bt.bp.BpMachineViewModel
import com.humotron.app.bt.bp.BpSyncStatus
import com.humotron.app.bt.common.BluetoothManager
import com.humotron.app.bt.common.BluetoothState
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSmartCuffBpReadingBinding
import com.humotron.app.domain.modal.BPMachineBpResult
import com.humotron.app.domain.modal.BPMachineDeviceStatus
import com.humotron.app.domain.modal.BPMachineReadingType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import com.lepu.blepro.ext.bp2.RtStatus
import com.lepu.blepro.objs.Bluetooth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.lepu.blepro.ext.airbp.RtResult as AirBpRtResult
import com.lepu.blepro.ext.bp2.RtBpIng as Bp2RtBpIng
import com.lepu.blepro.ext.bp2.RtBpResult as Bp2RtBpResult
import com.lepu.blepro.ext.bp2w.RtBpIng as Bp2wRtBpIng
import com.lepu.blepro.ext.bp2w.RtBpResult as Bp2wRtBpResult
import com.lepu.blepro.ext.bp3.RtBpIng as Bp3RtBpIng
import com.lepu.blepro.ext.bp3.RtBpResult as Bp3RtBpResult

@AndroidEntryPoint
class SmartCuffBPReadingFragment :
    BaseFragment(R.layout.fragment_smart_cuff_bp_reading),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartCuffBpReadingBinding
    private val bpViewModel: BpMachineViewModel by activityViewModels()

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    private var userDevice: UserDevice? = null
    private var readingType: BPMachineReadingType? = null
    private var isResultSuccessOrFail: Boolean? = false
    private var hasNavigatedToResult = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartCuffBpReadingBinding.bind(view)

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
        binding.btnStop.setOnClickListener(this@SmartCuffBPReadingFragment)
        binding.btnRetryMeasurement.setOnClickListener(this@SmartCuffBPReadingFragment)
    }

    private fun initViews() = with(binding) {
        // SmartCuff BP layout does not include the header/instruction/ECG views from
        // BPMachineReadingFragment, so only BP-specific UI is initialized here.
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, UserDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }
        readingType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.READING_TYPE, BPMachineReadingType::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.READING_TYPE)
        }

        tvDeviceName.text = userDevice?.deviceFacingName ?: userDevice?.deviceName ?: ""
        tvMeasureTitle.text = getString(R.string.please_remain_calm_and_still)
        tvMeasureSubtitle.text = getString(R.string.your_blood_pressure_measurement_is_in_progress)

        showDefaultUi()
    }

    private fun observeData() {
        observeSyncStatus()
        observeBPMachine()
        observeBluetoothState()
    }

    private fun observeBluetoothState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bluetoothManager.bluetoothState.collect { state ->
                    val isConnected =
                        state == BluetoothState.ON &&
                                bpViewModel.connectionState.value is BpConnectionState.Connected
                    updateConnectionUi(isConnected)
                }
            }
        }
    }

    private fun observeSyncStatus() {
        bpViewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                BpSyncStatus.FETCHING_FILE_LIST,
                BpSyncStatus.SYNCING_FILES,
                    -> {
                    //showProgress()
                }

                else -> {
                    //hideProgress()
                }
            }
        }
    }

    private fun observeBPMachine() {
        bpViewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BpConnectionState.Connected -> {
                    updateConnectionUi(true)
                    showDefaultUi()
                    when (state.device.model) {
                        Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                            bpViewModel.startBp2Realtime(state.device.model)
                            //bpViewModel.startSync()
                        }

                        Bluetooth.MODEL_BP2W -> bpViewModel.getBp2wInfo(state.device.model)
                        Bluetooth.MODEL_BP3 -> bpViewModel.getBp3Info(state.device.model)
                        Bluetooth.MODEL_AIRBP -> bpViewModel.getAirBpInfo(state.device.model)
                    }
                }

                is BpConnectionState.Connecting,
                is BpConnectionState.Reconnecting,
                    -> updateConnectionUiConnecting()

                is BpConnectionState.Disconnected,
                is BpConnectionState.Failed,
                is BpConnectionState.Idle,
                    -> updateConnectionUi(false)

                else -> Unit
            }
        }

        bpViewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is BpMachineEvent.AirBp.BatteryInfo -> {
                    updateBattery(event.data.percent, false)
                }

                is BpMachineEvent.AirBp.Pressure -> {
                    renderAirBpPressure(event.pressure)
                }

                is BpMachineEvent.AirBp.State -> {
                    renderAirBpState(event.state)
                }

                is BpMachineEvent.AirBp.Result -> {
                    renderAirBpResult(event.result)
                }

                is BpMachineEvent.Bp2.RtData -> {
                    val (_, rtData) = event
                    renderBpRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        source = "BP2"
                    )
                }

                is BpMachineEvent.Bp2W.RtData -> {
                    val (_, rtData) = event
                    renderBpRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        source = "BP2W"
                    )
                }

                is BpMachineEvent.Bp3.RtData -> {
                    val (_, rtData) = event
                    renderBpRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        source = "BP3"
                    )
                }

                else -> Unit
            }
        }

        bpViewModel.errors.observe(viewLifecycleOwner) { error ->
            renderError(error)
        }
    }

    private fun renderBpRtData(
        status: Any,
        paramDataType: Int,
        payload: Any?,
        source: String,
    ) {
        val percent = when (status) {
            is RtStatus -> status.percent
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.percent
            is com.lepu.blepro.ext.bp3.RtStatus -> status.percent
            else -> return
        }
        val batteryStatus = when (status) {
            is RtStatus -> status.batteryStatus
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.batteryStatus
            is com.lepu.blepro.ext.bp3.RtStatus -> status.batteryStatus
            else -> 0
        }
        val deviceStatus = when (status) {
            is RtStatus -> status.deviceStatus
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.deviceStatus
            is com.lepu.blepro.ext.bp3.RtStatus -> status.deviceStatus
            else -> -1
        }
        val deviceStatusMsg = BPMachineDeviceStatus.fromValue(deviceStatus).label
        val batteryStatusMsg = when (status) {
            is RtStatus -> status.batteryStatusMsg
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.batteryStatusMsg
            is com.lepu.blepro.ext.bp3.RtStatus -> status.batteryStatusMsg
            else -> ""
        }
        val avgCnt = when (status) {
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.avgCnt
            is com.lepu.blepro.ext.bp3.RtStatus -> status.avgCnt
            else -> -1
        }
        val avgWaitTick = when (status) {
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.avgWaitTick
            is com.lepu.blepro.ext.bp3.RtStatus -> status.avgWaitTick
            else -> -1
        }
        val deviceStatusEnum = BPMachineDeviceStatus.fromValue(deviceStatus)

        updateBattery(percent, batteryStatus == 1)

        if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_READY && isResultSuccessOrFail == false) {
            showDefaultUi()
        }

        when (paramDataType) {
            0 -> {
                if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_BP_MEASURING) {
                    isResultSuccessOrFail = false
                    renderBpPressure(
                        payload,
                        source,
                        deviceStatusMsg,
                        batteryStatusMsg,
                        avgCnt,
                        avgWaitTick
                    )
                }
            }

            1 -> {
                if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_BP_MEASURE_END) {
                    isResultSuccessOrFail = true
                    renderBpResult(
                        payload,
                        source,
                        deviceStatusMsg,
                        batteryStatusMsg,
                        avgCnt,
                        avgWaitTick
                    )
                }
            }

            else -> Unit
        }
    }

    private fun renderBpPressure(
        payload: Any?,
        source: String,
        statusText: String,
        detailText: String,
        avgCnt: Int,
        avgWaitTick: Int,
    ) {
        val parsed = when (payload) {
            is Bp2RtBpIng -> payload
            is Bp2wRtBpIng -> payload
            is Bp3RtBpIng -> payload
            is ByteArray -> runCatching { Bp2RtBpIng(payload) }.getOrNull()
                ?: runCatching { Bp2wRtBpIng(payload) }.getOrNull()
                ?: runCatching { Bp3RtBpIng(payload) }.getOrNull()

            else -> null
        }

        val pressure = when (parsed) {
            is Bp2RtBpIng -> parsed.pressure
            is Bp2wRtBpIng -> parsed.pressure
            is Bp3RtBpIng -> parsed.pressure
            else -> null
        }

        val pulse = when (parsed) {
            is Bp2RtBpIng -> parsed.pr
            is Bp2wRtBpIng -> parsed.pr
            is Bp3RtBpIng -> parsed.pr
            else -> null
        }

        val progressValue = pressure?.toFloat() ?: 0f

        with(binding) {
            mcvDeviceDetails.isVisible = true
            bpView.isVisible = true
            tvMeasureTitle.isVisible = true
            tvMeasureSubtitle.isVisible = true
            llBpMeasureError.isVisible = false
            clBPCalc.isVisible = true

            tvMeasureTitle.text = getString(R.string.please_remain_calm_and_still)
            tvMeasureSubtitle.text =
                getString(R.string.your_blood_pressure_measurement_is_in_progress)

            tvMeasureSystolicValue.text = pressure?.toString() ?: "--"
            bpView.setValue(progressValue, animate = true, 700)

            tvMeasureSystolicReading.text = getString(R.string.calculating)
            tvMeasureDiastolicReading.text = getString(R.string.calculating)
            tvMeasurePulseReading.text = getString(R.string.calculating)

            // ECG-only detail views from BPMachineReadingFragment are not present here.
            tvDeviceStatus.contentDescription = buildString {
                append(source)
                append(" | ")
                append(statusText)
                append(" | ")
                append(detailText)
                if (avgCnt >= 0) {
                    append(" | avg ")
                    append(avgCnt)
                }
                if (avgWaitTick >= 0) {
                    append(" | wait ")
                    append(avgWaitTick)
                }
            }
        }
    }

    private fun renderBpResult(
        payload: Any?,
        source: String,
        statusText: String,
        detailText: String,
        avgCnt: Int,
        avgWaitTick: Int,
    ) {
        val parsed = when (payload) {
            is Bp2RtBpResult -> payload
            is Bp2wRtBpResult -> payload
            is Bp3RtBpResult -> payload
            is ByteArray -> runCatching { Bp2RtBpResult(payload) }.getOrNull()
                ?: runCatching { Bp2wRtBpResult(payload) }.getOrNull()
                ?: runCatching { Bp3RtBpResult(payload) }.getOrNull()

            else -> null
        }

        val sys = when (parsed) {
            is Bp2RtBpResult -> parsed.sys
            is Bp2wRtBpResult -> parsed.sys
            is Bp3RtBpResult -> parsed.sys
            else -> 0
        }
        val dia = when (parsed) {
            is Bp2RtBpResult -> parsed.dia
            is Bp2wRtBpResult -> parsed.dia
            is Bp3RtBpResult -> parsed.dia
            else -> 0
        }
        val pulse = when (parsed) {
            is Bp2RtBpResult -> parsed.pr
            is Bp2wRtBpResult -> parsed.pr
            is Bp3RtBpResult -> parsed.pr
            else -> 0
        }
        val resultValue = when (parsed) {
            is Bp2RtBpResult -> parsed.result
            is Bp2wRtBpResult -> parsed.result
            is Bp3RtBpResult -> parsed.result
            else -> -1
        }

        val bpResult = BPMachineBpResult.fromValue(resultValue)

        with(binding) {
            if (bpResult == BPMachineBpResult.NORMAL) {
                clBPCalc.isVisible = true
                llBpMeasureError.isVisible = false
                tvMeasureTitle.isVisible = true
                tvMeasureSubtitle.isVisible = true

                tvMeasureTitle.text = getString(R.string.analysis_complete_results_are_ready)
                tvMeasureSubtitle.text = getString(R.string.the_results_are_ready)

                tvMeasureSystolicValue.text = sys.toString()
                tvMeasureSystolicReading.text = sys.toString()
                tvMeasureDiastolicReading.text = dia.toString()
                tvMeasurePulseReading.text = pulse.toString()
                bpView.setValue(sys.toFloat(), animate = true, 700)

                bpViewModel.startSync()
                if (!hasNavigatedToResult) {
                    hasNavigatedToResult = true
                    findNavController().navigate(
                        R.id.fragmentSmartCuffBPResult,
                        Bundle().apply {
                            putParcelable(NavKeys.WEARABLE, userDevice)
                            putParcelable(NavKeys.READING_TYPE, readingType)
                        }
                    )
                }
            } else {
                clBPCalc.isVisible = false
                tvMeasureTitle.isVisible = false
                tvMeasureSubtitle.isVisible = false
                llBpMeasureError.isVisible = true
            }

            tvDeviceStatus.contentDescription = buildString {
                append(source)
                append(" | ")
                append(statusText)
                append(" | ")
                append(detailText)
                if (avgCnt >= 0) {
                    append(" | avg ")
                    append(avgCnt)
                }
                if (avgWaitTick >= 0) {
                    append(" | wait ")
                    append(avgWaitTick)
                }
            }
        }
    }

    private fun renderAirBpPressure(pressure: Int) {
        // AirBP BP-only flow is not wired beyond the shared realtime parser in this screen.
    }

    private fun renderAirBpState(state: Int) {
        // AirBP BP-only flow is not wired beyond the shared realtime parser in this screen.
    }

    private fun renderAirBpResult(result: AirBpRtResult) {
        // AirBP BP-only flow is not wired beyond the shared realtime parser in this screen.
    }

    private fun renderError(error: BpError) {
        val message = when (error) {
            is BpError.Initialization -> error.message
            is BpError.Scan -> error.message
            is BpError.Connection -> error.message
            is BpError.Command -> error.message
            is BpError.Unknown -> error.message
        }

        with(binding) {
            llBpMeasureError.isVisible = true
            clBPCalc.isVisible = false
            tvMeasureTitle.isVisible = false
            tvMeasureSubtitle.isVisible = false
            bpView.isVisible = false
            tvDeviceStatus.contentDescription = message
        }
    }

    private fun updateBattery(level: Int, isCharging: Boolean) = with(binding) {
        batteryView.batteryLevel = level
        tvBatteryLevel.text = "$level%"
        batteryView.isCharging = isCharging
    }

    private fun updateConnectionUi(isConnected: Boolean) = with(binding) {
        ivStatusDot.setImageResource(
            if (isConnected) R.drawable.dot_connected
            else R.drawable.dot_disconnected
        )

        tvDeviceStatus.text = if (isConnected) "Connected" else "Disconnected"
        tvDeviceStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isConnected) R.color.colorBgBtn else R.color.disconnected
            )
        )

        batteryView.isVisible = isConnected
        tvBatteryLevel.isVisible = isConnected
        batteryView.isCharging = false
    }

    private fun updateConnectionUiConnecting() = with(binding) {
        ivStatusDot.setImageResource(R.drawable.dot_connecting)
        tvDeviceStatus.text = "Connecting"
        tvDeviceStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.w200
            )
        )
        batteryView.isVisible = false
        tvBatteryLevel.isVisible = false
    }

    private fun showDefaultUi() = with(binding) {
        mcvDeviceDetails.isVisible = true
        tvMeasureTitle.isVisible = true
        tvMeasureSubtitle.isVisible = true
        bpView.isVisible = true
        clBPCalc.isVisible = true
        llBpMeasureError.isVisible = false

        tvMeasureTitle.text = getString(R.string.please_remain_calm_and_still)
        tvMeasureSubtitle.text = getString(R.string.your_blood_pressure_measurement_is_in_progress)

        tvMeasureSystolicValue.text = "--"
        tvMeasureSystolicReading.text = getString(R.string.calculating)
        tvMeasureDiastolicReading.text = getString(R.string.calculating)
        tvMeasurePulseReading.text = getString(R.string.calculating)

        bpView.setValue(10f, animate = true, 700)
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnStop -> {
                stopBpRealtime()
                findNavController().popBackStack()
            }

            binding.btnRetryMeasurement -> {
                isResultSuccessOrFail = false
                showDefaultUi()
            }
        }
    }

    override fun onDestroyView() {
        hideProgress()
        stopBpRealtime()
        hasNavigatedToResult = false
        super.onDestroyView()
    }

    private fun stopBpRealtime() {
        (bpViewModel.connectionState.value as? BpConnectionState.Connected)?.let {
            when (val model = it.device.model) {
                Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                    bpViewModel.stopBp2Realtime(model)
                }

                Bluetooth.MODEL_BP2W -> bpViewModel.stopBp2wRealtime(model)
                Bluetooth.MODEL_BP3 -> bpViewModel.stopBp3Realtime(model)
            }
        }
    }
}
