package com.humotron.app.ui.decode

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDecodeOptimizeDetailBinding
import com.humotron.app.domain.modal.response.GetOptimizedRecommendationDetailResponse
import com.humotron.app.domain.modal.response.RecommendationDetailData
import com.humotron.app.domain.modal.response.RecipeDetailItem
import com.humotron.app.ui.shop.ShopViewModel
import androidx.core.text.HtmlCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DecodeOptimizeDetailFragment : BaseFragment(R.layout.fragment_decode_optimize_detail) {

    private lateinit var binding: FragmentDecodeOptimizeDetailBinding
    private val viewModel: ShopViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeOptimizeDetailBinding.bind(view)

        val productId = arguments?.getString("productId")
        val productType = arguments?.getString("productType")

        initViews()
        initObservers()

        if (productId != null && productType != null) {
            viewModel.fetchRecommendationDetail(productId, productType)
        } else {
            binding.tvNoData.visibility = View.VISIBLE
            binding.nsvContent.visibility = View.GONE
        }
    }

    private fun initViews() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun initObservers() {
        viewModel.getRecommendationDetailLiveData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> {
                    handleLoader(false)
                    resource.data?.data?.let {
                        bindRecommendationData(it)
                        binding.nsvContent.visibility = View.VISIBLE
                        binding.llCtaBar.visibility = View.VISIBLE
                        binding.vBottomGradient.visibility = View.VISIBLE
                        binding.tvNoData.visibility = View.GONE
                    } ?: run {
                        binding.tvNoData.visibility = View.VISIBLE
                        binding.nsvContent.visibility = View.GONE
                        binding.llCtaBar.visibility = View.GONE
                        binding.vBottomGradient.visibility = View.GONE
                    }
                }
                Status.ERROR -> {
                    handleLoader(false)
                    binding.tvNoData.visibility = View.VISIBLE
                    binding.nsvContent.visibility = View.GONE
                    binding.llCtaBar.visibility = View.GONE
                    binding.vBottomGradient.visibility = View.GONE
                }
                Status.LOADING -> {
                    handleLoader(true)
                    binding.nsvContent.visibility = View.GONE
                    binding.llCtaBar.visibility = View.GONE
                    binding.vBottomGradient.visibility = View.GONE
                    binding.tvNoData.visibility = View.GONE
                }
                Status.EXCEPTION -> {
                    handleLoader(false)
                }
                else -> {}
            }
        }
    }

    private fun handleLoader(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun bindRecommendationData(data: RecommendationDetailData) {
        val titleText = data.title ?: data.productName ?: data.bundleName ?: ""
        binding.tvHeaderTitle.text = titleText
        binding.tvTitle.text = titleText

        binding.tvEyebrow.text = data.category ?: data.type ?: "RECOMMENDATION"
        binding.tvPrice.text = if (data.price != null && data.price > 0) "£" + String.format("%.0f", data.price) else "Free"
        binding.tvSubtitle.text = data.short ?: data.shortDescription ?: data.bundleShortDescription ?: ""

        // Config "Why you're seeing this" box
        binding.tvMatchPercentage.text = if (data.attributes?.matchConfidence != null) {
            val confidence = data.attributes.matchConfidence.trim()
            val pct = when (confidence.lowercase()) {
                "high" -> "91% match"
                "moderate" -> "84% match"
                else -> "75% match"
            }
            pct
        } else {
            "91% match"
        }

        // Bind Why signal details from fragment arguments
        val metricName = arguments?.getString("metricName")
        val metricDelta = arguments?.getString("metricDelta")
        val metricStatus = arguments?.getString("metricStatus")

        if (!metricName.isNullOrEmpty() && !metricDelta.isNullOrEmpty()) {
            binding.llWhyMetricRow.visibility = View.VISIBLE
            binding.tvWhyMetricName.text = metricName
            binding.tvWhyMetricDelta.text = metricDelta
            binding.tvWhyRecoveryTarget.text = "your recovery gap"
            
            val statusColor = when (metricStatus) {
                "attention" -> {
                    binding.vWhyStatusDot.setBackgroundResource(R.drawable.bg_red_indicator)
                    Color.parseColor("#EE4D3D")
                }
                "watch" -> {
                    binding.vWhyStatusDot.setBackgroundResource(R.drawable.bg_amber_indicator)
                    Color.parseColor("#E7A93C")
                }
                else -> {
                    binding.vWhyStatusDot.setBackgroundResource(R.drawable.bg_round_green)
                    Color.parseColor("#C4F23E")
                }
            }
            binding.tvWhyMetricDelta.setTextColor(statusColor)
            binding.flCardContainer.setBackgroundColor(Color.parseColor("#0DC4F23E"))
            binding.vWhyLeftBorder.setBackgroundColor(Color.parseColor("#C4F23E"))
        } else {
            // Goal based fallback
            binding.llWhyMetricRow.visibility = View.VISIBLE
            binding.vWhyStatusDot.setBackgroundResource(R.drawable.bg_amber_indicator)
            binding.tvWhyMetricName.text = "Your goal"
            binding.tvWhyMetricDelta.text = "skin care"
            binding.tvWhyMetricDelta.setTextColor(Color.parseColor("#E7A93C"))
            binding.tvWhyRecoveryTarget.text = "visible ageing"
            binding.flCardContainer.setBackgroundColor(Color.parseColor("#0DC4F23E"))
            binding.vWhyLeftBorder.setBackgroundColor(Color.parseColor("#C4F23E"))
        }

        // Why you're seeing this expandable details
        binding.tvDetailWhyThisContent.text = data.attributes?.whyThis ?: ""
        binding.tvDetailWhyYouContent.text = data.attributes?.benefits?.firstOrNull() ?: ""
        binding.tvDetailWhyNowContent.text = data.attributes?.trackStatement ?: ""

        binding.clWhyExpandHint.setOnClickListener {
            val isVisible = binding.llWhyDetails.visibility == View.VISIBLE
            binding.llWhyDetails.visibility = if (isVisible) View.GONE else View.VISIBLE
            binding.ivWhyArrow.rotation = if (isVisible) 0f else 90f
        }

        // Hide standard optional cards
        binding.cardWhatItDoes.visibility = View.GONE
        binding.cardHowToTakeIt.visibility = View.GONE
        binding.cardIngredients.visibility = View.GONE
        binding.cardEvidence.visibility = View.GONE
        binding.tvEvidenceBadge.visibility = View.GONE
        binding.cardSafety.visibility = View.GONE
        binding.cardStacks.visibility = View.GONE
        binding.cardTrack.visibility = View.GONE
        binding.cardFaqs.visibility = View.GONE
        binding.tvFaqsHeaderOutside.visibility = View.GONE
        binding.tvRecipesHeader.visibility = View.GONE
        binding.rvRecipes.visibility = View.GONE
        binding.flexAllergenChips.visibility = View.GONE

        if (data.type == "supplement") {
            // WHAT IT DOES
            data.attributes?.typeSpecific?.primaryMechanism?.let { primary ->
                binding.cardWhatItDoes.visibility = View.VISIBLE
                binding.tvWhatItDoesContent.text = HtmlCompat.fromHtml(
                    primary + "\n\n" + (data.attributes.typeSpecific.mechanismDetail ?: ""),
                    HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }

            // HOW TO TAKE IT
            val usageList = data.attributes?.usage
            if (!usageList.isNullOrEmpty()) {
                binding.cardHowToTakeIt.visibility = View.VISIBLE
                val tableLayout = binding.llUsageTable
                tableLayout.removeAllViews()

                for ((index, usageItem) in usageList.withIndex()) {
                    if (index > 0) {
                        val divider = View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                            )
                            setBackgroundColor(Color.parseColor("#12FFFFFF"))
                        }
                        tableLayout.addView(divider)
                    }

                    val parts = usageItem.split(":", limit = 2)
                    if (parts.size == 2) {
                        val row = LinearLayout(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            val paddingPx = (10 * resources.displayMetrics.density).toInt()
                            setPadding(0, paddingPx, 0, paddingPx)
                            orientation = LinearLayout.HORIZONTAL
                        }
                        val keyTv = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            text = parts[0].trim()
                            setTextColor(Color.parseColor("#8A9594"))
                            setTextAppearance(R.style.Text_12x_Manrope_Medium)
                        }
                        val valTv = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                            text = parts[1].trim()
                            if (parts[0].trim().equals("Dose", ignoreCase = true)) {
                                setTextColor(Color.WHITE)
                            } else {
                                setTextColor(Color.parseColor("#9DACA9"))
                            }
                            setTextAppearance(R.style.Text_12x_Manrope_Bold)
                            gravity = android.view.Gravity.END
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                        }
                        row.addView(keyTv)
                        row.addView(valTv)
                        tableLayout.addView(row)
                    } else {
                        val row = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            val paddingPx = (10 * resources.displayMetrics.density).toInt()
                            setPadding(0, paddingPx, 0, paddingPx)
                            text = usageItem
                            setTextColor(Color.WHITE)
                            setTextAppearance(R.style.Text_12x_Manrope_Medium)
                        }
                        tableLayout.addView(row)
                    }
                }

                // Why form note
                val formNote = data.attributes?.typeSpecific?.whyThisForm
                if (!formNote.isNullOrEmpty()) {
                    binding.llFormNote.visibility = View.VISIBLE
                    binding.tvFormNoteText.text = formNote
                } else {
                    binding.llFormNote.visibility = View.GONE
                }
            }

            // WHAT'S IN IT
            val keyIngredientsJson = data.attributes?.typeSpecific?.keyIngredientsJson
            val inactiveIngredients = data.attributes?.typeSpecific?.inactiveIngredients
            val allergenFlags = data.attributes?.typeSpecific?.allergenFlags

            if (!keyIngredientsJson.isNullOrEmpty() || !inactiveIngredients.isNullOrEmpty()) {
                binding.cardIngredients.visibility = View.VISIBLE
                val listLayout = binding.llIngredientsList
                listLayout.removeAllViews()

                val parsedIngredients = mutableListOf<KeyIngredientItem>()
                if (!keyIngredientsJson.isNullOrEmpty()) {
                    try {
                        val gson = com.google.gson.Gson()
                        val parsed = gson.fromJson(keyIngredientsJson, Array<KeyIngredientItem>::class.java)
                        parsedIngredients.addAll(parsed)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (!inactiveIngredients.isNullOrEmpty()) {
                    parsedIngredients.add(KeyIngredientItem(
                        name = inactiveIngredients,
                        dose = "—",
                        purpose = "Inactive carrier"
                    ))
                }

                parsedIngredients.forEachIndexed { i, ing ->
                    if (i > 0) {
                        val divider = View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                            )
                            setBackgroundColor(Color.parseColor("#12FFFFFF"))
                        }
                        listLayout.addView(divider)
                    }

                    val row = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        val paddingPx = (10 * resources.displayMetrics.density).toInt()
                        setPadding(0, paddingPx, 0, paddingPx)
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.TOP
                    }

                    val doseText = when (val d = ing.dose) {
                        is Double -> "${d.toInt()}${ing.unit ?: ""}"
                        is Float -> "${d.toInt()}${ing.unit ?: ""}"
                        is Int -> "$d${ing.unit ?: ""}"
                        is String -> d
                        else -> "—"
                    }

                    val doseTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            (62 * resources.displayMetrics.density).toInt(),
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        text = doseText
                        setTextColor(Color.parseColor("#C4F23E")) // Lime
                        setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                        textSize = 12f
                    }

                    val infoLayout = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        orientation = LinearLayout.VERTICAL
                    }

                    val nameTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        text = ing.name
                        setTextColor(Color.WHITE)
                        setTextAppearance(R.style.Text_12x_Manrope_Bold)
                        textSize = 13f
                    }

                    val purposeTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, (2 * resources.displayMetrics.density).toInt(), 0, 0)
                        }
                        text = ing.purpose
                        setTextColor(Color.parseColor("#859390"))
                        setTextAppearance(R.style.Text_12x_Manrope_Medium)
                        textSize = 11.5f
                        setLineSpacing(0f, 1.2f)
                    }

                    infoLayout.addView(nameTv)
                    if (!ing.purpose.isNullOrEmpty()) {
                        infoLayout.addView(purposeTv)
                    }

                    row.addView(doseTv)
                    row.addView(infoLayout)
                    listLayout.addView(row)
                }
            }

            // Allergen Chips
            if (!allergenFlags.isNullOrEmpty()) {
                binding.flexAllergenChips.visibility = View.VISIBLE
                binding.flexAllergenChips.removeAllViews()
                val chipsList = allergenFlags.split("|")
                chipsList.forEach { chipName ->
                    val chipClean = chipName.trim()
                    if (chipClean.isNotEmpty()) {
                        val chipTv = TextView(context).apply {
                            text = chipClean
                            setTextColor(Color.parseColor("#9DACA9"))
                            setBackgroundResource(R.drawable.bg_allergen_chip)
                            val padHoriz = (10 * resources.displayMetrics.density).toInt()
                            val padVert = (5 * resources.displayMetrics.density).toInt()
                            setPadding(padHoriz, padVert, padHoriz, padVert)
                            textSize = 10.5f
                            setTypeface(null, android.graphics.Typeface.BOLD)
                            
                            layoutParams = com.google.android.flexbox.FlexboxLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, (7 * resources.displayMetrics.density).toInt(), (7 * resources.displayMetrics.density).toInt())
                            }
                        }
                        binding.flexAllergenChips.addView(chipTv)
                    }
                }
            } else {
                binding.flexAllergenChips.visibility = View.GONE
            }

            // THE EVIDENCE
            val evidence = data.attributes?.typeSpecific?.mechanismDetail ?: ""
            if (evidence.isNotEmpty()) {
                binding.cardEvidence.visibility = View.VISIBLE
                binding.tvEvidenceContent.text = HtmlCompat.fromHtml(evidence, HtmlCompat.FROM_HTML_MODE_LEGACY)
                
                val honestyShape = data.attributes?.honestyShape
                if (!honestyShape.isNullOrEmpty()) {
                    binding.tvEvidenceBadge.visibility = View.VISIBLE
                    binding.tvEvidenceBadge.text = honestyShape
                } else {
                    binding.tvEvidenceBadge.visibility = View.GONE
                }
            }

            // WHO IT SUITS / SAFETY
            val bestFor = data.bestFor
            val avoidIf = data.notFor
            val interactions = data.attributes?.typeSpecific?.drugInteractions
            val sideEffects = data.attributes?.typeSpecific?.sideEffects

            if (!bestFor.isNullOrEmpty() || !avoidIf.isNullOrEmpty() || !interactions.isNullOrEmpty() || !sideEffects.isNullOrEmpty()) {
                binding.cardSafety.visibility = View.VISIBLE
                val tableLayout = binding.llSafetyTable
                tableLayout.removeAllViews()

                val rowsData = mutableListOf<Triple<String, String, Boolean>>()
                if (!bestFor.isNullOrEmpty()) {
                    rowsData.add(Triple("Best for", bestFor.replace("|", " · "), true))
                }
                if (!avoidIf.isNullOrEmpty()) {
                    rowsData.add(Triple("Avoid if", avoidIf.replace("|", " · "), false))
                }
                if (!interactions.isNullOrEmpty()) {
                    rowsData.add(Triple("Interactions", interactions.replace("|", " · "), false))
                }
                if (!sideEffects.isNullOrEmpty()) {
                    rowsData.add(Triple("Side effects", sideEffects.replace("|", " · "), false))
                }

                rowsData.forEachIndexed { i, rowItem ->
                    if (i > 0) {
                        val divider = View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                            )
                            setBackgroundColor(Color.parseColor("#12FFFFFF"))
                        }
                        tableLayout.addView(divider)
                    }

                    val row = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        val paddingPx = (10 * resources.displayMetrics.density).toInt()
                        setPadding(0, paddingPx, 0, paddingPx)
                        orientation = LinearLayout.HORIZONTAL
                    }
                    val keyTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = rowItem.first
                        setTextColor(Color.parseColor("#8A9594"))
                        setTextAppearance(R.style.Text_12x_Manrope_Medium)
                    }
                    val valTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                        text = rowItem.second
                        if (rowItem.third) {
                            setTextColor(Color.WHITE)
                            setTextAppearance(R.style.Text_12x_Manrope_Bold)
                        } else {
                            setTextColor(Color.parseColor("#9DACA9"))
                            setTextAppearance(R.style.Text_12x_Manrope_Medium)
                        }
                        gravity = android.view.Gravity.END
                        textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                    }
                    row.addView(keyTv)
                    row.addView(valTv)
                    tableLayout.addView(row)
                }
            }

            // STACKS WELL WITH
            val pairsWith = data.attributes?.whyThis
            if (!pairsWith.isNullOrEmpty()) {
                binding.cardStacks.visibility = View.VISIBLE
                val tableLayout = binding.llStacksTable
                tableLayout.removeAllViews()

                val rowsList = pairsWith.split(Regex("\n|<br>|<br/>"))
                var visibleRowCount = 0

                for (rowItem in rowsList) {
                    val cleanRow = HtmlCompat.fromHtml(rowItem, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
                    if (cleanRow.isEmpty()) continue

                    if (visibleRowCount > 0) {
                        val divider = View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                            )
                            setBackgroundColor(Color.parseColor("#12FFFFFF"))
                        }
                        tableLayout.addView(divider)
                    }
                    visibleRowCount++

                    val parts = cleanRow.split(":", limit = 2)
                    if (parts.size == 2) {
                        val row = LinearLayout(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            val paddingPx = (10 * resources.displayMetrics.density).toInt()
                            setPadding(0, paddingPx, 0, paddingPx)
                            orientation = LinearLayout.HORIZONTAL
                        }
                        val keyTv = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                            text = parts[0].trim()
                            setTextColor(Color.parseColor("#8A9594"))
                            setTextAppearance(R.style.Text_12x_Manrope_Medium)
                        }
                        val valTv = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                            text = parts[1].trim()
                            setTextColor(Color.parseColor("#9DACA9"))
                            setTextAppearance(R.style.Text_12x_Manrope_Bold)
                            gravity = android.view.Gravity.END
                            textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                        }
                        row.addView(keyTv)
                        row.addView(valTv)
                        tableLayout.addView(row)
                    } else {
                        val row = TextView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            val paddingPx = (10 * resources.displayMetrics.density).toInt()
                            setPadding(0, paddingPx, 0, paddingPx)
                            text = cleanRow
                            setTextColor(Color.WHITE)
                            setTextAppearance(R.style.Text_12x_Manrope_Medium)
                        }
                        tableLayout.addView(row)
                    }
                }
            }

            // HOW WE'LL TRACK THIS
            data.attributes?.trackStatement?.let { track ->
                binding.cardTrack.visibility = View.VISIBLE
                binding.tvTrackContent.text = HtmlCompat.fromHtml(track, HtmlCompat.FROM_HTML_MODE_LEGACY)
            }

            // QUESTIONS (FAQs)
            val faqsLayout = binding.llFaqsList
            faqsLayout.removeAllViews()
            val faqs = data.faqsJson
            if (!faqs.isNullOrEmpty()) {
                binding.cardFaqs.visibility = View.VISIBLE
                binding.tvFaqsHeaderOutside.visibility = View.VISIBLE
                faqs.forEachIndexed { i, faq ->
                    if (i > 0) {
                        val divider = View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                            )
                            setBackgroundColor(Color.parseColor("#12FFFFFF"))
                        }
                        faqsLayout.addView(divider)
                    }

                    val faqContainer = LinearLayout(context).apply {
                        orientation = LinearLayout.VERTICAL
                        val paddingPx = (13 * resources.displayMetrics.density).toInt()
                        setPadding(0, paddingPx, 0, paddingPx)
                    }
                    val qRow = LinearLayout(context).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.CENTER_VERTICAL
                    }
                    val numTv = TextView(context).apply {
                        text = String.format("0%d", i + 1)
                        setTextColor(Color.parseColor("#C4F23E")) // Lime
                        setTypeface(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
                        textSize = 11f
                        setPadding(0, 0, (9 * resources.displayMetrics.density).toInt(), 0)
                    }
                    val qTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        text = faq.question
                        setTextColor(Color.WHITE)
                        setTextAppearance(R.style.Text_12x_Manrope_Bold)
                        textSize = 13f
                    }
                    val arrowIv = ImageView(context).apply {
                        setImageResource(R.drawable.ic_chevron_right_24px)
                        imageTintList = ColorStateList.valueOf(Color.parseColor("#8A9594"))
                    }
                    qRow.addView(numTv)
                    qRow.addView(qTv)
                    qRow.addView(arrowIv)

                    val aTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins((20 * resources.displayMetrics.density).toInt(), (7 * resources.displayMetrics.density).toInt(), 0, 0)
                        }
                        text = faq.answer
                        setTextColor(Color.parseColor("#859390"))
                        setTextAppearance(R.style.Text_12x_Manrope_Regular)
                        textSize = 12f
                        setLineSpacing(0f, 1.25f)
                        visibility = View.GONE
                    }

                    qRow.setOnClickListener {
                        val isVisible = aTv.visibility == View.VISIBLE
                        aTv.visibility = if (isVisible) View.GONE else View.VISIBLE
                        arrowIv.rotation = if (isVisible) 0f else 90f
                    }

                    faqContainer.addView(qRow)
                    faqContainer.addView(aTv)
                    faqsLayout.addView(faqContainer)
                }
            }
        } else if (data.type == "recipeBundle") {
            // Recipe bundle specific content
            binding.cardWhatItDoes.visibility = View.VISIBLE
            binding.tvWhatItDoesContent.text = data.bundleShortDescription ?: ""

            // Why Pillars
            val pillars = data.whyPillars
            if (!pillars.isNullOrEmpty()) {
                binding.cardIngredients.visibility = View.VISIBLE
                (binding.cardIngredients.findViewById<TextView>(R.id.tvIngredientsTitle))?.text = "Key Benefits & Pillars"
                val listLayout = binding.llIngredientsList
                listLayout.removeAllViews()
                pillars.forEachIndexed { i, pillar ->
                    if (i > 0) {
                        val divider = View(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                (1 * resources.displayMetrics.density).toInt().coerceAtLeast(1)
                            )
                            setBackgroundColor(Color.parseColor("#12FFFFFF"))
                        }
                        listLayout.addView(divider)
                    }

                    val row = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        val paddingPx = (10 * resources.displayMetrics.density).toInt()
                        setPadding(0, paddingPx, 0, paddingPx)
                        orientation = LinearLayout.HORIZONTAL
                        gravity = android.view.Gravity.TOP
                    }

                    val bulletTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            (24 * resources.displayMetrics.density).toInt(),
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        text = "•"
                        setTextColor(Color.parseColor("#C4F23E")) // Lime
                        setTypeface(null, android.graphics.Typeface.BOLD)
                        textSize = 14f
                    }

                    val infoLayout = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        orientation = LinearLayout.VERTICAL
                    }

                    val nameTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        text = pillar.name
                        setTextColor(Color.WHITE)
                        setTextAppearance(R.style.Text_12x_Manrope_Bold)
                        textSize = 13f
                    }

                    val descTv = TextView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            setMargins(0, (2 * resources.displayMetrics.density).toInt(), 0, 0)
                        }
                        text = pillar.description
                        setTextColor(Color.parseColor("#859390"))
                        setTextAppearance(R.style.Text_12x_Manrope_Medium)
                        textSize = 11.5f
                        setLineSpacing(0f, 1.2f)
                    }

                    infoLayout.addView(nameTv)
                    if (!pillar.description.isNullOrEmpty()) {
                        infoLayout.addView(descTv)
                    }

                    row.addView(bulletTv)
                    row.addView(infoLayout)
                    listLayout.addView(row)
                }
            }

            // Recipes list
            val recipesList = data.recipes
            if (!recipesList.isNullOrEmpty()) {
                binding.tvRecipesHeader.visibility = View.VISIBLE
                binding.rvRecipes.visibility = View.VISIBLE
                binding.rvRecipes.adapter = RecipesAdapter(recipesList)
            }
        }

        // Setup CTA buttons
        val primaryText = data.ctaPrimary ?: "Buy on Humotron"
        binding.btnBuyOnHumotron.text = if (primaryText.endsWith("→")) primaryText else "$primaryText →"
        binding.btnBuyOnHumotron.setOnClickListener {
            data.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }
        }

        binding.btnSeeRetailers.setOnClickListener {
            val matchPercent = when (data.attributes?.matchConfidence?.lowercase()) {
                "high" -> 95
                "moderate" -> 91
                else -> 85
            }
            val bundle = Bundle().apply {
                putString("productId", data.id)
                putString("productTitle", data.title)
                putInt("matchPercentage", matchPercent)
                putString("productUrl", data.url)
            }
            findNavController().navigate(R.id.fragmentSeeRetailers, bundle)
        }
    }

    inner class RecipesAdapter(private val items: List<RecipeDetailItem>) :
        RecyclerView.Adapter<RecipesAdapter.RecipeViewHolder>() {

        inner class RecipeViewHolder(val binding: com.humotron.app.databinding.ItemWhyProductBinding) : 
            RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeViewHolder {
            return RecipeViewHolder(
                com.humotron.app.databinding.ItemWhyProductBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: RecipeViewHolder, position: Int) {
            val item = items[position]
            holder.binding.tvTitle.text = item.recipeName
            holder.binding.tvDescription.text = item.shortDescription + "\n\nCalories: ${item.caloriesPerServing} kcal\nCooking Time: ${item.cookingTime}\nComplexity: ${item.complexity}"
        }

        override fun getItemCount(): Int = items.size
    }

    private data class KeyIngredientItem(
        val name: String? = null,
        val dose: Any? = null,
        val unit: String? = null,
        val purpose: String? = null
    )
}
