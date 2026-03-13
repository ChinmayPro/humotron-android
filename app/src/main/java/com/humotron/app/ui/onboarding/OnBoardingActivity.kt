package com.humotron.app.ui.onboarding

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.humotron.app.R
import com.humotron.app.core.Preference
import com.humotron.app.ui.MainActivity
import com.humotron.app.util.PrefUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnBoardingActivity : AppCompatActivity() {
    @Inject
    lateinit var prefUtils: PrefUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_on_boarding)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        if (prefUtils.isLogin()) {
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        } else {
            val isPrivacy = prefUtils.getBoolean(Preference.ONBOARD_PRIVACY)
            val navController = navHostFragment.navController
            val navInflater = navController.navInflater
            val navGraph = navInflater.inflate(R.navigation.nav_onboarding)
            navGraph.setStartDestination(
                if (isPrivacy) {
                    R.id.loginMethodFragment
                } else {
                    R.id.onBoardFragment
                }
            )
            navController.graph = navGraph
        }

    }

    override fun attachBaseContext(newBase: Context?) {
        val newOverride = Configuration(newBase?.resources?.configuration)
        newOverride.densityDpi = DisplayMetrics.DENSITY_DEVICE_STABLE
        newOverride.fontScale = 1.0f
        applyOverrideConfiguration(newOverride)

        super.attachBaseContext(newBase)
    }
}