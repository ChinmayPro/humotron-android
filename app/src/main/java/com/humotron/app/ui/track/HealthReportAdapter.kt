package com.humotron.app.ui.track

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemHealthReportBinding
import com.humotron.app.domain.modal.response.MedicalPdf

class HealthReportAdapter(
    private val mContext: Context,
    private var data: List<MedicalPdf>,
    private val onItemClick: (MedicalPdf, Action) -> Unit,
) : RecyclerView.Adapter<HealthReportAdapter.HealthReportViewHolder>() {

    enum class Action {
        VIEW, DELETE, ITEM_CLICK
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthReportViewHolder {
        val binding = ItemHealthReportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HealthReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HealthReportViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<MedicalPdf>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class HealthReportViewHolder(private val binding: ItemHealthReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MedicalPdf) {
            binding.tvPdfName.text = item.fileName
            
            val subtitleText = buildString {
                val lab = item.details?.labName
                val date = item.details?.date
                if (!lab.isNullOrEmpty()) append(lab)
                if (!lab.isNullOrEmpty() && !date.isNullOrEmpty()) append(" • ")
                if (!date.isNullOrEmpty()) append(date)
                
                if (isEmpty() && !item.metrics.isNullOrEmpty()) {
                    append(item.metrics.take(2).joinToString(" • ") { it.metricName ?: "" })
                }
            }
            binding.tvSubtitle.text = subtitleText
            
            // Sync status
            binding.llSynced.alpha = if (item.isSynced) 1.0f else 0.5f

            binding.tvView.setOnClickListener {
                onItemClick(item, Action.VIEW)
            }

            binding.ivDelete.setOnClickListener {
                onItemClick(item, Action.DELETE)
            }

            binding.root.setOnClickListener {
                onItemClick(item, Action.ITEM_CLICK)
            }
        }
    }
}
