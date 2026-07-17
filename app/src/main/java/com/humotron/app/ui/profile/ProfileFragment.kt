package com.humotron.app.ui.profile

import android.content.Intent
import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.Log
import android.view.View
import android.widget.Toast
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.humotron.app.R
import com.humotron.app.core.Preference
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.local.AppDatabase
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentProfileBinding
import com.humotron.app.ui.onboarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: ProfileViewModel by viewModels()

    @Inject
    lateinit var database: AppDatabase

    private var isExpanded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        initViews()
        initObservers()
    }

    private fun initViews() {
        val user = prefUtils.getLoginResponse()

        val fullName = if (!user.name.isNullOrBlank() && user.name != "null") {
            user.name.trim()
        } else {
            val f = user.firstName ?: ""
            val l = user.lastName ?: ""
            "$f $l".trim()
        }

        val displayName = if (fullName.isEmpty()) "User" else fullName
        binding.tvName.text = displayName
        binding.tvBadge.text = getString(R.string.profile_plan_format, getString(R.string.basic))

        val initial = if (displayName.isNotEmpty()) {
            displayName.split(" ").firstOrNull()?.firstOrNull()?.toString()?.uppercase() ?: "U"
        } else {
            "U"
        }
        binding.tvAvatarInitial.text = initial

        binding.ivProfile.visibility = View.GONE
        binding.tvAvatarInitial.visibility = View.VISIBLE

        binding.ivArrowDown.setOnClickListener {
            toggleExpansion()
        }



        binding.btnInsights.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentInsights)
        }

        binding.clActivePlan.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("selectedTabId", R.id.fragmentShopTools)
            }
            findNavController().navigate(R.id.action_fragmentProfile_to_nav_graph_shop, bundle)
        }

        binding.tvManageAccount.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentManageAccount)
        }

        binding.clShop.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("selectedTabId", R.id.fragmentShopTools)
            }
            findNavController().navigate(R.id.action_fragmentProfile_to_nav_graph_shop, bundle)
        }

        binding.clOrder.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentOrder)
        }

        binding.clCart.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentCart)
        }


        binding.btnDevices.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentDeviceList)
        }

        binding.clPrivacy.setOnClickListener {
            // TODO: Navigate to Privacy screen
        }

        binding.clSupport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentSupport)
        }

        

        binding.clLegal.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentLegalCentre)
        }

        
        binding.clRecipes.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentRecipes)
        }

        binding.clDigitalTools.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("selectedTabId", R.id.fragmentShopTools)
            }
            findNavController().navigate(R.id.action_fragmentProfile_to_nav_graph_shop, bundle)
        }

        binding.btnDataSources.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentDataSources)
        }

        binding.btnGoals.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentGoals)
        }



        binding.btnChatWithAI.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentAiChat)
        }

        binding.btnRecipeGenerator.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentRecipeGenerator)
        }

        binding.btnLearning.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentLearningPreferences)
        }

        binding.btnShopping.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentShopping)
        }

        binding.clDeleteAccount.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete account")
                .setMessage("Are you sure you want to delete your account? This action is permanent and cannot be undone.")
                .setPositiveButton("Delete") { dialog, _ ->
                    dialog.dismiss()
                    val userId = user.id
                    if (!userId.isNullOrBlank()) {
                        viewModel.deleteUserById(userId)
                    } else {
                        Toast.makeText(requireContext(), "User ID not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.clLogout.setOnClickListener {
            (activity as? com.humotron.app.ui.MainActivity)?.showLogoutDialog()
        }
    }

    private fun initObservers() {
        viewModel.getDeleteUserLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                }
                Status.SUCCESS -> {
                    hideProgress()
                    Toast.makeText(requireContext(), "Account deleted successfully", Toast.LENGTH_LONG).show()
                    prefUtils.clear()
                    prefUtils.setBoolean(Preference.ONBOARD_PRIVACY, true)

                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            database.clearAllTables()
                        }
                        val intent = Intent(requireActivity(), OnBoardingActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    val errorMsg = resource.error?.errorMessage ?: "Failed to delete account. Please try again."
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun toggleExpansion() {
        isExpanded = !isExpanded
        val rotation = if (isExpanded) 180f else 0f
        val animDuration = 400L

        binding.ivArrowDown.animate()
            .rotation(rotation)
            .setDuration(animDuration)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        val transition = TransitionSet().apply {
            addTransition(ChangeBounds())
            addTransition(Fade())
            duration = animDuration
            interpolator = AccelerateDecelerateInterpolator()
        }

        TransitionManager.beginDelayedTransition(binding.root as android.view.ViewGroup, transition)
        binding.clExpandedContent.visibility = if (isExpanded) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
