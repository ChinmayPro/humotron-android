package com.humotron.app.ui.track

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.humotron.app.R
import com.humotron.app.core.AppConstant.ASSESSMENT
import com.humotron.app.core.AppConstant.ASSESSMENT_ID
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentTrackBinding
import com.humotron.app.domain.modal.response.GetAllDeviceResponse.Data.Wearable
import com.humotron.app.domain.modal.response.MergedAssessment
import com.humotron.app.ui.assesment.AssessmentActivity
import com.humotron.app.ui.assesment.CardiovascularAssessmentBottomSheet
import com.humotron.app.ui.connect.dialog.DeviceSelectionBottomSheet
import com.humotron.app.ui.device.DeviceViewModel
import com.humotron.app.util.fadeIn
import com.humotron.app.util.showWithFade
import com.yarolegovich.discretescrollview.transform.Pivot
import com.yarolegovich.discretescrollview.transform.ScaleTransformer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TrackFragment : BaseFragment(R.layout.fragment_track), OnClickListener {

    private lateinit var binding: FragmentTrackBinding
    private val viewModel: DeviceViewModel by viewModels()
    private var wearableAdapter: WearableAdapter? = null
    private var assessmentAdapter: AssessmentAdapter? = null

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
        viewModel.getMergedAssessmentList()
    }
    override fun onResume() {
        super.onResume()
        binding.swipeRefreshLayout.isRefreshing = false
        viewModel.refreshUserDeviceData(true)
        viewModel.getMergedAssessmentList()    }

    private fun initClicks() {
        binding.ivAdd.setOnClickListener(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = false
            viewModel.refreshUserDeviceData(true)
            viewModel.getMergedAssessmentList()
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
                        binding.dsvWearables.showWithFade { }
                    } else {
                        binding.dsvWearables.isVisible = false
                        binding.clNoDeviceFound.showWithFade { }
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

        viewModel.mergedAssessmentListLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    val data = it.data?.data ?: return@observe
                    if (data.isNotEmpty()) {
                        setupAssessmentRecyclerView(data)
                        binding.dsvAssessments.fadeIn()
                        binding.clNoPictureData.isVisible = false
                    } else {
                        binding.dsvAssessments.isVisible = false
                        binding.clNoPictureData.fadeIn()
                    }
                }

                Status.ERROR -> {
                    hideProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }

                Status.LOADING -> {
                    //showProgress()
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

    private fun showAssessmentSheet(assessment: MergedAssessment) {

        val json = Gson().toJson(assessment)
        Log.e("TAG", "showAddddssessmentSheet: ${json} ", )
        Log.e("TAG", "showAddddssessmentSheet00: ${assessment} ", )

        val sheet = CardiovascularAssessmentBottomSheet.newInstance(json)

        sheet.onProceedClicked = {

            if (isAdded) {
                val intent = Intent(requireContext(), AssessmentActivity::class.java)
                intent.putExtra(ASSESSMENT, json)
                startActivity(intent)
            }
        }

        sheet.show(parentFragmentManager, CardiovascularAssessmentBottomSheet.TAG)
    }

    private fun setupAssessmentRecyclerView(assessments: List<MergedAssessment>) {
        if (assessmentAdapter == null) {
            assessmentAdapter = AssessmentAdapter(requireActivity(),assessments) { assessment ->
                Log.e("TAG", "setupAssedwdddssmentRecyclerView:  $assessment.", )
                showAssessmentSheet(assessment)

                // Handle assessment item click
            }
            binding.dsvAssessments.adapter = assessmentAdapter
            binding.dsvAssessments.setItemTransformer(
                ScaleTransformer.Builder()
                    .setMaxScale(1.05f)
                    .setMinScale(0.8f)
                    .setPivotX(Pivot.X.CENTER)
                    .setPivotY(Pivot.Y.CENTER)
                    .build()
            )
        } else {
            assessmentAdapter?.updateData(assessments)
        }
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
