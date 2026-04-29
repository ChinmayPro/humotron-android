package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
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

        // Set Status Bar Color to Black
        activity?.window?.statusBarColor = androidx.core.content.ContextCompat.getColor(requireContext(), com.humotron.app.R.color.black)

        val postcode = arguments?.getString("postcode") ?: ""

        initViews()
        setupInsets()
        setupObservers()
        
        // Fetch Labs
        viewModel.fetchAllLabs(postcode)
    }

    private fun setupInsets() {
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            
            val layoutParams = binding.btnContinue.layoutParams as android.view.ViewGroup.MarginLayoutParams
            layoutParams.bottomMargin = systemBars.bottom + dpToPx(24)
            binding.btnContinue.layoutParams = layoutParams
            
            insets
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun initViews() {
        binding.toolbar.setNavigationOnClickListener {
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
            // Navigate directly to Choose Date & Time
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
