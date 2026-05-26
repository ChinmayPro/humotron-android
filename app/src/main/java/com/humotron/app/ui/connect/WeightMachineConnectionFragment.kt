package com.humotron.app.ui.connect

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.weight.WeightScaleConnectionState
import com.humotron.app.bt.weight.WeightScaleDeviceSummary
import com.humotron.app.bt.weight.WeightScaleError
import com.humotron.app.bt.weight.WeightScaleEvent
import com.humotron.app.bt.weight.WeightScaleMeasurementState
import com.humotron.app.bt.weight.WeightScaleScanState
import com.humotron.app.bt.weight.WeightScaleSdkState
import com.humotron.app.bt.weight.WeightScaleUserProfile
import com.humotron.app.bt.weight.WeightScaleViewModel
import com.humotron.app.core.Preference
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentWeightMachineConnectionBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.domain.modal.param.DeviceMetaDataParam
import com.humotron.app.ui.connect.adapter.WeightScaleDeviceAdapter
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.ToastUtils
import dagger.hilt.android.AndroidEntryPoint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

@AndroidEntryPoint
class WeightMachineConnectionFragment : Fragment(R.layout.fragment_weight_machine_connection) {

    private lateinit var binding: FragmentWeightMachineConnectionBinding
    private val viewModel: WeightScaleViewModel by viewModels()

    @Inject
    lateinit var prefUtils: PrefUtils

    private var dialog: LoadingDialog? = null
    private var selectedDevice: WeightScaleDeviceSummary? = null

