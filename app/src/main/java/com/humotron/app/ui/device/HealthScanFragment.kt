package com.humotron.app.ui.device

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.humotron.app.R
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentHealthScanBinding
import com.humotron.app.domain.modal.param.BaselineScanDataParam
import com.humotron.app.domain.modal.param.SaveScanDataParam
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable
import com.humotron.app.domain.modal.ui.HealthScanResult
import com.humotron.app.domain.modal.ui.SmartBraceletHRVReading
import com.humotron.app.ui.device.adapter.HealthScanItem
import com.humotron.app.ui.device.adapter.HealthScanType
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.BandSyncManager
import androidx.navigation.fragment.findNavController
import com.humotron.app.util.ToastUtils
import com.jstyle.blesdk2208a.Util.BleSDK
import com.jstyle.blesdk2208a.callback.DataListener2025
import com.jstyle.blesdk2208a.constant.BleConst
import com.jstyle.blesdk2208a.constant.DeviceKey
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class HealthScanFragment : BaseFragment(R.layout.fragment_health_scan) {

    private lateinit var binding: FragmentHealthScanBinding
    private val viewModel: HealthScanViewModel by viewModels()
    private var wearable: Wearable? = null
    private var healthScanItem: HealthScanItem? = null
    private var timerJob: Job? = null
    private val scanResults = mutableListOf<HealthScanResult>()

    private var baseLine: Double = 0.0

    private var current: Double = 0.0

    @javax.inject.Inject
    lateinit var bandBleManager: BandBleManager

    @javax.inject.Inject
    lateinit var bandSyncManager: BandSyncManager

    private var isScanning = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHealthScanBinding.bind(view)

        initClicks()
        initData()
        observeData()
        observeBleEvents()
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnStartScan.setOnClickListener {
            if (isScanning) {
                stopScan()
            } else {
                val isSyncing = bandSyncManager.isBandSyncingLocal.value
                if (bandBleManager.isBusy() || isSyncing) {
                    Toast.makeText(
                        requireContext(),
                        "Band is busy, please wait.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                startScan()
            }
        }
        binding.btnViewResults.setOnClickListener {
            val result = scanResults.lastOrNull() ?: return@setOnClickListener
            val bundle = Bundle().apply {
                putParcelable("result", result)
                putParcelable(NavKeys.HEALTH_SCAN_ITEM, healthScanItem)
                putFloat("baseLine", baseLine.toFloat())
                putFloat("current", current.toFloat())
                putParcelable(NavKeys.WEARABLE, wearable)
            }
            findNavController().navigate(R.id.fragmentHealthScanResult, bundle)
        }
    }

    private fun startScan() {
        scanResults.clear()
        healthScanItem?.let {
            val command = BleSDK.StartDeviceMeasurementWithType(it.type.type, true, 120000)
            bandBleManager.writeValue(command)
            startTimer(it.type)
        }

        isScanning = true
        binding.llBase.visibility = View.GONE
        binding.llScanDone.visibility = View.GONE
        binding.llScanIncomplete.visibility = View.GONE
        binding.btnViewResults.visibility = View.GONE
        binding.btnStartScan.visibility = View.VISIBLE

        binding.tvLiveValue.text = "00"
        binding.tvLiveOxygenValue.text = "00"
        binding.tvHRVTime.text = "02:00"
        binding.tvTimer.text = "00:00"
        binding.tvOxygenTimer.text = "00:00:00"

        binding.btnStartScan.text = getString(R.string.stop)
        binding.btnStartScan.backgroundTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.d700)
        binding.btnStartScan.setTextColor(
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.white70
            )
        )

        healthScanItem?.let {
            when (it.type) {
                HealthScanType.HRV -> {
                    binding.llHrv.visibility = View.VISIBLE
                    binding.lottieAnimationView.playAnimation()
                }

                HealthScanType.HR -> {
                    binding.llHr.visibility = View.VISIBLE
                    binding.lottieViewHR.playAnimation()
                }

                HealthScanType.SPO2 -> {
                    binding.llOxygen.visibility = View.VISIBLE
                }

                HealthScanType.TEMPERATURE -> binding.llThermal.visibility = View.VISIBLE
            }
        }
    }

    private fun stopScan(isFinished: Boolean = false) {
        Log.e("Band Scan", "stopScan: ")
        healthScanItem?.let {
            val command = BleSDK.StartDeviceMeasurementWithType(it.type.type, false, 120000)
            bandBleManager.writeValue(command)
        }
        stopTimer()

        isScanning = false

        binding.llHrv.visibility = View.GONE
        binding.llHr.visibility = View.GONE
        binding.llOxygen.visibility = View.GONE
        binding.llThermal.visibility = View.GONE

        binding.lottieAnimationView.cancelAnimation()
        binding.lottieViewHR.cancelAnimation()

        if (isFinished) {
            if (scanResults.isNotEmpty()) {
                binding.llBase.visibility = View.GONE
                binding.btnStartScan.visibility = View.GONE
                binding.llScanDone.visibility = View.VISIBLE
                binding.llScanIncomplete.visibility = View.GONE
                binding.btnViewResults.visibility = View.VISIBLE
                saveResultsToApi()
            } else {
                binding.llBase.visibility = View.GONE
                binding.llScanDone.visibility = View.GONE
                binding.llScanIncomplete.visibility = View.VISIBLE
                binding.btnStartScan.visibility = View.VISIBLE
                binding.btnViewResults.visibility = View.GONE

                binding.btnStartScan.text = getString(R.string.scan_again)
                binding.btnStartScan.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), R.color.colorBgBtn)
                binding.btnStartScan.setTextColor(
                    ContextCompat.getColorStateList(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        } else {
            binding.llBase.visibility = View.VISIBLE
            binding.llScanDone.visibility = View.GONE
            binding.llScanIncomplete.visibility = View.GONE
            binding.btnStartScan.visibility = View.VISIBLE
            binding.btnViewResults.visibility = View.GONE

            binding.btnStartScan.text = getString(R.string.start_scan)
            binding.btnStartScan.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.colorBgBtn)
            binding.btnStartScan.setTextColor(
                ContextCompat.getColorStateList(
                    requireContext(),
                    R.color.black
                )
            )
        }
    }

    private fun initData() {
        wearable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.WEARABLE, Wearable::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.WEARABLE)
        }

        val item = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM, HealthScanItem::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(NavKeys.HEALTH_SCAN_ITEM)
        }

        item?.let {
            healthScanItem = it
            binding.llBase.visibility = View.VISIBLE
            binding.llHrv.visibility = View.GONE
            binding.llHr.visibility = View.GONE
            binding.llOxygen.visibility = View.GONE
            binding.llThermal.visibility = View.GONE
            binding.llScanDone.visibility = View.GONE
            binding.btnViewResults.visibility = View.GONE

            binding.tvScanUnit.text = it.type.getUnit()

            val displayName = it.type.getDisplayName()
            val displayName2 = it.type.getDisplayName2()
            val displayName3 = it.type.getDisplayName3()

            binding.tvScanType.text =
                getString(R.string.average_, displayName2)
            binding.tvBaseline.text =
                getString(R.string.your_baseline_, displayName)

            when (it.type) {
                HealthScanType.HRV -> {
                    binding.header.title.text = getString(R.string.ready_for_your_stress_check)
                    binding.ecvWhenShould.setDescription(getString(R.string.ans_1_hrv))
                    binding.ecvWhatDoesScan.setDescription(getString(R.string.ans_2_hrv))
                    binding.ecvWhatShouldIdDo.setDescription(getString(R.string.ans_3_hrv))
                }

                HealthScanType.HR -> {
                    binding.header.title.text = getString(R.string.check_your_body_load)
                    binding.ecvWhenShould.setDescription(getString(R.string.ans_1_hr))
                    binding.ecvWhatDoesScan.setDescription(getString(R.string.ans_2_hr))
                    binding.ecvWhatShouldIdDo.setDescription(getString(R.string.ans_3_hr))

                }

                HealthScanType.SPO2 -> {
                    binding.header.title.text = getString(R.string.oxygen_level_check)
                    binding.tvScanType.text = getString(R.string.usual_spo2)
                    binding.tvBaseline.text = getString(R.string.your_usual_oxygen_level)

                    binding.ecvWhenShould.setDescription(getString(R.string.ans_1_sop2))
                    binding.ecvWhatDoesScan.setDescription(getString(R.string.ans_2_sop2))
                    binding.ecvWhatShouldIdDo.setDescription(getString(R.string.ans_3_sop2))
                }

                HealthScanType.TEMPERATURE -> {
                    binding.header.title.text = getString(R.string.thermal_check)

                    binding.ecvWhenShould.setDescription(getString(R.string.ans_1_thermal))
                    binding.ecvWhatDoesScan.setDescription(getString(R.string.ans_2_thermal))
                    binding.ecvWhatShouldIdDo.setDescription(getString(R.string.ans_3_thermal))
                }
            }

            binding.tvNoBaselineTitle.text =
                getString(R.string.average_data_message, displayName)
            binding.tvNoBaselineDes.text =
                getString(
                    R.string.baseline_ready_message,
                    displayName,
                    displayName
                )
            binding.ecvWhenShould.setTitle(getString(R.string.when_should_i_run_a_, displayName3))

            wearable?.let { wearable ->
                viewModel.getBaselineScanData(
                    BaselineScanDataParam(
                        type = it.type.value,
                        deviceId = wearable.id ?: ""
                    )
                )
            }
        }
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
                        //PlutoLog.e("Band Scan", "$dataType")

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
        PlutoLog.e("updateLiveUI", "data: $data")

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
            }

            when (it.type) {
                HealthScanType.HRV -> {
                    ToastUtils.showShort(requireActivity(), hrv.toString())
                }

                HealthScanType.HR -> {
                    binding.tvLiveValue.text = heartRate.toString()
                }

                HealthScanType.SPO2 -> {
                    binding.tvLiveOxygenValue.text = bloodOxygen.toString()
                }

                HealthScanType.TEMPERATURE -> {
                    binding.tvLiveThermalValue.text = temperature.toString()
                }
            }
        }
    }

    private fun startTimer(type: HealthScanType) {
        timerJob?.cancel()
        timerJob = lifecycleScope.launch {
            var seconds = 10
            while (seconds >= 0) {
                val timeStr =
                    String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60)
                when (type) {
                    HealthScanType.HRV -> {
                        binding.tvHRVTime.text = timeStr
                        // Update title + description during HRV scan
                        updateLabels(seconds)
                    }

                    HealthScanType.HR -> binding.tvTimer.text = timeStr
                    HealthScanType.SPO2 -> {
                        val hours = seconds / 3600
                        val minutes = (seconds % 3600) / 60
                        val secs = seconds % 60

                        binding.tvOxygenTimer.text = String.format(
                            Locale.getDefault(),
                            "%02d:%02d:%02d",
                            hours,
                            minutes,
                            secs
                        )
                    }
                    HealthScanType.TEMPERATURE -> {
                        binding.tvThermalTimer.text = timeStr
                    }
                }
                delay(1000)
                seconds--
            }
            if (isScanning) stopScan(true)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
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

            val deviceId = wearable?.id ?: ""
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

    private fun observeData() {
        viewModel.getSaveScanDataResponse().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.data?.let {
                        current = it.current
                    }
                }

                Status.ERROR -> {
                }

                Status.LOADING -> {
                    // Show loading if needed
                }

                else -> {}
            }
        }

        viewModel.getBaselineScanDataResponse().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.shimmerBaseline.stopShimmer()
                    binding.shimmerBaseline.hideShimmer()
                    resource.data?.data?.let {
                        binding.tvValue.text = "$it"
                        baseLine = it

                    } ?: run {
                        binding.mcvBaseline.visibility = View.GONE
                    }
                }

                Status.ERROR -> {
                    binding.shimmerBaseline.stopShimmer()
                    binding.shimmerBaseline.hideShimmer()
                    binding.mcvBaseline.visibility = View.GONE
                    binding.cardNoBaseline.visibility = View.VISIBLE
                }

                Status.EXCEPTION -> {
                    binding.shimmerBaseline.stopShimmer()
                    binding.shimmerBaseline.hideShimmer()
                }

                Status.LOADING -> {
                    binding.shimmerBaseline.startShimmer()
                    binding.shimmerBaseline.showShimmer(true)
                }
            }
        }
    }

    private val readingList by lazy {
        listOf(
            SmartBraceletHRVReading(
                title = getString(R.string.hrv_title_initiating),
                description = getString(R.string.hrv_desc_initiating)
            ),
            SmartBraceletHRVReading(
                title = getString(R.string.hrv_title_measuring),
                description = getString(R.string.hrv_desc_measuring)
            ),
            SmartBraceletHRVReading(
                title = getString(R.string.hrv_title_comparing),
                description = getString(R.string.hrv_desc_comparing)
            ),
            SmartBraceletHRVReading(
                title = getString(R.string.hrv_title_finalizing),
                description = getString(R.string.hrv_desc_finalizing)
            )
        )
    }

    private fun updateLabels(time: Int) {
        var newTitle = ""
        var newDescription = ""

        when (time) {
            in 115..120 -> {
                newTitle = readingList[0].title
                newDescription = readingList[0].description
            }

            in 55..114 -> {
                newTitle = readingList[1].title
                newDescription = readingList[1].description
            }

            in 30..54 -> {
                newTitle = readingList[2].title
                newDescription = readingList[2].description
            }

            in 0..29 -> {
                newTitle = readingList[3].title
                newDescription = readingList[3].description
            }
        }

        if (binding.tvHrvScanTitle.text.toString() != newTitle ||
            binding.tvHrvScanDes.text.toString() != newDescription
        ) {
            setTextWithAnimation(binding.tvHrvScanTitle, newTitle, 900)
        }

        if (binding.tvHrvScanDes.text.toString() != newDescription) {
            binding.tvHrvScanDes.animate()
                .alpha(0f)
                .setDuration(250)
                .withEndAction {
                    binding.tvHrvScanDes.text = newDescription
                    binding.tvHrvScanDes.animate()
                        .alpha(1f)
                        .setDuration(250)
                        .start()
                }
                .start()
        }
    }

    private fun setTextWithAnimation(
        textView: TextView,
        newText: String,
        duration: Long,
    ) {
        textView.animate()
            .alpha(0f)
            .setDuration(duration / 2)
            .withEndAction {
                textView.text = newText
                textView.animate()
                    .alpha(1f)
                    .setDuration(duration / 2)
                    .start()
            }
            .start()
    }
}
