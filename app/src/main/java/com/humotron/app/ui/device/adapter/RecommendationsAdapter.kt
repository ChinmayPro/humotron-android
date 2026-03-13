package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.domain.modal.response.RecommendationItem

class RecommendationsAdapter :
    ListAdapter<RecommendationItem, RecommendationsAdapter.RecommendationsViewHolder>(
        RecommendationDiffCallback()
    ) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_recommendation, parent, false)
        return RecommendationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class RecommendationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvRecommendationsShort: TextView =
            itemView.findViewById(R.id.tvRecommendationsShort)
        private val tvRecommendationsLong: TextView =
            itemView.findViewById(R.id.tvRecommendationsLong)

        fun bind(recommendationItem: RecommendationItem) {
            tvRecommendationsShort.text = recommendationItem.recommendationsShort
            tvRecommendationsLong.text = recommendationItem.recommendationsLong
        }
    }

    class RecommendationDiffCallback : DiffUtil.ItemCallback<RecommendationItem>() {
        override fun areItemsTheSame(
            oldItem: RecommendationItem,
            newItem: RecommendationItem,
        ): Boolean {
            return oldItem.recommendationsTag == newItem.recommendationsTag
        }

        override fun areContentsTheSame(
            oldItem: RecommendationItem,
            newItem: RecommendationItem,
        ): Boolean {
            return oldItem == newItem
        }
    }
}