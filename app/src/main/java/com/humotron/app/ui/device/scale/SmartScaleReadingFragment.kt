package com.humotron.app.ui.device.scale

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.font.FontVariation.weight
import androidx.core.os.bundleOf
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.weight.WeightScaleConnectionState
import com.humotron.app.bt.weight.WeightScaleDeviceSummary
import com.humotron.app.bt.weight.WeightScaleMeasurement
import com.humotron.app.bt.weight.WeightScaleMeasurementState
import com.humotron.app.bt.weight.WeightScaleScanState
import com.humotron.app.bt.weight.WeightScaleSdkState
import com.humotron.app.bt.weight.WeightScaleUserProfile
import com.humotron.app.bt.weight.WeightScaleViewModel
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSmartScaleReadingBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@AndroidEntryPoint
class SmartScaleReadingFragment :
    BaseFragment(R.layout.fragment_smart_scale_reading),
    View.OnClickListener {

    private lateinit var binding: FragmentSmartScaleReadingBinding
    private val viewModel: WeightScaleViewModel by viewModels()
    private var userDevice: GetAllDeviceResponse.Data.UserDevice? = null

    private var selectedDevice: WeightScaleDeviceSummary? = null
    private var savedScaleMac: String? = null
    private var isSavedScaleConnecting = false
    private var shouldAutoConnectSavedScale = false
    private var hasNavigatedToResult = false

    private var mBluetoothAdapter: BluetoothAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSmartScaleReadingBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        observeData()
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
        binding.scanAnimationView.setScanData("0", "kg", "WEIGHT")
        tvDeviceName.text = userDevice?.deviceFacingName ?: userDevice?.deviceName ?: ""

        /*val deviceImage = userDevice?.deviceImage?.firstOrNull()
        if (deviceImage.isNullOrBlank()) {
            ivDevice.setImageResource(R.drawable.ic_connect_weightscale_logo)
        } else {
            Glide.with(this@WeightScaleReadingFragment)
                .load(deviceImage)
                .placeholder(R.drawable.ic_connect_weightscale_logo)
                .into(ivDevice)
        }*/

        bindConnectionState(WeightScaleConnectionState.Disconnected(null))
        renderMeasurement(null, false)
        savedScaleMac = prefUtils.getString(Preference.WEIGHT_SCALE)?.takeIf { it.isNotBlank() }

        if (isBlueToothSupported()) {
            requestPermission()
        }
    }

    private fun observeData() {
        viewModel.sdkState.observe(viewLifecycleOwner) { state ->
            if (
                shouldAutoConnectSavedScale &&
                state == WeightScaleSdkState.Ready &&
                !savedScaleMac.isNullOrBlank() &&
                !isSavedScaleConnecting
            ) {
                viewModel.startScan()
            }
        }

        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                WeightScaleScanState.Starting,
                WeightScaleScanState.Scanning,
                    -> {
                    binding.tvDeviceStatus.text = getString(R.string.scanning)
                }

                WeightScaleScanState.Idle,
                WeightScaleScanState.Stopping,
                    -> {
                }

                is WeightScaleScanState.Failed -> {

                }
            }
        }

        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            if (!shouldAutoConnectSavedScale) return@observe
            val preferredMac = savedScaleMac ?: return@observe
            if (isSavedScaleConnecting) return@observe

            val matchedDevice = devices.firstOrNull { device ->
                device.mac.equals(preferredMac, ignoreCase = true)
            } ?: return@observe

            selectedDevice = matchedDevice
            isSavedScaleConnecting = true
            shouldAutoConnectSavedScale = false
            viewModel.stopScan()
            connectWeightScale()
        }

        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            bindConnectionState(state)
            when (state) {
                WeightScaleConnectionState.Idle,
                is WeightScaleConnectionState.Disconnected,
                is WeightScaleConnectionState.Failed,
                    -> {
                    isSavedScaleConnecting = false
                }

                is WeightScaleConnectionState.Connected,
                is WeightScaleConnectionState.Ready,
                    -> {
                    isSavedScaleConnecting = false
                    shouldAutoConnectSavedScale = false
                }

                is WeightScaleConnectionState.Connecting,
                is WeightScaleConnectionState.Reconnecting,
                is WeightScaleConnectionState.Disconnecting,
                    -> Unit
            }
        }

        viewModel.measurementState.observe(viewLifecycleOwner) { state ->
            when (state) {
                WeightScaleMeasurementState.Idle -> {
                    binding.tvInstruction.text =
                        getString(R.string.weight_scale_instruction_default)
                }

                is WeightScaleMeasurementState.Measuring -> {
                    binding.tvInstruction.text =
                        getString(R.string.weight_scale_instruction_default)
                }

                is WeightScaleMeasurementState.UnsteadyWeight -> {
                    /*binding.tvWeightValue.text =
                        String.format(Locale.US, "%05.2f", state.weightKg)*/
                    Log.e(TAG, "renderMeasurement: UnsteadyWeight weight $state.weightKg" )
                    binding.scanAnimationView.setScanData(formatMeasurementValue(state.weightKg), "kg", "WEIGHT")
                    binding.tvInstruction.text =
                        getString(R.string.weight_scale_instruction_stabilizing)
                }

                is WeightScaleMeasurementState.Completed -> {
                    Log.e(
                        "Weight Scale Machine",
                        "observeData: WeightScaleMeasurementState.Completed"
                    )
                    binding.tvInstruction.text =
                        getString(R.string.weight_scale_instruction_complete)
                    viewModel.saveAndUploadMeasurement(state.measurement)
                    renderMeasurement(state.measurement, true)
                }

                is WeightScaleMeasurementState.StoredDataReceived -> {
                    binding.tvInstruction.text =
                        getString(R.string.weight_scale_instruction_default)
                }
            }
        }

        viewModel.latestMeasurement.observe(viewLifecycleOwner) { measurement ->
            renderMeasurement(
                measurement,
                false
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveMeasurementStatus.collect { resource ->
                    resource?.let {
                        when (it.status) {
                            Status.LOADING -> showProgress()
                            Status.SUCCESS -> {
                                hideProgress()
                                ToastUtils.showShort(
                                    requireActivity(),
                                    getString(R.string.measurement_saved_successfully)
                                )
                            }

                            Status.ERROR, Status.EXCEPTION -> {
                                hideProgress()
                                val msg = it.error?.errorMessage ?: it.error?.error
                                ?: getString(R.string.failed_to_save_measurement)
                                ToastUtils.showShort(requireActivity(), msg)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindConnectionState(state: WeightScaleConnectionState) = with(binding) {
        when (state) {
            WeightScaleConnectionState.Idle,
            is WeightScaleConnectionState.Disconnected,
            is WeightScaleConnectionState.Failed,
                -> {
                tvDeviceStatus.text = getString(R.string.inactive)
                tvDeviceStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.disconnected)
                )
                ivStatusDot.setImageResource(R.drawable.dot_disconnected)
            }

            is WeightScaleConnectionState.Connecting,
            is WeightScaleConnectionState.Reconnecting,
                -> {
                tvDeviceStatus.text = getString(R.string.connecting)
                tvDeviceStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.w200)
                )
                ivStatusDot.setImageResource(R.drawable.dot_connecting)
            }

            is WeightScaleConnectionState.Connected,
            is WeightScaleConnectionState.Ready,
                -> {
                hideProgress()
                tvDeviceStatus.text = getString(R.string.active)
                tvDeviceStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.colorBgBtn)
                )
                ivStatusDot.setImageResource(R.drawable.dot_connected)
            }

            is WeightScaleConnectionState.Disconnecting -> {
                tvDeviceStatus.text = getString(R.string.disconnected)
                tvDeviceStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.disconnected)
                )
                ivStatusDot.setImageResource(R.drawable.dot_disconnected)
            }
        }
    }

    private fun renderMeasurement(
        measurement: WeightScaleMeasurement?,
        isMeasurementCompleted: Boolean,
    ) = with(binding) {
        measurement?.let {
            if (isMeasurementCompleted) {
                val weight = measurement.weightKg
                Log.e(TAG, "renderMeasurement: weight $weight" )
                scanAnimationView.setScanData(formatMeasurementValue(weight), "kg", "WEIGHT")

                tvLeanBwValue.text = measurement.findMetricValue("lean body weight") ?: "--"
                tvMuscleMassValue.text = measurement.findMetricValue("muscle mass") ?: "--"
                tvBoneMassValue.text = measurement.findMetricValue("bone mass") ?: "--"

                shimmerFrameLayout1.stopShimmer()
                shimmerFrameLayout2.stopShimmer()
                shimmerFrameLayout3.stopShimmer()

                view1.isVisible = false
                view2.isVisible = false
                view3.isVisible = false

                navigateToResult(measurement)
            }
        }
    }

    private fun navigateToResult(measurement: WeightScaleMeasurement) {
        if (hasNavigatedToResult) return
        hasNavigatedToResult = true

        val bodyFatValue =
            measurement.findMetricValue("body fat rate") ?: measurement.findMetricValue("body fat")

        val resultArgs = bundleOf(
            NavKeys.WEARABLE to userDevice,
            NavKeys.SCALE_MEASURED_AT to formatMeasurementTimestamp(measurement.measuredAt),
            NavKeys.SCALE_WEIGHT to formatMeasurementValue(measurement.weightKg),
            NavKeys.SCALE_BMI to measurement.findMetricValue("BMI"),
            NavKeys.SCALE_BODY_FAT to bodyFatValue,
            NavKeys.SCALE_LEAN_BODY_WEIGHT to measurement.findMetricValue("lean body weight"),
            NavKeys.SCALE_MUSCLE_MASS to measurement.findMetricValue("muscle mass"),
            NavKeys.SCALE_BONE_MASS to measurement.findMetricValue("bone mass"),
        )

        findNavController().navigate(R.id.fragmentSmartScaleResult, resultArgs)
    }

    private fun formatMeasurementValue(value: Double): String {
        return String.format(Locale.US, "%.2f", value)
    }

    private fun formatMeasurementTimestamp(date: Date?): String? {
        return date?.let {
            SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(it)
        }
    }

    private fun WeightScaleMeasurement?.findMetricValue(metricName: String): String? {
        val metric = this?.bodyMetrics
            ?.firstOrNull { it.name?.contains(metricName, ignoreCase = true) == true }
            ?: return null
        return String.format(Locale.US, "%.2f", metric.value)
    }

    override fun onClick(view: View?) {
        if (view === binding.btnStop) {
            findNavController().popBackStack()
        }
    }

    private fun isBlueToothSupported(): Boolean {
        if (!requireActivity().packageManager.hasSystemFeature(
                PackageManager.FEATURE_BLUETOOTH_LE
            )
        ) {
            return false
        }

        val bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        return mBluetoothAdapter != null
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        } else {
            requestMultiplePermissions.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private val requestMultiplePermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->

        if (!permissions.entries.map { it.value }.contains(false)) {
            isBluetoothEnabled()
        }
    }

    private fun isBluetoothEnabled() {
        if (mBluetoothAdapter?.isEnabled == false) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            blueToothLauncher.launch(enableBtIntent)
        } else {
            connectSavedWeightScale()
        }
    }

    private val blueToothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                connectSavedWeightScale()
            }
        }

    private fun connectSavedWeightScale() {
        savedScaleMac = prefUtils.getString(Preference.WEIGHT_SCALE)?.takeIf { it.isNotBlank() }
        if (savedScaleMac.isNullOrBlank()) return
        shouldAutoConnectSavedScale = true

        if (viewModel.sdkState.value == WeightScaleSdkState.Ready) {
            viewModel.startScan()
        } else {
            viewModel.initializeSdk()
        }
    }

    private fun connectWeightScale() {
        val device = selectedDevice ?: return
        showProgress()
        viewModel.connect(device.mac, buildUserProfile())
    }

    private fun buildUserProfile(): WeightScaleUserProfile {
        val user = prefUtils.getLoginResponse()
        return WeightScaleUserProfile(
            userId = user.id ?: user.email ?: user.name ?: "humotron-user",
            heightCm = parseHeightInCm(user.height, user.heightUnit) ?: 170,
            heightUnit = user.heightUnit,
            gender = user.gender ?: "male",
            birthday = parseBirthDate(user.birthDate) ?: DEFAULT_BIRTHDAY,
        )
    }

    private fun parseHeightInCm(rawHeight: String?, rawUnit: String?): Int? {
        if (rawHeight.isNullOrBlank()) return null

        val normalizedUnit = rawUnit?.trim()?.lowercase(Locale.US).orEmpty()
        val trimmedHeight = rawHeight.trim()

        return when (normalizedUnit) {
            "", "cm" -> trimmedHeight.toDoubleOrNull()?.roundToInt()
            "m" -> trimmedHeight.toDoubleOrNull()?.times(100)?.roundToInt()
            "mm" -> trimmedHeight.toDoubleOrNull()?.div(10)?.roundToInt()
            "in" -> trimmedHeight.toDoubleOrNull()?.times(2.54)?.roundToInt()
            "ft in" -> parseFeetInchesToCm(trimmedHeight)
            else -> trimmedHeight.toDoubleOrNull()?.roundToInt()
        }
    }

    private fun parseFeetInchesToCm(rawHeight: String): Int? {
        val match = Regex("""^\s*(\d+)\s*'?\s*(\d+)?\s*"?\s*$""").find(rawHeight) ?: return null
        val feet = match.groupValues[1].toIntOrNull() ?: return null
        val inches = match.groupValues.getOrNull(2)?.takeIf { it.isNotBlank() }?.toIntOrNull() ?: 0
        return ((feet * 12) + inches).toDouble().times(2.54).roundToInt()
    }

    private fun parseBirthDate(rawDate: String?): Date? {
        if (rawDate.isNullOrBlank()) return null
        val patterns =
            listOf("yyyy-MM-dd", "dd-MM-yyyy", "MM/dd/yyyy", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        for (pattern in patterns) {
            try {
                return SimpleDateFormat(pattern, Locale.US).parse(rawDate)
            } catch (_: ParseException) {
                // Try the next supported format from the API payload.
            }
        }
        return null
    }

    override fun onDestroyView() {
        hideProgress()
        shouldAutoConnectSavedScale = false
        isSavedScaleConnecting = false
        hasNavigatedToResult = false
        viewModel.stopScan()
        viewModel.disconnect()
        viewModel.clearMeasurements()
        super.onDestroyView()
    }

    companion object {
        private val DEFAULT_BIRTHDAY = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse("1990-01-01")
            ?: Date(631152000000L)
    }
}
