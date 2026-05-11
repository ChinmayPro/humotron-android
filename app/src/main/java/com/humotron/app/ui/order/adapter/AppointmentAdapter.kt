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
            binding.tvTitle.text = appointment.title ?: context.getString(com.humotron.app.R.string.comprehensive_full_body_checkup)
            binding.tvOrderNumber.text = "#${appointment.orderNumber ?: ""}"
            
            // iOS logic: lbl_bookingDate = appointment.date
            binding.tvBookingDate.text = utcOffsetToLocalTime(appointment.date, "d MMM''yy, hh:mm a")

            // Default visibility
            binding.ivBookingIcon.visibility = View.VISIBLE
            binding.ivCall.visibility = View.VISIBLE
            binding.ivMessage.visibility = View.VISIBLE
            binding.ivLocation.visibility = View.VISIBLE
            binding.btnCancel.visibility = View.VISIBLE
            binding.tvReschedule.visibility = View.VISIBLE
            binding.btnJoinHere.visibility = View.GONE

            binding.tvBookingDetailsLabel.text = context.getString(com.humotron.app.R.string.booking_details_format, appointment.title ?: "")

            if (appointment.bookingType == "expert_review") {
                binding.ivBookingIcon.visibility = View.GONE
                binding.ivCall.visibility = View.GONE
                binding.ivMessage.visibility = View.GONE
                binding.ivLocation.visibility = View.GONE
                binding.btnCancel.visibility = View.GONE
                binding.tvReschedule.visibility = View.GONE
                binding.btnJoinHere.visibility = View.VISIBLE

                binding.tvBookingDetailsLabel.text = context.getString(com.humotron.app.R.string.appointment_details)
                
                binding.tvSubTitle.text = utcOffsetToLocalTime(appointment.date, "d MMM''yy, hh:mm a")
                
                binding.tvAppointmentDateTime.text = context.getString(com.humotron.app.R.string.audio_only)
            } else {
                // Visibility for normal blood tests
                binding.btnJoinHere.visibility = View.GONE
                binding.ivBookingIcon.visibility = View.VISIBLE
                binding.ivCall.visibility = View.VISIBLE
                binding.ivMessage.visibility = View.VISIBLE
                binding.ivLocation.visibility = View.VISIBLE
                binding.btnCancel.visibility = View.VISIBLE
                binding.tvReschedule.visibility = View.VISIBLE

                // Address formatting matching iOS
                val addr = appointment.address
                if (addr != null) {
                    val address1 = addr.address1 ?: ""
                    val address2 = addr.address2 ?: ""
                    val address1And2 = if (address1.isNotBlank() && address2.isNotBlank()) {
                        "$address1, $address2"
                    } else {
                        address1.ifBlank { address2 }
                    }
                    
                    val addressParts = listOf(
                        address1And2,
                        addr.address3 ?: "",
                        addr.city ?: "",
                        addr.country ?: "",
                        addr.postcode ?: ""
                    ).filter { it.isNotBlank() }
                    
                    binding.tvSubTitle.text = addressParts.joinToString(", ")
                } else {
                    binding.tvSubTitle.text = context.getString(com.humotron.app.R.string.comprehensive_full_body_checkup)
                }

                // iOS logic: lbl_appointment_date = appointment.paymentDate
                binding.tvAppointmentDateTime.text = utcOffsetToLocalTime(appointment.paymentDate, "d MMM''yy, hh:mm a")
            }

            val prepareText = context.getString(com.humotron.app.R.string.read_how_to_prepare_html)
            binding.tvPrepare.text = android.text.Html.fromHtml(prepareText, android.text.Html.FROM_HTML_MODE_LEGACY)

            // Icons and Buttons
            binding.ivCall.setOnClickListener { }
            binding.ivMessage.setOnClickListener {  }
            binding.ivLocation.setOnClickListener {  }
            binding.btnCancel.setOnClickListener {  }
            binding.tvReschedule.setOnClickListener {  }
        }
    }
}
