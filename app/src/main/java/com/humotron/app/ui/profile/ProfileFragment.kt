package com.humotron.app.ui.profile

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
import com.bumptech.glide.Glide
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private var isExpanded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)
        initViews()
    }

    private fun initViews() {
        val user = prefUtils.getLoginResponse()

        binding.tvName.text = "${user.firstName} ${user.lastName}"
        binding.tvBadge.text = getString(R.string.profile_plan_format, getString(R.string.basic))
//        binding.tvPlanBadge.text = getString(R.string.basic)
//        binding.tvAddOnsStatus.text = getString(R.string.activated_count, 2)

        Glide.with(this)
            .load(user.profileImages)
            .placeholder(R.drawable.ic_profile)
            .into(binding.ivProfile)

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
            // TODO: Navigate to Legal screen
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
            Toast.makeText(context, "Delete Account clicked", Toast.LENGTH_SHORT).show()
        }

        binding.clLogout.setOnClickListener {
            (activity as? com.humotron.app.ui.MainActivity)?.showLogoutDialog()
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
