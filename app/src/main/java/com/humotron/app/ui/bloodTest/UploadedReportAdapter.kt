package com.humotron.app.ui.bloodTest

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemUploadedReportBinding
import com.humotron.app.domain.modal.response.MedicalPdf

class UploadedReportAdapter(
    private var data: List<MedicalPdf>,
    private val onItemClick: (MedicalPdf) -> Unit,
) : RecyclerView.Adapter<UploadedReportAdapter.UploadedReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadedReportViewHolder {
        val binding = ItemUploadedReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UploadedReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UploadedReportViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<MedicalPdf>) {
        data = newData
        notifyDataSetChanged()
    }

    fun getItem(position: Int): MedicalPdf? {
        return if (position in data.indices) data[position] else null
    }

    inner class UploadedReportViewHolder(private val binding: ItemUploadedReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicalPdf) {
            binding.tvPdfName.text = item.fileName

            // Updated Logic: Label gets Sync Type, Value gets Sync Date
            binding.tvDeviceLabel.text = if (item.uploadType == "MANUAL") "Device" else "Email"

            binding.tvDeviceValue.text = item.details?.date ?: "NA"
            binding.tvNameValue.text = item.details?.patientName ?: "NA"
            binding.tvLabValue.text = item.details?.labNo ?: item.details?.labName ?: "NA"

            binding.tvDaysOldValue.text = item.dateSince.toString()

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
