package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.databinding.FragmentProgressExplorerBinding


class ProgressExplorerFragment : Fragment(R.layout.fragment_progress_explorer) {

    private lateinit var binding: FragmentProgressExplorerBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProgressExplorerBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }

        val level = arguments?.getString("level") ?: ""
        val score = arguments?.getInt("score") ?: 0

        binding.textview.text = level
        binding.header.title.text = getString(R.string.levels_explained)
        binding.tvNuggetsRemain.text =
            getString(R.string.you_are_d_nuggets_away_from_an_upgrade, score)


        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

    }


}