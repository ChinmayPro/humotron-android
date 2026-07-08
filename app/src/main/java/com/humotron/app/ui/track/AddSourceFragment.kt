package com.humotron.app.ui.track

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentAddSourceBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddSourceFragment : BaseFragment(R.layout.fragment_add_source) {

    private lateinit var binding: FragmentAddSourceBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddSourceBinding.bind(view)
        initViews()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.track_add_source)
        binding.mcvPairHumotronDevice.setOnClickListener {
            findNavController().navigate(R.id.fragmentPairHumotronDevice)
        }

        binding.header.btnBack.setOnClickListener {
            if (findNavController().previousBackStackEntry == null) {
                requireActivity().finish()
            } else {
                findNavController().navigateUp()
            }
        }
    }
}
