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
        binding.ivPrefNuggets.setOnClickListener(this)
        binding.tvPrefNuggets.setOnClickListener(this)
        binding.ivPrefBooks.setOnClickListener(this)
        binding.tvPrefBooks.setOnClickListener(this)
        binding.ivPrefTrack.setOnClickListener(this)
        binding.tvPrefTracks.setOnClickListener(this)
        binding.btnSubmit.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            binding.header.ivNuggets, binding.ivPrefNuggets, binding.tvPrefNuggets -> {
                binding.tvPrefNuggets.isChecked = binding.tvPrefNuggets.isChecked.not()
                val iconRes = if (binding.tvPrefNuggets.isChecked) {
                    R.drawable.ic_nuggets_checked
                } else {
                    R.drawable.ic_nuggets_disable
                }
                binding.ivPrefNuggets.setImageResource(iconRes)
                binding.header.ivNuggets.setImageResource(iconRes)
            }

            binding.header.ivBooks, binding.ivPrefBooks, binding.tvPrefBooks -> {
                binding.tvPrefBooks.isChecked = binding.tvPrefBooks.isChecked.not()
                val iconRes = if (binding.tvPrefBooks.isChecked) {
                    R.drawable.ic_books_checked
                } else {
                    R.drawable.ic_books_disable
                }
                binding.ivPrefBooks.setImageResource(iconRes)
                binding.header.ivBooks.setImageResource(iconRes)
            }

            binding.header.ivProgress, binding.ivPrefTrack, binding.tvPrefTracks -> {
                binding.tvPrefTracks.isChecked = binding.tvPrefTracks.isChecked.not()
                val iconRes = if (binding.tvPrefTracks.isChecked) {
                    R.drawable.ic_biohack_progress_checked
                } else {
                    R.drawable.ic_biohack_progress_disable
                }
                binding.ivPrefTrack.setImageResource(iconRes)
                binding.header.ivProgress.setImageResource(iconRes)
            }

            binding.btnSubmit -> {
                findNavController().navigate(R.id.fragmentQuestion)
            }

        }

    }


}