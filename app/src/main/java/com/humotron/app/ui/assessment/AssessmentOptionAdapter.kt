package com.humotron.app.ui.assessment

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemAssessmentQueSingleBinding

class AssessmentOptionAdapter(
    private var options: List<String>,
    private var selectedIndex: Int? = null,
    private val onOptionSelected: (Int) -> Unit
) : RecyclerView.Adapter<AssessmentOptionAdapter.OptionViewHolder>() {

    inner class OptionViewHolder(private val binding: ItemAssessmentQueSingleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(option: String, position: Int) {
            binding.tvQue.text = option
            val isSelected = selectedIndex == position
            binding.rbQue.isChecked = isSelected

            val context = binding.root.context
            if (isSelected) {
                binding.mcvQue.strokeColor = ContextCompat.getColor(context, R.color.lime)
            } else {
                binding.mcvQue.strokeColor = ContextCompat.getColor(context, R.color.transparent)
            }

            binding.mcvQue.setOnClickListener {
                if (selectedIndex != position) {
                    val oldIndex = selectedIndex
                    selectedIndex = position
                    oldIndex?.let { notifyItemChanged(it) }
                    notifyItemChanged(position)
                    onOptionSelected(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val binding = ItemAssessmentQueSingleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OptionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        holder.bind(options[position], position)
    }

    override fun getItemCount(): Int = options.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newOptions: List<String>, newSelectedIndex: Int?) {
        options = newOptions
        selectedIndex = newSelectedIndex
        notifyDataSetChanged()
    }
}
