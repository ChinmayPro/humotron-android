package com.humotron.app.ui.shop

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.FragmentShopOptimizeDetailBinding
import com.humotron.app.domain.modal.response.ProductDetailResponse
import com.humotron.app.domain.modal.response.RecipeItem
import com.humotron.app.domain.modal.response.SupplementItem
import com.humotron.app.ui.shop.dialog.ProductFaqBottomSheet
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShopOptimizeDetailFragment : BaseFragment(R.layout.fragment_shop_optimize_detail) {

    private lateinit var binding: FragmentShopOptimizeDetailBinding
    private val viewModel: ShopViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShopOptimizeDetailBinding.bind(view)

        val supplement = arguments?.getParcelable<SupplementItem>("supplement")
        val recipe = arguments?.getParcelable<RecipeItem>("recipe")
        val productId = supplement?.productId ?: recipe?.recipeBundleId

        initViews()
        initObservers()

        productId?.let {
            viewModel.fetchProductDetail(it)
        } ?: run {
            binding.tvNoData.visibility = View.VISIBLE
            binding.nsvContent.visibility = View.GONE
        }
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(binding.clBottom) { v, insets ->
            val systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft,
                v.paddingTop,
                v.paddingRight,
                systemBars.bottom + (20 * resources.displayMetrics.density).toInt()
            )
            insets
        }

        // Expandable Sections Logic
        binding.clUsageHeader.setOnClickListener { toggleSection(binding.tvUsageContent, binding.ivUsageArrow) }
        binding.clIngredientsHeader.setOnClickListener { toggleSection(binding.tvIngredientsContent, binding.ivIngredientsArrow) }
        binding.clHowItWorksHeader.setOnClickListener { toggleSection(binding.tvHowItWorksContent, binding.ivHowItWorksArrow) }
    }

    private fun toggleSection(content: View, arrow: View) {
        if (content.visibility == View.VISIBLE) {
            content.visibility = View.GONE
        } else {
            content.visibility = View.VISIBLE
        }
    }

    private fun initObservers() {
        viewModel.getProductDetailLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    handleLoader(false)
                    resource.data?.data?.product?.let {
                        bindProductData(it)
                        binding.nsvContent.visibility = View.VISIBLE
                        binding.clBottom.visibility = View.VISIBLE
                        binding.tvNoData.visibility = View.GONE
                    } ?: run {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.nsvContent.visibility = View.GONE
                        binding.clBottom.visibility = View.GONE
                    }
                }
                Status.ERROR -> {
                    handleLoader(false)
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.nsvContent.visibility = View.GONE
                    binding.clBottom.visibility = View.GONE
                }
                Status.LOADING -> {
                    handleLoader(true)
                    binding.nsvContent.visibility = View.GONE
                    binding.clBottom.visibility = View.GONE
                    binding.tvNoData.visibility = View.GONE
                }
                else -> {}
            }
        }
    }

    private fun bindProductData(product: ProductDetailResponse.Product) {
        binding.tvProductName.text = product.productName
        binding.tvPrice.text = "£ ${product.productPrice}"
        binding.tvBrand.text = product.brandName
        binding.tvPackSize.text = "Pack Size: ${product.packSize} / ${product.courseLength}"
        binding.tvDescription.text = product.productDesc
        
        // Daily Servings and Supply Duration cards
        binding.tvDailyServingsValue.text = product.productDossage
        binding.tvSupplyDurationValue.text = product.supplyDuration

        // Course Length and When to Take cards
        binding.tvCourseLengthValue.text = product.courseLength
        binding.tvWhenToTakeValue.text = product.whenToTakeOneWord

        // Usage Instructions
        val usage = "• When to Take: ${product.whenToTakeSentence}\n\n• How to Take: ${product.howToTake}"
        binding.tvUsageContent.text = usage

        // Key Ingredients
        val ingredients = product.keyIngredients?.joinToString("\n\n") { "• ${it.name}: ${it.description}" }
        binding.tvIngredientsContent.text = ingredients

        // How It Works
        binding.tvHowItWorksContent.text = product.howToWork

        // FAQs
        val faqs = product.productFaqs?.joinToString("\n\n") { "Q: ${it.question}\nA: ${it.answer}" }
        binding.tvFaqContent.text = faqs

        // Why Product?
        bindWhyProduct(product.whyProduct)

        // Chat Prompts
        bindChatPrompts(product.chatPrompts)

        // FAQ BottomSheet Setup
        binding.clFaqHeader.setOnClickListener {
            if (!product.productFaqs.isNullOrEmpty()) {
                ProductFaqBottomSheet.newInstance(product.productName ?: "", product.productFaqs)
                    .show(childFragmentManager, "ProductFaq")
            }
        }

        Glide.with(this)
            .load(product.productImage)
            .into(binding.ivProduct)

        if (product.isLiked == true) {
            binding.btnLike.setImageResource(R.drawable.ic_fav_selected)
            binding.btnLike.imageTintList = null
            binding.btnLike.alpha = 1.0f
        } else {
            binding.btnLike.setImageResource(R.drawable.ic_fav_checkbox)
            binding.btnLike.imageTintList = android.content.res.ColorStateList.valueOf(resources.getColor(R.color.white))
            binding.btnLike.alpha = 0.6f
        }
    }

    private fun bindWhyProduct(whyProduct: ProductDetailResponse.WhyProduct?) {
        if (whyProduct == null || whyProduct.list.isNullOrEmpty()) {
            binding.tvWhyProductHeader.visibility = View.GONE
            binding.rvWhyProduct.visibility = View.GONE
            return
        }

        binding.tvWhyProductHeader.visibility = View.VISIBLE
        binding.rvWhyProduct.visibility = View.VISIBLE
        binding.rvWhyProduct.adapter = WhyProductAdapter(whyProduct.list)
    }

    private fun bindChatPrompts(prompts: List<ProductDetailResponse.ChatPrompt>?) {
        if (prompts.isNullOrEmpty()) {
            binding.rvChatPrompts.visibility = View.GONE
            return
        }

        binding.rvChatPrompts.visibility = View.VISIBLE
        binding.rvChatPrompts.adapter = ChatPromptAdapter(prompts)
    }

    private fun handleLoader(isVisible: Boolean) {
        binding.layoutLoader.root.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    // Adapters for better performance
    inner class WhyProductAdapter(private val items: List<ProductDetailResponse.WhyProductItem>) :
        RecyclerView.Adapter<WhyProductAdapter.WhyViewHolder>() {

        inner class WhyViewHolder(val binding: com.humotron.app.databinding.ItemWhyProductBinding) : 
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhyViewHolder {
            return WhyViewHolder(
                com.humotron.app.databinding.ItemWhyProductBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: WhyViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvTitle.text = item.short
            holder.binding.tvDescription.text = item.long
        }

        override fun getItemCount(): Int = items.size
    }

    inner class ChatPromptAdapter(private val items: List<ProductDetailResponse.ChatPrompt>) :
        RecyclerView.Adapter<ChatPromptAdapter.PromptViewHolder>() {

        inner class PromptViewHolder(val binding: com.humotron.app.databinding.ItemChatPromptBinding) : 
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromptViewHolder {
            return PromptViewHolder(
                com.humotron.app.databinding.ItemChatPromptBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: PromptViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvPrompt.text = item.title
            
            holder.itemView.setOnClickListener {
                // Handle prompt click if needed
            }
        }

        override fun getItemCount(): Int = items.size
    }
}
