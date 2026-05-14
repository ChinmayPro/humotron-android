package com.humotron.app.ui.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Resource
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSubOrderBinding
import com.humotron.app.ui.order.adapter.OrderAdapter
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class SubOrderFragment : BaseFragment(R.layout.fragment_sub_order) {
    private lateinit var binding: FragmentSubOrderBinding
    private val viewModel: OrderViewModel by viewModels()
    private val orderAdapter by lazy { OrderAdapter() }
    private var isFirstPage = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubOrderBinding.bind(view)
        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    fun fetchData() {
        isFirstPage = true
        viewModel.fetchOrderList(isFirstPage = true)
    }

    private fun setupRecyclerView() {
        binding.rvOrders.apply {
            adapter = orderAdapter
            orderAdapter.onItemClick = { order ->
                val bundle = Bundle().apply {
                    putParcelable("order", order)
                }
                findNavController().navigate(R.id.action_fragmentOrder_to_fragmentOrderDetail, bundle)
            }
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount && firstVisibleItemPosition >= 0) {
                        viewModel.fetchOrderList()
                    }
                }
            })
        }
    }

    private fun setupObservers() {
        viewModel.getOrderListLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (isFirstPage && orderAdapter.itemCount == 0) {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.tvMainLabel.visibility = View.GONE
                        binding.tvSubLabel.visibility = View.GONE
                        binding.rvOrders.visibility = View.GONE
                    }
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    val orders = resource.data?.orderList ?: emptyList()
                    if (isFirstPage) {
                        if (orders.isEmpty()) {
                            binding.tvMainLabel.visibility = View.VISIBLE
                            binding.tvSubLabel.visibility = View.VISIBLE
                            binding.rvOrders.visibility = View.GONE
                        } else {
                            binding.tvMainLabel.visibility = View.GONE
                            binding.tvSubLabel.visibility = View.GONE
                            binding.rvOrders.visibility = View.VISIBLE
                            orderAdapter.setOrders(orders, true)
                        }
                        isFirstPage = false
                    } else {
                        orderAdapter.setOrders(orders, false)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    // Optionally show error message
                }
            }
        }
    }
}
