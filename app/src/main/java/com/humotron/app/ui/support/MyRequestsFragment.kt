package com.humotron.app.ui.support

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentMyRequestsBinding
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.ui.support.adapter.SupportTicketAdapter
import com.humotron.app.domain.modal.response.TicketDetail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyRequestsFragment : BaseFragment(R.layout.fragment_my_requests) {

    private lateinit var binding: FragmentMyRequestsBinding
    private val viewModel: SupportViewModel by activityViewModels()
    private lateinit var ticketAdapter: SupportTicketAdapter
    private var allTicketsList = emptyList<TicketDetail>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentMyRequestsBinding.bind(view)

        // Clear top padding, but apply bottom system navigation bar inset as padding to prevent button overlaps
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        binding.header.title.text = getString(R.string.support_my_requests)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnContactSupport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentMyRequests_to_fragmentContactSupport)
        }
        binding.btnEmptyContactSupport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentMyRequests_to_fragmentContactSupport)
        }

        ticketAdapter = SupportTicketAdapter { ticket ->
            if (ticket.currentStatus.equals("draft", ignoreCase = true)) {
                val bundle = Bundle().apply {
                    putParcelable("draftTicket", ticket)
                }
                findNavController().navigate(R.id.action_fragmentMyRequests_to_fragmentContactSupport, bundle)
            } else {
                val bundle = Bundle().apply {
                    putParcelable("ticket", ticket)
                }
                findNavController().navigate(R.id.action_fragmentMyRequests_to_fragmentSupportRequestDetails, bundle)
            }
        }
        binding.rvTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTickets.adapter = ticketAdapter

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab) {
                filterAndDisplayTickets()
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab) {}
        })

        initObservers()
        viewModel.fetchMyTickets()
    }

    private fun initObservers() {
        viewModel.myTicketsData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    val response = resource.data
                    if (response?.status == "success" && response.data != null) {
                        allTicketsList = response.data.tickets ?: emptyList()

                        val openCount = allTicketsList.count { 
                            !it.currentStatus.equals("draft", ignoreCase = true) && 
                            it.status.equals("open", ignoreCase = true) 
                        }
                        val waitingCount = allTicketsList.count { 
                            !it.currentStatus.equals("draft", ignoreCase = true) && 
                            (it.status.equals("waiting", ignoreCase = true) || it.status.equals("waiting_for_user", ignoreCase = true))
                        }
                        val resolvedCount = allTicketsList.count { 
                            !it.currentStatus.equals("draft", ignoreCase = true) && 
                            it.status.equals("resolved", ignoreCase = true)
                        }
                        val draftCount = allTicketsList.count { 
                            it.currentStatus.equals("draft", ignoreCase = true) 
                        }

                        updateTabs(openCount, waitingCount, resolvedCount, draftCount)
                        filterAndDisplayTickets()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                }
                Status.EXCEPTION -> {
                    hideProgress()
                }
            }
        }
    }

    private fun filterAndDisplayTickets() {
        val selectedTabPosition = binding.tabLayout.selectedTabPosition
        val filteredTickets = when (selectedTabPosition) {
            0 -> allTicketsList.filter { !it.currentStatus.equals("draft", ignoreCase = true) && it.status.equals("open", ignoreCase = true) }
            1 -> allTicketsList.filter { !it.currentStatus.equals("draft", ignoreCase = true) && (it.status.equals("waiting", ignoreCase = true) || it.status.equals("waiting_for_user", ignoreCase = true)) }
            2 -> allTicketsList.filter { !it.currentStatus.equals("draft", ignoreCase = true) && it.status.equals("resolved", ignoreCase = true) }
            3 -> allTicketsList.filter { it.currentStatus.equals("draft", ignoreCase = true) }
            else -> emptyList()
        }
        ticketAdapter.setData(filteredTickets)

        val hasTickets = filteredTickets.isNotEmpty()
        binding.llResponseNote.visibility = if (selectedTabPosition == 0 && hasTickets) View.VISIBLE else View.GONE
        binding.llWaitingNote.visibility = if (selectedTabPosition == 1 && hasTickets) View.VISIBLE else View.GONE
        binding.llResolvedNote.visibility = if (selectedTabPosition == 2 && hasTickets) View.VISIBLE else View.GONE
        binding.llDraftNote.visibility = if (selectedTabPosition == 3 && hasTickets) View.VISIBLE else View.GONE

        val isEmpty = filteredTickets.isEmpty()
        if (isEmpty) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.scrollRoot.visibility = View.GONE
            when (selectedTabPosition) {
                0 -> {
                    binding.tvEmptyTitle.text = getString(R.string.support_empty_open_title)
                    binding.tvEmptyDesc.text = getString(R.string.support_empty_open_desc)
                }
                1 -> {
                    binding.tvEmptyTitle.text = getString(R.string.support_empty_waiting_title)
                    binding.tvEmptyDesc.text = getString(R.string.support_empty_waiting_desc)
                }
                2 -> {
                    binding.tvEmptyTitle.text = getString(R.string.support_empty_resolved_title)
                    binding.tvEmptyDesc.text = getString(R.string.support_empty_resolved_desc)
                }
                3 -> {
                    binding.tvEmptyTitle.text = getString(R.string.support_empty_drafts_title)
                    binding.tvEmptyDesc.text = getString(R.string.support_empty_drafts_desc)
                }
            }
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.scrollRoot.visibility = View.VISIBLE
        }
    }

    private fun updateTabs(openCount: Int, waitingCount: Int, resolvedCount: Int, draftCount: Int) {
        binding.tabLayout.getTabAt(0)?.text = getTabTitle(R.string.support_my_requests_tab_open, openCount)
        binding.tabLayout.getTabAt(1)?.text = getTabTitle(R.string.support_my_requests_tab_waiting, waitingCount)
        binding.tabLayout.getTabAt(2)?.text = getTabTitle(R.string.support_my_requests_tab_resolved, resolvedCount)
        binding.tabLayout.getTabAt(3)?.text = getTabTitle(R.string.support_my_requests_tab_drafts, draftCount)
    }

    private fun getTabTitle(titleResId: Int, count: Int): String {
        val title = getString(titleResId)
        return if (count > 0) "$title ($count)" else title
    }
}
