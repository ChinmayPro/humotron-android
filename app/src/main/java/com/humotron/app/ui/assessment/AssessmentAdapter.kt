package com.humotron.app.ui.assessment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemAssessmentBinding
import com.humotron.app.domain.modal.response.MergedAssessment

class AssessmentAdapter(
    private val mContext: Context,
    private var data: List<MergedAssessment>,
    private val onItemClick: (MergedAssessment) -> Unit,
) : RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentViewHolder {
        val binding = ItemAssessmentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AssessmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssessmentViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newData: List<MergedAssessment>) {
        data = newData
        notifyDataSetChanged()
    }

    inner class AssessmentViewHolder(private val binding: ItemAssessmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MergedAssessment) {
            binding.tvAssessName.text = item.assessmentName
            binding.tvAssessMeta.text = item.assessmentDetails?.alertCriteria ?: ""
            binding.tvAssessPillLabel.text = item.status

            val progress = if (item.totalQuestions > 0) {
                (item.answeredCount * 100) / item.totalQuestions
            } else 0
            binding.pbAssessProgress.progress = progress
            binding.tvAssessProgressFraction.text = "${item.answeredCount}/${item.totalQuestions}"

            // Status pill colors
            val (pillColor, textColor) = when (item.status) {
                "Completed" -> Pair(R.color.good_14, R.color.good)
                "Resume", "Start Now" -> Pair(R.color.lime_14, R.color.lime)
                else -> Pair(R.color.white10, R.color.white)
            }
            binding.pillAssessStatus.setCardBackgroundColor(ContextCompat.getColor(mContext, pillColor))
            binding.tvAssessPillLabel.setTextColor(ContextCompat.getColor(mContext, textColor))

            // Accent and Icon colors based on name or status
            val accentColor = when {
                item.assessmentName.contains("Sleep", true) -> R.color.lime
                item.assessmentName.contains("Cardio", true) -> R.color.series
                else -> R.color.watch
            }
            binding.vAssessAccent.setBackgroundColor(ContextCompat.getColor(mContext, accentColor))
            binding.cvAssessIconBg.setCardBackgroundColor(ContextCompat.getColor(mContext, accentColor).run {
                androidx.core.graphics.ColorUtils.setAlphaComponent(this, 30)
            })
            binding.ivAssessIcon.setColorFilter(ContextCompat.getColor(mContext, accentColor))
            binding.pbAssessProgress.progressTintList = ColorStateList.valueOf(ContextCompat.getColor(mContext, accentColor))

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}
