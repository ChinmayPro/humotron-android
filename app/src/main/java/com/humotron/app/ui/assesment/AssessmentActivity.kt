package com.humotron.app.ui.assesment

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.fragment.NavHostFragment
import com.humotron.app.R
import com.humotron.app.core.AppConstant.ASSESSMENT
import com.humotron.app.core.AppConstant.ASSESSMENT_ID
import com.humotron.app.domain.modal.response.MergedAssessment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class   AssessmentActivity : AppCompatActivity() {
 
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assessment)
//        val assessmentId = intent.getStringExtra(ASSESSMENT_ID)
//        val assessment = intent.getParcelableExtra<MergedAssessment>("assessment_obj")
        val json = intent.getStringExtra(ASSESSMENT)
        Log.e("TAG", "onViewCrefefrffated00:  ${json}", )


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        val navController = navHostFragment.navController

        val bundle = Bundle().apply {
            putString(ASSESSMENT, json)
        }

        navController.setGraph(R.navigation.nav_graph_assessment, bundle)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }
}
 