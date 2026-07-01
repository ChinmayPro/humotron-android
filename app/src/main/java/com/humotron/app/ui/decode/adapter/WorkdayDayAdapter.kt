package com.humotron.app.ui.decode.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemWeekDayBinding
import com.humotron.app.ui.decode.data.WorkdayDay
import java.text.SimpleDateFormat
import java.util.Locale

class WorkdayDayAdapter(
    private val days: List<WorkdayDay>,
    private val onDaySelected: (Int) -> Unit
) : RecyclerView.Adapter<WorkdayDayAdapter.DayViewHolder>() {

    var selectedIndex: Int = days.size - 1
        private set

    private val dowFormat = SimpleDateFormat("EEE", Locale.ENGLISH)

    fun selectDay(index: Int) {
        val old = selectedIndex
        selectedIndex = index
        notifyItemChanged(old)
        notifyItemChanged(index)
    }

    inner class DayViewHolder(val binding: ItemWeekDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION && days[pos].hasData) {
                    selectDay(pos)
                    onDaySelected(pos)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemWeekDayBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        val isSelected = position == selectedIndex
        val context = holder.itemView.context

        // Day abbreviation (THU, FRI, etc.)
        holder.binding.tvDayOfWeek.text = dowFormat.format(day.date).uppercase()

        // Date number
        val cal = java.util.Calendar.getInstance().apply { time = day.date }
        holder.binding.tvDateNumber.text = cal.get(java.util.Calendar.DAY_OF_MONTH).toString()

        // Selection state
        if (isSelected) {
            holder.binding.chipRoot.setBackgroundResource(R.drawable.bg_day_chip_selected)
        } else {
            holder.binding.chipRoot.setBackgroundResource(R.drawable.bg_day_chip_default)
        }

        // Zone dot color
        if (day.hasData) {
            holder.binding.dotZone.backgroundTintList =
                android.content.res.ColorStateList.valueOf(day.zone.color)
            holder.binding.root.alpha = 1f
        } else {
            holder.binding.dotZone.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor("#26FFFFFF"))
            holder.binding.root.alpha = 0.38f
        }
    }

    override fun getItemCount(): Int = days.size
}
