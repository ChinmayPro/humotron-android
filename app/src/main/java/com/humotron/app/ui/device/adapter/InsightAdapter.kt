package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.domain.modal.response.Insight

class InsightAdapter :
    ListAdapter<Insight, InsightAdapter.InsightViewHolder>(InsightDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InsightViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_insight, parent, false)
        return InsightViewHolder(view)
    }

    override fun onBindViewHolder(holder: InsightViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class InsightViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvHeading: TextView = itemView.findViewById(R.id.tvHeading)
        private val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        private val tvHypothesisDesc: TextView = itemView.findViewById(R.id.tvHypothesisDesc)

        fun bind(insight: Insight) {
            tvTitle.text = insight.observationalLens
            tvHeading.text = insight.title
            tvDate.text = insight.insightRange
            tvHypothesisDesc.text = insight.hypothesis?.narrative
        }
    }

    class InsightDiffCallback : DiffUtil.ItemCallback<Insight>() {
        override fun areItemsTheSame(oldItem: Insight, newItem: Insight): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Insight, newItem: Insight): Boolean {
            return oldItem == newItem
        }
    }
}