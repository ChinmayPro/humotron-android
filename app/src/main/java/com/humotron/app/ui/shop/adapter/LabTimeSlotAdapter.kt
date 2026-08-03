package com.humotron.app.ui.shop.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemLabTimeChipBinding
import com.humotron.app.databinding.ItemLabTimeHeaderBinding

sealed class LabSlotListItem {
    data class Header(val title: String, val iconRes: Int) : LabSlotListItem()
    data class Slot(val time: String, val isAvailable: Boolean) : LabSlotListItem()
}

class LabTimeSlotAdapter(
    private val onSlotSelected: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<LabSlotListItem>()
    private var selectedTime: String? = "09:30"

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_SLOT = 1

        fun getDefaultLabSlots(): List<LabSlotListItem> {
            return listOf(
                LabSlotListItem.Header("MORNING", R.drawable.ic_time_morning),
                LabSlotListItem.Slot("08:00", true),
                LabSlotListItem.Slot("08:30", true),
                LabSlotListItem.Slot("09:00", true),
                LabSlotListItem.Slot("09:30", true),
                LabSlotListItem.Slot("10:00", false),
                LabSlotListItem.Slot("10:30", true),

                LabSlotListItem.Header("AFTERNOON", R.drawable.ic_time_afternoon),
                LabSlotListItem.Slot("12:00", true),
                LabSlotListItem.Slot("13:00", true),
                LabSlotListItem.Slot("13:30", false),
                LabSlotListItem.Slot("14:00", true),
                LabSlotListItem.Slot("14:30", true),
                LabSlotListItem.Slot("15:30", true),

                LabSlotListItem.Header("EVENING", R.drawable.ic_time_evening),
                LabSlotListItem.Slot("17:00", true),
                LabSlotListItem.Slot("17:30", true),
                LabSlotListItem.Slot("18:00", true),
                LabSlotListItem.Slot("18:30", false)
            )
        }
    }

    fun setData(newItems: List<LabSlotListItem>, defaultSelected: String? = "09:30") {
        items.clear()
        items.addAll(newItems)
        selectedTime = defaultSelected
        notifyDataSetChanged()
        defaultSelected?.let { onSlotSelected(it) }
    }

    fun getSelectedTime(): String? = selectedTime

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is LabSlotListItem.Header -> TYPE_HEADER
            is LabSlotListItem.Slot -> TYPE_SLOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val binding = ItemLabTimeHeaderBinding.inflate(inflater, parent, false)
            HeaderViewHolder(binding)
        } else {
            val binding = ItemLabTimeChipBinding.inflate(inflater, parent, false)
            SlotViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is LabSlotListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is LabSlotListItem.Slot -> (holder as SlotViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class HeaderViewHolder(private val binding: ItemLabTimeHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LabSlotListItem.Header) {
            binding.tvHeaderTitle.text = item.title
            binding.ivHeaderIcon.setImageResource(item.iconRes)
        }
    }

    inner class SlotViewHolder(private val binding: ItemLabTimeChipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LabSlotListItem.Slot) {
            binding.tvSlotTime.text = item.time

            if (!item.isAvailable) {
                // Disabled slot: dark background, muted strikethrough text
                binding.cardSlot.setCardBackgroundColor(Color.parseColor("#4D080F10"))
                binding.cardSlot.strokeColor = Color.parseColor("#0FFFFFFF")
                binding.cardSlot.strokeWidth = (1 * binding.root.resources.displayMetrics.density).toInt()
                binding.tvSlotTime.setTextColor(Color.parseColor("#3F4B4A"))
                binding.tvSlotTime.paintFlags = binding.tvSlotTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.root.setOnClickListener(null)
                binding.root.isClickable = false
            } else {
                binding.tvSlotTime.paintFlags = binding.tvSlotTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.root.isClickable = true

                if (item.time == selectedTime) {
                    // Selected slot: solid teal background, dark text
                    binding.cardSlot.setCardBackgroundColor(Color.parseColor("#5FB7C4"))
                    binding.cardSlot.strokeColor = Color.parseColor("#5FB7C4")
                    binding.cardSlot.strokeWidth = (1.5f * binding.root.resources.displayMetrics.density).toInt()
                    binding.tvSlotTime.setTextColor(Color.parseColor("#080F10"))
                } else {
                    // Available slot: dark background, white text, subtle border
                    binding.cardSlot.setCardBackgroundColor(Color.parseColor("#8C080F10"))
                    binding.cardSlot.strokeColor = Color.parseColor("#1AFFFFFF")
                    binding.cardSlot.strokeWidth = (1 * binding.root.resources.displayMetrics.density).toInt()
                    binding.tvSlotTime.setTextColor(Color.parseColor("#FFFFFF"))
                }

                binding.root.setOnClickListener {
                    val prev = selectedTime
                    selectedTime = item.time
                    notifyDataSetChanged()
                    onSlotSelected(item.time)
                }
            }
        }
    }
}
