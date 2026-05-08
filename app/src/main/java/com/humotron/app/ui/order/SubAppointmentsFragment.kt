package com.humotron.app.ui.order

import android.os.Bundle
import android.view.View
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentSubAppointmentsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SubAppointmentsFragment : BaseFragment(R.layout.fragment_sub_appointments) {
    private lateinit var binding: FragmentSubAppointmentsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSubAppointmentsBinding.bind(view)
    }
}
