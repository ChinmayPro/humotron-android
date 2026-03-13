package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemPrefQuestionBinding
import com.humotron.app.domain.modal.response.Tag

class QuestionAdapter(val onSelected: (Int) -> Unit) : RecyclerView.Adapter<QuestionAdapter.ViewHolder>() {

    val questionList = ArrayList<Tag>()

    class ViewHolder(val binding: ItemPrefQuestionBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
        // Inflate your item layout and return a new ViewHolder instance
        return ViewHolder(
            ItemPrefQuestionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind data to the ViewHolder
        val data = questionList[holder.adapterPosition]
        holder.binding.apply {
            tvQuestion.text = data.tagName
            cbToggle.isChecked = data.isChecked

            root.setOnClickListener {
                data.isChecked = !data.isChecked
                cbToggle.isChecked = data.isChecked
                onSelected(questionList.filter { it.isChecked }.size)
            }

        }


    }

    override fun getItemCount(): Int {
        // Return the total number of items
        return questionList.size // Replace with actual item count
    }

    fun setData(data: List<Tag>) {
        questionList.clear()
        questionList.addAll(data)
        notifyDataSetChanged()
    }
}