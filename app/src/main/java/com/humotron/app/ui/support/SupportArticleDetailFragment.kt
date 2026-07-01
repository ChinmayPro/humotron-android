package com.humotron.app.ui.support

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentSupportArticleDetailBinding
import com.humotron.app.databinding.ItemSupportRelatedArticleBinding
import com.humotron.app.databinding.ItemSupportStepBinding
import com.humotron.app.domain.modal.response.SearchTopicItem
import com.humotron.app.util.getTimeAgo
import com.humotron.app.util.loadImage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SupportArticleDetailFragment : BaseFragment(R.layout.fragment_support_article_detail) {

    private lateinit var binding: FragmentSupportArticleDetailBinding
    private val viewModel: SupportViewModel by activityViewModels()

    private var article: SearchTopicItem? = null
    private var categoryKey: String = ""
    private var categoryLabel: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSupportArticleDetailBinding.bind(view)

        article = arguments?.getParcelable("article")
        categoryKey = article?.categoryKey ?: ""
        categoryLabel = article?.categoryLabel ?: ""

        val originalPaddingLeft = binding.contentContainer.paddingLeft
        val originalPaddingTop = binding.contentContainer.paddingTop
        val originalPaddingRight = binding.contentContainer.paddingRight

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.contentContainer.setPadding(
                originalPaddingLeft,
                originalPaddingTop,
                originalPaddingRight,
                systemBars.bottom
            )
            insets
        }

        initViews()
        initObservers()

        val topicId = article?.id ?: article?.topicId
        if (!topicId.isNullOrEmpty()) {
            viewModel.fetchTopicDetail(topicId)
        }
    }

    private fun initViews() {
        binding.header.title.text = getString(R.string.support)
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }

        article?.let { item ->
            binding.tvArticleTitle.text = item.title ?: ""

            val subcategory = item.subcategoryLabel ?: ""
            val category = item.categoryLabel ?: ""
            val metaText = if (subcategory.isNotEmpty() && category.isNotEmpty()) {
                "$category • $subcategory"
            } else {
                subcategory.ifEmpty { category }
            }
            binding.tvArticleMeta.text = metaText

            binding.tvShortAnswerContent.text = item.shortAnswer ?: item.subtitle ?: ""
            
            binding.tvArticleUpdated.text = getString(R.string.support_updated_just_now)
        }

        binding.llViewMoreArticles.setOnClickListener {
            val bundle = Bundle().apply {
                putString("categoryKey", categoryKey)
                putString("categoryLabel", categoryLabel)
            }
            findNavController().navigate(
                R.id.action_fragmentSupportArticleDetail_to_fragmentSupportViewAllArticles,
                bundle
            )
        }

        binding.btnContactSupport.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentSupportArticleDetail_to_fragmentContactSupport)
        }
    }

    private fun initObservers() {
        viewModel.topicDetailData.observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    showProgress()
                    binding.contentContainer.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    hideProgress()
                    binding.contentContainer.visibility = View.VISIBLE
                    val response = resource.data
                    if (response?.status == "success" && response.data != null) {
                        val detailData = response.data
                        detailData.topic?.let { topic ->
                            binding.tvArticleTitle.text = topic.title ?: ""

                            val subcategoryLabel = detailData.subcategory?.label ?: topic.subcategoryLabel ?: ""
                            val categoryVal = detailData.category?.label ?: topic.categoryLabel ?: ""
                            categoryLabel = categoryVal
                            categoryKey = detailData.category?.key ?: topic.categoryKey ?: ""

                            val metaText = if (categoryVal.isNotEmpty() && subcategoryLabel.isNotEmpty()) {
                                "$categoryVal • $subcategoryLabel"
                            } else {
                                subcategoryLabel.ifEmpty { categoryVal }
                            }
                            binding.tvArticleMeta.text = metaText

                            val categoryIconUrl = detailData.category?.icon
                            if (!categoryIconUrl.isNullOrEmpty()) {
                                binding.ivCategoryIcon.visibility = View.VISIBLE
                                binding.ivCategoryIcon.imageTintList = null
                                binding.ivCategoryIcon.loadImage(categoryIconUrl)
                            } else {
                                binding.ivCategoryIcon.visibility = View.GONE
                            }

                            // Relative updatedAt time
                            val parsedTime = try {
                                java.time.Instant.parse(topic.updatedAt).toEpochMilli()
                            } catch (e: Exception) {
                                0L
                            }
                            val timeAgo = if (parsedTime > 0L) {
                                getTimeAgo(parsedTime)
                            } else {
                                null
                            }
                            binding.tvArticleUpdated.text = if (timeAgo != null && timeAgo != "just now") {
                                getString(R.string.support_updated_format, timeAgo)
                            } else {
                                getString(R.string.support_updated_just_now)
                            }

                            binding.tvShortAnswerContent.text = topic.shortAnswer ?: topic.subtitle ?: ""

                            binding.llStepsContainer.removeAllViews()
                            val steps = topic.steps
                            if (!steps.isNullOrEmpty()) {
                                binding.llStepsHeader.visibility = View.VISIBLE
                                binding.llStepsContainer.visibility = View.VISIBLE
                                steps.forEachIndexed { index, stepText ->
                                    val stepBinding = ItemSupportStepBinding.inflate(
                                        LayoutInflater.from(requireContext()),
                                        binding.llStepsContainer,
                                        false
                                    )
                                    stepBinding.tvStepText.text = stepText
                                    stepBinding.vStepDivider.visibility = if (index == steps.size - 1) View.GONE else View.VISIBLE
                                    binding.llStepsContainer.addView(stepBinding.root)
                                }
                            } else {
                                binding.llStepsHeader.visibility = View.GONE
                                binding.llStepsContainer.visibility = View.GONE
                            }

                            binding.llRelatedContainer.removeAllViews()
                            val relatedTopics = detailData.relatedTopics
                            val hasRelated = !relatedTopics.isNullOrEmpty()
                            binding.llViewMoreArticles.visibility = if (hasRelated) View.VISIBLE else View.GONE
                            if (hasRelated) {
                                binding.llRelatedHeader.visibility = View.VISIBLE
                                binding.llRelatedContainer.visibility = View.VISIBLE
                                relatedTopics!!.forEachIndexed { index, relatedArticle ->
                                    val relatedBinding = ItemSupportRelatedArticleBinding.inflate(
                                        LayoutInflater.from(requireContext()),
                                        binding.llRelatedContainer,
                                        false
                                    )
                                    relatedBinding.tvArticleTitle.text = relatedArticle.title ?: ""
                                    
                                    val relatedCategory = relatedArticle.categoryLabel ?: detailData.category?.label ?: ""
                                    relatedBinding.tvArticleCategory.text = relatedCategory

                                    val typeIcon = when (relatedArticle.articleType?.lowercase()) {
                                        "how_to" -> R.drawable.ic_menu_book_24px
                                        "troubleshooting" -> R.drawable.ic_signal_waves
                                        "faq" -> R.drawable.ic_help_outline_24px
                                        "policy" -> R.drawable.ic_sheet_document
                                        else -> R.drawable.ic_sheet_document
                                    }
                                    relatedBinding.ivArticleIcon.setImageResource(typeIcon)

                                    relatedBinding.vArticleDivider.visibility = if (index == relatedTopics.size - 1) View.GONE else View.VISIBLE

                                    relatedBinding.root.setOnClickListener {
                                        val bundle = Bundle().apply {
                                            putParcelable("article", relatedArticle)
                                        }
                                        findNavController().navigate(R.id.fragmentSupportArticleDetail, bundle)
                                    }

                                    binding.llRelatedContainer.addView(relatedBinding.root)
                                }
                            } else {
                                binding.llRelatedHeader.visibility = View.GONE
                                binding.llRelatedContainer.visibility = View.GONE
                            }
                        }
                    } else {
                        val msg = response?.message ?: getString(R.string.support_failed_get_details)
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
                Status.ERROR -> {
                    hideProgress()
                    binding.contentContainer.visibility = View.VISIBLE
                    val errorMsg = resource.error?.errorMessage ?: getString(R.string.error_occurred)
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                }
                Status.EXCEPTION -> {
                    hideProgress()
                    binding.contentContainer.visibility = View.VISIBLE
                    val exceptionMsg = resource.error?.errorMessage ?: getString(R.string.support_exception_occurred)
                    Toast.makeText(requireContext(), exceptionMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
