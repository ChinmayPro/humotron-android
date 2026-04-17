package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemFaqBinding
import com.humotron.app.domain.modal.response.DeviceFaqResponse

class ShopFaqAdapter(
    private val faqs: List<DeviceFaqResponse.FaqData>
) : RecyclerView.Adapter<ShopFaqAdapter.FaqViewHolder>() {

    private var expandedPosition = -1

    inner class FaqViewHolder(val binding: ItemFaqBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(faq: DeviceFaqResponse.FaqData, position: Int) {
            val qNum = position + 1
            val context = binding.root.context
            binding.tvQPrefix.text = context.getString(com.humotron.app.R.string.faq_question_prefix, qNum)
            binding.tvQuestion.text = faq.question
            
            binding.tvAPrefix.text = context.getString(com.humotron.app.R.string.faq_answer_prefix, qNum)
            binding.tvAnswer.text = faq.answer

            val isExpanded = position == expandedPosition
            binding.clAnswer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            
            // Rotate arrow based on state
            binding.ivArrow.rotation = if (isExpanded) 90f else 0f

            binding.clQuestion.setOnClickListener {
                val prevExpandedPosition = expandedPosition
                expandedPosition = if (isExpanded) -1 else position
                
                notifyItemChanged(prevExpandedPosition)
                notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val binding = ItemFaqBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FaqViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        holder.bind(faqs[position], position)
    }

    override fun getItemCount(): Int = faqs.size
}
