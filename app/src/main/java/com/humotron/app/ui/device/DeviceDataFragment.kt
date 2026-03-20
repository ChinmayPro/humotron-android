package com.humotron.app.ui.device

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import com.humotron.app.R
import com.humotron.app.bt.BleDevice
import com.humotron.app.core.App
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDeviceDataBinding
import com.humotron.app.domain.modal.response.ExerciseIntensityMetric
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable
import com.humotron.app.domain.modal.response.MetricType
import com.humotron.app.domain.modal.response.PhysicalRecoveryMetric
import com.humotron.app.domain.modal.response.SleepDurationMetric
import com.humotron.app.domain.modal.response.StressScoreMetric
import com.humotron.app.ui.connect.DeviceConnectedFragment
import com.humotron.app.ui.connect.HomeViewModel
import com.humotron.app.ui.device.adapter.MetricsAdapter
import com.humotron.app.util.STATE_DEVICE_CHARGING
import com.humotron.app.util.STATE_DEVICE_CONNECTED
import com.humotron.app.util.STATE_DEVICE_CONNECTING
import com.humotron.app.util.STATE_DEVICE_DISCHARGING
import com.humotron.app.util.STATE_DEVICE_DISCONNECTED
import com.humotron.app.util.convertDecimalHours
import com.humotron.app.util.formatDateToMMMddyyyy
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class DeviceDataFragment : BaseFragment(R.layout.fragment_device_data), View.OnClickListener {

    private lateinit var binding: FragmentDeviceDataBinding
    private val viewModel: DeviceViewModel by viewModels()
    private val app by lazy { requireActivity().application as App }
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var device: BleDevice? = null

    private lateinit var metricsAdapter: MetricsAdapter
    private val homeViewModel by activityViewModels<HomeViewModel>()
    private var wearable: Wearable? = null


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeviceDataBinding.bind(view)
        binding.header.title.text = "Humotron Smart Ring Metrics"

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        metricsAdapter = MetricsAdapter { item, dateTime ->
            val deviceId = wearable?.id
            val deviceName = wearable?.deviceName
            deviceId?.let {
                findNavController().navigate(
                    R.id.fragmentMetric,
                    bundleOf(
                        "id" to deviceId,
                        "dateTime" to dateTime,
                        "metric" to item,
                        "deviceName" to deviceName
                    )
                )
            }
        }
        binding.clTabMetrics.rvMetrics.adapter = metricsAdapter
        initClicks()
        getDataFromServer()

        if (app.deviceManager.connected.value == true) {
            app.deviceManager.registerCb()
            viewModel.getDeviceData()
        }

        app.deviceManager.isSyncingData.observe(viewLifecycleOwner) { isSyncing ->
            isSyncing?.let {
                if (!it) {
                    binding.progress.isVisible = false
                } else {
                    binding.progress.isVisible = true
                }
            }
        }

        app.deviceManager.batteryLevel.observe(viewLifecycleOwner) {
            when (it.first) {
                STATE_DEVICE_CONNECTING -> {
                    binding.tvDeviceStatus.text = "Connecting"
                }

                STATE_DEVICE_CONNECTED -> {
                    binding.ivDeviceStatus.setImageResource(R.drawable.dot_connected)
                    binding.tvDeviceStatus.text = "Connected"
                    binding.tvDeviceStatus.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorBgBtn
                        )
                    )
                    binding.batteryView.isVisible = true
                    binding.tvBatteryLevel.isVisible = true
                    binding.batteryView.isCharging = false
                    binding.btnTakeReading.isEnabled = true
                    binding.btnTakeReading.alpha = 1f
                    PlutoLog.e(
                        "Bluetooth",
                        "STATE_DEVICE_CONNECTED"
                    )

                }

                STATE_DEVICE_DISCONNECTED -> {
                    binding.ivDeviceStatus.setImageResource(R.drawable.dot_disconnected)
                    binding.tvDeviceStatus.text = "Disconnected"
                    binding.tvDeviceStatus.setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.disconnected
                        )
                    )
                    binding.batteryView.isVisible = false
                    binding.tvBatteryLevel.isVisible = false
                    binding.batteryView.isCharging = false
                    binding.btnTakeReading.isEnabled = false
                    binding.btnTakeReading.alpha = 0.5f
                    PlutoLog.e(
                        "Bluetooth",
                        "STATE_DEVICE_DISCONNECTED"
                    )
                }

                STATE_DEVICE_CHARGING -> {
                    binding.batteryView.isCharging = true
                    PlutoLog.e(
                        "Bluetooth",
                        "STATE_DEVICE_CHARGING"
                    )
                }

                STATE_DEVICE_DISCHARGING -> {
                    binding.batteryView.isCharging = false
                    PlutoLog.e(
                        "Bluetooth",
                        "STATE_DEVICE_DISCHARGING"
                    )
                }

            }
            binding.batteryView.batteryLevel = it.second
            binding.tvBatteryLevel.text = "${it.second}%"
        }

        app.deviceManager.connected.observe(viewLifecycleOwner) {
            DeviceConnectedFragment.device = device
            homeViewModel.currBtMac = device?.device?.address ?: ""
            if (it) {
                PlutoLog.e(
                    "Bluetooth",
                    "deviceManager connected"
                )
                viewModel.getDeviceData()
            }
        }
        app.deviceManager.bleAdapterEnabled.observe(viewLifecycleOwner) { isEnabled ->
            updateBtStatusIcon(isEnabled)
        }

        subscribeToObserver()
    }

    private fun subscribeToObserver() {
        /* lifecycleScope.launch {
             viewModel.deviceData.collect {
                 it.hrMapper.let { hr ->
                     binding.tvHeartRate.text = "${hr.hr}"
                     binding.tvHeartTime.text = hr.time
                     binding.tvHeartDate.text = hr.date
                 }

                 it.hrvMapper.let { hr ->
                     binding.tvHrv.text = "${hr.hrv}"
                     binding.tvHrvTime.text = hr.time
                     binding.tvHrvDate.text = hr.date
                 }

                 it.tempMapper.let { temp ->
                     binding.tvTemp.text = "${temp.temp}"
                     binding.tvTempTime.text = temp.time
                     binding.tvTempDate.text = temp.date
                 }

                 it.stepMapper.let { step ->
                     binding.tvSteps.text = "${step.step}"
                     binding.tvStepsTime.text = step.time
                     binding.tvStepsDate.text = step.date
                 }
                 it.sleepMapper.let { sleep ->
                     binding.tvSleepDuration.text = "${sleep.duration / (1000 * 60)}"
                     binding.tvSleepTime.text = sleep.date
                     binding.tvSleepEfficiency.text = sleep.efficiency.formatPercent()
                     binding.tvDeepSleepEfficiency.text = "${sleep.deepEfficiency}"
                 }

             }
         }*/

        viewModel.getRingReadingData().observe(viewLifecycleOwner) { state ->
            when (state.status) {
                Status.SUCCESS -> {
                    val data = state.data?.data ?: return@observe
                    setComputedMetricsData(
                        data.exerciseIntensityMetric,
                        data.physicalRecoveryMetric,
                        data.stressScoreMetric,
                        data.sleepDurationMetric
                    )
                }

                Status.ERROR -> {
                }

                Status.EXCEPTION -> {
                }

                Status.LOADING -> {

                }
            }
        }

        viewModel.getAllMetricsByDeviceIdData().observe(viewLifecycleOwner) { state ->
            when (state.status) {
                Status.SUCCESS -> {
                    val data = state.data?.data ?: return@observe
                    data.metrics?.let { metrics ->
                        val filteredMetrics = metrics.filter { it.metricOrder != 0 }
                            .sortedBy { it.metricOrder }
                        metricsAdapter.submitList(filteredMetrics)
                    }
                    binding.tvLastSyncTime.text =
                        viewModel.getDaysAgoString(data.device?.dataSync ?: "")

                    val deviceName = wearable?.deviceName
                    deviceName?.let {
                        when (deviceName) {
                            "WristBand" -> {
                                setComputedMetricsData(
                                    data.wristbandMetrics?.exerciseIntensityMetric,
                                    data.wristbandMetrics?.physicalRecoveryMetric,
                                    data.wristbandMetrics?.stressScoreMetric,
                                    data.wristbandMetrics?.sleepDurationMetric
                                )
                            }
                        }
                    }
                }

                Status.ERROR -> {
                    // Handle error
                }

                Status.EXCEPTION -> {
                    // Handle exception
                }

                Status.LOADING -> {
                    // Handle loading
                }
            }
        }
    }

    private fun setComputedMetricsData(
        exerciseIntensityMetric: ExerciseIntensityMetric?,
        physicalRecoveryMetric: PhysicalRecoveryMetric?,
        stressScoreMetric: StressScoreMetric?,
        sleepDurationMetric: SleepDurationMetric?,
    ) {
        exerciseIntensityMetric?.let { metric ->
            binding.clTabMetrics.cardExerciseIntensity.visibility = View.VISIBLE
            binding.clTabMetrics.tvExerciseScore.text =
                String.format(Locale.getDefault(), "%.2f", metric.score)
            binding.clTabMetrics.tvExerciseUnit.text = metric.unit
            val date = formatDateToMMMddyyyy(metric.time)
            binding.clTabMetrics.tvExerciseTime.text = date
        } ?: run {
            //binding.cardExerciseIntensity.visibility = View.GONE
        }
        physicalRecoveryMetric?.let { metric ->
            binding.clTabMetrics.cardPhysicalRecovery.visibility = View.VISIBLE
            binding.clTabMetrics.tvPhysicalRecoveryValue.text =
                String.format(Locale.getDefault(), "%.2f", metric.value)
            binding.clTabMetrics.tvPhysicalRecoveryUnit.text = metric.unit
            val date = formatDateToMMMddyyyy(metric.time)
            binding.clTabMetrics.tvPhysicalRecoveryTime.text = date
        } ?: run {
            //binding.cardPhysicalRecovery.visibility = View.GONE
        }

        stressScoreMetric?.let { metric ->
            binding.clTabMetrics.cardStressScore.visibility = View.VISIBLE
            binding.clTabMetrics.tvStressScoreValue.text =
                String.format(Locale.getDefault(), "%.2f", metric.value)
            binding.clTabMetrics.tvStressScoreUnit.text = metric.unit
            val date = formatDateToMMMddyyyy(metric.time)
            binding.clTabMetrics.tvStressScoreTime.text = date
        } ?: run {
            //binding.cardStressScore.visibility = View.GONE
        }

        sleepDurationMetric?.let { metric ->
            binding.clTabMetrics.cardSleepMetrics.visibility = View.VISIBLE
            val date = formatDateToMMMddyyyy(metric.time)
            binding.clTabMetrics.tvSleepTime.text = date

            metric.value?.let { value ->
                val (hours, minutes) = convertDecimalHours(value.totalSleepHours)
                binding.clTabMetrics.tvSleepDurationHr.text = "$hours"
                binding.clTabMetrics.tvSleepDurationMinute.text = "$minutes"

                binding.clTabMetrics.tvSleepEfficiency.text = "${value.avgEfficiency}"
            }
        } ?: run {
            //binding.cardSleepMetrics.visibility = View.GONE
        }
    }

    private fun getDataFromServer() {
        wearable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("wearable", Wearable::class.java)
        } else {
            arguments?.getParcelable("wearable")
        }
        val deviceId = wearable?.id
        deviceId?.let {
            when (wearable?.deviceName) {
                "WristBand" -> {
                    viewModel.getAllMetricsByDeviceId(it)
                }

                else -> {
                    viewModel.getRingReadingData(it)
                    viewModel.getAllMetricsByDeviceId(it)
                }
            }
        }

        val deviceImage = wearable?.deviceImage
        deviceImage?.let {
            Glide.with(requireActivity())
                .load("${it[0]}")
                .into(binding.ivDevice)
        }
        binding.tvDeviceName.text = wearable?.deviceName ?: ""
        binding.header.title.text = "${wearable?.deviceFacingName} Metrics"
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener(this)
        binding.clTabMetrics.cardExerciseIntensity.setOnClickListener(this)
        binding.clTabMetrics.cardPhysicalRecovery.setOnClickListener(this)
        binding.clTabMetrics.cardStressScore.setOnClickListener(this)
        binding.clTabMetrics.cardSleepMetrics.setOnClickListener(this)

        binding.tabLayout.addOnTabSelectedListener(object :
            TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {

                when (tab?.position) {
                    0 -> {
                        binding.clTabMetrics.root.visibility = View.VISIBLE
                        binding.clTabInsights.root.visibility = View.GONE
                    }

                    1 -> {
                        binding.clTabInsights.root.visibility = View.VISIBLE
                        binding.clTabMetrics.root.visibility = View.GONE
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    override fun onClick(p0: View?) {
        val isWristBand = wearable?.deviceName == "WristBand"

        val exerciseIntensityMetric: ExerciseIntensityMetric?
        val physicalRecoveryMetric: PhysicalRecoveryMetric?
        val stressScoreMetric: StressScoreMetric?
        val sleepDurationMetric: SleepDurationMetric?
        val deviceId: String?
        val dataSync: String?
        val deviceName: String = wearable?.deviceName ?: ""

        if (isWristBand) {
            val data = viewModel.getAllMetricsByDeviceIdData().value?.data?.data
            exerciseIntensityMetric = data?.wristbandMetrics?.exerciseIntensityMetric
            physicalRecoveryMetric = data?.wristbandMetrics?.physicalRecoveryMetric
            stressScoreMetric = data?.wristbandMetrics?.stressScoreMetric
            sleepDurationMetric = data?.wristbandMetrics?.sleepDurationMetric
            deviceId = data?.device?.id
            dataSync = data?.device?.dataSync
        } else {
            val data = viewModel.getRingReadingData().value?.data?.data
            exerciseIntensityMetric = data?.exerciseIntensityMetric
            physicalRecoveryMetric = data?.physicalRecoveryMetric
            stressScoreMetric = data?.stressScoreMetric
            sleepDurationMetric = data?.sleepDurationMetric
            deviceId = data?.device?.id
            dataSync = data?.device?.dataSync
        }

        when (p0) {
            binding.header.ivBack -> {
                findNavController().popBackStack(R.id.fragmentTrack, false)
            }

            binding.clTabMetrics.cardExerciseIntensity -> {
                exerciseIntensityMetric?.let {
                    val metricType = MetricType.Exercise(it, deviceId, dataSync, deviceName)
                    navigateToMetricDetails(metricType)
                }
            }

            binding.clTabMetrics.cardPhysicalRecovery -> {
                physicalRecoveryMetric?.let {
                    val metricType = MetricType.PhysicalRecovery(it, deviceId, dataSync, deviceName)
                    navigateToMetricDetails(metricType)
                }
            }

            binding.clTabMetrics.cardStressScore -> {
                stressScoreMetric?.let {
                    val metricType = MetricType.Stress(it, deviceId, dataSync, deviceName)
                    navigateToMetricDetails(metricType)
                }
            }

            binding.clTabMetrics.cardSleepMetrics -> {
                sleepDurationMetric?.let {
                    val metricType = MetricType.Sleep(it, deviceId, dataSync, deviceName)
                    navigateToMetricDetails(metricType)
                }
            }
        }
    }

    private fun navigateToMetricDetails(metricType: MetricType) {
        findNavController().navigate(
            R.id.action_fragmentDeviceData_to_fragmentMetricDetails,
            bundleOf("metric" to metricType)
        )
    }

    private fun updateBtStatusIcon(isEnabled: Boolean) {
        if (isEnabled) {
            binding.ivBtStatus.setImageResource(R.drawable.ic_bluetooth_24px)
        } else {
            binding.ivBtStatus.setImageResource(R.drawable.ic_bluetooth_disabled_24px)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        app.deviceManager.unregisterCb()
    }
}