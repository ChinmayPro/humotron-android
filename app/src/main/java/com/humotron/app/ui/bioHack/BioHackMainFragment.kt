package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentBioHackMainBinding
import com.humotron.app.ui.MainActivity
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BioHackMainFragment : BaseFragment(R.layout.fragment_bio_hack_main) {
    private val viewModel: NuggetsViewModel by activityViewModels()
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var binding: FragmentBioHackMainBinding
    val showNavList = listOf(
        R.id.fragmentSetup,
        R.id.fragmentQuestion,
        R.id.fragmentNuggets,
        R.id.fragmentCategoryTag,
        R.id.fragmentPreferenceDone,
        R.id.fragmentNuggetDetail,
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBioHackMainBinding.bind(view)


        navHostFragment = childFragmentManager
            .findFragmentById(R.id.nav_host_fragment_bio_hack) as NavHostFragment
        val navController = navHostFragment.navController
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (showNavList.contains(destination.id)) {
                (activity as? MainActivity)?.showOrHideBottomNav(true)
            } else {
                (activity as? MainActivity)?.showOrHideBottomNav(false)
            }
        }

        viewModel.getNuggetsPreference()

        subscribeToObservers()
    }


    private fun subscribeToObservers() {
        viewModel.nuggetsPreferenceData().observe(viewLifecycleOwner) {
            Log.e("TAG", "subscribeToObservers:BioHack $it")
            when (it.status) {
                Status.SUCCESS -> {
                    // Handle success
                    hideProgress()
                    it.data?.nuggetsData?.let { data ->
                        // Update UI with the data
                        viewModel.nuggetsPreference.value = data.nugget
                        setStartDestination(data.hasUserSetPreferences == true)
                    }
                }

                Status.ERROR -> {
                    // Handle error
                    hideProgress()
                }

                Status.LOADING -> {
                    showProgress()
                }

                Status.EXCEPTION -> {
                    hideProgress()
                }
            }
        }
    }

    private fun setStartDestination(prefSet: Boolean) {
        val navController = navHostFragment.navController
        val navInflater = navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_bio_hack)
        navGraph.setStartDestination(
            if (prefSet) {
                R.id.fragmentNuggets
            } else {
                R.id.fragmentSetup
            }
        )
        navController.graph = navGraph
    }


}