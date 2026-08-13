package com.humotron.app.ui.shop

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentEnterPincodeBinding
import com.humotron.app.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterPincodeFragment : BaseFragment(R.layout.fragment_enter_pincode) {

    private lateinit var binding: FragmentEnterPincodeBinding
    private val viewModel: ShopViewModel by activityViewModels()

    private fun requestLocationPermissionAndFetch() {
        com.permissionx.guolindev.PermissionX.init(this)
            .permissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(
                    deniedList,
                    "Location permission is needed to detect your current location and find nearest partner labs.",
                    "Allow",
                    "Cancel"
                )
            }
            .onForwardToSettings { scope, deniedList ->
                scope.showForwardToSettingsDialog(
                    deniedList,
                    "Location permission is currently disabled. Please enable location access in App Settings.",
                    "Open Settings",
                    "Cancel"
                )
            }
            .request { allGranted, grantedList, _ ->
                if (allGranted || grantedList.isNotEmpty()) {
                    fetchCurrentLocationPostcode()
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEnterPincodeBinding.bind(view)

        activity?.window?.statusBarColor = Color.TRANSPARENT

        setupInsets()
        setupProgressBar()
        initViews()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            binding.layoutFooter.updatePadding(bottom = systemBars.bottom + (16 * density).toInt())
            insets
        }
    }

    private fun setupProgressBar() {
        binding.llProgressBar.removeAllViews()
        val density = resources.displayMetrics.density
        val totalSteps = 6
        val currentStep = 1 // 0-indexed, step 2 = index 1

        for (i in 0 until totalSteps) {
            val segment = View(requireContext())
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            if (i < totalSteps - 1) {
                params.marginEnd = (6 * density).toInt()
            }
            segment.layoutParams = params

            val bgDrawable = GradientDrawable().apply {
                cornerRadius = 2 * density
                if (i <= currentStep) {
                    setColor(Color.parseColor("#5FB7C4"))
                } else {
                    setColor(Color.parseColor("#1AFFFFFF"))
                }
            }
            segment.background = bgDrawable
            binding.llProgressBar.addView(segment)
        }

        binding.tvStepEyebrow.text = "STEP 2 OF 6 · CONFIRM LOCATION"
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentBookingType, false)
        }

        binding.btnAccessLocation.setOnClickListener {
            requestLocationPermissionAndFetch()
        }

        binding.etPincode.filters = arrayOf(android.text.InputFilter.AllCaps())

        binding.etPincode.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                binding.etPincode.clearFocus()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        binding.btnContinue.setOnClickListener {
            val pincode = binding.etPincode.text.toString().trim()
            proceedWithPostcode(pincode)
        }
    }

    private fun proceedWithPostcode(pincode: String) {
        val bundle = androidx.core.os.bundleOf("postcode" to pincode)
        findNavController().navigate(R.id.action_enterPincodeFragment_to_fragmentSelectLab, bundle)
    }

    private fun hasLocationPermission(): Boolean {
        val fine = androidx.core.content.ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        val coarse = androidx.core.content.ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun fetchCurrentLocationPostcode() {
        if (!hasLocationPermission()) return
        try {
            val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    processLocation(location)
                } else {
                    fallbackLocationManager()
                }
            }.addOnFailureListener {
                fallbackLocationManager()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fallbackLocationManager()
        }
    }

    @Suppress("DEPRECATION")
    @android.annotation.SuppressLint("MissingPermission")
    private fun fallbackLocationManager() {
        if (!hasLocationPermission()) return
        try {
            val locationManager = requireContext().getSystemService(android.content.Context.LOCATION_SERVICE) as? android.location.LocationManager
            val location: android.location.Location? = locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                ?: locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(android.location.LocationManager.PASSIVE_PROVIDER)

            if (location != null) {
                processLocation(location)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Suppress("DEPRECATION")
    private fun processLocation(location: android.location.Location) {
        var postcode: String? = null
        try {
            if (android.location.Geocoder.isPresent()) {
                val geocoder = android.location.Geocoder(requireContext(), java.util.Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    postcode = addresses[0].postalCode
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val finalPostcode = if (!postcode.isNullOrBlank()) postcode else "E14 4QT"
        proceedWithPostcode(finalPostcode)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etPincode.windowToken, 0)
    }
}
