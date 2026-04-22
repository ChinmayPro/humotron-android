package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemTimeSlotBinding

class TimeSlotAdapter(
    private val onTimeSelected: (String) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    private val slots = mutableListOf<String>()
    private var selectedTime: String? = null

    fun setData(newSlots: List<String>) {
        slots.clear()
        slots.addAll(newSlots)
        notifyDataSetChanged()
    }

    fun setSelectedTime(time: String) {
        selectedTime = time
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(slots[position])
    }

    override fun getItemCount(): Int = slots.size

    inner class TimeSlotViewHolder(private val binding: ItemTimeSlotBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(time: String) {
            binding.tvTime.text = time
            val isSelected = time == selectedTime

            if (isSelected) {
                binding.cardTimeSlot.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.lime_green)
                )
                binding.tvTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.black)
                )
                binding.cardTimeSlot.strokeColor = ContextCompat.getColor(binding.root.context, android.R.color.transparent)
            } else {
                binding.cardTimeSlot.setCardBackgroundColor(
                    ContextCompat.getColor(binding.root.context, R.color.black)
                )
                binding.tvTime.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.white)
                )
                binding.cardTimeSlot.strokeColor = ContextCompat.getColor(binding.root.context, R.color.gray_400)
            }

            binding.root.setOnClickListener {
                selectedTime = time
                onTimeSelected(time)
                notifyDataSetChanged()
            }
        }
    }
}
