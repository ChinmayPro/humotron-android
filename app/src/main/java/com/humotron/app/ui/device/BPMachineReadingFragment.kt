package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.graphics.Color
import android.util.Log
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
import com.humotron.app.databinding.FragmentBpMachineReadingBinding
import com.humotron.app.domain.modal.BPMachineBpResult
import com.humotron.app.domain.modal.BPMachineDeviceStatus
import com.humotron.app.domain.modal.BPMachineReadingType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.loge
import com.lepu.blepro.objs.Bluetooth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.lepu.blepro.ext.airbp.RtResult as AirBpRtResult
import com.lepu.blepro.ext.bp2.RtBpIng as Bp2RtBpIng
import com.lepu.blepro.ext.bp2.RtBpResult as Bp2RtBpResult
import com.lepu.blepro.ext.bp2.RtEcgIng as Bp2RtEcgIng
import com.lepu.blepro.ext.bp2.RtEcgResult as Bp2RtEcgResult
import com.lepu.blepro.ext.bp2w.RtBpIng as Bp2wRtBpIng
import com.lepu.blepro.ext.bp2w.RtBpResult as Bp2wRtBpResult
import com.lepu.blepro.ext.bp2w.RtEcgIng as Bp2wRtEcgIng
import com.lepu.blepro.ext.bp2w.RtEcgResult as Bp2wRtEcgResult
import com.lepu.blepro.ext.bp3.RtBpEcgIng as Bp3RtBpEcgIng
import com.lepu.blepro.ext.bp3.RtBpIng as Bp3RtBpIng
import com.lepu.blepro.ext.bp3.RtBpResult as Bp3RtBpResult
import com.lepu.blepro.ext.bp3.RtEcgIng as Bp3RtEcgIng
import com.lepu.blepro.ext.bp3.RtEcgResult as Bp3RtEcgResult

