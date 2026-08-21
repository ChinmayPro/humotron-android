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
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    fun fetchData() {
        viewModel.fetchBloodTestOrders()
    }

    private fun initRecyclerView() {
        binding.rvAppointments.adapter = appointmentAdapter
        appointmentAdapter.onCancelClick = { appointment ->
            val cancelId = appointment.bookingId ?: appointment.id ?: appointment.orderId
            if (!cancelId.isNullOrEmpty()) {
                showCancelAppointmentDialog(cancelId)
            }
        }
    }

    private fun showCancelAppointmentDialog(bookingId: String) {
        com.humotron.app.ui.dialogs.CancelConfirmationBottomSheet.newInstance(
            title = getString(R.string.cancel_appointment),
            subtitle = getString(R.string.cancel_appointment_confirmation),
            keepButtonText = getString(R.string.keep_appointment),
            cancelButtonText = getString(R.string.cancel_appointment),
            onCancel = { reason ->
                viewModel.cancelBloodTestBooking(bookingId, reason)
            }
        ).show(childFragmentManager, com.humotron.app.ui.dialogs.CancelConfirmationBottomSheet.TAG)
    }

    private fun setupObservers() {
        viewModel.getBloodTestOrdersLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (appointmentAdapter.itemCount == 0) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvMainLabel.visibility = View.GONE
                        binding.tvSubLabel.visibility = View.GONE
                        binding.rvAppointments.visibility = View.GONE
                    }
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    val response = resource.data
                    if (response?.status == "fail") {
                        binding.tvMainLabel.visibility = View.VISIBLE
                        binding.tvSubLabel.visibility = View.VISIBLE
                        binding.rvAppointments.visibility = View.GONE
                        val errorMsg = if (!response.message.isNullOrEmpty()) response.message else getString(R.string.something_went_wrong)
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    } else {
                        val orderList = response?.orderList
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
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    val errorMsg = getErrorMessage(resource.error)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                    
                    binding.tvMainLabel.visibility = View.VISIBLE
                    binding.tvSubLabel.visibility = View.VISIBLE
                    binding.rvAppointments.visibility = View.GONE
                }
            }
        }

        viewModel.getCancelOrderLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    val response = resource.data
                    if (response?.status == "fail" || response?.status == "error") {
                        val errorMsg = if (!response.message.isNullOrEmpty()) response.message else getString(R.string.something_went_wrong)
                        com.humotron.app.util.DialogUtils.showInfoAlertDialog(requireContext(), message = errorMsg)
                    } else {
                        val msg = response?.message ?: getString(R.string.appointment_cancelled_successfully)
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                        fetchData()
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val errorMsg = getErrorMessage(resource.error)
                    com.humotron.app.util.DialogUtils.showInfoAlertDialog(requireContext(), message = errorMsg)
                }
            }
        }
    }

    private fun getErrorMessage(error: com.humotron.app.data.network.error.Error?): String {
        if (error == null) return getString(R.string.something_went_wrong)

        if (!error.errorMessage.isNullOrEmpty()) return error.errorMessage

        val rawError = error.error
        if (!rawError.isNullOrEmpty()) {
            return try {
                val json = org.json.JSONObject(rawError)
                when {
                    json.has("message") -> json.getString("message")
                    json.has("error") -> json.getString("error")
                    else -> rawError
                }
            } catch (e: Exception) {
                rawError
            }
        }

        return getString(R.string.something_went_wrong)
    }
}
