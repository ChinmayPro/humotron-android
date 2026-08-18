package com.humotron.app.ui.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentEditCategoryPreferenceBinding
import com.humotron.app.databinding.ItemEditCategorySubtagBinding
import com.humotron.app.domain.modal.param.CreateNuggetPrefParam
import com.humotron.app.domain.modal.param.SelectedTag
import com.humotron.app.domain.modal.response.PrimaryTagLevel
import com.humotron.app.domain.modal.response.Tag
import com.humotron.app.ui.bioHack.viewModel.NuggetsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditCategoryPreferencesFragment : BaseFragment(R.layout.fragment_edit_category_preference) {

    private lateinit var binding: FragmentEditCategoryPreferenceBinding
    private val viewModel: NuggetsViewModel by activityViewModels()

    private var targetCategory: Tag? = null
    private var allCategories: ArrayList<Tag> = arrayListOf()
    private var categoryIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            targetCategory = it.getParcelable(ARG_TARGET_CATEGORY)
            allCategories = it.getParcelableArrayList(ARG_ALL_CATEGORIES) ?: arrayListOf()
            categoryIndex = it.getInt(ARG_CATEGORY_INDEX, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEditCategoryPreferenceBinding.bind(view)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.root.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        setupHeader()
        setupCategoryData()
        observeViewModel()
    }

    private fun setupHeader() {
        binding.header.title.text = "Master Biohacking"
        binding.header.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupCategoryData() {
        val category = targetCategory ?: return
        val catName = category.tagName ?: "Category"

        binding.tvStepLabel.text = "STEP ${categoryIndex + 1}"
        binding.tvQuestionTitle.text = "What areas of $catName do you want to focus on?"

        val primaryTags = category.primaryTag ?: emptyList()
        // Initialize checked state from isSelected if not set
        primaryTags.forEach { tag ->
            if (!tag.isChecked) {
                tag.isChecked = (tag.isSelected == true)
            }
        }

        updateSelectedCount(primaryTags)

        val adapter = SubTagAdapter(primaryTags) {
            updateSelectedCount(primaryTags)
        }
        binding.rvSubCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSubCategories.adapter = adapter

        binding.btnSave.setOnClickListener {
            savePreferences(primaryTags)
        }
    }

    private fun updateSelectedCount(primaryTags: List<PrimaryTagLevel>) {
        val selectedCount = primaryTags.count { it.isChecked }
        val totalCount = primaryTags.size
        binding.tvSelectedCount.text = "Selected $selectedCount of $totalCount (min 1)"
        binding.btnSave.isEnabled = selectedCount > 0
    }

    private fun savePreferences(currentCategoryPrimaryTags: List<PrimaryTagLevel>) {
        showProgress()

        val activeCategoryIds = mutableSetOf<String>()
        val activePrimaryTagIds = mutableSetOf<String>()

        allCategories.forEach { cat ->
            val catId = cat.id ?: return@forEach
            val tags = if (catId == targetCategory?.id) {
                currentCategoryPrimaryTags
            } else {
                cat.primaryTag.orEmpty()
            }

            val checkedPrimary = tags.filter { it.isChecked || (catId != targetCategory?.id && it.isSelected == true) }
            if (checkedPrimary.isNotEmpty()) {
                activeCategoryIds.add(catId)
                checkedPrimary.mapNotNull { it.id }.forEach { activePrimaryTagIds.add(it) }
            } else if (catId != targetCategory?.id && cat.isSelected == true) {
                activeCategoryIds.add(catId)
            }
        }

        targetCategory?.id?.let { targetId ->
            val checkedCurrent = currentCategoryPrimaryTags.filter { it.isChecked }
            if (checkedCurrent.isNotEmpty()) {
                activeCategoryIds.add(targetId)
                checkedCurrent.mapNotNull { it.id }.forEach { activePrimaryTagIds.add(it) }
            } else {
                activeCategoryIds.remove(targetId)
                currentCategoryPrimaryTags.mapNotNull { it.id }.forEach { activePrimaryTagIds.remove(it) }
            }
        }

        val paramList = arrayListOf<SelectedTag>()
        paramList.add(SelectedTag(activeCategoryIds.toList(), "CATEGORY", "CONTEXT TAG"))
        paramList.add(SelectedTag(activePrimaryTagIds.toList(), "PRIMARY", "CONTEXT TAG"))

        viewModel.createPreference(CreateNuggetPrefParam(paramList))
    }

    private fun observeViewModel() {
        viewModel.createPreferenceData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    viewModel.getUserPreferences()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    Toast.makeText(requireContext(), resource.error?.errorMessage ?: "Failed to update preferences", Toast.LENGTH_SHORT).show()
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }

        viewModel.getUserPreferencesData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    hideProgress()
                    Toast.makeText(requireContext(), "Preferences updated successfully!", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    findNavController().navigateUp()
                }
                Status.LOADING -> {
                    showProgress()
                }
            }
        }
    }

    inner class SubTagAdapter(
        private val list: List<PrimaryTagLevel>,
        private val onItemChecked: () -> Unit
    ) : RecyclerView.Adapter<SubTagAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemEditCategorySubtagBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = list[position]
            val catTitle = targetCategory?.tagName ?: ""
            val iconRes = getIconResForCategory(catTitle)

            holder.binding.apply {
                tvSubCategoryTitle.text = item.tagName
                ivIcon.setImageResource(iconRes)

                val limeColor = Color.parseColor("#C4F23E")
                val selectedIconBg = Color.parseColor("#24C4F23E")
                val unselectedIconBg = Color.parseColor("#182623")

                if (item.isChecked) {
                    llContainer.setBackgroundResource(R.drawable.bg_subcat_card_selected)
                    tvSubCategoryTitle.setTextColor(Color.WHITE)
                    llIconBox.backgroundTintList = ColorStateList.valueOf(selectedIconBg)
                    ivIcon.imageTintList = ColorStateList.valueOf(limeColor)
                    ivCheckIndicator.setImageResource(R.drawable.ic_check_custom_selected)
                } else {
                    llContainer.setBackgroundResource(R.drawable.bg_subcat_card_unselected)
                    tvSubCategoryTitle.setTextColor(Color.parseColor("#D0DFDD"))
                    llIconBox.backgroundTintList = ColorStateList.valueOf(unselectedIconBg)
                    ivIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#5E7571"))
                    ivCheckIndicator.setImageResource(R.drawable.ic_check_custom_unselected)
                }

                root.setOnClickListener {
                    item.isChecked = !item.isChecked
                    notifyItemChanged(position)
                    onItemChecked()
                }
            }
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(val binding: ItemEditCategorySubtagBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    private fun getIconResForCategory(categoryName: String): Int {
        return when {
            categoryName.contains("Metabolic", ignoreCase = true) -> R.drawable.ic_spark
            categoryName.contains("Brain", ignoreCase = true) || categoryName.contains("Cognitive", ignoreCase = true) -> R.drawable.ic_target
            categoryName.contains("Environmental", ignoreCase = true) || categoryName.contains("Lifestyle", ignoreCase = true) -> R.drawable.ic_onboard_moon
            else -> R.drawable.ic_spark
        }
    }

    companion object {
        const val ARG_TARGET_CATEGORY = "arg_target_category"
        const val ARG_ALL_CATEGORIES = "arg_all_categories"
        const val ARG_CATEGORY_INDEX = "arg_category_index"
    }
}
