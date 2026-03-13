package com.humotron.app.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.R
import com.humotron.app.core.Preference
import com.humotron.app.data.local.AppDatabase
import com.humotron.app.databinding.ActivityMainBinding
import com.humotron.app.ui.onboarding.OnBoardingActivity
import com.humotron.app.util.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var prefUtils: PrefUtils

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

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            val isVisible =
                destination.id == R.id.fragmentTrack || destination.id == R.id.fragmentBioHack
            binding.rlBottom.isVisible = isVisible
            binding.rlBtnNavigation.isVisible = isVisible
        }
        highlightView(0)
        setonClickListener()
    }

    fun showOrHideBottomNav(show: Boolean) {
        binding.rlBottom.isVisible = show
        binding.rlBtnNavigation.isVisible = show
    }

    private fun setonClickListener() {
        binding.llTrack.setOnClickListener(this)
        binding.llBioHack.setOnClickListener(this)
        binding.llProfile.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.llTrack -> {
                navController.navigate(R.id.fragmentTrack)
                highlightView(0)
            }

            binding.llBioHack -> {
                navController.navigate(R.id.fragmentBioHack)
                highlightView(1)
            }

            binding.llProfile -> {
                showLogoutDialog()
            }
        }
    }

    private fun highlightView(count: Int) {
        val textList = listOf(binding.tvTrack, binding.tvBioHack)
        for (i in 0 until textList.size) {
            if (i == count) {
                textList[i].setTextColor(ContextCompat.getColor(this, R.color.colorBgBtn))
            } else {
                textList[i].setTextColor(ContextCompat.getColor(this, R.color._7e))
            }
        }

        if (count == 0) {
            binding.ivTrack.setImageResource(R.drawable.ic_trends_selected)
            binding.ivBioHack.setImageResource(R.drawable.ic_bio_hack)
        } else {
            binding.ivTrack.setImageResource(R.drawable.ic_trends)
            binding.ivBioHack.setImageResource(R.drawable.ic_bio_hack_selected)
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
}


