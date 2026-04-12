package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemShopMetricBinding
import com.humotron.app.domain.modal.response.DeviceDetailResponse

class ShopMetricAdapter(private val metrics: List<DeviceDetailResponse.Metric>) :
    RecyclerView.Adapter<ShopMetricAdapter.MetricViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetricViewHolder {
        val binding = ItemShopMetricBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MetricViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MetricViewHolder, position: Int) {
        holder.bind(metrics[position])
    }

    override fun getItemCount(): Int = metrics.size

    class MetricViewHolder(private val binding: ItemShopMetricBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(metric: DeviceDetailResponse.Metric) {
            binding.tvMetricName.text = metric.metricName
            binding.tvMetricWhat.text = metric.metricWhat
        }
    }
}
