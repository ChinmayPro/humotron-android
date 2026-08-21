package com.humotron.app.ui.order.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemAppointmentBinding
import com.humotron.app.domain.modal.response.GetBloodTestOrderResponse
import com.humotron.app.util.utcOffsetToLocalTime
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentAdapter(
    private var appointments: List<GetBloodTestOrderResponse.BloodTestOrder> = emptyList()
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    var onCancelClick: ((GetBloodTestOrderResponse.BloodTestOrder) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAppointmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    fun updateData(newList: List<GetBloodTestOrderResponse.BloodTestOrder>) {
        appointments = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: GetBloodTestOrderResponse.BloodTestOrder) {
            val context = binding.root.context

            // Formatted Date (e.g. 21 Aug 2026)
            val displayDate = formatDate(appointment.date ?: appointment.paymentDate)

            // Header line: #BT-20260819-C6A0 · 21 Aug 2026
            val orderNo = appointment.orderNumber ?: ""
            binding.tvOrderHeader.text = if (displayDate.isNotEmpty()) "#$orderNo · $displayDate" else "#$orderNo"

            // Status Badge (Confirmed vs Payment failed)
            val statusStr = appointment.status ?: "Confirmed"
            binding.tvStatusBadge.text = statusStr

            val isFailed = statusStr.contains("fail", true) || statusStr.contains("decline", true) || statusStr.contains("cancel", true)
            if (isFailed) {
                binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_status_pill_failed)
                binding.tvStatusBadge.setTextColor(Color.parseColor("#FF5C5C"))
                binding.llStepperContainer.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                binding.tvSubInfo.text = "Card declined · no charge taken"
            } else {
                binding.tvStatusBadge.background = ContextCompat.getDrawable(context, R.drawable.bg_status_pill_confirmed)
                binding.tvStatusBadge.setTextColor(Color.parseColor("#3ED9D1"))
                binding.llStepperContainer.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE

                // Calculate progress step index based on status string from API
                val statusLower = statusStr.lowercase()
                val activeStepCount = when {
                    statusLower.contains("done") || statusLower.contains("complete") -> 5
                    statusLower.contains("result") -> 4
                    statusLower.contains("sample") -> 3
                    statusLower.contains("kit") || statusLower.contains("sent") || statusLower.contains("dispatch") -> 2
                    else -> 1 // Default: Confirmed (1st step)
                }

                val activeColor = Color.parseColor("#3ED9D1")
                val inactiveColor = Color.parseColor("#5E7574")
                val stepNodes = listOf(binding.vStep1, binding.vStep2, binding.vStep3, binding.vStep4, binding.vStep5)
                val stepLabels = listOf(binding.tvStepLabel1, binding.tvStepLabel2, binding.tvStepLabel3, binding.tvStepLabel4, binding.tvStepLabel5)

                for (i in 0 until 5) {
                    if (i < activeStepCount) {
                        stepNodes[i].background = ContextCompat.getDrawable(context, R.drawable.bg_step_node_active)
                        stepLabels[i].setTextColor(activeColor)
                    } else {
                        stepNodes[i].background = ContextCompat.getDrawable(context, R.drawable.bg_step_node_inactive)
                        stepLabels[i].setTextColor(inactiveColor)
                    }
                }
            }

            // Title: At-Home Service
            binding.tvTitle.text = appointment.title ?: "At-Home Service"

            // Collection Service Type string
            val serviceTypeStr = when {
                appointment.serviceType?.contains("home", true) == true || appointment.productType?.contains("home", true) == true -> "At home collection"
                appointment.serviceType?.contains("lab", true) == true || appointment.productType?.contains("lab", true) == true -> "Lab visit"
                else -> "Self-collection kit"
            }

            // Amount string ($179 / £0.50)
            val amountStr = when {
                !appointment.formattedAmount.isNullOrEmpty() -> appointment.formattedAmount
                appointment.amountPaid != null -> {
                    val curr = if (appointment.currency == "USD") "$" else "£"
                    val valStr = if (appointment.amountPaid % 1.0 == 0.0) String.format(Locale.ENGLISH, "%.0f", appointment.amountPaid) else String.format(Locale.ENGLISH, "%.2f", appointment.amountPaid)
                    "$curr$valStr"
                }
                else -> ""
            }

            val priceAndTypeStr = if (amountStr.isNotEmpty()) "$serviceTypeStr · $amountStr" else serviceTypeStr
            binding.tvCollectionTypePrice.text = priceAndTypeStr

            // Secondary Subtitle info line
            if (!isFailed) {
                binding.tvSubInfo.text = if (displayDate.isNotEmpty()) "$displayDate · $serviceTypeStr" else serviceTypeStr
            }

            binding.btnCancel.setOnClickListener {
                onCancelClick?.invoke(appointment)
            }
        }

        private fun formatDate(rawDate: String?): String {
            if (rawDate.isNullOrEmpty()) return ""
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val dateObj = inputFormat.parse(rawDate.take(10))
                val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                if (dateObj != null) outputFormat.format(dateObj) else utcOffsetToLocalTime(rawDate, "dd MMM yyyy", true)
            } catch (e: Exception) {
                utcOffsetToLocalTime(rawDate, "dd MMM yyyy", true)
            }
        }
    }
}
