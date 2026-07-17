package com.humotron.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.R
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.local.AppDatabase
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentManageAccountBinding
import com.humotron.app.ui.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ManageAccountFragment : BaseFragment(R.layout.fragment_manage_account) {

    private lateinit var binding: FragmentManageAccountBinding
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var database: AppDatabase

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentManageAccountBinding.bind(view)
        initViews()
    }

    private fun initViews() {
        val user = prefUtils.getLoginResponse()

        // Set user info with robust fallback logic
        val fullName = if (!user.name.isNullOrBlank() && user.name != "null") {
            user.name.trim()
        } else {
            val f = user.firstName ?: ""
            val l = user.lastName ?: ""
            "$f $l".trim()
        }

        val displayName = if (fullName.isEmpty()) "User" else fullName
        binding.tvName.text = displayName

        val initial = if (displayName.isNotEmpty()) {
            displayName.split(" ").firstOrNull()?.firstOrNull()?.toString()?.uppercase() ?: "U"
        } else {
            "U"
        }
        binding.tvAvatarInitials.text = initial

        binding.ivAvatarBg.visibility = View.GONE
        binding.tvAvatarInitials.visibility = View.VISIBLE

        // Set data (Fallback defaults per design)
        binding.tvGender.text = user.gender?.takeIf { it.isNotBlank() } ?: "Male"
        binding.tvBirthdate.text = user.birthDate?.takeIf { it.isNotBlank() } ?: "24 Aug 1984"
        binding.tvEmail.text = user.email?.takeIf { it.isNotBlank() } ?: "chinmay@humotron.com"
        
        // Height, Weight, Country typically come from extended user profile or defaults
        binding.tvHeight.text = user.height?.takeIf { it.isNotBlank() } ?: "180 cm"
        binding.tvWeight.text = user.weight?.takeIf { it.isNotBlank() } ?: "74 kg"
        binding.tvCountry.text = "United Kingdom"
        binding.tvPassword.text = "• • • • • •"

        // Set Header Title
        binding.layoutHeader.title.text = "Personal information"

        // Click listeners
        binding.layoutHeader.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditDetails.setOnClickListener {
            Toast.makeText(requireContext(), "Edit details clicked", Toast.LENGTH_SHORT).show()
        }
    }
}
