package com.humotron.app.ui.track

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemReportTrackBinding
import com.humotron.app.domain.modal.response.MedicalPdf

class HealthReportTrackAdapter(
    private val context: Context,
    private var data: List<MedicalPdf>,
    private val onViewReportClick: (MedicalPdf, Int, List<MedicalPdf>) -> Unit,
) : RecyclerView.Adapter<HealthReportTrackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReportTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], position)
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<MedicalPdf>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemReportTrackBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicalPdf, position: Int) {
            binding.tvReportName.text = item.fileName
            
            val metricsCount = item.metrics?.size ?: 0
            binding.tvReportStatus.text = context.getString(R.string.data_points_synced_format, metricsCount)

            binding.tvViewReport.setOnClickListener {
                onViewReportClick(item, position, data)
            }
        }
    }
}
