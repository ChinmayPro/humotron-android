package com.humotron.app.ui.shop.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDayCardBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayCard(
    val calendar: Calendar,
    val dow: String,
    val dayNum: String,
    val mon: String,
    val fullFormatted: String
)

class DayCardAdapter(
    private val onDateSelected: (DayCard) -> Unit
) : RecyclerView.Adapter<DayCardAdapter.ViewHolder>() {

    private var items = listOf<DayCard>()
    private var selectedPosition = 0

    fun setData(newItems: List<DayCard>, selectedCal: Calendar? = null) {
        items = newItems
        selectedPosition = if (selectedCal != null) {
            items.indexOfFirst {
                it.calendar.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) &&
                it.calendar.get(Calendar.DAY_OF_YEAR) == selectedCal.get(Calendar.DAY_OF_YEAR)
            }.takeIf { it != -1 } ?: 0
        } else {
            0
        }
        notifyDataSetChanged()
        if (items.isNotEmpty()) {
            onDateSelected(items[selectedPosition])
        }
    }

    fun getSelectedDate(): DayCard? = items.getOrNull(selectedPosition)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDayCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemDayCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: DayCard, isSelected: Boolean) {
            binding.tvDow.text = item.dow
            binding.tvDayNum.text = item.dayNum
            binding.tvMon.text = item.mon

            if (isSelected) {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#1F5FB7C4"))
                binding.cardView.strokeColor = Color.parseColor("#5FB7C4")
                binding.cardView.strokeWidth = (1.5f * binding.root.context.resources.displayMetrics.density).toInt()
                binding.tvDow.setTextColor(Color.parseColor("#5FB7C4"))
                binding.tvMon.setTextColor(Color.parseColor("#5FB7C4"))
            } else {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#8C080F10"))
                binding.cardView.strokeColor = Color.parseColor("#1AFFFFFF")
                binding.cardView.strokeWidth = (1f * binding.root.context.resources.displayMetrics.density).toInt()
                binding.tvDow.setTextColor(Color.parseColor("#6F7E7D"))
                binding.tvMon.setTextColor(Color.parseColor("#6F7E7D"))
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                val prev = selectedPosition
                selectedPosition = position
                notifyItemChanged(prev)
                notifyItemChanged(selectedPosition)
                onDateSelected(items[selectedPosition])
            }
        }
    }

    companion object {
        fun generateNextDays(count: Int = 14): List<DayCard> {
            val list = mutableListOf<DayCard>()
            val cal = Calendar.getInstance()
            // Formatters
            val dowFormat = SimpleDateFormat("EEE", Locale.ENGLISH)
            val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
            val monFormat = SimpleDateFormat("MMM", Locale.ENGLISH)
            val fullFormat = SimpleDateFormat("EEE, dd MMM", Locale.ENGLISH)

            for (i in 0 until count) {
                val dayCal = cal.clone() as Calendar
                list.add(
                    DayCard(
                        calendar = dayCal,
                        dow = dowFormat.format(dayCal.time).uppercase(),
                        dayNum = dayFormat.format(dayCal.time),
                        mon = monFormat.format(dayCal.time),
                        fullFormatted = fullFormat.format(dayCal.time)
                    )
                )
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return list
        }
    }
}
