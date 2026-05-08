package com.humotron.app.ui.order.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemOrderBinding
import com.humotron.app.domain.modal.response.GetAllOrderResponse
import com.humotron.app.util.utcOffsetToLocalTime

class OrderAdapter : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    private var orders: MutableList<GetAllOrderResponse.Order> = mutableListOf()
    var onItemClick: ((GetAllOrderResponse.Order) -> Unit)? = null

    fun setOrders(newOrders: List<GetAllOrderResponse.Order>, isFirstPage: Boolean) {
        if (isFirstPage) {
            orders.clear()
        }
        val oldSize = orders.size
        orders.addAll(newOrders)
        if (isFirstPage) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeInserted(oldSize, newOrders.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(order: GetAllOrderResponse.Order) {
            binding.tvOrderId.text = "#${order.orderNumber ?: ""}"
            
            val formattedDate = utcOffsetToLocalTime(order.updatedAt, "dd MMM yyyy")
            binding.tvOrderDate.text = formattedDate.lowercase()
            
            binding.tvStatus.text = order.orderStatusName ?: ""
            
            // Apply Status Color if available
            try {
                val colorStr = order.statusColorCode
                if (!colorStr.isNullOrEmpty()) {
                    binding.tvStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorStr))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val price = order.payableAmount ?: 0.0
            // Format price as in screenshot £229.06
            binding.tvPrice.text = "£${String.format("%.2f", price)}"
            
            val itemCount = order.cartProducts?.sumOf { it.quantity ?: 0 } ?: 0
            binding.tvItemCount.text = binding.root.context.getString(com.humotron.app.R.string.items_count_format, itemCount)
            
            binding.tvExpectedDelivery.text = binding.root.context.getString(com.humotron.app.R.string.expected_delivery_format, order.estimatedDelivery ?: "")
            
            binding.root.setOnClickListener {
                onItemClick?.invoke(order)
            }
        }
    }
}
