package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemAddressSelectBinding

import com.humotron.app.domain.modal.response.GetCartResponse

class AddressSelectAdapter(
    private val onEditClick: (GetCartResponse.Address) -> Unit,
    private val onAddressClick: (GetCartResponse.Address) -> Unit
) : RecyclerView.Adapter<AddressSelectAdapter.ViewHolder>() {

    private var addresses: List<GetCartResponse.Address> = emptyList()
    private var selectedAddressId: String? = null

    fun submitList(list: List<GetCartResponse.Address>, selectedId: String?) {
        addresses = list
        selectedAddressId = selectedId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddressSelectBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(addresses[position])
    }

    override fun getItemCount(): Int = addresses.size

    inner class ViewHolder(private val binding: ItemAddressSelectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(address: GetCartResponse.Address) {
            binding.tvName.text = binding.root.context.getString(R.string.full_name_format, address.firstName ?: "", address.lastName ?: "").trim()
            binding.tvPhone.text = address.contactNo ?: ""
            
            val details = listOfNotNull(
                address.address1,
                address.address2,
                address.address3,
                address.city,
                address.country,
                address.postcode
            ).filter { it.isNotBlank() }.joinToString(", ")
            
            binding.tvDetails.text = details

            binding.ivSelected.visibility = if (address.id == selectedAddressId || (selectedAddressId == null && address.isDefault == true)) View.VISIBLE else View.GONE

            binding.btnEdit.setOnClickListener {
                onEditClick(address)
            }

            binding.root.setOnClickListener {
                onAddressClick(address)
            }
        }
    }
}
