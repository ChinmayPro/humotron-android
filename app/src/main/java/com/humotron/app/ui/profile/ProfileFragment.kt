package com.humotron.app.ui.profile

import android.os.Bundle
import android.transition.ChangeBounds
import android.transition.Fade
import android.transition.TransitionManager
import android.transition.TransitionSet
import android.util.Log
import android.view.View
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

        binding.tvName.text =  "${user.firstName} ${user.lastName}"
        binding.tvBadge.text = getString(R.string.basic)
        binding.tvPlanBadge.text = getString(R.string.basic)
        binding.tvAddOnsStatus.text = getString(R.string.activated_count, 2)

        Glide.with(this)
            .load(user.profileImages)
            .placeholder(R.drawable.ic_profile)
            .into(binding.ivProfile)

        binding.llExpandGroup.setOnClickListener {
            toggleExpansion()
        }

        binding.clActivePlan.setOnClickListener {
            // Toast.makeText(context, "Active Plan clicked", Toast.LENGTH_SHORT).show()
        }

        binding.clAddOns.setOnClickListener {
            // Toast.makeText(context, "Add-ons clicked", Toast.LENGTH_SHORT).show()
        }

        binding.tvManageAccount.setOnClickListener {
            // Toast.makeText(context, "Manage Account clicked", Toast.LENGTH_SHORT).show()
        }

        binding.clShop.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_nav_graph_shop)
        }

        binding.clCart.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentProfile_to_fragmentCart)
        }

        binding.clPrivacy.setOnClickListener {
            // TODO: Navigate to Privacy screen
        }

        binding.clSupport.setOnClickListener {
            // TODO: Navigate to Support screen
        }

        binding.clFaq.setOnClickListener {
            // TODO: Navigate to FAQ screen
        }

        binding.clLegal.setOnClickListener {
            // TODO: Navigate to Legal screen
        }

        binding.clLogout.setOnClickListener {
            (activity as? com.humotron.app.ui.MainActivity)?.showLogoutDialog()
        }
    }




    private fun toggleExpansion() {
        isExpanded = !isExpanded
        val rotation = if (isExpanded) 180f else 0f
        val animDuration = 400L

        binding.llExpandGroup.animate()
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
