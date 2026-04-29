package com.humotron.app.ui.shop.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemLabSelectBinding
import com.humotron.app.domain.modal.response.GetAllLabResponse

class LabSelectAdapter(
    private val onLabSelected: (GetAllLabResponse.Lab?) -> Unit
) : RecyclerView.Adapter<LabSelectAdapter.ViewHolder>() {

    private var labs: List<GetAllLabResponse.Lab> = emptyList()
    private var selectedPosition: Int = -1

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
        holder.bind(lab, position == selectedPosition)
    }

    override fun getItemCount(): Int = labs.size

    inner class ViewHolder(private val binding: ItemLabSelectBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(lab: GetAllLabResponse.Lab, isSelected: Boolean) {
            binding.tvLabName.text = lab.labName
            binding.tvAddress.text = lab.address?.line1
            binding.tvCity.text = lab.address?.city
            binding.tvPostcode.text = lab.address?.postcode
            binding.tvCountry.text = lab.address?.country

            // Update selection state
            if (isSelected) {
                binding.cardLab.strokeColor = ContextCompat.getColor(binding.root.context, R.color.lime_green)
                binding.cardLab.strokeWidth = 2 * binding.root.resources.displayMetrics.density.toInt()
            } else {
                binding.cardLab.strokeColor = ContextCompat.getColor(binding.root.context, R.color.d900)
                binding.cardLab.strokeWidth = 1 * binding.root.resources.displayMetrics.density.toInt()
            }

            binding.root.setOnClickListener {
                val previousSelected = selectedPosition
                selectedPosition = adapterPosition
                notifyItemChanged(previousSelected)
                notifyItemChanged(selectedPosition)
                onLabSelected(labs[selectedPosition])
            }
        }
    }
}
