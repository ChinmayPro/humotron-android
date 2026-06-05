package com.humotron.app.ui.support

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSupportSearchBinding
import com.humotron.app.domain.modal.response.SearchTopicItem
import com.humotron.app.ui.support.adapter.FilterType
import com.humotron.app.ui.support.adapter.SearchFilterChip
import com.humotron.app.ui.support.adapter.SupportSearchArticleAdapter
import com.humotron.app.ui.support.adapter.SupportSearchFilterAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportSearchFragment : BaseFragment(R.layout.fragment_support_search) {

    private lateinit var binding: FragmentSupportSearchBinding
    private val viewModel: SupportViewModel by activityViewModels()

    private var allArticles = listOf<SearchTopicItem>()
    private var currentQuery: String = ""
    private var subcategoryIcons = mapOf<String, String>()
    private var categoryIcons = mapOf<String, String>()

    private var selectedFilterChip: SearchFilterChip? = null

    private val filterAdapter by lazy {
        SupportSearchFilterAdapter { selectedChip ->
            selectedFilterChip = selectedChip
            applyFilter(selectedChip)
        }
    }

    private val articleAdapter by lazy {
        SupportSearchArticleAdapter { article ->
            val bundle = Bundle().apply {
                putParcelable("article", article)
            }
            findNavController().navigate(
                R.id.action_fragmentSupportSearch_to_fragmentSupportArticleDetail,
                bundle
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportSearchBinding.bind(view)

        val originalPaddingLeft = binding.contentContainer.paddingLeft
        val originalPaddingTop = binding.contentContainer.paddingTop
        val originalPaddingRight = binding.contentContainer.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            if (!imeVisible && binding.etSearchHelp.hasFocus()) {
                binding.etSearchHelp.clearFocus()
            }
            binding.contentContainer.setPadding(
                originalPaddingLeft,
                originalPaddingTop,
                originalPaddingRight,
                systemBars.bottom
            )
            insets
        }

        val hideKeyboardAndClearFocus = {
            binding.etSearchHelp.clearFocus()
            val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(binding.etSearchHelp.windowToken, 0)
        }

        view.setOnClickListener { hideKeyboardAndClearFocus() }
        binding.contentContainer.setOnClickListener { hideKeyboardAndClearFocus() }
        binding.nsvContent.setOnClickListener { hideKeyboardAndClearFocus() }

        binding.etSearchHelp.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboardAndClearFocus()
                true
            } else {
                false
            }
        }

        // Setup filter recycler view
        binding.rvFilters.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvFilters.adapter = filterAdapter

        // Setup articles recycler view
        binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArticles.adapter = articleAdapter

        // Footer "Load more" click triggers next page
        articleAdapter.setOnLoadMoreClickListener {
            viewModel.loadMoreSearchTopics()
        }

        initViews()
        initObservers()
        
        val searchQuery = arguments?.getString("searchQuery") ?: ""
        if (searchQuery.isNotEmpty()) {
            binding.etSearchHelp.setText(searchQuery)
            binding.etSearchHelp.setSelection(searchQuery.length)
        }
    }

    private fun initViews() {
        // Set standard header title and back listener
        binding.header.title.text = getString(R.string.search)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Clear search text listener
        binding.ivClearSearch.setOnClickListener {
            binding.etSearchHelp.text?.clear()
        }

        // Setup Search EditText text watcher to toggle clear button
        binding.etSearchHelp.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s?.toString()?.trim() ?: ""
                binding.ivClearSearch.visibility = if (query.isEmpty()) View.GONE else View.VISIBLE
                viewModel.searchTopics(query)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun initObservers() {
        viewModel.searchTopicsData.observe(viewLifecycleOwner) { resource ->
            val queryText = binding.etSearchHelp.text?.toString()?.trim() ?: ""
            when (resource.status) {
                Status.LOADING -> {
                    binding.pbSearchLoading.visibility = View.VISIBLE
                    binding.ivClearSearch.visibility = View.GONE
                    // Show footer loading state if we have existing articles (pagination loading)
                    if (allArticles.isNotEmpty()) {
                        articleAdapter.setFooterState(
                            show = true,
                            loading = true,
                            firstPage = viewModel.getCurrentPage() == 1,
                            total = viewModel.getTotalTopicsCount(),
                            loaded = allArticles.size
                        )
                    }
                }
                Status.SUCCESS -> {
                    binding.pbSearchLoading.visibility = View.GONE
                    binding.ivClearSearch.visibility = if (queryText.isEmpty()) View.GONE else View.VISIBLE
                    
                    val searchResponse = resource.data
                    if (searchResponse?.status == "success" && searchResponse.data != null) {
                        val responseData = searchResponse.data
                        allArticles = responseData.topics ?: emptyList()
                        
                        currentQuery = queryText
                        
                        if (queryText.length > 1) {
                            showSearchResultsState(responseData, queryText)
                        } else {
                            showInitialState()
                        }
                    } else {
                        showInitialState()
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.pbSearchLoading.visibility = View.GONE
                    binding.ivClearSearch.visibility = if (queryText.isEmpty()) View.GONE else View.VISIBLE
                    // Hide footer loading on error
                    articleAdapter.setFooterState(
                        show = viewModel.hasMorePages(),
                        loading = false,
                        firstPage = viewModel.getCurrentPage() == 1,
                        total = viewModel.getTotalTopicsCount(),
                        loaded = allArticles.size
                    )
                    showInitialState()
                }
            }
        }
    }

    private fun showInitialState() {
        selectedFilterChip = null
        binding.tvSearchSubtitle.visibility = View.VISIBLE
        binding.tvResultCount.visibility = View.GONE
        binding.rvFilters.visibility = View.GONE
        binding.tvTopArticlesHeader.visibility = View.GONE
        binding.rvArticles.visibility = View.GONE
        articleAdapter.setFooterState(show = false)
    }

    private fun showSearchResultsState(data: com.humotron.app.domain.modal.response.SearchTopicsData, query: String) {
        binding.tvSearchSubtitle.visibility = View.GONE

        // Extract subcategory and category icon maps
        val subcatMap = subcategoryIcons.toMutableMap()
        data.subcategories?.forEach { sub ->
            val key = sub.subcategoryKey
            val icon = sub.subcategoryIcon
            if (!key.isNullOrEmpty() && !icon.isNullOrEmpty()) {
                subcatMap[key] = icon
            }
        }
        subcategoryIcons = subcatMap

        val catMap = categoryIcons.toMutableMap()
        data.categories?.forEach { cat ->
            val key = cat.categoryKey
            val icon = cat.categoryIcon
            if (!key.isNullOrEmpty() && !icon.isNullOrEmpty()) {
                catMap[key] = icon
            }
        }
        categoryIcons = catMap

        val page = data.pagination?.page ?: 1
        if (page == 1) {
            // Update result count text
            val totalCount = data.counts?.topics ?: allArticles.size
            val countText = if (totalCount == 1) {
                "1 result for \"$query\""
            } else {
                "$totalCount results for \"$query\""
            }
            binding.tvResultCount.text = countText
            binding.tvResultCount.visibility = View.VISIBLE

            // Populate filters list
            val filterChips = mutableListOf<SearchFilterChip>()
            val allChip = SearchFilterChip("All", FilterType.ALL, "all", isSelected = true)
            filterChips.add(allChip)
            selectedFilterChip = allChip

            // Categories
            data.categories?.forEach { category ->
                val count = category.count ?: 0
                if (count > 0 && !category.categoryLabel.isNullOrEmpty() && !category.categoryKey.isNullOrEmpty()) {
                    filterChips.add(
                        SearchFilterChip(
                            label = "${category.categoryLabel} ($count)",
                            type = FilterType.CATEGORY,
                            key = category.categoryKey
                        )
                    )
                }
            }

            // Subcategories
            data.subcategories?.forEach { subcategory ->
                val count = subcategory.count ?: 0
                if (count > 0 && !subcategory.subcategoryLabel.isNullOrEmpty() && !subcategory.subcategoryKey.isNullOrEmpty()) {
                    filterChips.add(
                        SearchFilterChip(
                            label = "${subcategory.subcategoryLabel} ($count)",
                            type = FilterType.SUBCATEGORY,
                            key = subcategory.subcategoryKey
                        )
                    )
                }
            }

            filterAdapter.setData(filterChips)
            binding.rvFilters.visibility = if (filterChips.size > 1) View.VISIBLE else View.GONE
        }

        // Apply selected filter to the full articles list (allArticles)
        val activeFilter = selectedFilterChip ?: SearchFilterChip("All", FilterType.ALL, "all", isSelected = true)
        applyFilter(activeFilter)
    }

    private fun applyFilter(chip: SearchFilterChip) {
        val filteredList = when (chip.type) {
            FilterType.ALL -> allArticles
            FilterType.CATEGORY -> allArticles.filter { it.categoryKey.equals(chip.key, ignoreCase = true) }
            FilterType.SUBCATEGORY -> allArticles.filter { it.subcategoryKey.equals(chip.key, ignoreCase = true) }
        }
        articleAdapter.setData(filteredList, currentQuery, subcategoryIcons, categoryIcons)

        // Show "Load more" footer only when there are more pages and filter is "All"
        val showFooter = viewModel.hasMorePages() && chip.type == FilterType.ALL
        articleAdapter.setFooterState(
            show = showFooter,
            loading = viewModel.isLoadingPage(),
            firstPage = viewModel.getCurrentPage() == 1,
            total = viewModel.getTotalTopicsCount(),
            loaded = allArticles.size
        )

        binding.tvTopArticlesHeader.visibility = if (filteredList.isNotEmpty()) View.VISIBLE else View.GONE
        binding.rvArticles.visibility = if (filteredList.isNotEmpty()) View.VISIBLE else View.GONE
    }
}
