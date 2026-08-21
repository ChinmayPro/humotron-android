package com.humotron.app.ui.track

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemTrackingBinding
import com.humotron.app.domain.modal.response.HealthMetricTrackingData
import com.humotron.app.util.utcOffsetToLocalTime
import com.humotron.app.util.utcOffsetToOrdinalDate

class TrackingAdapter(
    private var metrics: List<HealthMetricTrackingData>,
    private val onItemClick: (HealthMetricTrackingData) -> Unit,
) : RecyclerView.Adapter<TrackingAdapter.TrackingViewHolder>() {

    private var isSmartSuggestionsEnabled: Boolean = false

    fun setSmartSuggestionsEnabled(enabled: Boolean) {
        this.isSmartSuggestionsEnabled = enabled
        notifyItemRangeChanged(0, itemCount, "SUGGESTIONS_TOGGLE")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackingViewHolder {
        val binding =
            ItemTrackingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackingViewHolder, position: Int) {
        holder.bind(metrics[position])
    }

    override fun onBindViewHolder(
        holder: TrackingViewHolder,
        position: Int,
        payloads: MutableList<Any>,
    ) {
        if (payloads.contains("SUGGESTIONS_TOGGLE")) {
            holder.updateSuggestionsVisibility(isSmartSuggestionsEnabled)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount(): Int = metrics.size

    fun updateData(newMetrics: List<HealthMetricTrackingData>) {
        metrics = newMetrics
        notifyDataSetChanged()
    }

    inner class TrackingViewHolder(private val binding: ItemTrackingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(metric: HealthMetricTrackingData) {
            binding.mtvMetricName.text = metric.metricUserFacingName ?: metric.metricName
            binding.mtvValue.text = metric.metricValue?.value ?: "-"
            binding.mtvValueUnit.text = metric.metricUnit ?: ""
            binding.mtvRecText1.text = metric.metricWhat ?: ""
            binding.mtvRecText2.text = metric.metricWhy ?: ""
            binding.mtvStatusBadge.text = metric.status ?: ""
            binding.mtvGuide.text = metric.observationLens ?: ""

            val supplementsCount = metric.supplementCount
            val recipesCount = metric.recipeCount

            binding.btnSupplements.text =
                getString(R.string.trk_btn_supplements, supplementsCount)

            binding.btnRecipes.text =
                getString(R.string.trk_btn_recipes, recipesCount)

            updateSuggestionsVisibility(isSmartSuggestionsEnabled)

            metric.metricValue?.timestamp?.let { ts ->
                binding.mtvTimestamp.text = formatTimestamp(ts, metric.deviceName)
                binding.mtvTimestamp.isVisible = true
                binding.mcvTimestampDot.isVisible = true
            } ?: run {
                binding.mtvTimestamp.isVisible = false
                binding.mcvTimestampDot.isVisible = false
            }

            binding.root.setOnClickListener {
                onItemClick(metric)
            }
        }

        fun updateSuggestionsVisibility(enabled: Boolean) {
            binding.cvTrackingExpand.isVisible = enabled
        }

        private fun getString(resId: Int, vararg formatArgs: Any?): String {
            return binding.root.context.getString(resId, *formatArgs)
        }

        private fun formatTimestamp(timestamp: String, deviceName: String?): String {
            return try {
                // Normalize the timestamp to an ISO format that utcOffsetToOrdinalDate can parse
                val normalized = utcOffsetToLocalTime(timestamp, "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                val dateStr = utcOffsetToOrdinalDate(normalized)

                if (dateStr.isNotEmpty()) {
                    if (!deviceName.isNullOrBlank()) "$dateStr $deviceName" else dateStr
                } else {
                    timestamp
                }
            } catch (e: Exception) {
                timestamp
            }
        }
    }
}
