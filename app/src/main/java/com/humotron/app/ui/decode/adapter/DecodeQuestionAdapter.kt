package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDecodeQuestionBinding
import com.humotron.app.domain.modal.response.FeltOffQuestionData

class DecodeQuestionAdapter(
    private val items: List<FeltOffQuestionData>,
    private val onItemClick: (FeltOffQuestionData) -> Unit
) : RecyclerView.Adapter<DecodeQuestionAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDecodeQuestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvQuestion.text = item.question
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(val binding: ItemDecodeQuestionBinding) :
        RecyclerView.ViewHolder(binding.root)
}
