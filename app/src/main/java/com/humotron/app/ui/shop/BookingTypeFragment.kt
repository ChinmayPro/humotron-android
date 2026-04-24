package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBookingTypeBinding
import com.humotron.app.ui.shop.adapter.BookingTypeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BookingTypeFragment : BaseFragment(R.layout.fragment_booking_type) {

    private lateinit var binding: FragmentBookingTypeBinding
    private val viewModel: ShopViewModel by activityViewModels()
    private lateinit var adapter: BookingTypeAdapter
    private var selectedType: com.humotron.app.domain.modal.response.BookingTypeResponse.BookingType? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBookingTypeBinding.bind(view)

        setupInsets()
        initViews()
        initObservers()
        viewModel.fetchBookingTypes()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            binding.btnContinue.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + dpToPx(24)
            }
            
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

        adapter = BookingTypeAdapter { selectedItem ->
            selectedType = selectedItem
            binding.btnContinue.isEnabled = selectedItem != null
        }
        binding.rvBookingTypes.adapter = adapter

        binding.btnContinue.setOnClickListener {
            viewModel.setSelectedBookingType(selectedType)
            findNavController().navigate(R.id.action_fragmentBookingType_to_fragmentSelectAddress)
        }
    }

    private fun initObservers() {
        viewModel.getBookingTypeLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideLoader()
                    binding.clContent.visibility = View.VISIBLE
                    val data = resource.data?.data
                    if (data != null) {
                        adapter.setData(data)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideLoader()
                    binding.clContent.visibility = View.GONE
                }
                Status.LOADING -> {
                    showLoader()
                    binding.clContent.visibility = View.GONE
                }
            }
        }
    }

    private fun showLoader() {
        binding.layoutLoader.root.visibility = View.VISIBLE
        binding.layoutLoader.lottieLoader.playAnimation()
    }

    private fun hideLoader() {
        binding.layoutLoader.root.visibility = View.GONE
        binding.layoutLoader.lottieLoader.cancelAnimation()
    }
}
