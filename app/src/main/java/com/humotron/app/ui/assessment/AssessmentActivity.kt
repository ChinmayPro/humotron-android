package com.humotron.app.ui.assessment

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.humotron.app.R
import com.humotron.app.ui.navigation.NavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AssessmentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)
//        val assessmentId = intent.getStringExtra(ASSESSMENT_ID)
//        val assessment = intent.getParcelableExtra<MergedAssessment>("assessment_obj")
        val json = intent.getStringExtra(NavKeys.ASSESSMENT)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        val bundle = Bundle().apply {
            putString(NavKeys.ASSESSMENT, json)
        }

        navController.setGraph(R.navigation.nav_graph_assessment, bundle)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }
}
 