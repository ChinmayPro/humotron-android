package com.humotron.app.ui.decode

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.humotron.app.R
import com.humotron.app.core.base.BaseFragment
import com.humotron.app.data.network.Status
import com.humotron.app.databinding.FragmentDecodeDetailBinding
import com.humotron.app.domain.modal.response.InsightDetailData
import com.humotron.app.domain.modal.response.SuggestedAction
import com.humotron.app.ui.decode.adapter.DecodeSuggestedActionsAdapter
import com.humotron.app.ui.decode.view.LineChartView
import com.humotron.app.ui.decode.viewmodel.DecodeViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.widget.TextView
import androidx.core.text.HtmlCompat

@AndroidEntryPoint
class DecodeDetailFragment : BaseFragment(R.layout.fragment_decode_detail) {

    private lateinit var binding: FragmentDecodeDetailBinding
    private val viewModel: DecodeViewModel by viewModels()
    private lateinit var actionsAdapter: DecodeSuggestedActionsAdapter
    private var metricName: String = ""
    private var insightId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDecodeDetailBinding.bind(view)

        metricName = arguments?.getString("metricName") ?: ""
        insightId = arguments?.getString("insightId") ?: ""

        setupInsets()
        initViews()
        initClicks()
        setupRecyclerView()
        initObservers()

        if (insightId.isNotEmpty()) {
            viewModel.getInsightById(insightId)
        } else {
            populateFallbackData()
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.layoutDetail.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom
            }
            insets
        }
    }

    private fun initViews() {
        binding.header.title.text = metricName
    }

    private fun initClicks() {
        binding.header.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupRecyclerView() {
        binding.rvSuggestedActions.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        actionsAdapter = DecodeSuggestedActionsAdapter(emptyList())
        binding.rvSuggestedActions.adapter = actionsAdapter
    }

    private fun initObservers() {
        viewModel.insightDetailData().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.LOADING -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.layoutDetail.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutDetail.visibility = View.VISIBLE
                    resource.data?.data?.let { insightData ->
                        bindInsightDetail(insightData)
                    }
                }
                Status.ERROR, Status.EXCEPTION -> {
                    binding.progressBar.visibility = View.GONE
                    binding.layoutDetail.visibility = View.VISIBLE
                    populateFallbackData()
                }
            }
        }
    }

    private fun bindInsightDetail(data: InsightDetailData) {
        val insight = data.insightJson?.insight
        val metric = data.metricName ?: metricName
        val label = data.insightJson?.observationalLens ?: metric
        binding.tvDetailMetricLabel.text = label.uppercase()
        binding.header.title.text = insight?.shortTitle ?: metric

        // Title format using user's name
        val firstName = prefUtils.getLoginResponse()?.firstName ?: ""
        binding.tvDetailTitle.text = insight?.title ?: getString(R.string.insights_detail_title_format, firstName, metric)

        // Date range
        binding.tvDetailDateRange.text = data.insightRange ?: ""

        // Observation title and desc
        binding.tvBpLoadTitle.text = insight?.shortTitle ?: ""
        setMarkdownText(binding.tvBpLoadDesc, insight?.shortDescription ?: "")

        // Summary desc
        setMarkdownText(binding.tvSummaryDesc, insight?.summary ?: "")

        // Hypothesis narrative and reasons
        val hypothesis = insight?.hypothesis
        val hypothesisBuilder = StringBuilder()
        val narrative = hypothesis?.narrative ?: ""
        if (narrative.isNotEmpty()) {
            hypothesisBuilder.append(narrative).append("\n\n")
        }
        hypothesis?.reasons?.forEach { reason ->
            hypothesisBuilder.append("• ").append(reason).append("\n")
        }
        setMarkdownText(binding.tvHypothesisDesc, hypothesisBuilder.toString().trim())

        // Suggested actions (using Adapter)
        val suggestedActions = insight?.suggestedActions ?: emptyList()
        actionsAdapter.updateItems(suggestedActions)

        // Chart visualization
        val vis = insight?.nuggetVisualisation
        val yValues = vis?.yValues ?: emptyList()
        val xValues = vis?.xValues ?: emptyList()

        val avg1 = yValues.getOrNull(0) ?: 120f
        val avg2 = yValues.getOrNull(1) ?: 118f

        val name1 = xValues.getOrNull(0) ?: "Work Hours"
        val name2 = xValues.getOrNull(1) ?: "Recovery Hours"

        val orangeHex = String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(requireContext(), R.color.insights_orange))
        val greenHex = String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(requireContext(), R.color.insights_light_green))

        binding.chartMetricDetail.setSeries(
            listOf(
                LineChartView.Series(name1, listOf(avg1, avg1 - 0.5f, avg1 + 0.2f, avg1 - 0.2f, avg1 + 0.5f, avg1 - 0.7f), orangeHex),
                LineChartView.Series(name2, listOf(avg2, avg2 - 0.5f, avg2 + 0.2f, avg2 - 0.2f, avg2 + 0.4f, avg2 - 0.9f), greenHex)
            )
        )
    }

    private fun populateFallbackData() {
        binding.tvDetailMetricLabel.text = metricName.uppercase()
        binding.header.title.text = metricName

        val firstName = prefUtils.getLoginResponse()?.firstName ?: ""
        binding.tvDetailTitle.text = getString(R.string.insights_detail_title_format, firstName, metricName)
        binding.tvDetailDateRange.text = "18–25 May"

        binding.tvBpLoadTitle.text = getString(R.string.insights_detail_bp_load_title)
        binding.tvBpLoadDesc.text = getString(R.string.insights_detail_bp_load_desc)
        binding.tvSummaryTitle.text = getString(R.string.insights_detail_summary_title)
        binding.tvSummaryDesc.text = getString(R.string.insights_detail_summary_desc)
        binding.tvHypothesisTitle.text = getString(R.string.insights_detail_hypothesis_title)
        binding.tvHypothesisDesc.text = getString(R.string.insights_detail_hypothesis_desc)

        val orangeHex = String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(requireContext(), R.color.insights_orange))
        val greenHex = String.format("#%06X", 0xFFFFFF and ContextCompat.getColor(requireContext(), R.color.insights_light_green))

        binding.chartMetricDetail.setSeries(
            listOf(
                LineChartView.Series("Work Hours", listOf(120f, 119.5f, 120.2f, 119.8f, 120.5f, 119.3f), orangeHex),
                LineChartView.Series("Recovery Hours", listOf(118f, 117.5f, 118.2f, 117.8f, 118.4f, 117.1f), greenHex)
            )
        )

        // Fallback suggested actions (using Adapter)
        val fallbackActions = listOf(
            SuggestedAction(getString(R.string.insights_action_midday_title), getString(R.string.insights_action_midday_desc)),
            SuggestedAction(getString(R.string.insights_action_hydration_title), getString(R.string.insights_action_hydration_desc))
        )
        actionsAdapter.updateItems(fallbackActions)
    }

    private fun convertMarkdownToHtml(text: String): String {
        return text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
            .replace(Regex("__(.*?)__"), "<b>$1</b>")
            .replace(Regex("\\*(.*?)\\*"), "<i>$1</i>")
            .replace(Regex("_(.*?)_"), "<i>$1</i>")
            .replace("\n", "<br>")
    }

    private fun setMarkdownText(textView: TextView, text: String) {
        val html = convertMarkdownToHtml(text)
        textView.text = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}
