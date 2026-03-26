package com.humotron.app.ui.track

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemAssessmentsBinding
import com.humotron.app.domain.modal.response.MergedAssessment

class AssessmentAdapter(
    private val mContext: Context,
    private var data: List<MergedAssessment>,
    private val onItemClick: (MergedAssessment) -> Unit,
) : RecyclerView.Adapter<AssessmentAdapter.AssessmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssessmentViewHolder {
        val binding = ItemAssessmentsBinding.inflate(
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

    inner class AssessmentViewHolder(private val binding: ItemAssessmentsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MergedAssessment) {
            binding.tvAssessmentName.text = item.assessmentName
            binding.tvQuestion.text = "${item.answeredCount}/${item.totalQuestions} questions"
            binding.tvStatus.text = item.status
            Log.e("TAG", "bindwsddddsdddd:  ${item}", )

            when (item.status) {
                "Completed" -> binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.d700
                    )
                )

                "Resume" -> binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.colorBgBtn1
                    )
                )

                "Start Now" -> binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.colorBgBtn1
                    )
                )

                else -> binding.tvStatus.setTextColor(
                    ContextCompat.getColor(
                        mContext,
                        R.color.white
                    )
                )
            }

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }
    }
}