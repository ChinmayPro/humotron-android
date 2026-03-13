package com.humotron.app.ui.onboarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.humotron.app.R
import com.humotron.app.databinding.FragmentSplashBinding


class SplashFragment : Fragment(R.layout.fragment_splash) {


    private lateinit var binding: FragmentSplashBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSplashBinding.bind(view)
    }


}