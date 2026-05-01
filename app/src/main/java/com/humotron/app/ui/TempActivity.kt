package com.humotron.app.ui

import android.graphics.Color
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.humotron.app.R
import com.humotron.app.databinding.ActivityTempBinding
import com.humotron.app.view.LayerCard

class TempActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTempBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTempBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val data = listOf(
            LayerCard(
                1,
                "Sleep",
                "Detailed sleep analysis and recommendations",
                Color.parseColor("#4A90E2")
            ),
            LayerCard(
                2,
                "Stress",
                "Stress tracking and management tips",
                Color.parseColor("#7B61FF")
            ),
            LayerCard(
                3,
                "Meditation",
                "Daily guided meditation sessions",
                Color.parseColor("#FF6B6B")
            )
        )
        binding.layeredCardView.setCards(data)
    }

}