package com.humotron.app.ui.profile

import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemGoalBinding
import com.humotron.app.domain.modal.response.HealthProfileConfigResponse.HealthItem

class GoalAdapter(
    private var items: List<HealthItem>,
    private val selectedGoals: MutableSet<String>,
    private val onItemClick: (HealthItem, Boolean) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val binding = ItemGoalBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GoalViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<HealthItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class GoalViewHolder(private val binding: ItemGoalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HealthItem) {
            val title = item.name ?: "Health Goal"
            binding.tvGoalName.text = title

            val isSelected = selectedGoals.contains(title) || item.isSelected == true

            if (item.isSelected == true && !selectedGoals.contains(title)) {
                selectedGoals.add(title)
            }

            val descFormatted = if (isSelected) {
                "<font color=\"#7BD88F\">Prioritised goal</font> · Active"
            } else {
                "Tap to prioritise this goal"
            }

            binding.tvGoalDesc.text = Html.fromHtml(descFormatted, Html.FROM_HTML_MODE_LEGACY)

            updateSelectionState(isSelected)

            // Select icon based on name
            val nameLower = title.lowercase()
            val iconRes = when {
                nameLower.contains("cardio") || nameLower.contains("heart") -> R.drawable.ic_metrics_pulse
                nameLower.contains("recovery") -> R.drawable.ic_start_recovery
                nameLower.contains("sleep") -> R.drawable.ic_start_sleep
                nameLower.contains("stress") -> R.drawable.ic_start_stress
                nameLower.contains("athletic") || nameLower.contains("performance") -> R.drawable.ic_start_energy
                nameLower.contains("longevity") || nameLower.contains("wellness") -> R.drawable.ic_humotron_leaf
                nameLower.contains("weight") || nameLower.contains("muscle") -> R.drawable.ic_weight_scale
                nameLower.contains("foot") || nameLower.contains("joint") || nameLower.contains("mobility") -> R.drawable.ic_wrist_band
                else -> R.drawable.ic_target
            }
            binding.ivGoalIcon.setImageResource(iconRes)

            binding.root.setOnClickListener {
                val newSelected = !selectedGoals.contains(title)
                if (newSelected) {
                    selectedGoals.add(title)
                } else {
                    selectedGoals.remove(title)
                }
                updateSelectionState(newSelected)
                val newDesc = if (newSelected) {
                    "<font color=\"#7BD88F\">Prioritised goal</font> · Active"
                } else {
                    "Tap to prioritise this goal"
                }
                binding.tvGoalDesc.text = Html.fromHtml(newDesc, Html.FROM_HTML_MODE_LEGACY)
                onItemClick(item, newSelected)
            }
        }

        private fun updateSelectionState(isSelected: Boolean) {
            binding.ivCheckbox.tag = isSelected
            if (isSelected) {
                binding.ivCheckbox.setImageResource(R.drawable.bg_checkbox_checked_mockup)
                binding.ivCheckbox.imageTintList = null
                binding.ivGoalIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#C4F23E"))
                binding.llDeviceIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#24C4F23E"))
            } else {
                binding.ivCheckbox.setImageResource(R.drawable.ic_checkbox_unselected)
                binding.ivCheckbox.imageTintList = ColorStateList.valueOf(Color.parseColor("#4DFFFFFF"))
                binding.ivGoalIcon.imageTintList = ColorStateList.valueOf(Color.parseColor("#8EA09E"))
                binding.llDeviceIcon.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0DFFFFFF"))
            }
        }
    }
}
