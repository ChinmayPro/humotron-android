package com.humotron.app.ui

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.R
import com.humotron.app.bt.ring.RingBleDevice
import com.humotron.app.bt.ring.OnBleScanCallback
import com.humotron.app.bt.band.BandBleManager
import com.humotron.app.core.App
import com.humotron.app.core.Preference
import com.humotron.app.data.local.AppDatabase
import com.humotron.app.databinding.ActivityMainBinding
import com.humotron.app.ui.connect.DeviceConnectedFragment
import com.humotron.app.ui.connect.DeviceConnectionFragment
import com.humotron.app.ui.connect.HomeViewModel
import com.humotron.app.ui.onboarding.OnBoardingActivity
import com.humotron.app.util.PrefUtils
import com.humotron.app.util.TAG_RING_DEBUG
import com.permissionx.guolindev.PermissionX
import com.pluto.plugins.logger.PlutoLog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val homeViewModel by viewModels<HomeViewModel>()

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var prefUtils: PrefUtils

    @Inject
    lateinit var bandBleManager: BandBleManager

    private val app by lazy { application as App }

    var device: RingBleDevice? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment_content_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            val extraPadding = (40 * resources.displayMetrics.density).toInt()
            (binding.rlBottom.layoutParams).height = systemBars.bottom + extraPadding
            insets
        }

        initClicks()
        initViews()
        initObservers()
    }

    private fun initClicks() {
        binding.llTrack.setOnClickListener(this)
        binding.llDecode.setOnClickListener(this)
        binding.llBioHack.setOnClickListener(this)
        binding.llProfile.setOnClickListener(this)
        binding.llDecode.setOnClickListener(this)
    }

    private fun initViews() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            val isVisible =
                destination.id == R.id.fragmentTrack || destination.id == R.id.fragmentBioHack || destination.id == R.id.fragmentDecode || destination.id == R.id.fragmentDecodeMetrics || destination.id == R.id.fragmentProfile || destination.id == R.id.fragmentShop
            binding.rlBottom.isVisible = isVisible
            binding.rlBtnNavigation.isVisible = isVisible

            when (destination.id) {
                R.id.fragmentTrack -> highlightView(0)
                R.id.fragmentDecode, R.id.fragmentDecodeMetrics -> highlightView(1)
                R.id.fragmentBioHack -> highlightView(2)
                R.id.fragmentProfile -> highlightView(3)
            }

            // Centralized Status Bar Management
            val window = this.window
            val decorView = window.decorView
            val controller = WindowInsetsControllerCompat(window, decorView)
            
            when (destination.id) {
                R.id.fragmentProfile, R.id.fragmentUploadedReports -> {
                    window.statusBarColor = android.graphics.Color.BLACK
                    controller.isAppearanceLightStatusBars = false
                }
                R.id.fragmentShop -> {
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    controller.isAppearanceLightStatusBars = false
                }
                else -> {
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    controller.isAppearanceLightStatusBars = false
                }
            }
        }
        highlightView(0)
        // Start background connection service
        /*ContextCompat.startForegroundService(
            this,
            Intent(this, RingConnectionService::class.java)
        )*/

        //Force initialize HomeViewModel here so its init{} runs before any Fragment uses it, otherwise homeViewModel?.loadDateData() not work in RingDeviceManager
        homeViewModel
        checkBlePermissionsAndStart()
    }

    private fun initObservers() {
        app.ringDeviceManager.connected.observe(this) { isConnected ->
            if (isConnected) {
                device?.let {
                    homeViewModel.currBtMac = it.device.address ?: ""
                    DeviceConnectedFragment.device = device
                    DeviceConnectionFragment.device = device
                }
            }
        }
    }

    fun showOrHideBottomNav(show: Boolean) {
        binding.rlBottom.isVisible = show
        binding.rlBtnNavigation.isVisible = show
    }

    override fun onClick(p0: View?) {
        val options = NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, false)
            .setLaunchSingleTop(true)
            .build()

        when (p0) {
            binding.llTrack -> {
                navController.navigate(R.id.fragmentTrack, null, options)
                highlightView(0)
            }

            binding.llDecode -> {
                navController.navigate(R.id.fragmentDecode, null, options)
                highlightView(1)
            }

            binding.llBioHack -> {
                navController.navigate(R.id.fragmentBioHack, null, options)
                highlightView(2)
            }

            binding.llProfile -> {
                navController.navigate(R.id.nav_graph_profile, null, options)
                highlightView(3)
            }
        }
    }
    /*  private fun showAssessmentSheet() {
          val sheet = CardiovascularAssessmentBottomSheet.newInstance()
          sheet.onProceedClicked = {
              findNavController().navigate(R.id.action_home_to_assessment)
          }
          sheet.show(childFragmentManager, CardiovascularAssessmentBottomSheet.TAG)
      }*/
    /*  private fun showAssessmentSheet() {
          val sheet = CardiovascularAssessmentBottomSheet.newInstance()

          sheet.onProceedClicked = {
    //          findNavController().navigate(R.id.assessmentFragment)
              startActivity(Intent(this, AssessmentActivity::class.java))


          }

          sheet.show(supportFragmentManager, CardiovascularAssessmentBottomSheet.TAG)
      }*/

    private fun highlightView(count: Int) {
        val textList = listOf(binding.tvTrack, binding.tvDecode, binding.tvBioHack)
        for (i in 0 until textList.size) {
            if (i == count) {
                textList[i].setTextColor(ContextCompat.getColor(this, R.color.colorBgBtn))
            } else {
                textList[i].setTextColor(ContextCompat.getColor(this, R.color._7e))
            }
        }

        when (count) {
            0 -> {
                binding.ivTrack.setImageResource(R.drawable.ic_trends_selected)
                binding.ivDecode.setImageResource(R.drawable.ic_decode)
                binding.ivBioHack.setImageResource(R.drawable.ic_bio_hack)
                binding.ivProfile.setImageResource(R.drawable.ic_profile)
            }

            1 -> {
                binding.ivTrack.setImageResource(R.drawable.ic_trends)
                binding.ivDecode.setImageResource(R.drawable.ic_decode_selected)
                binding.ivBioHack.setImageResource(R.drawable.ic_bio_hack)
                binding.ivProfile.setImageResource(R.drawable.ic_profile)
            }

            2 -> {
                binding.ivTrack.setImageResource(R.drawable.ic_trends)
                binding.ivDecode.setImageResource(R.drawable.ic_decode)
                binding.ivBioHack.setImageResource(R.drawable.ic_bio_hack_selected)
                binding.ivProfile.setImageResource(R.drawable.ic_profile)
            }

            3 -> {
                binding.ivTrack.setImageResource(R.drawable.ic_trends)
                binding.ivDecode.setImageResource(R.drawable.ic_decode)
                binding.ivBioHack.setImageResource(R.drawable.ic_bio_hack)
                binding.ivProfile.setImageResource(R.drawable.ic_profile)
            }
        }
    }

    fun checkBlePermissionsAndStart() {
        if (!isBleSupported()) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show()
            return
        }

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        PermissionX.init(this)
            .permissions(permissions)
            .request { allGranted, grantedList, deniedList ->

                if (allGranted) {
                    if (!isBluetoothEnabled()) {
                        requestEnableBluetooth()
                    } else {
                        connectToRing()
                        connectToBand()
                    }
                } else {
                    if (deniedList.isNotEmpty()) {
                        //showPermissionSettingsDialog()
                    }
                }
            }
    }

    fun isBleSupported(): Boolean {
        val pm = packageManager
        val hasBleFeature = pm.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val adapterAvailable = bluetoothManager?.adapter != null
        return hasBleFeature && adapterAvailable
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        return bluetoothManager?.adapter?.isEnabled == true
    }

    fun requestEnableBluetooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(intent)
    }

    private val enableBluetoothLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (isBluetoothEnabled()) {
                connectToRing()
                connectToBand()
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_SHORT).show()
            }
        }

    private fun connectToRing() {
        PlutoLog.e(TAG_RING_DEBUG, "connectToRing")
        app.ringDeviceManager.registerCb()
        val address = prefUtils.getString(Preference.WEARABLE_RING) ?: ""
        if (address.isNotEmpty()) {
            app.ringBleManager.startScan(300000, object : OnBleScanCallback {
                override fun onScanning(result: RingBleDevice) {
                    if (result.device.address == address) {
                        PlutoLog.e(
                            TAG_RING_DEBUG,
                            "ring found"
                        )
                        device = result
                        app.ringDeviceManager.connect(result.device.address)
                    }
                }

                override fun onScanFinished() {

                }

            })
        } else {
            PlutoLog.e(
                TAG_RING_DEBUG,
                "ring address is empty"
            )
        }
    }

    private fun connectToBand() {
        val address = prefUtils.getString(Preference.WEARABLE_BAND) ?: ""
        if (address.isNotEmpty() && bandBleManager.isBluetoothEnabled()) {
            bandBleManager.connectDevice(address)
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)

        super.attachBaseContext(newBase)
    }

    fun showLogoutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()

                // Clear SharedPreferences
                prefUtils.clear()
                prefUtils.setBoolean(Preference.ONBOARD_PRIVACY, true)

                // Clear Room database in coroutine
                (this as? androidx.fragment.app.FragmentActivity)?.lifecycleScope?.launch {
                    withContext(Dispatchers.IO) {
                        database.clearAllTables()
                    }

                    // Navigate to login screen (adjust activity name)
                    val intent = Intent(this@MainActivity, OnBoardingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun showPermissionSettingsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permission Required")
            .setMessage("Bluetooth permission is required for connecting to your ring")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = "package:$packageName".toUri()
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, MainActivity::class.java)
        }
    }
}