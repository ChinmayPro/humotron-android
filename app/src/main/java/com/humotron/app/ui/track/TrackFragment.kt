package com.humotron.app.ui.track

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionManager
import com.google.android.material.transition.MaterialFade
import com.humotron.app.R
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentTrackBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable
import com.humotron.app.ui.connect.dialog.DeviceSelectionBottomSheet
import com.humotron.app.ui.device.DeviceViewModel
import com.pluto.plugins.logger.PlutoLog
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackFragment : BaseFragment(R.layout.fragment_track), OnClickListener {


    private lateinit var binding: FragmentTrackBinding
    private val viewModel: DeviceViewModel by viewModels()
    private var wearableAdapter: WearableAdapter? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentTrackBinding.bind(view)

        initClicks()
        initViews()
        initObservers()
    }

    private fun initViews() {
        prefUtils.getString(Preference.WEARABLE_RING).apply {
            if (isNullOrEmpty()) {
                binding.dsvWearables.isVisible = false
                viewModel.getHardwareList()
            } else {
                viewModel.getDeviceData()
                binding.dsvWearables.isVisible = true
            }
        }
    }

    private fun initClicks() {
        binding.ivAdd.setOnClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refreshUserDeviceData(true)
        }
    }

    private fun initObservers() {
        viewModel.observeUserDeviceData()
        lifecycleScope.launch {
            viewModel.deviceData.collect {
                /*it.hrvMapper.let {
                    binding.tvHrv.text = "${it.hrv}"
                    if (it.milisecond > 0) {
                        binding.tvLastSync.text = getTimeAgo(it.milisecond)
                    } else {
                        binding.tvLastSync.text = "-"
                    }

                }
                it.hrMapper.let {
                    binding.tvHr.text = it.hr.toString()
                }*/
            }
        }

        viewModel.getDeviceListData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = it.data?.data ?: return@observe
                    if (!data.wearables.isNullOrEmpty()) {
                        setupDiscreteScrollView(data.wearables)
                        binding.dsvWearables.isVisible = true
                    } else {
                        binding.dsvWearables.isVisible = false
                    }
                    val transition = MaterialFade().apply {
                        duration = 1000
                    }
                    TransitionManager.beginDelayedTransition(binding.root, transition)
                    binding.nsvTrack.isVisible = true
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.getHardwareListData().observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    val data = it.data?.data ?: return@observe
                    data.hardwareDetails?.firstOrNull { it.hardwareType == "HumotronRing" }?.let {
                        prefUtils.setHardwareData(it)
                        it.userHardwareUUID?.let { value ->
                            prefUtils.setString(
                                Preference.WEARABLE_RING,
                                value
                            )
                        }
                        viewModel.refreshUserDeviceData(true)
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    private fun setupDiscreteScrollView(wearables: List<Wearable>) {
        wearableAdapter = WearableAdapter(wearables) { wearable ->
            findNavController().navigate(R.id.fragmentDeviceData, Bundle().apply {
                putParcelable("wearable", wearable)
            })
        }
        binding.dsvWearables.adapter = wearableAdapter
        binding.dsvWearables.setItemTransformer(
            ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER)
                .setPivotY(Pivot.Y.CENTER)
                .build()
        )
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.ivAdd -> {
                val bottomSheet = DeviceSelectionBottomSheet()
                bottomSheet.setDeviceSelectionListener { deviceInfo ->
                    bottomSheet.dismiss()
                    findNavController().navigate(R.id.fragmentConnectInfo, Bundle().apply {
                        putParcelable("deviceInfo", deviceInfo)
                    })
                }
                bottomSheet.show(childFragmentManager, "device_list")
            }
        }
    }

    fun getTimeAgo(timeInMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timeInMillis

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7

        return when {
            seconds < 60 -> "$seconds seconds ago"
            minutes < 60 -> "$minutes minutes ago"
            hours < 24 -> "$hours hours ago"
            days < 7 -> "$days days ago"
            else -> "$weeks weeks ago"
        }
    }
}
