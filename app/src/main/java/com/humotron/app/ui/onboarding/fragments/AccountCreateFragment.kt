package com.humotron.app.ui.onboarding.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentAccountCreateBinding


class AccountCreateFragment : Fragment(R.layout.fragment_account_create) {

    private lateinit var binding: FragmentAccountCreateBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAccountCreateBinding.bind(view)

        binding.btnSubmit.setOnClickListener {
            findNavController().navigate(R.id.personalizeFragment)
        }

    }

}