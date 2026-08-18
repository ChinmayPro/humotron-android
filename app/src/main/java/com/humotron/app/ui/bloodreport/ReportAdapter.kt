package com.humotron.app.ui.bloodreport

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemReportBinding
import com.humotron.app.domain.modal.response.MedicalPdf

class ReportAdapter(
    private var reports: List<MedicalPdf>,
    private val onReportSelected: (MedicalPdf) -> Unit,
) : RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val binding = ItemReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReportViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        holder.bind(reports[position], position)
    }

    override fun getItemCount(): Int = reports.size

    fun updateData(newReports: List<MedicalPdf>, initialSelectedPosition: Int = 0) {
        this.reports = newReports
        this.selectedPosition = initialSelectedPosition
        notifyDataSetChanged()
    }

    inner class ReportViewHolder(private val binding: ItemReportBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(report: MedicalPdf, position: Int) {
            binding.tvTitle.text = report.id

            val isSelected = selectedPosition == position
            val context = binding.root.context

            if (isSelected) {
                binding.mcvReport.setCardBackgroundColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.cool_80
                        )
                    )
                )
                binding.mcvReport.strokeColor = ContextCompat.getColor(context, R.color.cool_26)
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.cool))
            } else {
                binding.mcvReport.setCardBackgroundColor(
                    ColorStateList.valueOf(
                        ContextCompat.getColor(
                            context,
                            R.color.white_3
                        )
                    )
                )
                binding.mcvReport.strokeColor = ContextCompat.getColor(context, R.color.hair)
                binding.tvTitle.setTextColor(ContextCompat.getColor(context, R.color.ink3))
            }

            binding.mcvReport.setOnClickListener {
                if (selectedPosition != bindingAdapterPosition) {
                    val previousPosition = selectedPosition
                    selectedPosition = bindingAdapterPosition
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(selectedPosition)
                    onReportSelected(report)
                }
            }
        }
    }
}
