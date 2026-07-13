package com.humotron.app.ui.recipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R

class RecipesFragment : Fragment() {

    private val viewModel: RecipesViewModel by activityViewModels()

    private lateinit var rvMetrics: RecyclerView
    private lateinit var rvCategories: RecyclerView
    private lateinit var ivBack: ImageView
    private lateinit var tvFocusMetricTitle: TextView
    private lateinit var tvFocusMetricValue: TextView
    private lateinit var tvFocusMetricUnit: TextView
    private lateinit var tvTrendValue: TextView
    private lateinit var tvReplacedMeals: TextView
    private lateinit var pbReplacedMeals: ProgressBar
    private lateinit var llTrendPill: LinearLayout
    private lateinit var ivTrendArrow: ImageView
    private lateinit var sparklineView: com.humotron.app.view.SparklineView

    private lateinit var metricAdapter: MetricCapsuleAdapter
    private lateinit var categoryAdapter: RecipeCategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupAdapters()
        setupListeners()
        observeViewModel()
    }

    private fun initViews(view: View) {
        rvMetrics = view.findViewById(R.id.rvMetrics)
        rvCategories = view.findViewById(R.id.rvCategories)
        ivBack = view.findViewById(R.id.ivBack)
        tvFocusMetricTitle = view.findViewById(R.id.tvFocusMetricTitle)
        tvFocusMetricValue = view.findViewById(R.id.tvFocusMetricValue)
        tvFocusMetricUnit = view.findViewById(R.id.tvFocusMetricUnit)
        tvTrendValue = view.findViewById(R.id.tvTrendValue)
        tvReplacedMeals = view.findViewById(R.id.tvReplacedMeals)
        pbReplacedMeals = view.findViewById(R.id.pbReplacedMeals)
        llTrendPill = view.findViewById(R.id.llTrendPill)
        ivTrendArrow = view.findViewById(R.id.ivTrendArrow)
        sparklineView = view.findViewById(R.id.sparklineView)
    }

    private fun setupAdapters() {
        metricAdapter = MetricCapsuleAdapter { selectedId ->
            viewModel.selectMetric(selectedId)
        }
        rvMetrics.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMetrics.adapter = metricAdapter

        categoryAdapter = RecipeCategoryAdapter(
            onRecipeClicked = { recipe ->
                val bundle = Bundle().apply {
                    putString("recipeId", recipe.id)
                }
                findNavController().navigate(R.id.action_fragmentRecipes_to_fragmentRecipeDetail, bundle)
            },
            onLogRecipe = { recipe ->
                viewModel.logRecipe(recipe.id)
            },
            onToggleFavorite = { recipe ->
                viewModel.toggleFavorite(recipe.id)
            }
        )
        rvCategories.layoutManager = LinearLayoutManager(requireContext())
        rvCategories.adapter = categoryAdapter
    }

    private fun setupListeners() {
        ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.metricCapsules.observe(viewLifecycleOwner) { capsules ->
            metricAdapter.submitList(capsules)
        }

        viewModel.selectedMetric.observe(viewLifecycleOwner) { metric ->
            metric?.let {
                tvFocusMetricTitle.text = it.title
                tvFocusMetricValue.text = it.value
                tvFocusMetricUnit.text = it.unit

                // Update trend arrow direction
                if (it.isUpwardTrend) {
                    ivTrendArrow.setImageResource(R.drawable.ic_arrow_upward)
                } else {
                    ivTrendArrow.setImageResource(R.drawable.ic_arrow_downward)
                }

                // Update colors dynamically based on trend (improving/watch)
                val colorRes = if (it.isImproving) R.color.insights_green else R.color.deep_dives_attention
                val color = ContextCompat.getColor(requireContext(), colorRes)

                ivTrendArrow.imageTintList = android.content.res.ColorStateList.valueOf(color)
                tvTrendValue.setTextColor(color)

                val bgTint = ColorUtils.setAlphaComponent(color, 26) // ~10% opacity
                llTrendPill.backgroundTintList = android.content.res.ColorStateList.valueOf(bgTint)

                val trendText = if (it.isImproving) getString(R.string.improving) else "watch"
                tvTrendValue.text = "${it.trendValue} · $trendText"

                tvReplacedMeals.text = "${it.replacedMeals} / ${it.targetMeals} · ${getString(R.string.last_30_days)}"

                val progress = if (it.targetMeals > 0) {
                    (it.replacedMeals.toFloat() / it.targetMeals * 100).toInt()
                } else {
                    0
                }
                pbReplacedMeals.progress = progress
                sparklineView.setData(it.sparkData)
            }
        }

        viewModel.recipeCategories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.recipeLogs.observe(viewLifecycleOwner) { logs ->
            populateLogs(logs)
        }
    }

    private fun populateLogs(logs: List<RecipeLog>) {
        val view = view ?: return
        val llLogs = view.findViewById<android.widget.LinearLayout>(R.id.llLogs)
        val tvLogCount = view.findViewById<android.widget.TextView>(R.id.tvLogCount)
        llLogs.removeAllViews()

        tvLogCount.text = "${logs.size} entries"

        logs.forEach { log ->
            val logView = layoutInflater.inflate(R.layout.item_recipe_log, llLogs, false)
            logView.findViewById<android.widget.TextView>(R.id.tvLogName).text = log.recipeName
            logView.findViewById<android.widget.TextView>(R.id.tvLogMeta).text = "${log.mealType} · ${log.logTime}"
            logView.findViewById<android.widget.TextView>(R.id.tvLogMetric).text = log.metricName

            llLogs.addView(logView)
        }
    }
}
