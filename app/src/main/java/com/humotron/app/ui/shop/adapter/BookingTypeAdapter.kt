package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemBookingTypeBinding
import com.humotron.app.domain.modal.response.BookingTypeResponse.BookingType

class BookingTypeAdapter(private val onItemSelected: (BookingType?) -> Unit) :
    RecyclerView.Adapter<BookingTypeAdapter.ViewHolder>() {

    private var items = listOf<BookingType>()
    private var selectedPosition = -1

    fun setData(newItems: List<BookingType>) {
        items = newItems
        selectedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBookingTypeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemBookingTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BookingType, isSelected: Boolean) {
            binding.tvTitle.text = item.title
            binding.tvDescription.text = item.description
            binding.tvPrice.text = binding.root.context.getString(R.string.price_format, item.currency ?: "$", item.price)

            // Handle selection UI
            if (isSelected) {
                binding.cardView.strokeColor = ContextCompat.getColor(binding.root.context, R.color.colorBgBtn)
                binding.cardView.strokeWidth = dpToPx(binding.root.context, 2)
            } else {
                binding.cardView.strokeColor = ContextCompat.getColor(binding.root.context, R.color.gray_400)
                binding.cardView.strokeWidth = dpToPx(binding.root.context, 1)
            }

            binding.root.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onItemSelected(items[selectedPosition])
            }
        }
    }

    private fun dpToPx(context: android.content.Context, dp: Int): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
