package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemPrefQuestionBinding
import com.humotron.app.domain.modal.response.PrimaryTagLevel

class PrimaryTagAdapter(val onValueSelected: (Int) -> Unit) :
    RecyclerView.Adapter<PrimaryTagAdapter.PrimaryTagViewHolder>() {

    var list = arrayListOf<PrimaryTagLevel>()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PrimaryTagViewHolder {
        return PrimaryTagViewHolder(
            ItemPrefQuestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: PrimaryTagViewHolder,
        position: Int
    ) {
        val item = list[position]
        holder.binding.apply {
            tvQuestion.text = item.tagName
            cbToggle.isChecked = item.isChecked
            root.setOnClickListener {
                item.isChecked = !item.isChecked
                cbToggle.isChecked = item.isChecked
                onValueSelected(list.filter { it.isChecked }.size)
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setData(levels: List<PrimaryTagLevel>) {
        this.list = ArrayList(levels)
        notifyDataSetChanged()
    }

    class PrimaryTagViewHolder(val binding: ItemPrefQuestionBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}