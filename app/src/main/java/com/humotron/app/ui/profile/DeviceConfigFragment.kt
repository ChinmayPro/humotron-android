package com.humotron.app.ui.profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.R
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.bt.band.STATE_BAND_CONNECTED
import com.humotron.app.bt.band.STATE_BAND_CONNECTING
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDeviceConfigBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.UserDevice
import com.humotron.app.domain.modal.response.GetDeviceConfigResponse
import com.humotron.app.domain.modal.ui.DeviceAction
import com.humotron.app.ui.dialogs.MeasureFrequencyBottomSheetDialog
import com.humotron.app.ui.dialogs.PowerOptionBottomSheetDialog
import com.humotron.app.ui.profile.adapter.DeviceActionAdapter
import com.humotron.app.util.DialogUtils
import com.humotron.app.util.GridSpacingItemDecoration
import com.humotron.app.util.STATE_DEVICE_CHARGING
import com.humotron.app.util.STATE_DEVICE_CONNECTED
import com.humotron.app.util.STATE_DEVICE_CONNECTING
import com.humotron.app.util.STATE_DEVICE_DISCHARGING
import com.humotron.app.util.STATE_DEVICE_DISCONNECTED
import com.humotron.app.util.TAG_BAND_DEBUG
import com.humotron.app.util.ToastUtils
import com.humotron.app.util.getTimeAgo
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lib.smart.carering.api.CareRingManager
import lib.smart.carering.api.DeviceInfo
import java.time.Instant
import javax.inject.Inject

@AndroidEntryPoint
class DeviceConfigFragment : BaseFragment(R.layout.fragment_device_config) {

    private lateinit var binding: FragmentDeviceConfigBinding
    private val args: DeviceConfigFragmentArgs by navArgs()
    private val viewModel: DeviceConfigViewModel by viewModels()

    @Inject
    lateinit var bandBleManager: BandBleManager
    private val app by lazy { requireActivity().application as App }

    private var userDevice: UserDevice? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeviceConfigBinding.bind(view)

        initClicks()
        initObservers()
        initData()
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.btnPower.setOnClickListener {
            if (isRingDeviceDisconnected()) showRingNotConnectedDialog()
            else if (isBandDeviceDisconnected()) showBandNotConnectedDialog()
            else showPowerOptions()
        }

        //Ring
        binding.mcvMeasureFreq.setOnClickListener {
            if (isRingDeviceDisconnected()) showRingNotConnectedDialog()
            else showMeasureFrequency()
        }
        binding.switchLowPowerMode.setOnClickListener {
            if (isRingDeviceDisconnected()) {
                binding.switchLowPowerMode.isChecked = !binding.switchLowPowerMode.isChecked
                showRingNotConnectedDialog()
            } else {
                val seconds = if (binding.switchLowPowerMode.isChecked) 60 else 30
                /*CareRingManager.get().settingsApi()
                    .setHealthMeasurementDuration(seconds) {
                        viewModel.setMeasureFrequency(seconds)
                        userDevice?.let {
                            it.id?.let { deviceId ->
                                val des = binding.tvDeviceDes.text.toString() ?: ""
                                viewModel.addDeviceMetaData(
                                    deviceId,
                                    des,
                                    seconds,
                                    binding.switchLowPowerMode.isChecked
                                )
                            }
                        }
                    }*/
            }
        }
        binding.btnClearRing.setOnClickListener {
            if (isRingDeviceDisconnected()) showRingNotConnectedDialog()
            else showClearDataConfirmation()
        }

        //Band
        binding.cvIntervalSettings.setOnClickListener {
            if (isBandDeviceDisconnected()) showBandNotConnectedDialog()
            else {
            }
        }
        binding.cvAlarmClock.setOnClickListener {
            if (isBandDeviceDisconnected()) showBandNotConnectedDialog()
            else {
            }
        }
        binding.btnClear.setOnClickListener {
            if (isBandDeviceDisconnected()) showBandNotConnectedDialog()
            else showClearDataConfirmation()
        }


