package com.humotron.app.ui.bloodTest

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.humotron.app.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BloodTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blood_test)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_blood_test) as NavHostFragment
        val navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph_blood_test)

        window.statusBarColor = Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, BloodTestActivity::class.java)
        }
    }
}
