package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSetUpPreferenceBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SetUpPreferenceFragment : BaseFragment(R.layout.fragment_set_up_preference),
    View.OnClickListener {

    private lateinit var binding: FragmentSetUpPreferenceBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSetUpPreferenceBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollRoot) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }
        setClicks()
    }

    private fun setClicks() {
        binding.header.ivNuggets.setOnClickListener(this)
        binding.header.ivBooks.setOnClickListener(this)
        binding.header.ivProgress.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.header.ivNuggets -> {
                binding.tvNuggets.isChecked = binding.tvNuggets.isChecked.not()
                binding.header.ivNuggets.setImageResource(
                    if (binding.tvNuggets.isChecked) {
                        R.drawable.ic_nuggets_checked
                    } else {
                        R.drawable.ic_nuggets_disable
                    }
                )
            }

            binding.header.ivBooks -> {
                binding.tvBooks.isChecked = binding.tvBooks.isChecked.not()
                binding.header.ivBooks.setImageResource(
                    if (binding.tvBooks.isChecked) {
                        R.drawable.ic_books_checked
                    } else {
                        R.drawable.ic_books_disable
                    }
                )
            }

            binding.header.ivProgress -> {
                binding.tvTracks.isChecked = binding.tvTracks.isChecked.not()
                binding.header.ivProgress.setImageResource(
                    if (binding.tvTracks.isChecked) {
                        R.drawable.ic_biohack_progress_checked
                    } else {
                        R.drawable.ic_biohack_progress_disable
                    }
                )
            }

            binding.btnSubmit -> {
                findNavController().navigate(R.id.fragmentQuestion)
            }

        }

    }


}