        binding.tvUpdated.setOnClickListener {
            if (isRingDeviceDisconnected()) showRingNotConnectedDialog()
            else if (isBandDeviceDisconnected()) showBandNotConnectedDialog()
        }
    }

    private fun initData() {
        userDevice = args.wearable
        userDevice?.let {
            binding.tvDeviceName.text = it.deviceFacingName ?: "Unknown Device"
            // Calling the API with the device id passed from wearable
            it.id?.let { id ->
                viewModel.getDeviceConfiguration(id)
            }
            if (!it.dataSync.isNullOrEmpty()) {
                try {
                    val timeInMillis = Instant.parse(it.dataSync).toEpochMilli()
                    binding.tvSyncTime.text = getTimeAgo(timeInMillis)
                } catch (e: Exception) {
                    binding.tvSyncTime.text = "12 mins ago"
                }
            } else {
                binding.tvSyncTime.text = "12 mins ago"
            }
        }
        binding.header.title.text = getString(R.string.device_configurations)

        // Mockup fallback / default values
        binding.tvSerialNumber.text = getString(R.string.serial_number_format, "J2208A 0668A8")
        binding.tvMacAddress.text = getString(R.string.mac_format, "21:02:02:06:68:A8")
        binding.tvFirmwareLabel.text = getString(R.string.firmware_version_format, "0646")
        binding.tvUpdated.text = "Updated"
        binding.tvBatteryValue.text = "52%"
        binding.tvLocalDataTime.text = "Today"
        binding.tvWarrantyStatus.text = getString(R.string.remaining_days_format, 184)

        binding.llDeviceConfig.visibility = View.VISIBLE
        binding.tvControlsLabel.visibility = View.VISIBLE

        binding.mcvMeasureFreq.visibility = View.GONE
        binding.mcvLowPowerMode.visibility = View.GONE
        binding.mcvLocalData2.visibility = View.GONE
        binding.clDeviceInsight.visibility = View.GONE
        binding.clLocalData1.visibility = View.GONE

        binding.cvIntervalSettings.visibility = View.GONE
        binding.cvAlarmClock.visibility = View.GONE

        val deviceType = DeviceType.from(userDevice?.deviceName)

        // Treat all non-RING devices as BAND to match the HTML wristband fallback UI
        val effectiveDeviceType = if (deviceType == DeviceType.RING) DeviceType.RING else DeviceType.BAND

        setupDeviceActions(effectiveDeviceType)

        when (effectiveDeviceType) {
            DeviceType.BAND -> {
                binding.clLocalData1.visibility = View.VISIBLE
                binding.clDeviceInsight.visibility = View.GONE

                binding.cvIntervalSettings.visibility = View.VISIBLE
                binding.cvAlarmClock.visibility = View.VISIBLE
                binding.clLocalData1.visibility = View.VISIBLE

                binding.tvBatteryLabel.visibility = View.VISIBLE
                binding.tvBatteryValue.visibility = View.VISIBLE
                binding.btnPower.visibility = View.VISIBLE

                updateConnectionUi(true)

                if (deviceType == DeviceType.BAND) {
                    observeBand()
                }
            }

            DeviceType.RING -> {
                binding.llDeviceConfig.visibility = View.VISIBLE
                binding.clDeviceInsight.visibility = View.VISIBLE
                binding.clLocalData1.visibility = View.GONE

                binding.mcvMeasureFreq.visibility = View.VISIBLE
                binding.mcvLowPowerMode.visibility = View.VISIBLE
                binding.mcvLocalData2.visibility = View.VISIBLE
                binding.clDeviceInsight.visibility = View.VISIBLE

                // For ring, defaults/mock values might be different
                binding.tvSerialNumber.text = "Serial Number —"
                binding.tvMacAddress.text = "MAC: —"
                binding.tvFirmwareLabel.text = "Firmware Version —"
                binding.tvUpdated.text = ""
                binding.tvBatteryValue.text = "—"
                binding.tvLocalDataTime.text = "—"
                binding.tvWarrantyStatus.text = "Remaining 0 days"
                updateConnectionUi(false) // Searching / disconnected

                observeRing()
            }

            DeviceType.SMART_CUFF -> {
                binding.llDeviceConfig.visibility = View.VISIBLE
                binding.clDeviceInsight.visibility = View.VISIBLE
                binding.clLocalData1.visibility = View.GONE

                binding.mcvMeasureFreq.visibility = View.GONE
                binding.mcvLowPowerMode.visibility = View.GONE
                binding.mcvLocalData2.visibility = View.GONE
                binding.clDeviceInsight.visibility = View.GONE
            }

            DeviceType.WEIGHT_MACHINE -> {
                binding.llDeviceConfig.visibility = View.VISIBLE
                binding.clDeviceInsight.visibility = View.VISIBLE
                binding.clLocalData1.visibility = View.GONE

                binding.mcvMeasureFreq.visibility = View.GONE
                binding.mcvLowPowerMode.visibility = View.GONE
                binding.mcvLocalData2.visibility = View.GONE
                binding.clDeviceInsight.visibility = View.GONE
            }

            DeviceType.UNKNOWN -> {
                binding.llDeviceConfig.visibility = View.GONE
            }
            else -> {}
        }
    }

    private fun initObservers() {
        viewModel.getMeasureFrequency().observe(viewLifecycleOwner) { frequency ->
            binding.tvivMeasureFreqIconDesc.text = getString(R.string.every_s_sec, frequency)
        }

        viewModel.getRingDeviceInfoLiveData().observe(viewLifecycleOwner) { deviceInfo ->
            updateRingDeviceInfoUI(deviceInfo)
        }

        viewModel.getDeviceConfigLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    resource.data?.let { response ->
                        updateUI(response)
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                    ToastUtils.showShort(
                        requireContext(),
                        resource.error?.errorMessage ?: "Error fetching configuration"
                    )
                }

                Status.LOADING -> {
                    showProgress()
                }

                else -> {}
            }
        }

        viewModel.getDeleteHardwareLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    disconnectDeletedDevice()
                    clearDeletedDevicePreferences()
                    ToastUtils.showShort(
                        requireContext(),
                        resource.data?.message ?: "Device removed successfully"
                    )
                    findNavController().popBackStack(R.id.fragmentProfile, false)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    ToastUtils.showShort(
                        requireContext(),
                        resource.error?.errorMessage ?: "Error removing device"
                    )
                }

                Status.LOADING -> {
                    showProgress()
                }

                else -> {}
            }
        }
    }

    private fun showClearDataConfirmation() {
        DialogUtils.showConfirmationDialog(
            context = requireContext(),
            title = "Clear Local Data",
            message = "Are you sure you want to clear all local data stored on this device?",
            btnPositiveText = "Clear",
            onPositiveClick = {
                val currBtMac = app.ringBleManager.connectedDevice?.address
                if (currBtMac != null) {
                    CareRingManager.get().healthApi().clearHistoricalData {
                        ToastUtils.showShort(requireContext(), "Data cleared successfully")
                    }
                    /*CareRingManager.get().activityApi().clearHistory {
                    }*/
                }
            }
        )
    }

    private fun showMeasureFrequency() {
        val currentFrequency = viewModel.getMeasureFrequency().value ?: 60
        MeasureFrequencyBottomSheetDialog.newInstance(currentFrequency).apply {
            setOnSaveListener { frequency ->
                /*CareRingManager.get().settingsApi()
                    .setHealthMeasurementDuration(frequency) {
                        viewModel.setMeasureFrequency(frequency)
                        userDevice?.let {
                            it.id?.let { deviceId ->
                                val des = binding.tvDeviceDes.text.toString() ?: ""
                                viewModel.addDeviceMetaData(
                                    deviceId,
                                    des,
                                    frequency,
                                    binding.switchLowPowerMode.isChecked
                                )
                            }
                        }
                    }*/
            }
        }.show(childFragmentManager, MeasureFrequencyBottomSheetDialog.TAG)
    }

    private fun showPowerOptions() {
        PowerOptionBottomSheetDialog.newInstance().apply {
            setListeners(
                onRestart = {
                    val deviceType = DeviceType.from(userDevice?.deviceName)
                    if (deviceType == DeviceType.RING) {
                        CareRingManager.get().deviceApi().reboot()
                        ToastUtils.showShort(requireContext(), "Restarting ring...")
                    }
                },
                onShutDown = {
                    val deviceType = DeviceType.from(userDevice?.deviceName)
                    if (deviceType == DeviceType.RING) {
                        CareRingManager.get().deviceApi().shutdown()
                        ToastUtils.showShort(requireContext(), "Shutting down ring...")
                    } else {
                        ToastUtils.showShort(requireContext(), "Shutting down device...")
                    }
                }
            )
        }.show(childFragmentManager, PowerOptionBottomSheetDialog.TAG)
    }

    private fun setupDeviceActions(deviceType: DeviceType) {
        val actions = when (deviceType) {
            DeviceType.BAND -> listOf(
                DeviceAction.RESTART,
                DeviceAction.RESET_FACTORY,
                DeviceAction.REMOVE_FROM_ACCOUNT,
                DeviceAction.RE_SETUP
            )

            DeviceType.RING -> listOf(
                DeviceAction.RESET_FACTORY,
                DeviceAction.REMOVE_FROM_ACCOUNT,
                DeviceAction.RE_SETUP
            )

            DeviceType.SMART_CUFF -> {
                listOf(
                    DeviceAction.RESET_FACTORY,
                    DeviceAction.REMOVE_FROM_ACCOUNT,
                    DeviceAction.RE_SETUP
                )
            }

            DeviceType.WEIGHT_MACHINE -> {
                listOf(
                    DeviceAction.RESET_FACTORY,
                    DeviceAction.REMOVE_FROM_ACCOUNT,
                    DeviceAction.RE_SETUP
                )
            }

            DeviceType.UNKNOWN -> listOf(
                DeviceAction.REMOVE_FROM_ACCOUNT
            )
        }

        binding.rvDeviceActions.apply {
            adapter = DeviceActionAdapter(actions) { action ->
                handleDeviceAction(action)
            }
            if (itemDecorationCount == 0) {
                val spacing = resources.getDimensionPixelSize(R.dimen._12dp)
                addItemDecoration(GridSpacingItemDecoration(2, spacing, false))
            }
        }
    }

    private fun handleDeviceAction(action: DeviceAction) {
        val deviceType = DeviceType.from(userDevice?.deviceName)

        val isDisconnected = when (deviceType) {
            DeviceType.RING -> isRingDeviceDisconnected()
            DeviceType.BAND -> isBandDeviceDisconnected()
            else -> false
        }

        val shouldCheckConnection =
            (deviceType == DeviceType.RING && action == DeviceAction.RESET_FACTORY) ||
                    (deviceType == DeviceType.BAND &&
                            (action == DeviceAction.RESTART || action == DeviceAction.RESET_FACTORY))

        if (isDisconnected && shouldCheckConnection) {
            when (deviceType) {
                DeviceType.RING -> showRingNotConnectedDialog()
                DeviceType.BAND -> showBandNotConnectedDialog()
                else -> Unit
            }
            return
        }

        when (action) {

            DeviceAction.RESTART -> {
                when (deviceType) {
                    DeviceType.RING -> {
                        CareRingManager.get().deviceApi().reboot()
                        ToastUtils.showShort(requireContext(), "Restarting ring...")
                    }

                    DeviceType.BAND -> {
                    }

                    DeviceType.SMART_CUFF -> {
                    }

                    DeviceType.WEIGHT_MACHINE -> {
                    }

                    DeviceType.UNKNOWN -> {

                    }
                }
            }

            DeviceAction.RESET_FACTORY -> {
                if (deviceType == DeviceType.RING) {
                    CareRingManager.get().deviceApi().factoryReset {
                        ToastUtils.showShort(
                            requireContext(),
                            "To reconnect your ring after reset, place it on the charger."
                        )
                    }
                } else {
                    ToastUtils.showShort(requireContext(), "Resetting to factory settings...")
                }
            }

            DeviceAction.REMOVE_FROM_ACCOUNT -> {
                DialogUtils.showConfirmationDialog(
                    context = requireContext(),
                    title = "Remove from account",
                    message = "This device will be removed from your account.",
                    btnPositiveText = "Remove",
                    onPositiveClick = {
                        val hwId = when (deviceType) {
                            DeviceType.BAND -> prefUtils.getBandHardware()?.id
                            DeviceType.RING -> prefUtils.getRingHardwareData()?.id
                            DeviceType.SMART_CUFF -> {
                                prefUtils.getBpHardware()?.id
                            }

                            DeviceType.WEIGHT_MACHINE -> {
                                prefUtils.getWeightHardware()?.id
                            }

                            DeviceType.UNKNOWN -> null
                        }

                        if (hwId.isNullOrBlank()) {
                            ToastUtils.showShort(
                                requireContext(),
                                "Unable to remove device. Hardware ID not found."
                            )
                        } else {
                            viewModel.deleteUserHardwareById(hwId)
                        }
                    },
                    btnNegativeText = "Cancel"
                )
            }

            DeviceAction.RE_SETUP -> {
                ToastUtils.showShort(requireContext(), "Re-setting up device...")
            }
        }
    }

    private fun updateUI(response: GetDeviceConfigResponse) {
        val data = response.data ?: return
        val deviceMeta = data.deviceMeta
        val deviceType = DeviceType.from(userDevice?.deviceName)

        if (deviceType == DeviceType.RING) {
            binding.tvSerialNumber.text = getString(R.string.serial_number_format, deviceMeta?.sn ?: "—")
            binding.tvMacAddress.text = getString(R.string.mac_format, deviceMeta?.mac ?: "—")
            binding.tvFirmwareLabel.text = getString(R.string.firmware_version_format, deviceMeta?.fw ?: "—")
            binding.tvUpdated.text = ""
        } else {
            binding.tvSerialNumber.text = getString(R.string.serial_number_format, deviceMeta?.sn ?: "J2208A 0668A8")
            binding.tvMacAddress.text = getString(R.string.mac_format, deviceMeta?.mac ?: "21:02:02:06:68:A8")
            binding.tvFirmwareLabel.text = getString(R.string.firmware_version_format, deviceMeta?.fw ?: "0646")
            binding.tvUpdated.text = "Updated"
        }

        val deviceDes: String? = deviceMeta?.desc ?: ""
        deviceMeta?.let {
            binding.tvDeviceDes.isVisible = true
            binding.tvDeviceDes.text = deviceDes
        }

        deviceMeta?.measureFreq?.let {
            val freq = it.toIntOrNull() ?: 60
            viewModel.setMeasureFrequency(freq)
            binding.switchLowPowerMode.isChecked = freq > 30
        }

        data.warrantyRemainingDays?.let {
            binding.tvWarrantyStatus.text = getString(R.string.remaining_days_format, it)
        }

        data.insight?.let { insight ->
            val deviceName = if (deviceType == DeviceType.RING) "Ring" else "Band"
            binding.tvDeviceInsightDesc.text = getString(
                R.string.device_insight_desc_format,
                insight.usage ?: "Unknown",
                deviceName,
                insight.daysUsed ?: 0
            )
        }
    }

    private fun clearDeletedDevicePreferences() {
        val deviceType = DeviceType.from(userDevice?.deviceName)
        val deletedDeviceId = prefUtils.getHardwareDetailsList()
            .find { it.hardwareType == deviceType.value }?.id

        if (!deletedDeviceId.isNullOrEmpty()) {
            val updatedHardwareList =
                prefUtils.getHardwareDetailsList().filterNot { it.id == deletedDeviceId }
            prefUtils.setHardwareDetailsList(updatedHardwareList)
        }

        when (DeviceType.from(userDevice?.deviceName)) {
            DeviceType.BAND -> {
                prefUtils.remove(Preference.WEARABLE_BAND)
                prefUtils.remove(Preference.BAND_HARDWARE_DATA)
            }

            DeviceType.RING -> {
                prefUtils.remove(Preference.WEARABLE_RING)
                prefUtils.remove(Preference.HARDWARE_DATA)
            }

            DeviceType.SMART_CUFF -> {
                prefUtils.remove(Preference.BP_MACHINE)
                prefUtils.remove(Preference.BP_HARDWARE_DATA)
            }

            DeviceType.WEIGHT_MACHINE -> {
                prefUtils.remove(Preference.WEIGHT_SCALE)
                prefUtils.remove(Preference.WEIGHT_HARDWARE_DATA)
            }

            DeviceType.UNKNOWN -> Unit
        }
    }

    private fun disconnectDeletedDevice() {
        when (DeviceType.from(userDevice?.deviceName)) {
            DeviceType.BAND -> {
                bandBleManager.disconnect()
            }

            DeviceType.RING -> {
                app.ringBleManager.disconnect()
                app.ringDeviceManager.unregisterCb()
            }

            DeviceType.SMART_CUFF -> {
            }

            DeviceType.WEIGHT_MACHINE -> {
            }

            DeviceType.UNKNOWN -> Unit
        }
    }

    private fun observeRing() {
        if (app.ringDeviceManager.connected.value == true) {
            app.ringDeviceManager.registerCb()
            updateConnectionUi(true)
            viewModel.fetchRingDeviceInfo()
        }

        app.ringDeviceManager.batteryLevel.observe(viewLifecycleOwner) {
            when (it.first) {
                STATE_DEVICE_CONNECTING -> {
                    updateConnectionUiConnecting()
                }

                STATE_DEVICE_CONNECTED -> {
                    updateConnectionUi(true)
                    viewModel.fetchRingDeviceInfo()
                }

                STATE_DEVICE_DISCONNECTED -> {
                    updateConnectionUi(false)
                }

                STATE_DEVICE_CHARGING -> {
                    // Update charging UI if needed
                }

                STATE_DEVICE_DISCHARGING -> {
                    // Update discharging UI if needed
                }
            }
            binding.tvBatteryValue.text = "${it.second}%"
        }

        /*app.ringDeviceManager.connected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                updateConnectionUi(true)
            } else {
                updateConnectionUi(false)
            }
        }*/
    }

    private fun observeBand() {
        viewLifecycleOwner.lifecycleScope.launch {
            bandBleManager.connectionState.collect { state ->
                PlutoLog.e(TAG_BAND_DEBUG, "DeviceConfigFragment bandManager state: $state")
                when (state) {
                    STATE_BAND_CONNECTED -> {
                        updateConnectionUi(true)
                    }

                    STATE_BAND_CONNECTING -> {
                        updateConnectionUiConnecting()
                    }

                    else -> {
                        updateConnectionUi(false)
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            bandBleManager.batteryLevel.collect { level ->
                if (level != null) {
                    binding.tvBatteryValue.text = "$level%"
                }
            }
        }
    }

    private fun updateConnectionUi(isConnected: Boolean) = with(binding) {
        ivStatusDot.setImageResource(
            if (isConnected) R.drawable.dot_connected
            else R.drawable.dot_disconnected
        )

        tvStatus.text =
            if (isConnected) getString(R.string.connected) else getString(R.string.disconnected)

        tvStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isConnected) R.color.colorBgBtn else R.color.disconnected
            )
        )

        btnPower.iconTint = ColorStateList.valueOf(
            ContextCompat.getColor(
                requireContext(),
                if (isConnected) R.color.green_1 else R.color.gray_400
            )
        )
    }

    private fun updateConnectionUiConnecting() = with(binding) {
        ivStatusDot.setImageResource(R.drawable.dot_connecting)
        tvStatus.text = getString(R.string.connecting)
        tvStatus.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.w200
            )
        )
    }

    private fun updateRingDeviceInfoUI(deviceInfo: DeviceInfo) = with(binding) {
        //tvSerialNumber.text = "Serial Number — ${deviceInfo.productModel ?: "-"}"
        tvMacAddress.text = "MAC: ${deviceInfo.bluetoothAddress}"
        binding.tvFirmwareLabel.text =
            getString(R.string.firmware_version_format, deviceInfo.firmwareVersion)

        /*val frequency = viewModel.getMeasureFrequency().value ?: 60
        prefUtils.getRingHardwareData()?.let { userHardware ->
            viewModel.addDeviceMetaData(userHardware, frequency)
        }*/
    }

    private fun isRingDeviceDisconnected(): Boolean {
        return DeviceType.from(userDevice?.deviceName) == DeviceType.RING &&
                app.ringDeviceManager.connected.value != true
    }

    private fun isBandDeviceDisconnected(): Boolean {
        return DeviceType.from(userDevice?.deviceName) == DeviceType.BAND &&
                bandBleManager.connectionState.value != STATE_BAND_CONNECTED
    }

    private fun showRingNotConnectedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ring not connected")
            .setMessage("Connect your ring to use this feature.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showBandNotConnectedDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Band not connected")
            .setMessage("Connect your band to use this feature.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        app.ringDeviceManager.unregisterCb()
    }
}
