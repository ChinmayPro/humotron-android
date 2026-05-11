package com.humotron.app.ui.order

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSubAppointmentsBinding
import com.humotron.app.ui.order.adapter.AppointmentAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubAppointmentsFragment : BaseFragment(R.layout.fragment_sub_appointments) {
    private lateinit var binding: FragmentSubAppointmentsBinding
    private val viewModel: OrderViewModel by viewModels()
    private val appointmentAdapter by lazy { AppointmentAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubAppointmentsBinding.bind(view)
        initRecyclerView()
        setupObservers()
        
        viewModel.fetchBloodTestOrders()
    }

    private fun initRecyclerView() {
        binding.rvAppointments.adapter = appointmentAdapter
    }

    private fun setupObservers() {
        viewModel.getBloodTestOrdersLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.tvMainLabel.visibility = View.GONE
                    binding.tvSubLabel.visibility = View.GONE
                    binding.rvAppointments.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    val orderList = resource.data?.orderList
                    if (orderList.isNullOrEmpty()) {
                        binding.tvMainLabel.visibility = View.VISIBLE
                        binding.tvSubLabel.visibility = View.VISIBLE
                        binding.rvAppointments.visibility = View.GONE
                    } else {
                        binding.tvMainLabel.visibility = View.GONE
                        binding.tvSubLabel.visibility = View.GONE
                        binding.rvAppointments.visibility = View.VISIBLE
                        appointmentAdapter.updateData(orderList)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.something_went_wrong)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    
                    binding.tvMainLabel.visibility = View.VISIBLE
                    binding.tvSubLabel.visibility = View.VISIBLE
                    binding.rvAppointments.visibility = View.GONE
                }
            }
        }
    }
}
