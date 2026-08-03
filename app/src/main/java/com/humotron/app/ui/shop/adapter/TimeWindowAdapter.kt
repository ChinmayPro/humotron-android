package com.humotron.app.ui.shop.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemTimeWindowBinding

data class TimeWindow(
    val id: String,
    val name: String,
    val timeRange: String,
    val slotsAvailable: String,
    val iconRes: Int
)

class TimeWindowAdapter(
    private val onWindowSelected: (TimeWindow) -> Unit
) : RecyclerView.Adapter<TimeWindowAdapter.ViewHolder>() {

    private var items = listOf<TimeWindow>()
    private var selectedPosition = 0

    fun setData(newItems: List<TimeWindow>, selectedId: String? = null) {
        items = newItems
        selectedPosition = if (!selectedId.isNullOrEmpty()) {
            items.indexOfFirst { it.id == selectedId }.takeIf { it != -1 } ?: 0
        } else {
            0
        }
        notifyDataSetChanged()
        if (items.isNotEmpty()) {
            onWindowSelected(items[selectedPosition])
        }
    }

    fun getSelectedWindow(): TimeWindow? = items.getOrNull(selectedPosition)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTimeWindowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemTimeWindowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TimeWindow, isSelected: Boolean) {
            binding.tvWindowName.text = item.name
            binding.tvWindowTime.text = item.timeRange
            binding.tvSlotsBadge.text = item.slotsAvailable
            binding.ivIcon.setImageResource(item.iconRes)

            if (isSelected) {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#14272B"))
                binding.cardView.strokeColor = Color.parseColor("#5FB7C4")
                binding.cardView.strokeWidth = (1.5f * binding.root.context.resources.displayMetrics.density).toInt()
            } else {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#8C080F10"))
                binding.cardView.strokeColor = Color.parseColor("#1AFFFFFF")
                binding.cardView.strokeWidth = (1f * binding.root.context.resources.displayMetrics.density).toInt()
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = selectedPosition
                selectedPosition = position
                notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onWindowSelected(items[selectedPosition])
            }
        }
    }

    companion object {
        fun getDefaultWindows(): List<TimeWindow> {
            return listOf(
                TimeWindow(
                    id = "w1",
                    name = "Morning",
                    timeRange = "8:00 – 11:00",
                    slotsAvailable = "6 slots",
                    iconRes = R.drawable.ic_time_morning
                ),
                TimeWindow(
                    id = "w2",
                    name = "Afternoon",
                    timeRange = "12:00 – 16:00",
                    slotsAvailable = "4 slots",
                    iconRes = R.drawable.ic_time_afternoon
                ),
                TimeWindow(
                    id = "w3",
                    name = "Evening",
                    timeRange = "17:00 – 20:00",
                    slotsAvailable = "3 slots",
                    iconRes = R.drawable.ic_time_evening
                )
            )
        }
    }
}
