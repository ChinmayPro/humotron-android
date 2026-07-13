package com.humotron.app.ui.recipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R

class MetricCapsuleAdapter(
    private val onMetricSelected: (String) -> Unit
) : ListAdapter<MetricCapsule, MetricCapsuleAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metric_capsule, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMetricTitle: TextView = itemView.findViewById(R.id.tvMetricTitle)
        private val tvMetricValue: TextView = itemView.findViewById(R.id.tvMetricValue)
        private val vIndicator: View = itemView.findViewById(R.id.vIndicator)

        fun bind(item: MetricCapsule) {
            tvMetricTitle.text = item.title
            val displayUnit = if (item.unit.trim() == "%") "%" else ""
            tvMetricValue.text = "${item.value}$displayUnit"

            if (item.isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_metric_capsule_active)
                tvMetricTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.textColorWhite))
                tvMetricValue.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorTextGreyDark))
            } else {
                itemView.setBackgroundResource(R.drawable.bg_metric_capsule_inactive)
                tvMetricTitle.setTextColor(ContextCompat.getColor(itemView.context, R.color.textColorMuted))
                tvMetricValue.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorTextGreyDark))
            }

            vIndicator.visibility = View.VISIBLE
            val colorRes = if (item.isImproving) R.color.insights_green else R.color.deep_dives_attention
            vIndicator.backgroundTintList = ContextCompat.getColorStateList(itemView.context, colorRes)

            itemView.setOnClickListener {
                onMetricSelected(item.id)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<MetricCapsule>() {
        override fun areItemsTheSame(oldItem: MetricCapsule, newItem: MetricCapsule): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MetricCapsule, newItem: MetricCapsule): Boolean {
            return oldItem == newItem
        }
    }
}
