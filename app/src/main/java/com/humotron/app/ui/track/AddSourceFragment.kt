package com.humotron.app.ui.track

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initData()
        observeData()
    }

    private fun initData() {
        binding.header.tvTitle.text = getString(R.string.track_add_source)

    }

    private fun observeData() {

    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            if (findNavController().previousBackStackEntry == null) {
                requireActivity().finish()
            } else {
                findNavController().navigateUp()
            }
        }
        binding.mcvPairHumotronDevice.setOnClickListener {
            findNavController().navigate(R.id.fragmentPairHumotronDevice)
        }
        binding.mcvConnectWearable.setOnClickListener {
            findNavController().navigate(R.id.fragmentConnectWearable)
        }
        binding.mcvFillAssessment.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentAddSource_to_fragmentAssessmentList)
        }
        binding.mcvUploadReport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentAddSource_to_fragmentUploadReportIntro)
        }
    }
}
