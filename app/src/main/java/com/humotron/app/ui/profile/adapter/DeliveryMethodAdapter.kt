package com.humotron.app.ui.profile.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemDeliveryMethodBinding
import com.humotron.app.domain.modal.response.GetCartResponse

class DeliveryMethodAdapter(
    private var selectedMethodId: String? = null,
    private val onMethodSelected: (GetCartResponse.DeliveryMethod) -> Unit
) : RecyclerView.Adapter<DeliveryMethodAdapter.ViewHolder>() {

    private var methods: List<GetCartResponse.DeliveryMethod> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun setMethods(newMethods: List<GetCartResponse.DeliveryMethod>, selectedId: String?) {
        this.methods = newMethods
        this.selectedMethodId = selectedId
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDeliveryMethodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(methods[position])
    }

    override fun getItemCount(): Int = methods.size

    inner class ViewHolder(private val binding: ItemDeliveryMethodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(method: GetCartResponse.DeliveryMethod) {
            binding.tvMethodName.text = "${method.methodName}\n(${method.minDays}-${method.maxDays} Days)"
            binding.tvPrice.text = "£${method.price}"
            binding.tvExpectedDeliveryValue.text = method.estimatedDelivery ?: ""
            
            binding.rbSelect.isChecked = method.id == selectedMethodId

            binding.root.setOnClickListener {
                selectedMethodId = method.id
                onMethodSelected(method)
                notifyDataSetChanged()
            }
            
            binding.rbSelect.setOnClickListener {
                selectedMethodId = method.id
                onMethodSelected(method)
                notifyDataSetChanged()
            }

            // Hide divider for last item
            binding.viewDivider.visibility = if (absoluteAdapterPosition == methods.size - 1) View.GONE else View.VISIBLE
        }
    }
}
