package com.humotron.app.ui.track

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentYetToTrackDetailsBinding
import com.humotron.app.domain.modal.response.UntrackedMetricData
import com.humotron.app.domain.modal.response.YetToTrackMetricDetailsData
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.ui.navigation.NavKeys
import com.humotron.app.util.MetricIconMapper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class YetToTrackDetailsFragment : Fragment(R.layout.fragment_yet_to_track_details) {

    private lateinit var binding: FragmentYetToTrackDetailsBinding
    private val viewModel: DeviceViewModel by viewModels()
    private var metricData: UntrackedMetricData? = null
    private lateinit var buyDeviceAdapter: BuyDeviceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentYetToTrackDetailsBinding.bind(view)

        metricData = arguments?.getParcelable(NavKeys.KEY_METRIC)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initClicks()
        initViews()
        observeViewModel()

        metricData?.id?.let { id ->
            viewModel.getMetricTrackingDetails(id)
        }
    }

    private fun initViews() {
        binding.header.tvTitle.text = metricData?.metricName ?: getString(R.string.metrics_details)
        val deviceName = metricData?.deviceName ?: "-"
        val text = getString(R.string.cta_start_tracking, deviceName)
        binding.btnCta.text = text

        buyDeviceAdapter = BuyDeviceAdapter(emptyList()) { device ->
            // TODO: Navigate to device shop/detail if needed
        }
        binding.rvBuyDevices.adapter = buyDeviceAdapter
    }

    private fun initClicks() {
        binding.header.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        binding.btnCta.setOnClickListener {
            findNavController().navigate(R.id.fragmentPairHumotronDevice)
        }
    }

    private fun observeViewModel() {
        viewModel.metricTrackingDetailsLiveData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val details = resource.data?.data ?: return@observe
                    updateUI(details)
                }

                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    private fun showProgress() {
        binding.shimmer.visibility = View.VISIBLE
        binding.shimmer.startShimmer()
        binding.scrollContent.visibility = View.GONE
    }

    private fun hideProgress() {
        binding.shimmer.stopShimmer()
        binding.shimmer.visibility = View.GONE
        binding.scrollContent.visibility = View.VISIBLE
    }

    private fun updateUI(details: YetToTrackMetricDetailsData) {
        binding.tvUnitValue.text = details.metricUnit ?: "-"
        binding.tvInfoWhatIsBody.text = details.metricWhat ?: details.metricDescription
        binding.tvInfoWhyTrackBody.text = details.metricWhy

        binding.tvSectionWhatIs.text =
            getString(R.string.what_is, details.metricName)
        binding.tvSectionStartToday.text =
            getString(R.string.section_start_today, details.metricName)

        binding.ivTileIcon.setImageResource(MetricIconMapper.getIconResource(details.metricName))

        buyDeviceAdapter.updateData(details.devices ?: emptyList())
    }

    companion object {

    }
}