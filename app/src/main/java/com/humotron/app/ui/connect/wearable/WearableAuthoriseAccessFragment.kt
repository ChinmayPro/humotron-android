package com.humotron.app.ui.connect.wearable

import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentWearableAuthoriseAccessBinding
import com.humotron.app.domain.modal.BackendProviderType
import com.humotron.app.ui.dialogs.StatusBottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WearableAuthoriseAccessFragment : Fragment(R.layout.fragment_wearable_authorise_access) {

    private lateinit var binding: FragmentWearableAuthoriseAccessBinding
    private val viewModel: ConnectWearableViewModel by viewModels()
    private val args: WearableAuthoriseAccessFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentWearableAuthoriseAccessBinding.bind(view)
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
        binding.header.tvTitle.text = getString(R.string.authorise_access)
        val provider = args.provider
        val providerName = provider.providerName ?: ""

        binding.tvProviderName.text = providerName
        binding.tvProviderInitial.text = providerName.firstOrNull()?.toString() ?: ""

        binding.tvSubheadline.text = getString(R.string.subheadline_ext_auth, providerName)
        binding.tvAuthRequestDesc.text = getString(R.string.auth_request_desc, providerName)
        binding.tvPromiseReadonlyDesc.text = getString(R.string.promise_readonly_desc, providerName)
        binding.tvOauthUrl.text =
            getString(R.string.auth_oauth_url, providerName.lowercase().replace(" ", ""))

        provider.bgColorCode?.let {
            try {
                binding.cvProviderMark.setCardBackgroundColor(it.toColorInt())
            } catch (e: Exception) {
            }
        }

        provider.txtColorCode?.let {
            try {
                binding.tvProviderInitial.setTextColor(it.toColorInt())
            } catch (e: Exception) {
            }
        }
    }

    private fun initClicks() {
        binding.btnAllowReadOnly.setOnClickListener {
            val provider = args.provider
            val type = BackendProviderType.from(provider.backendProvider)
            if (type == BackendProviderType.GOOGLE_HEALTH) {
                binding.btnAllowReadOnly.isEnabled = false
                viewModel.connectGoogleHealth()
            } else {
                provider.providerValue?.let {
                    binding.btnAllowReadOnly.isEnabled = false
                    viewModel.connectWearableProvider(it)
                }
            }
        }
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.tvNotNow.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeViewModel() {
        viewModel.connectResponse.observe(viewLifecycleOwner) { resource ->
            resource ?: return@observe
            when (resource.status) {
                Status.SUCCESS -> {
                    val authUrl = resource.data?.data?.authorizationUrl
                    if (authUrl != null) {
                        viewModel.resetConnectResponse()
                        val action = WearableAuthoriseAccessFragmentDirections
                            .actionFragmentWearableAuthoriseAccessToFragmentConnectWearableAuth(
                                args.provider,
                                authUrl
                            )
                        findNavController().navigate(action)
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    binding.btnAllowReadOnly.isEnabled = true
                }

                Status.LOADING -> {

                }
            }
        }
    }

    companion object {

    }
}