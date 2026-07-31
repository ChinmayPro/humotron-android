package com.humotron.app.ui.connect.wearable

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentConnectWearableBinding
import com.humotron.app.domain.modal.response.ProviderResponse
import com.humotron.app.ui.connect.adapter.ProviderAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ConnectWearableFragment : Fragment(R.layout.fragment_connect_wearable) {

    private lateinit var binding: FragmentConnectWearableBinding
    private val viewModel: ConnectWearableViewModel by viewModels()
    private var selectedProvider: ProviderResponse.Data.Provider? = null

    private val adapter by lazy {
        ProviderAdapter { provider ->
            selectedProvider = provider
            updateActionButtonState()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConnectWearableBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.track_connect_wearable_title)
        binding.rvWearables.adapter = adapter
        updateActionButtonState()
    }

    private fun updateActionButtonState() {
        if (selectedProvider != null) {
            binding.btnChooseDevice.backgroundTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.lime)
            binding.btnChooseDevice.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.insights_btn_text_dark
                )
            )
            (getString(R.string.connect) + " ${selectedProvider?.providerName}").also {
                binding.btnChooseDevice.text = it
            }
        }
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.cvFoldToggle.setOnClickListener {
            val isExpanded = binding.cvFoldContent.isVisible
            binding.cvFoldContent.isVisible = !isExpanded
            binding.ivFoldChevron.rotation = if (isExpanded) 0f else 180f
        }
        binding.btnChooseDevice.setOnClickListener {
            selectedProvider?.let {
                val action = ConnectWearableFragmentDirections
                    .actionFragmentConnectWearableToFragmentWearableAuthoriseAccess(it)
                findNavController().navigate(action)
            }
        }
        binding.cvUnlocks.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentConnectWearable_to_fragmentWearableDataUnlocks)
        }
    }

    private fun observeViewModel() {
        viewModel.providers.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    resource.data?.data?.providers?.let {
                        adapter.updateData(it)
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {

                }

                Status.LOADING -> {

                }
            }
        }
    }

    companion object {

    }
}