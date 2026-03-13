package com.humotron.app.ui.onboarding.personalize.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemOnboardingUsecaseBinding
import com.humotron.app.domain.modal.response.UseCaseInterest

class UseCaseAdapter : RecyclerView.Adapter<UseCaseAdapter.ViewHolder>() {

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

            root.setOnClickListener {
                data.isChecked = !data.isChecked
                cb.isChecked = data.isChecked
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
        notifyDataSetChanged()
    }
}