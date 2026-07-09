package com.humotron.app.ui.order.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemAppointmentBinding
import com.humotron.app.domain.modal.response.GetBloodTestOrderResponse
import com.humotron.app.util.utcOffsetToLocalTime

class AppointmentAdapter(
    private var appointments: List<GetBloodTestOrderResponse.BloodTestOrder> = emptyList()
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

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
            
            // Format order number string similar to HTML
            binding.tvOrderNumber.text = "#${appointment.orderNumber ?: ""} · Self-collection kit"

            // Visibility defaults
            binding.btnCancel.visibility = View.VISIBLE
            binding.tvReschedule.visibility = View.VISIBLE
            binding.btnJoinHere.visibility = View.GONE

            binding.tvTitle.text = appointment.title ?: if (appointment.bookingType == "expert_review") {
                "Expert Consultation"
            } else {
                context.getString(com.humotron.app.R.string.comprehensive_full_body_checkup)
            }

            if (appointment.bookingType == "expert_review") {
                binding.btnCancel.visibility = View.VISIBLE
                binding.tvReschedule.visibility = View.VISIBLE
                binding.btnJoinHere.visibility = View.VISIBLE

                binding.tvSubTitle.text = context.getString(com.humotron.app.R.string.audio_only)
                binding.tvAppointmentDateTime.text = utcOffsetToLocalTime(appointment.date, "d MMM yyyy · HH:mm", true)
            } else {
                binding.btnJoinHere.visibility = View.GONE
                binding.btnCancel.visibility = View.VISIBLE
                binding.tvReschedule.visibility = View.VISIBLE

                // Address formatting: e.g. London E16 2PH format (no England, no postcode comma)
                val addr = appointment.address
                if (addr != null) {
                    val address1 = addr.address1 ?: ""
                    val address2 = addr.address2 ?: ""
                    val address1And2 = if (address1.isNotBlank() && address2.isNotBlank()) {
                        "$address1, $address2"
                    } else {
                        address1.ifBlank { address2 }
                    }
                    
                    val city = addr.city ?: ""
                    val postcode = addr.postcode ?: ""
                    val cityAndPostcode = if (city.isNotBlank() && postcode.isNotBlank()) {
                        "$city $postcode"
                    } else {
                        city.ifBlank { postcode }
                    }
                    
                    val addressParts = listOf(
                        address1And2,
                        addr.address3 ?: "",
                        cityAndPostcode
                    ).filter { it.isNotBlank() }
                    
                    binding.tvSubTitle.text = addressParts.joinToString(", ")
                } else {
                    binding.tvSubTitle.text = ""
                }

                binding.tvAppointmentDateTime.text = utcOffsetToLocalTime(appointment.paymentDate, "d MMM yyyy · HH:mm", true)
            }

            binding.btnCancel.setOnClickListener {  }
            binding.tvReschedule.setOnClickListener {  }
        }
    }
}
