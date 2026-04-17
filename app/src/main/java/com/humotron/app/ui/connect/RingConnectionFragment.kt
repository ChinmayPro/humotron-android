package com.humotron.app.ui.connect

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.bt.ring.OnBleScanCallback
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.bt.ring.isCharging
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentRingConnectionBinding
import com.humotron.app.domain.modal.param.AddHardware
import com.humotron.app.ui.connect.adapter.RingDeviceAdapter
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.util.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class RingConnectionFragment : Fragment(R.layout.fragment_ring_connection) {

    private lateinit var binding: FragmentRingConnectionBinding
    private val viewModel: RingConnectionViewModel by viewModels()

    @Inject
    lateinit var prefUtils: PrefUtils

    private val app by lazy { requireActivity().application as App }

    //private var device: RingBleDevice? = null
    var dialog: LoadingDialog? = null

    companion object {
        private const val TAG = "DCF"
        var device: RingBleDevice? = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private val adapter by lazy {
        RingDeviceAdapter { selected ->
            device = selected
            binding.btnSubmit.isEnabled = selected != null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRingConnectionBinding.bind(view)
        app.ringDeviceManager.registerCb()
        binding.ringsView.startAnimation()

        binding.rvDevices.adapter = adapter

        app.ringBleManager.startScan(30000, object : OnBleScanCallback {
            @SuppressLint("MissingPermission")
            override fun onScanning(result: RingBleDevice) {
                if (result.generation != null && result.batteryLevel != null && result.isCharging) {
                    binding.clNoDeviceFound.isVisible = false
                    binding.llConnectionNotes.isVisible = false
                    binding.tvDeviceDiscoverStatus.setText(R.string.showing_devices)
                    adapter.addDevice(result)
                    binding.ringsView.stopAnimation()
                }
            }

            override fun onScanFinished() {

            }

        })

        binding.btnSubmit.setOnClickListener { v ->
            device?.let {
                app.ringDeviceManager.connect(it.device.address)
                showProgress()
            }
        }

        app.ringDeviceManager.connected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected && device != null) {
                DeviceConnectedFragment.device = device
                device?.device?.address?.let {
                    prefUtils.setString(Preference.WEARABLE_RING, it)
                }
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