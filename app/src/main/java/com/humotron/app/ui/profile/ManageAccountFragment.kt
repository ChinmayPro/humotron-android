package com.humotron.app.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentManageAccountBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageAccountFragment : BaseFragment(R.layout.fragment_manage_account) {

    private lateinit var binding: FragmentManageAccountBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentManageAccountBinding.bind(view)
        initViews()
    }

    private fun initViews() {
        val user = prefUtils.getLoginResponse()

        // Set user info
        binding.tvName.text = "${user.firstName} ${user.lastName}"
        
        // Initial letter (Hide if profile image is present)
        val hasProfileImage = !user.profileImages.isNullOrBlank()
        binding.tvAvatarInitials.text = user.firstName?.firstOrNull()?.toString()?.uppercase() ?: "C"
        binding.tvAvatarInitials.visibility = if (hasProfileImage) View.GONE else View.VISIBLE

        Glide.with(this)
            .load(user.profileImages)
            .placeholder(R.drawable.ic_bg_trans) // Use transparent placeholder so initials show through
            .into(binding.ivAvatarBg)

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
