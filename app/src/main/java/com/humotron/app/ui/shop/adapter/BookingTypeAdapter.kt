package com.humotron.app.ui.shop.adapter

import android.graphics.Color
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
            binding.tvTitle.text = item.title ?: ""
            binding.tvDescription.text = item.description ?: ""
            
            val formattedPrice = item.price?.let { priceStr ->
                if (priceStr.startsWith("£") || priceStr.startsWith("$")) priceStr else "£$priceStr"
            } ?: "£19.99"
            binding.tvPrice.text = formattedPrice

            val titleLower = item.title?.lowercase() ?: ""
            when {
                titleLower.contains("self") -> {
                    binding.tvHintText.text = "Kit posted to your address"
                    binding.ivHintIcon.setImageResource(R.drawable.ic_opt_document)
                }
                titleLower.contains("home") -> {
                    binding.tvHintText.text = "Choose a date, time & where to visit"
                    binding.ivHintIcon.setImageResource(R.drawable.ic_opt_heart)
                }
                titleLower.contains("lab") -> {
                    binding.tvHintText.text = "Choose a lab, date & time"
                    binding.ivHintIcon.setImageResource(R.drawable.ic_opt_flask)
                }
                else -> {
                    binding.tvHintText.text = "Select option to proceed"
                    binding.ivHintIcon.setImageResource(R.drawable.ic_check_thin)
                }
            }

            val context = binding.root.context
            if (isSelected) {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#15C4F23E"))
                binding.cardView.strokeColor = Color.parseColor("#C4F23E")
                binding.cardView.strokeWidth = dpToPx(context, 1.5f)
            } else {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#8C080F10"))
                binding.cardView.strokeColor = Color.parseColor("#1AFFFFFF")
                binding.cardView.strokeWidth = dpToPx(context, 1f)
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                val previousSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onItemSelected(items[selectedPosition])
            }
        }
    }

    private fun dpToPx(context: android.content.Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
