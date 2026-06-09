package com.humotron.app.ui.device.adapter

import android.os.Parcelable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemReadingListBinding
import com.humotron.app.domain.modal.BPMachineReadingType
import kotlinx.parcelize.Parcelize

class MeasurementTypeAdapter(
    val items: List<MeasurementInfo>,
    val onItemClicked: (MeasurementInfo) -> Unit,
) :
    RecyclerView.Adapter<MeasurementTypeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        return ViewHolder(
            ItemReadingListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val data = items[position]
        holder.binding.apply {
            tvWearableName.text = data.title
            root.setOnClickListener {
                onItemClicked(data)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ViewHolder(val binding: ItemReadingListBinding) : RecyclerView.ViewHolder(binding.root)
}

@Parcelize
data class MeasurementInfo(
    val title: String,
    val readingType: BPMachineReadingType,
) : Parcelable