@AndroidEntryPoint
class BPMachineReadingFragment :
    BaseFragment(R.layout.fragment_bp_machine_reading),
    View.OnClickListener {

    private lateinit var binding: FragmentBpMachineReadingBinding
    private val bpViewModel: BpMachineViewModel by activityViewModels()

    @Inject
    lateinit var bluetoothManager: BluetoothManager
    private var userDevice: UserDevice? = null
    private var readingType: BPMachineReadingType? = null
    private var isResultSuccessOrFail: Boolean? = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "onViewCreated: ")
        binding = FragmentBpMachineReadingBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeData()
    }

    private fun initViews() = with(binding) {
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

        header.title.text = if (readingType == BPMachineReadingType.BLOOD_PRESSURE) {
            "Blood Pressure Reading"
        } else {
            "ECG Reading"
        }


        tvDeviceName.text = userDevice?.deviceFacingName ?: userDevice?.deviceName ?: ""

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                bluetoothManager.bluetoothState.collect {
                    updateInstructionLayouts()
                }
            }
        }
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener(this@BPMachineReadingFragment)
        binding.btnSeeAllReadings.setOnClickListener(this@BPMachineReadingFragment)
        binding.btnRetakeReadings.setOnClickListener(this@BPMachineReadingFragment)
        binding.btnRetryMeasurement.setOnClickListener(this@BPMachineReadingFragment)
        binding.btnRetakeReadingsECG.setOnClickListener(this@BPMachineReadingFragment)
        binding.btnSeeAllReadingsECG.setOnClickListener(this@BPMachineReadingFragment)
    }

    private fun observeData() {
        observeBPMachine()
        observeSyncStatus()
    }

    private fun observeSyncStatus() {
        bpViewModel.syncStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                BpSyncStatus.FETCHING_FILE_LIST,
                BpSyncStatus.SYNCING_FILES,
                    -> {
                    binding.header.progressIndicator.visibility = View.VISIBLE
                }

                else -> {
                    binding.header.progressIndicator.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun observeBPMachine() {
        bpViewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BpConnectionState.Connected -> {
                    updateConnectionUi(true)
                    when (state.device.model) {
                        Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> {
                            Log.e("BP Machine", "Connected")
                            //bpViewModel.startSync()
                            bpViewModel.startBp2Realtime(state.device.model)
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
                    -> {
                    updateConnectionUi(false)
                }

                else -> Unit
            }
            updateInstructionLayouts()
        }

        bpViewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is BpMachineEvent.AirBp.BatteryInfo -> {
                    //Log.e("BP Machine", "BatteryInfo")
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
                    //Log.e("BP Machine", "RtData")
                    val (_, rtData) = event
                    renderBpRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        waveformData = rtData.param.ecgFloatsFilter,
                        source = "BP2"
                    )
                }

                is BpMachineEvent.Bp2W.RtData -> {
                    val (_, rtData) = event
                    renderBpRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        waveformData = rtData.param.ecgFloatsFilter,
                        source = "BP2W"
                    )
                }

                is BpMachineEvent.Bp3.RtData -> {
                    val (_, rtData) = event
                    renderBpRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        waveformData = rtData.param.ecgFloatsFilter,
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
        waveformData: FloatArray?,
        source: String,
    ) {
        val percent = when (status) {
            is com.lepu.blepro.ext.bp2.RtStatus -> status.percent
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.percent
            is com.lepu.blepro.ext.bp3.RtStatus -> status.percent
            else -> return
        }
        val batteryStatus = when (status) {
            is com.lepu.blepro.ext.bp2.RtStatus -> status.batteryStatus
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.batteryStatus
            is com.lepu.blepro.ext.bp3.RtStatus -> status.batteryStatus
            else -> 0
        }
        val deviceStatus = when (status) {
            is com.lepu.blepro.ext.bp2.RtStatus -> status.deviceStatus
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.deviceStatus
            is com.lepu.blepro.ext.bp3.RtStatus -> status.deviceStatus
            else -> -1
        }
        /*val deviceStatusMsg = when (status) {
            is com.lepu.blepro.ext.bp2.RtStatus -> status.deviceStatusMsg
            is com.lepu.blepro.ext.bp2w.RtStatus -> status.deviceStatusMsg
            is com.lepu.blepro.ext.bp3.RtStatus -> status.deviceStatusMsg
            else -> ""
        }*/
        val deviceStatusMsg = BPMachineDeviceStatus.fromValue(deviceStatus).label
        //Log.e("BP Machine", "deviceStatusMsg: $deviceStatusMsg")

        val batteryStatusMsg = when (status) {
            is com.lepu.blepro.ext.bp2.RtStatus -> status.batteryStatusMsg
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

        val statusText = deviceStatusMsg.takeIf { it.isNotBlank() }
            ?: BPMachineDeviceStatus.UNKNOWN.label
        val detailText = buildString {
            append(source)
            append(" | ")
            append(batteryStatusMsg.ifBlank { "Battery $percent%" })
            if (avgCnt >= 0) {
                append(" | avg ")
                append(avgCnt)
            }
            if (avgWaitTick >= 0) {
                append(" | wait ")
                append(avgWaitTick)
            }
        }
        //Log.e("BP Machine", "paramDataType: $paramDataType")

        if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_READY && isResultSuccessOrFail == false) {
            updateInstructionLayouts() //return to default ui if user stop measurement
        }

        when (paramDataType) {
            0 -> {
                if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_BP_MEASURING) {
                    isResultSuccessOrFail = false
                    renderBpPressure(payload, statusText, detailText)
                }
            }

            1 -> {
                if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_BP_MEASURE_END) {
                    isResultSuccessOrFail = true
                    renderBpResult(payload, statusText, detailText)
                }
            }

            2 -> {
                if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_ECG_MEASURING) {
                    isResultSuccessOrFail = false
                    renderEcgProgress(payload, waveformData, statusText, detailText)
                }
            }

            3 -> {
                if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_ECG_MEASURE_END) {
                    isResultSuccessOrFail = true
                    renderEcgResult(payload, statusText, detailText)
                }
            }

            else -> {

            }
        }
    }

    private fun renderBpPressure(payload: Any?, statusText: String, detailText: String) {
        val parsed = when (payload) {
            is Bp2RtBpIng -> payload
            is Bp2wRtBpIng -> payload
            is Bp3RtBpIng -> payload
            is Bp3RtBpEcgIng -> payload
            is ByteArray -> runCatching { Bp2RtBpIng(payload) }.getOrNull()
                ?: runCatching { Bp2wRtBpIng(payload) }.getOrNull()
                ?: runCatching { Bp3RtBpIng(payload) }.getOrNull()
                ?: runCatching { Bp3RtBpEcgIng(payload) }.getOrNull()

            else -> null
        }
        //Log.e("BP Machine", "renderBpPressure parsed: $parsed")

        val pressure = when (parsed) {
            is Bp2RtBpIng -> parsed.pressure
            is Bp2wRtBpIng -> parsed.pressure
            is Bp3RtBpIng -> parsed.pressure
            is Bp3RtBpEcgIng -> parsed.pressure
            else -> null
        }

        val pulse = when (parsed) {
            is Bp2RtBpIng -> parsed.pr
            is Bp2wRtBpIng -> parsed.pr
            is Bp3RtBpIng -> parsed.pr
            is Bp3RtBpEcgIng -> parsed.pr
            else -> null
        }

        val isDeflating = when (parsed) {
            is Bp2RtBpIng -> parsed.isDeflate
            is Bp2wRtBpIng -> parsed.isDeflate
            is Bp3RtBpIng -> parsed.isDeflate
            is Bp3RtBpEcgIng -> parsed.isDeflate
            else -> null
        } == true

        val progressValue = pressure?.toFloat() ?: 0f
        val markerValue = pulse?.toFloat() ?: 0f

        with(binding) {
            mcvDeviceDetails.isVisible = false
            nsvInstructions.isVisible = false
            llEcgMeasure.isVisible = false
            llFooterBpMeasureResult.isVisible = false
            llBpMeasureError.isVisible = false

            cvBpMeasure.isVisible = true
            tvMeasureTitle.isVisible = true
            tvMeasureSubtitle.isVisible = true
            clBPCalc.isVisible = true

            tvMeasureTitle.text = getString(R.string.please_remain_calm_and_still)
            tvMeasureSubtitle.text = getString(
                R.string.your_blood_pressure_measurement_is_in_progress
            )

            tvMeasureSystolicValue.text = pressure.toString()
            gaugeBPView.primaryValue = progressValue
            gaugeBPView.secondaryValue = markerValue

            tvMeasureSystolicReading.text = getString(R.string.calculating)
            tvMeasureDiastolicReading.text = getString(R.string.calculating)
            tvMeasurePulseReading.text = getString(R.string.calculating)
        }
    }

    private fun renderBpResult(payload: Any?, statusText: String, detailText: String) {
        val parsed = when (payload) {
            is Bp2RtBpResult -> payload
            is Bp2wRtBpResult -> payload
            is Bp3RtBpResult -> payload
            is ByteArray -> runCatching { Bp2RtBpResult(payload) }.getOrNull()
                ?: runCatching { Bp2wRtBpResult(payload) }.getOrNull()
                ?: runCatching { Bp3RtBpResult(payload) }.getOrNull()

            else -> null
        }
        //Log.e("BP Machine", "renderBpResult parsed: $parsed")

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

                llFooterBpMeasureResult.isVisible = true

                gaugeBPView.primaryValue = sys.toFloat()
                gaugeBPView.secondaryValue = pulse.toFloat()

                bpViewModel.startSync()
            } else {
                clBPCalc.isVisible = false
                tvMeasureTitle.isVisible = false
                tvMeasureSubtitle.isVisible = false
                llFooterBpMeasureResult.isVisible = false
                llBpMeasureError.isVisible = true
            }
        }
    }

    private fun renderEcgProgress(
        payload: Any?,
        waveformData: FloatArray?,
        statusText: String,
        detailText: String,
    ) {
        val parsed = when (payload) {
            is Bp2RtEcgIng -> payload
            is Bp2wRtEcgIng -> payload
            is Bp3RtEcgIng -> payload
            is Bp3RtBpEcgIng -> payload
            is ByteArray -> runCatching { Bp2RtEcgIng(payload) }.getOrNull()
                ?: runCatching { Bp2wRtEcgIng(payload) }.getOrNull()
                ?: runCatching { Bp3RtEcgIng(payload) }.getOrNull()
                ?: runCatching { Bp3RtBpEcgIng(payload) }.getOrNull()

            else -> null
        }
        //Log.e("BP Machine", "renderEcgProgress parsed: $parsed")

        val hr = when (parsed) {
            is Bp2RtEcgIng -> parsed.hr
            is Bp2wRtEcgIng -> parsed.hr
            is Bp3RtEcgIng -> parsed.hr
            is Bp3RtBpEcgIng -> parsed.hr
            else -> null
        }
        val leadOff = when (parsed) {
            is Bp2RtEcgIng -> parsed.isLeadOff
            is Bp2wRtEcgIng -> parsed.isLeadOff
            is Bp3RtEcgIng -> parsed.isLeadOff
            is Bp3RtBpEcgIng -> parsed.isLeadOff
            else -> null
        } == true
        val poorSignal = when (parsed) {
            is Bp2RtEcgIng -> parsed.isPoolSignal
            is Bp2wRtEcgIng -> parsed.isPoolSignal
            is Bp3RtEcgIng -> parsed.isPoolSignal
            is Bp3RtBpEcgIng -> parsed.isPoolSignal
            else -> null
        } == true
        val duration = when (parsed) {
            is Bp2RtEcgIng -> parsed.curDuration
            is Bp2wRtEcgIng -> parsed.curDuration
            is Bp3RtEcgIng -> parsed.curDuration
            is Bp3RtBpEcgIng -> parsed.curDuration
            else -> null
        }
        with(binding) {
            mcvDeviceDetails.isVisible = false
            nsvInstructions.isVisible = false
            cvBpMeasure.isVisible = false
            llEcgMeasure.isVisible = true
            llEcgBtns.isVisible = false

            tvEcgMeasureTitle.setTextColor(Color.parseColor("#ADADAD"))
            tvEcgMeasureTitle.text = getString(R.string.ecg_reading_in_progress)
            tvEcgMeasureSubtitle.text = getString(R.string.please_remain_calm_and_still)
            tvEcgWaveformValue.text = if (leadOff || poorSignal) {
                "-1"
            } else {
                duration?.toString() ?: "-1"
            }
            tvPulseRateValue.text = hr?.toString() ?: "--"

            if (leadOff || poorSignal) {
                ecgWaveformView.clearWave()
            } else {
                ecgWaveformView.appendWaveData(waveformData)
            }
        }
    }

    private fun renderEcgResult(payload: Any?, statusText: String, detailText: String) {
        val parsed = when (payload) {
            is Bp2RtEcgResult -> payload
            is Bp2wRtEcgResult -> payload
            is Bp3RtEcgResult -> payload
            is ByteArray -> runCatching { Bp2RtEcgResult(payload) }.getOrNull()
                ?: runCatching { Bp2wRtEcgResult(payload) }.getOrNull()
                ?: runCatching { Bp3RtEcgResult(payload) }.getOrNull()

            else -> null
        }
        //Log.e("BP Machine", "renderEcgResult parsed: $parsed")

        val hr = when (parsed) {
            is Bp2RtEcgResult -> parsed.hr
            is Bp2wRtEcgResult -> parsed.hr
            is Bp3RtEcgResult -> parsed.hr
            else -> 0
        }
        val result = when (parsed) {
            is Bp2RtEcgResult -> parsed.result
            is Bp2wRtEcgResult -> parsed.result
            is Bp3RtEcgResult -> parsed.result
            else -> null
        } ?: 0

        val diagnosisText = when (parsed) {
            is Bp2RtEcgResult -> parsed.diagnosis?.resultMess
            is Bp2wRtEcgResult -> parsed.diagnosis?.resultMess
            is Bp3RtEcgResult -> parsed.diagnosis?.resultMess
            else -> null
        }?.toString().orEmpty()

        with(binding) {
            mcvDeviceDetails.isVisible = false
            nsvInstructions.isVisible = false
            cvBpMeasure.isVisible = false
            llEcgMeasure.isVisible = true
            //llEcgBtns.isVisible = true

            tvEcgMeasureTitle.text = diagnosisText.ifBlank {
                getString(R.string.ecg_reading_in_progress)
            }
            if (result != 4) {
                tvEcgMeasureTitle.setTextColor(Color.parseColor("#F44545"))
            }
            if (hr != 0) {
                bpViewModel.startSync()
            }
            tvEcgMeasureSubtitle.text = getString(R.string.analysis_complete_results_are_ready)
            tvPulseRateValue.text = hr?.toString() ?: "--"
            ecgWaveformView.clearWave()
        }
    }

    private fun renderAirBpPressure(pressure: Int) {

    }

    private fun renderAirBpState(state: Int) {

    }

    private fun renderAirBpResult(result: AirBpRtResult) {

    }

    private fun renderError(error: BpError) {
        val message = when (error) {
            is BpError.Initialization -> error.message
            is BpError.Scan -> error.message
            is BpError.Connection -> error.message
            is BpError.Command -> error.message
            is BpError.Unknown -> error.message
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

    private fun updateInstructionLayouts() = with(binding) {
        val isBtOn = bluetoothManager.bluetoothState.value == BluetoothState.ON
        val isConnected = bpViewModel.connectionState.value is BpConnectionState.Connected

        llBtOff.isVisible = !isBtOn

        ivIns.isVisible = isBtOn
        if (isBtOn) {
            val insRes = if (readingType == BPMachineReadingType.ECG) {
                R.drawable.ic_ecg_ins
            } else {
                R.drawable.ic_bp_machine_reading_ins
            }
            ivIns.setImageResource(insRes)
        }

        llBpMachineOff.isVisible =
            isBtOn && !isConnected && readingType == BPMachineReadingType.BLOOD_PRESSURE
        llEcgOff.isVisible = isBtOn && !isConnected && readingType == BPMachineReadingType.ECG
        llBpMachineOn.isVisible =
            isBtOn && isConnected && readingType == BPMachineReadingType.BLOOD_PRESSURE
        llEcgOn.isVisible = isBtOn && isConnected && readingType == BPMachineReadingType.ECG

        batteryView.isVisible = isBtOn && isConnected
        tvBatteryLevel.isVisible = isBtOn && isConnected

        mcvDeviceDetails.isVisible = true
        nsvInstructions.isVisible = true
        cvBpMeasure.isVisible = false
        llEcgMeasure.isVisible = false
    }

    private fun readingModeLabel(): String {
        return when (readingType) {
            BPMachineReadingType.BLOOD_PRESSURE -> "Blood Pressure"
            BPMachineReadingType.ECG -> "ECG"
            null -> "BP Machine"
        }
    }

    private fun airBpStateLabel(state: Int): String {
        return when (state) {
            0 -> "Inflating"
            1 -> "Stop pumping"
            2 -> "Measuring"
            3 -> "Measure result"
            4 -> "Pump again"
            5 -> "Measure failed, noise"
            6 -> "Measure failed, weak signal"
            7 -> "Measure failed, air leaking"
            8 -> "Measure failed, air blocked"
            9 -> "System error"
            10 -> "Not enough pumping"
            11 -> "Over range"
            12 -> "Pressure over 300 mmHg"
            13 -> "Pump timeout"
            14 -> "Irregular heart beat detected"
            15 -> "Disturb detected"
            16 -> "User deflate"
            17 -> "Over pumped"
            else -> "Unknown state ($state)"
        }
    }

    private fun dateText(result: AirBpRtResult): String {
        return buildString {
            append(result.year)
            append("-")
            append(result.month)
            append("-")
            append(result.day)
            append(" ")
            append(result.hour)
            append(":")
            append(result.minute)
            append(":")
            append(result.second)
        }
    }

    override fun onClick(view: View?) {
        if (view === binding.header.ivBack) {
            findNavController().popBackStack()
        }
        if (view === binding.btnSeeAllReadings) {
            findNavController().popBackStack()
        }
        if (view === binding.btnRetakeReadings) {
            isResultSuccessOrFail = false
            updateInstructionLayouts()
        }
        if (view === binding.btnRetryMeasurement) {
            isResultSuccessOrFail = false
            updateInstructionLayouts()
        }
        if (view === binding.btnSeeAllReadingsECG) {
            findNavController().popBackStack()
        }
        if (view === binding.btnRetakeReadingsECG) {
            isResultSuccessOrFail = false
            updateInstructionLayouts()
        }
    }

    override fun onDestroyView() {
        hideProgress()
        stopBp2Realtime()
        super.onDestroyView()
    }

    fun stopBp2Realtime() {
        (bpViewModel.connectionState.value as? BpConnectionState.Connected)?.let {
            when (val model = it.device.model) {
                Bluetooth.MODEL_BP2, Bluetooth.MODEL_BP2A, Bluetooth.MODEL_BP2T -> bpViewModel.stopBp2Realtime(
                    model
                )

                Bluetooth.MODEL_BP2W -> bpViewModel.stopBp2wRealtime(model)
                Bluetooth.MODEL_BP3 -> bpViewModel.stopBp3Realtime(model)
            }
        }
    }
}
