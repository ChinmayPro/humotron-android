package com.humotron.app.ui.order

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentOrderBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OrderFragment : BaseFragment(R.layout.fragment_order) {

    private lateinit var binding: FragmentOrderBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentOrderBinding.bind(view)
        initViews()
    }

    private fun initViews() {
        val adapter = OrderPagerAdapter(this)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // Disable swiping if needed, or keep enabled

        binding.tabOrder.setOnClickListener {
            binding.viewPager.currentItem = 0
            updateTabUI(0)
        }
        binding.tabAppointments.setOnClickListener {
            binding.viewPager.currentItem = 1
            updateTabUI(1)
        }
        binding.tabFavourite.setOnClickListener {
            binding.viewPager.currentItem = 2
            updateTabUI(2)
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabUI(position)
            }
        })

        // Initial state
        updateTabUI(0)
    }

    private fun updateTabUI(position: Int) {
        val inactiveColor = resources.getColor(R.color.white30, null)
        val activeColor = resources.getColor(R.color.lime_green, null)

        val tabs = listOf(
            Pair(binding.ivTabOrder, binding.tvTabOrder),
            Pair(binding.ivTabAppointments, binding.tvTabAppointments),
            Pair(binding.ivTabFavourite, binding.tvTabFavourite)
        )

        tabs.forEachIndexed { index, (icon, text) ->
            val color = if (index == position) activeColor else inactiveColor
            icon.imageTintList = android.content.res.ColorStateList.valueOf(color)
            text.setTextColor(color)
            
            // Update the main header title based on selection
            if (index == position) {
                binding.tvTitle.text = text.text
            }
        }
    }

    private inner class OrderPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> SubOrderFragment()
                1 -> SubAppointmentsFragment()
                2 -> SubFavouriteFragment()
                else -> SubOrderFragment()
            }
        }
    }
}
