package com.humotron.app.ui.connect.wearable

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentConnectWearableAuthBinding
import com.humotron.app.domain.modal.BackendProviderType
import com.humotron.app.domain.modal.DeviceType
import com.humotron.app.ui.dialogs.LoadingDialog
import com.humotron.app.ui.dialogs.StatusBottomSheetDialog
import com.humotron.app.util.TAG
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class ConnectWearableAuthFragment : Fragment(R.layout.fragment_connect_wearable_auth) {

    private lateinit var binding: FragmentConnectWearableAuthBinding
    private val viewModel: ConnectWearableViewModel by viewModels()
    private val args: ConnectWearableAuthFragmentArgs by navArgs()
    private var loadingDialog: LoadingDialog? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentConnectWearableAuthBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        initWebView()
        observeViewModel()
    }

    private fun initViews() {
        binding.header.tvTitle.text = getString(R.string.track_connect_wearable_title)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView() {
        binding.webView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?,
                ): Boolean {
                    val url = request?.url?.toString() ?: ""
                    Log.e(TAG, "shouldOverrideUrlLoading: $url")
                    if (isFallbackUrl(url)) {
                        val status = request?.url?.getQueryParameter("status")
                        when (status) {
                            "success" -> handleAuthSuccess()
                            "action_required" -> {
                                //val reason = request.url?.getQueryParameter("reason")
                                val reason = getString(R.string.to_connect_with_fitbit)
                                val setupUrl = request.url?.getQueryParameter("setupUrl")

                                showStatusDialog(
                                    false,
                                    "Action Required",
                                    reason,
                                    "Close"
                                ) {
                                    if (!setupUrl.isNullOrBlank()) {
                                        Log.e(TAG, "Opening setupUrl: $setupUrl")
                                        //view?.loadUrl(setupUrl)
                                        /*startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                setupUrl.toUri()
                                            )
                                        )*/
                                        findNavController().popBackStack()
                                    } else {
                                        findNavController().popBackStack()
                                    }
                                }
                            }

                            "fail" -> {
                                val message = request.url?.getQueryParameter("message")
                                    ?: getString(R.string.something_went_wrong)
                                showStatusDialog(
                                    false,
                                    "Authentication Failed",
                                    message,
                                    "Close"
                                ) {
                                    findNavController().popBackStack()
                                }
                            }
                        }
                        return true
                    }
                    return false
                }
            }
            loadUrl(args.authorizationUrl)
        }
    }

    private fun isFallbackUrl(url: String): Boolean {
        return url.startsWith("humotron://oauth/callback")
    }

    private fun handleAuthSuccess() {
        val provider = args.provider
        val type = BackendProviderType.from(provider.backendProvider)
        if (type == BackendProviderType.GOOGLE_HEALTH) {
            viewModel.getGoogleHealthStatus()
        } else {
            provider.providerValue?.let {
                viewModel.confirmWearableConnection(it)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.confirmResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    if (resource.data?.data?.isConnected == true) {
                        val provider = args.provider
                        val type = BackendProviderType.from(provider.backendProvider)
                        if (type == BackendProviderType.GOOGLE_HEALTH) {
                            viewModel.backfillGoogleHealth()
                        } else {
                            provider.providerValue?.let {
                                viewModel.syncWearableData(it)
                            }
                        }
                    } else {
                        hideLoading()
                        val providerName = args.provider.providerName ?: "Provider"
                        showStatusDialog(
                            false,
                            "Connection Failed",
                            "$providerName connection false",
                            "Close"
                        ) {
                            findNavController().popBackStack()
                        }
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideLoading()
                    showStatusDialog(
                        false,
                        "Connection Failed",
                        resources.getString(R.string.something_went_wrong),
                        "Close"
                    )
                }

                Status.LOADING -> {
                    showLoading()
                }
            }
        }

        viewModel.syncResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    showStatusDialog(
                        true,
                        "Success",
                        "Device connected and synced successfully.",
                        "Done"
                    ) {
                        findNavController().popBackStack(R.id.fragmentTrack, false)
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideLoading()
                    showStatusDialog(
                        false,
                        "Sync Failed",
                        resources.getString(R.string.something_went_wrong),
                        "Close"
                    ) {
                        findNavController().popBackStack()
                    }
                }

                Status.LOADING -> {
                    // confirmResponse already shows loading
                }
            }
        }

        viewModel.backfillResponse.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoading()
                    showStatusDialog(
                        true,
                        "Success",
                        "Google Health data backfilled successfully.",
                        "Done"
                    ) {
                        findNavController().popBackStack(R.id.fragmentTrack, false)
                    }
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideLoading()
                    showStatusDialog(
                        false,
                        "Backfill Failed",
                        resources.getString(R.string.something_went_wrong),
                        "Close"
                    ) {
                        findNavController().popBackStack()
                    }
                }

                Status.LOADING -> {
                    // confirmResponse already shows loading
                }
            }
        }
    }

    private fun showLoading() {
        if (loadingDialog == null) {
            loadingDialog = LoadingDialog(requireContext())
        }
        loadingDialog?.show()
    }

    private fun hideLoading() {
        loadingDialog?.dismiss()
    }

    private fun showStatusDialog(
        isSuccess: Boolean,
        title: String,
        subtitle: String,
        buttonText: String,
        onAction: (() -> Unit)? = null,
    ) {
        val dialog = StatusBottomSheetDialog.newInstance(isSuccess, title, subtitle, buttonText)
        dialog.setActionListener {
            onAction?.invoke()
        }
        dialog.show(childFragmentManager, StatusBottomSheetDialog.TAG)
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    companion object {

    }
}