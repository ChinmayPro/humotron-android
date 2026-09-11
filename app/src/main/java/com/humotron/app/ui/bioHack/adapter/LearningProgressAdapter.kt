package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemBiohackProgressBinding
import com.humotron.app.domain.modal.response.BioHackProgressResponse

class LearningProgressAdapter(
    var onLoadMoreStateChanged: ((hasMore: Boolean) -> Unit)? = null
) : RecyclerView.Adapter<LearningProgressAdapter.ViewHolder>() {

    private var fullList = arrayListOf<BioHackProgressResponse.Data.PrimaryTagScore>()
    var isExpanded = false
        private set

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemBiohackProgressBinding.inflate(
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
        val data = fullList[position]
        holder.binding.apply {
            tvTitle.text = data.primaryTagName
            tvDesc.text = data.categoryName
            val pct = data.percentage ?: 0
            tvLevel.text = "$pct"
            val isUnlocked = data.unlockedScore == 1

            if (isUnlocked || pct >= 100) {
                tvStatus.text = "Cleared"
                tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle_green, 0, 0, 0)
                tvStatus.visibility = View.VISIBLE
                dividerProgress.visibility = View.VISIBLE
                vLeftAccent.visibility = View.VISIBLE
            } else if (pct > 0) {
                tvStatus.text = "In Progress"
                tvStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_circle_green, 0, 0, 0)
                tvStatus.visibility = View.VISIBLE
                dividerProgress.visibility = View.VISIBLE
                vLeftAccent.visibility = View.VISIBLE
            } else {
                tvStatus.visibility = View.GONE
                dividerProgress.visibility = View.GONE
                vLeftAccent.visibility = View.INVISIBLE
            }
        }
    }

    override fun getItemCount(): Int {
        return if (isExpanded || fullList.size <= 4) {
            fullList.size
        } else {
            if (fullList.size < 4) fullList.size else 4
        }
    }

    fun expandAll() {
        if (!isExpanded) {
            isExpanded = true
            notifyDataSetChanged()
        }
    }

    fun collapse() {
        if (isExpanded) {
            isExpanded = false
            notifyDataSetChanged()
        }
    }

    fun setData(list: List<BioHackProgressResponse.Data.PrimaryTagScore>) {
        this.fullList = ArrayList(list)
        this.isExpanded = false
        notifyDataSetChanged()
        onLoadMoreStateChanged?.invoke(fullList.size > 4)
    }

    class ViewHolder(val binding: ItemBiohackProgressBinding) :
        RecyclerView.ViewHolder(binding.root)
}