package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentPreferenceDoneBinding


class PreferenceDoneFragment : Fragment(R.layout.fragment_preference_done) {

    private lateinit var binding: FragmentPreferenceDoneBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPreferenceDoneBinding.bind(view)

        binding.llNuggets.setOnClickListener {
            findNavController().navigate(R.id.fragmentNuggets)
        }

        binding.llBooks.setOnClickListener {
            findNavController().navigate(R.id.fragmentBookDetail)
        }

        binding.llTrack.setOnClickListener {
            findNavController().navigate(R.id.fragmentProgress)
        }
    }

}