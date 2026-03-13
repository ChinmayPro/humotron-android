package com.humotron.app.ui.bioHack

import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.databinding.FragmentCategoryTagBinding
import com.humotron.app.domain.modal.param.CreateNuggetPrefParam
import com.humotron.app.domain.modal.param.SelectedTag
import com.humotron.app.ui.bioHack.adapter.CategoryVpAdapter
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryTagFragment : BaseFragment(R.layout.fragment_category_tag) {


    private lateinit var binding: FragmentCategoryTagBinding
    private lateinit var adapter: CategoryVpAdapter
    private val viewModel: NuggetsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCategoryTagBinding.bind(view)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom + 60)
            insets
        }
        adapter = CategoryVpAdapter { selected, count ->
            binding.btnNext.isEnabled = selected > 0
            binding.tvSelected.text = getString(R.string.selected_d_d, selected, count)
        }
        binding.rvCategoryTags.adapter = adapter
        binding.btnNext.isEnabled = false

        viewModel.selectedTags.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.setData(it)
            }
        }

        binding.btnNext.setOnClickListener {
            val currentPosition = binding.rvCategoryTags.currentItem
            if (currentPosition < adapter.itemCount - 1) {
                binding.rvCategoryTags.setCurrentItem(currentPosition + 1, true)
            } else {
                val primaryTags = adapter.list.flatMap { it.primaryTag!! }.filter { it.isChecked }
                    .mapNotNull { it.id }
                val selectedTags = adapter.list.filter { it.isChecked }.mapNotNull { it.id }

                val list = arrayListOf<SelectedTag>()
                list.add(SelectedTag(selectedTags, "CATEGORY", "CONTEXT TAG"))
                list.add(SelectedTag(primaryTags, "PRIMARY", "CONTEXT TAG"))
                viewModel.createPreference(CreateNuggetPrefParam(list))

                // Navigate to the next screen or perform the final action
                findNavController().navigate(R.id.fragmentPreferenceDone)
            }
        }

        binding.btnPrevious.setOnClickListener {
            val currentPosition = binding.rvCategoryTags.currentItem
            if (currentPosition > 0) {
                binding.rvCategoryTags.setCurrentItem(currentPosition - 1, true)
            } else {
                // Handle the case when already at the first item
                findNavController().popBackStack()
            }
        }


        binding.rvCategoryTags.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val currentTag = adapter.list.getOrNull(position)
                val selectedSize = currentTag?.primaryTag?.filter { it.isChecked }?.size
                binding.textview1.text =
                    getString(
                        R.string.what_areas_of_s_do_you_want_to_focus_on,
                        currentTag?.tagName ?: ""
                    )
                binding.tvSelected.text = getString(
                    R.string.selected_d_d,
                    selectedSize,
                    currentTag?.primaryTag?.size
                )
                binding.btnNext.isEnabled = (selectedSize ?: 0) > 0
            }
        })


    }


}