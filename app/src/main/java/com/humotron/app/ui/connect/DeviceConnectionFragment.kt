package com.humotron.app.ui.connect

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.BleDevice
import com.humotron.app.bt.OnBleScanCallback
import com.humotron.app.bt.isCharging
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDeviceConnectionBinding
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.toRingColor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DeviceConnectionFragment : Fragment(R.layout.fragment_device_connection) {

    private lateinit var binding: FragmentDeviceConnectionBinding
    private val viewModel: DeviceConnectionViewModel by viewModels()

    @Inject
    lateinit var prefUtils: PrefUtils

    private val app by lazy { requireActivity().application as App }
    private var device: BleDevice? = null
    var dialog: LoadingDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDeviceConnectionBinding.bind(view)
        app.deviceManager.registerCb()
        binding.ringsView.startAnimation()

        app.bleManager.startScan(30000, object : OnBleScanCallback {
            @SuppressLint("MissingPermission")
            override fun onScanning(result: BleDevice) {
                if (result.generation != null && result.batteryLevel != null && result.isCharging) {
                    binding.tvNoDeviceFound.isVisible = false
                    binding.llConnectionNotes.isVisible = false
                    binding.tvDeviceDiscoverStatus.setText(R.string.showing_devices)
                    binding.clDevice.isVisible = true
                    binding.tvDeviceModel.text = result.device.name

                    binding.tvDeviceColor.text = requireActivity().toRingColor(result.color)
                    binding.tvDeviceGeneration.text =
                        getString(R.string.device_generation, result.generation)
                    binding.tvDeviceSize.text =
                        getString(R.string.device_size_us, result.size)
                    binding.ringsView.stopAnimation()
                    device = result
                }
            }

            override fun onScanFinished() {

            }

        })


        binding.cbDeviceChecked.setOnCheckedChangeListener { buttonView, isChecked ->
            binding.btnSubmit.isEnabled = isChecked
        }

        binding.btnSubmit.setOnClickListener { v ->
            device?.let {
                app.deviceManager.connect(it.device.address)
                showProgress()
            }
        }

        binding.clDevice.setOnClickListener {
            binding.cbDeviceChecked.isChecked = true
        }

        app.deviceManager.connected.observe(viewLifecycleOwner) {
            if (it) {
                DeviceConnectedFragment.device = device
                prefUtils.setString(Preference.WEARABLE_RING, device!!.device.address)
                if (prefUtils.getHardwareId().isNullOrEmpty()) {
                    viewModel.addHardwareInProfile(
                        AddHardware(
                            "HumotronRing",
                            device!!.device.address
                        )
                    )
                } else {
                    hideProgress()
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }
            }
        }

        viewModel.addHardwareData().observe(viewLifecycleOwner) { networkState ->
            when (networkState.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    networkState.data?.data?.userHardware?.let { prefUtils.setHardwareData(it) }
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    findNavController().navigate(R.id.fragmentDeviceConnected)
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }

//


    }

    fun showProgress() {
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

    fun hideProgress() {
        if (dialog?.isShowing == true) {
            dialog?.dismiss()
        }
    }


}