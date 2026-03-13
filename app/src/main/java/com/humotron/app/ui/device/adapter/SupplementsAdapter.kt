package com.humotron.app.ui.device.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.R
import com.humotron.app.domain.modal.response.Supplement

class SupplementsAdapter :
    ListAdapter<Supplement, SupplementsAdapter.SupplementsViewHolder>(SupplementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SupplementsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_supplements, parent, false)
        return SupplementsViewHolder(view)
    }

    override fun onBindViewHolder(holder: SupplementsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SupplementsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        private val tvProductDesc: TextView = itemView.findViewById(R.id.tvProductDesc)
        private val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)

        fun bind(supplement: Supplement) {
            tvProductName.text = supplement.productName
            tvProductDesc.text = supplement.productDesc

            Glide.with(itemView.context)
                .load(supplement.productImage)
                .into(ivProductImage)
        }
    }

    class SupplementDiffCallback : DiffUtil.ItemCallback<Supplement>() {
        override fun areItemsTheSame(
            oldItem: Supplement,
            newItem: Supplement,
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: Supplement,
            newItem: Supplement,
        ): Boolean {
            return oldItem == newItem
        }
    }
}
