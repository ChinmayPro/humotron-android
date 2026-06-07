package com.humotron.app.ui.support

import android.os.Bundle
import android.view.View
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSupportArticlesBinding
import com.humotron.app.ui.support.adapter.SupportAllArticlesAdapter
import com.humotron.app.domain.modal.response.SearchTopicItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportArticlesFragment : BaseFragment(R.layout.fragment_support_articles) {

    private lateinit var binding: FragmentSupportArticlesBinding
    private val viewModel: SupportViewModel by activityViewModels()
    private var categoryKey: String = "sync_connectivity"

    private val articlesAdapter by lazy {
        SupportAllArticlesAdapter { article ->
            val bundle = Bundle().apply {
                putParcelable("article", article)
            }
            findNavController().navigate(
                R.id.action_fragmentSupportArticles_to_fragmentSupportArticleDetail,
                bundle
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportArticlesBinding.bind(view)

        categoryKey = arguments?.getString("categoryKey") ?: "sync_connectivity"

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        initObservers()

        if (viewModel.allTopicsData.value == null) {
            viewModel.fetchAllTopics(isInitialLoad = true)
        }
    }

    private fun initViews() {
        binding.header.title.text = getString(R.string.support_all_articles)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.rvArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.rvArticles.adapter = articlesAdapter

        // NestedScrollView Pagination Scroll Listener
        binding.scrollRoot.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val child = v.getChildAt(0)
            if (child != null) {
                val childHeight = child.measuredHeight
                val viewHeight = v.measuredHeight
                if (scrollY >= (childHeight - viewHeight - 50)) {
                    android.util.Log.d("PaginationTest", "Reached bottom! hasMore: ${viewModel.hasMoreAllTopics()}, isLoading: ${viewModel.isAllTopicsLoadingPage()}")
                    if (viewModel.hasMoreAllTopics() && !viewModel.isAllTopicsLoadingPage()) {
                        binding.progressBar.visibility = View.VISIBLE
                        viewModel.fetchAllTopics(isInitialLoad = false)
                    }
                }
            }
        })
    }

    private fun initObservers() {
        viewModel.allTopicsData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    if (articlesAdapter.itemCount == 0) {
                        showProgress()
                    }
                }
                Status.SUCCESS -> {
                    hideProgress()
                    binding.progressBar.visibility = View.GONE
                    val response = resource.data
                    if (response?.status == "success" && response.data != null) {
                        val topics = response.data.topics ?: emptyList()
                        val mappedTopics = mapTopicsWithLabels(topics)
                        articlesAdapter.setData(mappedTopics)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    hideProgress()
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun mapTopicsWithLabels(topics: List<SearchTopicItem>): List<SearchTopicItem> {
        val categories = viewModel.supportHomeData.value?.data?.data?.categories ?: return topics
        return topics.map { topic ->
            var categoryLabel = topic.categoryLabel
            var subcategoryLabel = topic.subcategoryLabel

            if (categoryLabel.isNullOrEmpty() && !topic.categoryKey.isNullOrEmpty()) {
                val category = categories.find { it.key.equals(topic.categoryKey, ignoreCase = true) }
                categoryLabel = category?.label
                
                if (subcategoryLabel.isNullOrEmpty() && !topic.subcategoryKey.isNullOrEmpty() && category != null) {
                    val subcategory = category.subcategories?.find { it.key.equals(topic.subcategoryKey, ignoreCase = true) }
                    subcategoryLabel = subcategory?.label
                }
            }
            
            topic.copy(
                categoryLabel = categoryLabel,
                subcategoryLabel = subcategoryLabel
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRemoving || activity?.isFinishing == true) {
            viewModel.clearAllTopicsData()
        }
    }
}
