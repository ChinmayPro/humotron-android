package com.humotron.app.ui.shop

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSelectLabBinding
import com.humotron.app.ui.shop.adapter.LabSelectAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectLabFragment : BaseFragment(R.layout.fragment_select_lab) {

    private lateinit var binding: FragmentSelectLabBinding
    private val viewModel: ShopViewModel by activityViewModels()

    private lateinit var adapter: LabSelectAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSelectLabBinding.bind(view)

        activity?.window?.statusBarColor = Color.TRANSPARENT

        val postcode = arguments?.getString("postcode") ?: ""

        setupInsets()
        setupProgressBar()
        initViews()
        setupObservers()

        viewModel.fetchAllLabs(postcode)
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val density = resources.displayMetrics.density
            binding.layoutFooter.updatePadding(bottom = systemBars.bottom + (16 * density).toInt())
            insets
        }
    }

    private fun setupProgressBar() {
        binding.llProgressBar.removeAllViews()
        val density = resources.displayMetrics.density
        val totalSteps = 6
        val currentStep = 2 // 0-indexed, step 3

        for (i in 0 until totalSteps) {
            val segment = View(requireContext())
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            if (i < totalSteps - 1) {
                params.marginEnd = (6 * density).toInt()
            }
            segment.layoutParams = params

            val bgDrawable = GradientDrawable().apply {
                cornerRadius = 2 * density
                if (i <= currentStep) {
                    setColor(Color.parseColor("#5FB7C4"))
                } else {
                    setColor(Color.parseColor("#1AFFFFFF"))
                }
            }
            segment.background = bgDrawable
            binding.llProgressBar.addView(segment)
        }

        binding.tvStepEyebrow.text = "STEP 3 OF 6 · CHOOSE A LAB"
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnClose.setOnClickListener {
            findNavController().popBackStack(R.id.fragmentBookingType, false)
        }

        binding.cvSearchField.setOnClickListener {
            // Navigate back to postcode entry
            findNavController().popBackStack()
        }

        adapter = LabSelectAdapter { selectedLab ->
            if (selectedLab != null) {
                binding.btnContinue.isEnabled = true
                binding.btnContinue.alpha = 1.0f
                viewModel.setSelectedLab(selectedLab)
            }
        }
        binding.rvLabs.adapter = adapter

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSelectLab_to_fragmentChooseDateTime)
        }
    }

    private fun setupObservers() {
        viewModel.getLabsLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    val labs = resource.data?.data?.labList
                    if (!labs.isNullOrEmpty()) {
                        binding.tvNoData.visibility = View.GONE
                        binding.rvLabs.visibility = View.VISIBLE
                        adapter.setLabs(labs)
                    } else {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.rvLabs.visibility = View.GONE
                    }
                }
                Status.LOADING -> {
                    binding.layoutLoader.root.visibility = View.VISIBLE
                    binding.layoutLoader.tvLoadingMessage.text = "Searching for labs..."
                    binding.tvNoData.visibility = View.GONE
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.layoutLoader.root.visibility = View.GONE
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.tvNoData.text = resource.error?.errorMessage ?: "An error occurred"
                    binding.rvLabs.visibility = View.GONE
                }
            }
        }
    }
}
