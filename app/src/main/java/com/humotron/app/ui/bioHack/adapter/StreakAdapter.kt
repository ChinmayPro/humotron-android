package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemBiohackStreakBinding
import com.humotron.app.domain.modal.response.BioHackProgressResponse

class StreakAdapter : RecyclerView.Adapter<StreakAdapter.ViewHolder>() {

    var list = arrayListOf<BioHackProgressResponse.Data.Streak>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemBiohackStreakBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val data = list[position]
        holder.binding.apply {
            val count = data.count ?: 0
            tvCount.text = "$count"
            tvDate.text = data.formatDate
            tvDay.text = data.day

            val dpToPx = holder.itemView.context.resources.displayMetrics.density
            if (count > 0) {
                cardRoot.setCardBackgroundColor(android.graphics.Color.parseColor("#143B35"))
                cardRoot.strokeColor = android.graphics.Color.parseColor("#BEF264")
                cardRoot.strokeWidth = (1.5f * dpToPx).toInt()
                tvCount.setTextColor(android.graphics.Color.parseColor("#BEF264"))
                tvDate.setTextColor(android.graphics.Color.parseColor("#759D96"))
                tvDay.setTextColor(android.graphics.Color.parseColor("#5C7C76"))
            } else {
                cardRoot.setCardBackgroundColor(android.graphics.Color.parseColor("#12332F"))
                cardRoot.strokeColor = android.graphics.Color.parseColor("#1F4A43")
                cardRoot.strokeWidth = (1f * dpToPx).toInt()
                tvCount.setTextColor(android.graphics.Color.parseColor("#5C7C76"))
                tvDate.setTextColor(android.graphics.Color.parseColor("#5C7C76"))
                tvDay.setTextColor(android.graphics.Color.parseColor("#4A6761"))
            }
            root.alpha = 1.0f
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemBiohackStreakBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    fun setData(list: List<BioHackProgressResponse.Data.Streak>) {
        this.list = list as ArrayList<BioHackProgressResponse.Data.Streak>
        notifyDataSetChanged()
    }
}