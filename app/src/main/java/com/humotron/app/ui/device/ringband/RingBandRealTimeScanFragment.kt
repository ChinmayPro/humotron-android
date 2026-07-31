package com.humotron.app.ui.device.ringband

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentRingBandRealTimeScanBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.param.BaselineScanDataParam
import com.humotron.app.domain.modal.param.SaveScanDataParam
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.domain.modal.response.PastScanData
import com.humotron.app.domain.modal.ui.HealthScanResult
import com.humotron.app.ui.device.HealthScanViewModel
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import com.jstyle.blesdk2208a.Util.BleSDK
import com.jstyle.blesdk2208a.callback.DataListener2025
import com.jstyle.blesdk2208a.constant.BleConst
import com.jstyle.blesdk2208a.constant.DeviceKey
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class RingBandRealTimeScanFragment :
    BaseFragment(R.layout.fragment_ring_band_real_time_scan),
    View.OnClickListener {

    private lateinit var binding: FragmentRingBandRealTimeScanBinding
    private val viewModel: HealthScanViewModel by viewModels()
    private var userDevice: GetAllDeviceResponse.Data.UserDevice? = null
    private var healthScanItem: HealthScanItem? = null

    @javax.inject.Inject
    lateinit var bandBleManager: BandBleManager

    private var timerJob: Job? = null
    private val scanResults = mutableListOf<HealthScanResult>()
    private var isScanning = false
    private var baseLine: Double = 0.0
    private var current: Double = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRingBandRealTimeScanBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeData()
        observeBleEvents()
    }

    private fun initClicks() {
        binding.btnStopScan.setOnClickListener(this@RingBandRealTimeScanFragment)
    }

    private fun initViews() = with(binding) {
        userDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(
                NavKeys.WEARABLE,
                GetAllDeviceResponse.Data.UserDevice::class.java
            )
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        healthScanItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM, HealthScanItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM)
        }

        healthScanItem?.let {
            when (it.type) {
                HealthScanType.HRV -> {
                    binding.scanAnimationView.setScanData("0", "ms", "HRV")
                    binding.scanAnimationView.setScanColor(R.color.series)
                }

                HealthScanType.HR -> {
                    binding.scanAnimationView.setScanData("0", "bpm", "HEART RATE")
                    binding.scanAnimationView.setScanColor(R.color.watch)
                }

                HealthScanType.SPO2 -> {
                    binding.scanAnimationView.setScanData("0", "%", "SPO2")
                    binding.scanAnimationView.setScanColor(R.color.cool)
                }

                HealthScanType.TEMPERATURE -> {
                    binding.scanAnimationView.setScanData("0", "°C", "TEMPERATURE")
                    binding.scanAnimationView.setScanColor(R.color.series)
                }
            }

            userDevice?.let { wearable ->
                viewModel.getBaselineScanData(
                    BaselineScanDataParam(
                        type = it.type.value,
                        deviceId = wearable.id ?: ""
                    )
                )
            }
        }

        val deviceType = DeviceType.from(userDevice?.deviceName)
        if (deviceType == DeviceType.BAND) {
            startScan()
        } else {
            // For Ring or other devices, just start the progress animation
            binding.scanAnimationView.animateProgressContinuously(durationMillis = 60_000L)
        }
    }

    private fun startScan() {
        scanResults.clear()
        healthScanItem?.let {
            val command = BleSDK.StartDeviceMeasurementWithType(it.type.type, true, 120000)
            bandBleManager.writeValue(command)
            startTimer()
        }
        isScanning = true
        binding.scanAnimationView.animateProgressContinuously(durationMillis = 60_000L)
    }

    private fun stopScan(isFinished: Boolean = false) {
        healthScanItem?.let {
            val command = BleSDK.StartDeviceMeasurementWithType(it.type.type, false, 120000)
            bandBleManager.writeValue(command)
        }
        stopTimer()
        isScanning = false

        if (isFinished && scanResults.isNotEmpty()) {
            saveResultsToApi()
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            var seconds = 60 // 60 seconds for real-time scan as per requirement
            while (seconds >= 0) {
                delay(1000.milliseconds)
                seconds--
            }
            if (isScanning) stopScan(true)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun observeBleEvents() {
        lifecycleScope.launch {
            bandBleManager.bleEvents.collect { event ->
                if (event.action != BandBleManager.ACTION_DATA_AVAILABLE) return@collect
                val value = event.value ?: return@collect

                BleSDK.DataParsingWithData(value, object : DataListener2025 {
                    override fun dataCallback(maps: MutableMap<String, Any>?) {
                        if (maps == null) return
                        val dataType = maps[DeviceKey.DataType] as? String ?: return

                        activity?.runOnUiThread {
                            when (dataType) {
                                BleConst.MeasurementHrvCallback,
                                BleConst.MeasurementHeartCallback,
                                BleConst.MeasurementTemperatureCallback,
                                BleConst.MeasurementOxygenCallback,
                                    -> {
                                    val data = maps[DeviceKey.Data] as? Map<*, *>
                                    updateLiveUI(data)
                                }

                                BleConst.StopMeasurementHrvCallback,
                                BleConst.StopMeasurementHeartCallback,
                                BleConst.StopMeasurementOxygenCallback,
                                    -> {
                                    if (isScanning) stopScan(true)
                                }
                            }
                        }
                    }

                    override fun dataCallback(value: ByteArray?) {}
                })
            }
        }
    }

    private fun updateLiveUI(data: Map<*, *>?) {
        if (data == null) return

        val hrv = data[DeviceKey.HRV]?.toString()?.toIntOrNull() ?: 0
        val heartRate = data[DeviceKey.HeartRate]?.toString()?.toIntOrNull() ?: 0
        val bloodOxygen = data[DeviceKey.Blood_oxygen]?.toString()?.toIntOrNull() ?: 0
        val temperature = data[DeviceKey.KHrvTempValue]?.toString()?.toFloatOrNull() ?: 0f
        val stress = data[DeviceKey.Stress]?.toString()?.toIntOrNull() ?: 0
        val highPressure = data[DeviceKey.HighPressure]?.toString()?.toIntOrNull() ?: 0
        val lowPressure = data[DeviceKey.LowPressure]?.toString()?.toIntOrNull() ?: 0

        val result = HealthScanResult(
            stress = stress,
            lowPressure = lowPressure,
            heartRate = heartRate,
            temperature = temperature,
            bloodOxygen = bloodOxygen,
            hrv = hrv,
            highPressure = highPressure
        )

        healthScanItem?.let {
            val shouldStore = when (it.type) {
                HealthScanType.HRV -> hrv > 0
                HealthScanType.HR -> heartRate > 0
                HealthScanType.SPO2 -> bloodOxygen > 0
                HealthScanType.TEMPERATURE -> temperature > 0
            }
            if (shouldStore) {
                scanResults.add(result)
                current = when (it.type) {
                    HealthScanType.HRV -> hrv.toDouble()
                    HealthScanType.HR -> heartRate.toDouble()
                    HealthScanType.SPO2 -> bloodOxygen.toDouble()
                    HealthScanType.TEMPERATURE -> temperature.toDouble()
                }

                binding.scanAnimationView.setScanData(
                    current.toInt().toString(),
                    it.type.getUnit(),
                    it.type.getDisplayName().uppercase()
                )
            }
        }
    }

    private fun observeData() {
        viewModel.getSaveScanDataResponse().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                resource.data?.data?.let {
                    current = it.current
                    val pastScanData = PastScanData(
                        id = it.id,
                        userId = it.userId,
                        deviceId = it.deviceId,
                        baseline = it.baseline,
                        current = it.current,
                        isDeleted = it.isDeleted,
                        updatedAt = it.updatedAt,
                        type = it.type,
                        createdAt = it.createdAt
                    )
                    navigateToReport(pastScanData)
                }
            }
        }

        viewModel.getBaselineScanDataResponse().observe(viewLifecycleOwner) { resource ->
            if (resource.status == Status.SUCCESS) {
                resource.data?.data?.let {
                    baseLine = it
                }
            }
        }
    }

    private fun saveResultsToApi() {
        val type = healthScanItem?.type ?: return
        if (scanResults.isEmpty()) return

        val lastRecord = scanResults.lastOrNull()
        lastRecord?.let { record ->
            val currentValue = when (type) {
                HealthScanType.HRV -> record.hrv.toDouble()
                HealthScanType.HR -> record.heartRate.toDouble()
                HealthScanType.SPO2 -> record.bloodOxygen.toDouble()
                HealthScanType.TEMPERATURE -> record.temperature.toDouble()
            }

            val deviceId = userDevice?.id ?: ""
            if (deviceId.isEmpty()) return

            viewModel.saveScanData(
                SaveScanDataParam(
                    baseline = baseLine,
                    current = currentValue,
                    type = type.value,
                    deviceId = deviceId
                )
            )
        }
    }

    private fun navigateToReport(pastScanData: PastScanData? = null) {
        findNavController().navigate(
            R.id.fragmentScanReportRingBand,
            bundleOf(
                NavKeys.HEALTH_SCAN_ITEM to healthScanItem,
                NavKeys.WEARABLE to userDevice,
                NavKeys.PAST_SCAN to pastScanData
            )
        )
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.btnStopScan -> {
                if (isScanning) {
                    stopScan()
                }
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        if (isScanning) {
            stopScan()
        }
        super.onDestroyView()
    }
}