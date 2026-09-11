package com.humotron.app.ui.shop.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.humotron.app.R
import com.humotron.app.databinding.LayoutBottomsheetShopDeviceConnectBinding
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.ui.navigation.NavKeys

class ShopDeviceConnectBottomSheet : BottomSheetDialogFragment() {

    private var _binding: LayoutBottomsheetShopDeviceConnectBinding? = null
    private val binding get() = _binding!!

    private var deviceTitle: String = ""
    private var deviceType: DeviceType = DeviceType.UNKNOWN
    private var heroIconRes: Int = R.drawable.ic_smart_scale_hero
    private var isConnected: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = LayoutBottomsheetShopDeviceConnectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener {
            val d = it as BottomSheetDialog
            val bottomSheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                BottomSheetBehavior.from(sheet).state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        deviceTitle = arguments?.getString(ARG_DEVICE_TITLE) ?: ""
        heroIconRes = arguments?.getInt(ARG_HERO_ICON, R.drawable.ic_smart_scale_hero) ?: R.drawable.ic_smart_scale_hero
        isConnected = arguments?.getBoolean(ARG_IS_CONNECTED, false) ?: false
        
        val typeString = arguments?.getString(ARG_DEVICE_TYPE)
        deviceType = DeviceType.from(typeString)

        binding.tvDeviceTitle.text = deviceTitle
        binding.ivDeviceIcon.setImageResource(heroIconRes)

        binding.btnBack.setOnClickListener { dismiss() }
        binding.btnClose.setOnClickListener { dismiss() }

        if (isConnected) {
            binding.tvSheetTitle.text = "Device Connected"
            binding.tvSheetTitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.lime))
            binding.tvConnectStatus.text = "This device is currently paired and syncing data with your Humotron account."
            binding.btnConnectAction.text = "Close"
            binding.btnConnectAction.setOnClickListener {
                dismiss()
            }
        } else {
            binding.tvSheetTitle.text = "Connect Device"
            binding.tvConnectStatus.text = "Ensure Bluetooth is enabled on your phone and power on your device to start pairing."
            binding.btnConnectAction.text = "Start Pairing"
            binding.btnConnectAction.setOnClickListener {
                dismiss()
                try {
                    findNavController().navigate(
                        R.id.fragmentHumotronDeviceConnectInstruction,
                        Bundle().apply {
                            putSerializable(NavKeys.KEY_DEVICE_TYPE, deviceType)
                        }
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "ShopDeviceConnectBottomSheet"
        private const val ARG_DEVICE_TITLE = "device_title"
        private const val ARG_DEVICE_TYPE = "device_type"
        private const val ARG_HERO_ICON = "hero_icon"
        private const val ARG_IS_CONNECTED = "is_connected"

        fun newInstance(
            deviceTitle: String,
            deviceType: DeviceType,
            heroIconRes: Int,
            isConnected: Boolean
        ): ShopDeviceConnectBottomSheet {
            val fragment = ShopDeviceConnectBottomSheet()
            val args = Bundle().apply {
                putString(ARG_DEVICE_TITLE, deviceTitle)
                putString(ARG_DEVICE_TYPE, deviceType.value)
                putInt(ARG_HERO_ICON, heroIconRes)
                putBoolean(ARG_IS_CONNECTED, isConnected)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
