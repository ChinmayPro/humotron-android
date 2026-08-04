package com.humotron.app.ui.shop.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemLabSelectBinding
import com.humotron.app.domain.modal.response.GetAllLabResponse

class LabSelectAdapter(
    private val onLabSelected: (GetAllLabResponse.Lab?) -> Unit
) : RecyclerView.Adapter<LabSelectAdapter.ViewHolder>() {

    private var labs: List<GetAllLabResponse.Lab> = emptyList()
    private var selectedPosition: Int = -1

    // Mock data for prototype fields not in API
    private val mockDistances = listOf("0.8 mi", "2.1 mi", "3.4 mi", "4.2 mi", "5.0 mi")
    private val mockRatings = listOf("4.9", "4.8", "4.7", "4.6", "4.5")
    private val mockHours = listOf("Mon-Sat · 7am–7pm", "Mon-Fri · 8am–6pm", "Mon-Sat · 7am–8pm", "Mon-Fri · 9am–5pm", "Mon-Sun · 8am–6pm")
    private val mockNextAvailable = listOf("Tomorrow, 09:00", "Wed, 08:30", "Tomorrow, 14:00", "Thu, 10:00", "Fri, 09:30")

    fun setLabs(newLabs: List<GetAllLabResponse.Lab>) {
        this.labs = newLabs
        this.selectedPosition = -1
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLabSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lab = labs[position]
        holder.bind(lab, position == selectedPosition, position)
    }

    override fun getItemCount(): Int = labs.size

    inner class ViewHolder(private val binding: ItemLabSelectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lab: GetAllLabResponse.Lab, isSelected: Boolean, position: Int) {
            // Lab name
            binding.tvLabName.text = lab.labName ?: "Partner Lab"

            // Full address line
            val addrParts = listOfNotNull(
                lab.address?.line1,
                lab.address?.city,
                lab.address?.postcode
            ).filter { it.isNotBlank() }
            binding.tvAddress.text = if (addrParts.isNotEmpty()) addrParts.joinToString(", ") else "Address not available"

            // Mock meta data
            binding.tvDistance.text = mockDistances.getOrElse(position % mockDistances.size) { "1.0 mi" }
            binding.tvRating.text = mockRatings.getOrElse(position % mockRatings.size) { "4.5" }
            binding.tvHours.text = mockHours.getOrElse(position % mockHours.size) { "Mon-Fri · 9am–5pm" }
            binding.tvNextAvailable.text = "Next available: ${mockNextAvailable.getOrElse(position % mockNextAvailable.size) { "Tomorrow" }}"

            // Selection state
            if (isSelected) {
                binding.cardLab.strokeColor = Color.parseColor("#5FB7C4")
                binding.cardLab.strokeWidth = (2 * binding.root.resources.displayMetrics.density).toInt()
                binding.cardLab.setCardBackgroundColor(Color.parseColor("#145FB7C4"))
                binding.ivRadio.setImageResource(R.drawable.ic_radio_on)
            } else {
                binding.cardLab.strokeColor = Color.parseColor("#14FFFFFF")
                binding.cardLab.strokeWidth = (1 * binding.root.resources.displayMetrics.density).toInt()
                binding.cardLab.setCardBackgroundColor(Color.parseColor("#8C080F10"))
                binding.ivRadio.setImageResource(R.drawable.ic_radio_off)
            }

            binding.root.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                if (previousSelected >= 0) notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onLabSelected(labs[selectedPosition])
            }
        }
    }
}
