package com.humotron.app.ui.onboarding.personalize.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemOnboardingUsecaseBinding
import com.humotron.app.domain.modal.response.UseCaseInterest

class UseCaseAdapter(
    private val onItemSelected: (UseCaseInterest) -> Unit
) : RecyclerView.Adapter<UseCaseAdapter.ViewHolder>() {

    private var list = listOf<UseCaseInterest>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemOnboardingUsecaseBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val data = list[position]
        holder.binding.apply {
            tvTitle.text = data.title
            tvDesc.text = data.subtitle
            cb.isChecked = data.isChecked
            root.isActivated = data.isChecked

            if (data.title?.contains("sense of my data", ignoreCase = true) == true) {
                tvTag.visibility = android.view.View.VISIBLE
                tvTag.text = "CLARITY"
            } else {
                tvTag.visibility = android.view.View.GONE
            }

            root.setOnClickListener {
                if (!data.isChecked) {
                    list.forEach { it.isChecked = false }
                    data.isChecked = true
                    notifyDataSetChanged()
                    onItemSelected(data)
                }
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemOnboardingUsecaseBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    fun setList(list: List<UseCaseInterest>) {
        this.list = list
        // Prefill default checked item if none is checked
        if (list.isNotEmpty() && list.none { it.isChecked }) {
            // Find option 1 or default to index 0
            val defaultOption = list.find { it.title?.contains("sense of my data", ignoreCase = true) == true } ?: list[0]
            defaultOption.isChecked = true
            onItemSelected(defaultOption)
        }
        notifyDataSetChanged()
    }
}