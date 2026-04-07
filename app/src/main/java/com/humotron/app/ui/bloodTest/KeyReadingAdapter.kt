package com.humotron.app.ui.bloodTest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemKeyReadingBinding
import com.humotron.app.domain.modal.response.MetricReadingData

class KeyReadingAdapter(
    private var data: List<MetricReadingData> = emptyList()
) : RecyclerView.Adapter<KeyReadingAdapter.KeyReadingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KeyReadingViewHolder {
        val binding = ItemKeyReadingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return KeyReadingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KeyReadingViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    fun updateData(newData: List<MetricReadingData>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class KeyReadingViewHolder(private val binding: ItemKeyReadingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MetricReadingData) {
            binding.tvMetricReading.text = item.metricReading ?: "N/A"
            binding.tvMetricName.text = item.metricName ?: "N/A"
            binding.tvMetricDesc.text = item.metricReadingDescription ?: ""

            binding.tvSupplements.text = "${item.supplementCount ?: 0} Supplements"
            binding.tvRecipes.text = "${item.recipeBundleCount ?: 0} Recipes"

            // Optional: You can add logic to hide Manage Through if there are no recommendations
            // binding.llSupplements.isVisible = (item.supplementCount ?: 0) > 0
            // binding.llRecipes.isVisible = (item.recipeBundleCount ?: 0) > 0
        }
    }
}
