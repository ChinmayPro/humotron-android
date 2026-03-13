package com.humotron.app.ui.onboarding.personalize.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.humotron.app.ui.onboarding.personalize.CompleteFragment
import com.humotron.app.ui.onboarding.personalize.PersonalInfoFragment
import com.humotron.app.ui.onboarding.personalize.UseCaseFragment
import com.humotron.app.ui.onboarding.personalize.WidthHeightFragment

class PersonalizePagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {
    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> WidthHeightFragment()
            2 -> UseCaseFragment()
            3 -> CompleteFragment()
            else -> PersonalInfoFragment()
        }

    }
}