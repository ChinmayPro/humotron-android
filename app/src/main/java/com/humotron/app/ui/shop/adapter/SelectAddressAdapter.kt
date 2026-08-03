package com.humotron.app.ui.shop.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSelectAddressBinding
import com.humotron.app.domain.modal.response.GetCartResponse.Address

class SelectAddressAdapter(
    private val onAddressSelected: (Address) -> Unit,
    private val onEditAddress: (Address) -> Unit
) : RecyclerView.Adapter<SelectAddressAdapter.ViewHolder>() {

    private var items = listOf<Address>()
    private var selectedPosition = 0

    fun setData(newItems: List<Address>, selectedId: String? = null) {
        items = newItems
        selectedPosition = if (!selectedId.isNullOrEmpty()) {
            items.indexOfFirst { it.id == selectedId }.takeIf { it != -1 } ?: 0
        } else {
            0
        }
        notifyDataSetChanged()
        if (items.isNotEmpty()) {
            onAddressSelected(items[selectedPosition])
        }
    }

    fun getSelectedAddress(): Address? {
        return if (selectedPosition in items.indices) items[selectedPosition] else null
    }

    fun getItems(): List<Address> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSelectAddressBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position == selectedPosition)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(private val binding: ItemSelectAddressBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: Address, isSelected: Boolean) {
            val context = binding.root.context
            val fullName = context.getString(R.string.full_name_format, address.firstName ?: "", address.lastName ?: "").trim()
            binding.tvAddressName.text = if (fullName.isNotBlank()) fullName else "Chinmay Bhatt"
            binding.tvAddressPhone.text = address.contactNo ?: "+44 7417 519358"

            val addressParts = listOfNotNull(
                address.address1,
                address.address2,
                address.address3,
                address.city,
                address.country,
                address.postcode
            ).filter { it.isNotBlank() }

            binding.tvAddressDetails.text = if (addressParts.isNotEmpty()) {
                addressParts.joinToString(", ")
            } else {
                "113 Masthead House, 14 Rope Terrace, London, E16 2PH"
            }

            if (isSelected) {
                // Exact dark teal background tint matching prototype rgba(95,183,196,.07) over dark gradient
                binding.cardView.setCardBackgroundColor(Color.parseColor("#12282C"))
                binding.cardView.strokeColor = Color.parseColor("#5FB7C4")
                binding.cardView.strokeWidth = dpToPx(context, 1.5f)
                binding.ivRadio.setImageResource(R.drawable.ic_radio_selected_cyan)
            } else {
                binding.cardView.setCardBackgroundColor(Color.parseColor("#8C080F10"))
                binding.cardView.strokeColor = Color.parseColor("#1AFFFFFF")
                binding.cardView.strokeWidth = dpToPx(context, 1f)
                binding.ivRadio.setImageResource(R.drawable.ic_radio_unselected)
            }

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener
                val previousSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onAddressSelected(items[selectedPosition])
            }

            binding.tvEditAddress.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onEditAddress(items[position])
                }
            }
        }
    }

    private fun dpToPx(context: android.content.Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}
