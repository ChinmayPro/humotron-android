package com.humotron.app.ui.device.cuff

import android.graphics.Color
import android.os.Build
import android.os.Bundle
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
import com.humotron.app.databinding.FragmentSmartCuffEcgReadingBinding
import com.humotron.app.domain.modal.BPMachineDeviceStatus
import com.humotron.app.domain.modal.BPMachineReadingType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.ui.navigation.NavKeys
import com.lepu.blepro.objs.Bluetooth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.lepu.blepro.ext.bp2.RtEcgIng as Bp2RtEcgIng
import com.lepu.blepro.ext.bp2.RtEcgResult as Bp2RtEcgResult
import com.lepu.blepro.ext.bp2w.RtEcgIng as Bp2wRtEcgIng
import com.lepu.blepro.ext.bp2w.RtEcgResult as Bp2wRtEcgResult
import com.lepu.blepro.ext.bp3.RtBpEcgIng as Bp3RtBpEcgIng
import com.lepu.blepro.ext.bp3.RtEcgIng as Bp3RtEcgIng
import com.lepu.blepro.ext.bp3.RtEcgResult as Bp3RtEcgResult

@AndroidEntryPoint
class SmartCuffECGReadingFragment :
    BaseFragment(R.layout.fragment_smart_cuff_ecg_reading),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartCuffEcgReadingBinding
    private val bpViewModel: BpMachineViewModel by activityViewModels()

    @Inject
    lateinit var bluetoothManager: BluetoothManager

    private var userDevice: UserDevice? = null
    private var readingType: BPMachineReadingType? = null
    private var isResultSuccessOrFail: Boolean? = false

    private var hasNavigatedToResult = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartCuffEcgReadingBinding.bind(view)
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
        binding.btnStop.setOnClickListener(this@SmartCuffECGReadingFragment)
        binding.btnSeeAllReadingsECG.setOnClickListener(this@SmartCuffECGReadingFragment)
        binding.btnRetakeReadingsECG.setOnClickListener(this@SmartCuffECGReadingFragment)
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

        // Machine-only header/instruction views do not exist in the SmartCuff ECG layout.
        tvDeviceName.text = userDevice?.deviceFacingName ?: userDevice?.deviceName ?: ""
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
                            Log.e("SmartCuff ECG", "Connected")
                            bpViewModel.startBp2Realtime(state.device.model)
                        }

                        Bluetooth.MODEL_BP2W -> bpViewModel.getBp2wInfo(state.device.model)
                        Bluetooth.MODEL_BP3 -> bpViewModel.getBp3Info(state.device.model)
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
                is BpMachineEvent.Bp2.RtData -> {
                    val (_, rtData) = event
                    renderEcgRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        waveformData = rtData.param.ecgFloatsFilter,
                        source = "BP2"
                    )
                }

                is BpMachineEvent.Bp2W.RtData -> {
                    val (_, rtData) = event
                    renderEcgRtData(
                        status = rtData.status,
                        paramDataType = rtData.param.paramDataType,
                        payload = rtData.param.paramData,
                        waveformData = rtData.param.ecgFloatsFilter,
                        source = "BP2W"
                    )
                }

                is BpMachineEvent.Bp3.RtData -> {
                    val (_, rtData) = event
                    renderEcgRtData(
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

    private fun renderEcgRtData(
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
        val deviceStatusMsg = BPMachineDeviceStatus.fromValue(deviceStatus).label
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

        if (deviceStatusEnum == BPMachineDeviceStatus.STATUS_READY && isResultSuccessOrFail == false) {
            showDefaultUi()
        }

        when (paramDataType) {
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

            else -> Unit
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
            tvMeasureTitle.isVisible = true
            tvMeasureSubtitle.isVisible = true
            llEcgMeasure.isVisible = true
            llEcgBtns.isVisible = false

            tvMeasureTitle.setTextColor(Color.parseColor("#ADADAD"))
            tvMeasureTitle.text = getString(R.string.ecg_reading_in_progress)
            tvMeasureSubtitle.text = getString(R.string.please_remain_calm_and_still)
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

            tvPulseRateValue.contentDescription = buildString {
                append(statusText)
                append(" | ")
                append(detailText)
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
            tvMeasureTitle.isVisible = true
            tvMeasureSubtitle.isVisible = true
            llEcgMeasure.isVisible = true
            llEcgBtns.isVisible = true

            /*tvEcgMeasureTitle.text = diagnosisText.ifBlank {
                getString(R.string.ecg_reading_in_progress)
            }
            if (result != 4) {
                tvEcgMeasureTitle.setTextColor(Color.parseColor("#F44545"))
            }*/
            if (hr != 0) {
                bpViewModel.startSync()
                if (!hasNavigatedToResult) {
                    hasNavigatedToResult = true
                    findNavController().navigate(
                        R.id.fragmentSmartCuffECGResult,
                        Bundle().apply {
                            putParcelable(NavKeys.WEARABLE, userDevice)
                            putParcelable(NavKeys.READING_TYPE, readingType)
                        }
                    )
                }
            }
            //tvEcgMeasureSubtitle.text = getString(R.string.analysis_complete_results_are_ready)
            tvPulseRateValue.text = hr.toString()
            ecgWaveformView.clearWave()

            tvPulseRateValue.contentDescription = buildString {
                append(statusText)
                append(" | ")
                append(detailText)
            }
        }
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
            // Machine-only error panel is not present in the SmartCuff ECG layout.
            tvMeasureTitle.isVisible = true
            tvMeasureSubtitle.isVisible = true
            llEcgMeasure.isVisible = true
            llEcgBtns.isVisible = false

            tvMeasureTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            tvMeasureTitle.text = message
            tvMeasureSubtitle.text = getString(R.string.don_t_worry_it_happens)
            ecgWaveformView.clearWave()
            tvPulseRateValue.text = "--"
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
        llEcgMeasure.isVisible = true
        llEcgBtns.isVisible = false

        tvMeasureTitle.setTextColor(Color.parseColor("#ADADAD"))
        tvMeasureTitle.text = getString(R.string.ecg_reading_in_progress)
        tvMeasureSubtitle.text = getString(R.string.please_remain_calm_and_still)
        tvEcgWaveformValue.text = "-1"
        tvPulseRateValue.text = "--"
        ecgWaveformView.clearWave()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnStop -> {
                stopBpRealtime()
                findNavController().popBackStack()
            }

            binding.btnSeeAllReadingsECG -> {
                findNavController().popBackStack()
            }

            binding.btnRetakeReadingsECG -> {
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