    private val adapter by lazy {
        WeightScaleDeviceAdapter { selected ->
            selectedDevice = selected
            binding.btnSubmit.isEnabled = selected != null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWeightMachineConnectionBinding.bind(view)

        initClicks()
        initViews()
        observeData()
    }

    private fun initClicks() {
        binding.btnSubmit.setOnClickListener {
            val device = selectedDevice ?: return@setOnClickListener
            showProgress()
            viewModel.connect(device.mac, buildUserProfile())
        }
    }

    private fun initViews() {
        binding.btnSubmit.isEnabled = false
        binding.ringsView.startAnimation()
        binding.rvDevices.adapter = adapter
        viewModel.initializeSdk()
    }

    private fun observeData() {
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.sdkState.observe(viewLifecycleOwner) { state ->
            when (state) {
                WeightScaleSdkState.Ready -> viewModel.startScan()
                WeightScaleSdkState.Initializing -> binding.tvTitle.text =
                    getString(R.string.connecting)

                is WeightScaleSdkState.Failed -> {
                    binding.tvTitle.text = state.message
                    ToastUtils.showShort(requireContext(), state.message)
                }

                WeightScaleSdkState.Uninitialized -> Unit
            }
        }

        viewModel.scanState.observe(viewLifecycleOwner) { state ->
            when (state) {
                WeightScaleScanState.Starting,
                WeightScaleScanState.Scanning,
                    -> {
                    binding.ringsView.startAnimation()
                    binding.tvTitle.text = getString(R.string.smart_scale_instruction_1)
                }

                WeightScaleScanState.Idle,
                WeightScaleScanState.Stopping,
                    -> binding.ringsView.stopAnimation()

                is WeightScaleScanState.Failed -> {
                    binding.ringsView.stopAnimation()
                    binding.tvTitle.text = state.message
                }
            }
        }

        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            adapter.submitList(devices)
            val hasDevices = devices.isNotEmpty()
            binding.rvDevices.isVisible = hasDevices
            binding.clNoDeviceFound.isVisible = !hasDevices
            if (hasDevices) {
                binding.tvTitle.text =
                    getString(R.string.weight_scale_is_detected)
                binding.ivWeightScale.isVisible = true
                binding.ringsView.stopAnimation()
            }
        }

        viewModel.connectionState.observe(viewLifecycleOwner) { state ->
            when (state) {
                WeightScaleConnectionState.Idle -> hideProgress()
                is WeightScaleConnectionState.Connecting -> {
                    binding.tvTitle.text = getString(R.string.connecting_to_device)
                    showProgress()
                }

                is WeightScaleConnectionState.Connected -> {
                    binding.tvTitle.text = getString(R.string.connected)
                    handleDeviceConnected(state.device)
                }

                is WeightScaleConnectionState.Ready -> {
                    hideProgress()
                    binding.tvTitle.text = getString(R.string.connected)
                    //ToastUtils.showShort(requireContext(), "Scale connected successfully.")
                }

                is WeightScaleConnectionState.Disconnecting -> {
                    binding.tvTitle.text = getString(R.string.disconnected)
                }

                is WeightScaleConnectionState.Disconnected -> {
                    hideProgress()
                    binding.tvTitle.text = getString(R.string.smart_scale_connection_help)
                }

                is WeightScaleConnectionState.Reconnecting -> {
                    binding.tvTitle.text = "Reconnecting to scale (attempt ${state.attempt})"
                }

                is WeightScaleConnectionState.Failed -> {
                    hideProgress()
                    binding.tvTitle.text = state.message
                    ToastUtils.showShort(requireContext(), state.message)
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
                    val device = selectedDevice
                    if (userHardware != null && device != null) {
                        prefUtils.setWeightHardwareData(userHardware)
                        bundle.putParcelable(
                            "deviceMetaData",
                            DeviceMetaDataParam.from(
                                device,
                                userHardware,
                                deviceId
                            )
                        )
                        bundle.putSerializable("deviceType", DeviceType.WEIGHT_MACHINE)
                    }
                    findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val bundle = Bundle()
                    bundle.putSerializable("deviceType", DeviceType.WEIGHT_MACHINE)
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.measurementState.observe(viewLifecycleOwner) { state ->
            when (state) {
                WeightScaleMeasurementState.Idle -> Unit
                is WeightScaleMeasurementState.Measuring -> {
                    binding.tvTitle.text = getString(R.string.smart_scale_instruction_1)
                }

                is WeightScaleMeasurementState.UnsteadyWeight -> {
                    binding.tvTitle.text =
                        "Reading... ${String.format(Locale.US, "%.1f", state.weightKg)} kg"
                }

                is WeightScaleMeasurementState.Completed -> {
                    hideProgress()
                    binding.tvTitle.text =
                        "Weight ${String.format(Locale.US, "%.1f", state.measurement.weightKg)} kg"
                }

                is WeightScaleMeasurementState.StoredDataReceived -> {
                    binding.tvTitle.text = "Stored readings received: ${state.count}"
                }
            }
        }

        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                is WeightScaleEvent.BatteryUpdated -> {
                    binding.tvTitle.text = "Battery ${event.level}%"
                }

                is WeightScaleEvent.ResultMessage -> {
                    if (event.code != 0 && event.message.isNotBlank()) {
                        ToastUtils.showShort(requireContext(), event.message)
                    }
                }

                else -> Unit
            }
        }

        viewModel.errors.observe(viewLifecycleOwner) { error ->
            val message = when (error) {
                is WeightScaleError.Connection -> error.message
                is WeightScaleError.Initialization -> error.message
                is WeightScaleError.Scan -> error.message
                is WeightScaleError.UserValidation -> error.message
            }
            ToastUtils.showShort(requireContext(), message)
        }
    }

    private fun handleDeviceConnected(device: WeightScaleDeviceSummary) {
        prefUtils.setString(Preference.WEIGHT_SCALE, device.mac)

        if (prefUtils.getWeightHardwareId().isNullOrEmpty()) {
            viewModel.addHardwareInProfile(
                AddHardware(
                    DeviceType.WEIGHT_MACHINE.value,
                    device.mac
                )
            )
        } else {
            hideProgress()
            val userHardware = prefUtils.getWeightHardware()
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
                bundle.putSerializable("deviceType", DeviceType.WEIGHT_MACHINE)
            }
            findNavController().navigate(R.id.fragmentDeviceConnected, bundle)
        }
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
        viewModel.stopScan()
        super.onDestroyView()
    }

    companion object {
        private val DEFAULT_BIRTHDAY = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse("1990-01-01")
            ?: Date(631152000000L)
    }
}
