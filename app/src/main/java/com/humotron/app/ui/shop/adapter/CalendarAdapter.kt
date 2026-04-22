package com.humotron.app.ui.shop.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemCalendarDayBinding
import java.util.Calendar

class CalendarAdapter(
    private val onDateSelected: (Calendar) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    private val days = mutableListOf<Calendar?>()
    private var selectedDate: Calendar? = null
    private val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    fun setData(newDays: List<Calendar?>, selected: Calendar?) {
        days.clear()
        days.addAll(newDays)
        selectedDate = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val date = days[position]
        holder.bind(date)
    }

    override fun getItemCount(): Int = days.size

    inner class CalendarViewHolder(private val binding: ItemCalendarDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(date: Calendar?) {
            if (date == null) {
                binding.tvDay.text = ""
                binding.viewSelection.visibility = View.GONE
                binding.root.setOnClickListener(null)
            } else {
                binding.tvDay.text = date.get(Calendar.DAY_OF_MONTH).toString()
                
                val isPast = date.before(today)
                val isToday = today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                        today.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
                
                val isSelected = selectedDate?.let {
                    it.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                    it.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
                    it.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)
                } ?: false

                if (isSelected) {
                    binding.viewSelection.visibility = View.VISIBLE
                    binding.tvDay.setTextColor(Color.BLACK)
                } else {
                    binding.viewSelection.visibility = View.GONE
                    if (isPast) {
                        binding.tvDay.setTextColor(Color.parseColor("#4DFFFFFF"))
                    } else if (isToday) {
                        binding.tvDay.setTextColor(ContextCompat.getColor(binding.root.context, R.color.lime_green))
                    } else {
                        binding.tvDay.setTextColor(Color.WHITE)
                    }
                }

                if (isPast) {
                    binding.root.setOnClickListener(null)
                } else {
                    binding.root.setOnClickListener {
                        onDateSelected(date)
                    }
                }
            }
        }
    }
}
