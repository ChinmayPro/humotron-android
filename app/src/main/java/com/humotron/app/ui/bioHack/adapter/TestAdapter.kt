package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemBiohackTestBinding
import com.humotron.app.domain.modal.response.BioHackProgressResponse

class TestAdapter : RecyclerView.Adapter<TestAdapter.ViewHolder>() {

    var list = arrayListOf<BioHackProgressResponse.Data.TestPack>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemBiohackTestBinding.inflate(
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
            tvTag.text = data.primaryId?.tagName
            tvTitle.text = data.testName
            tvLevel.text = data.difficulty
            tvQuestionNumber.text =
                root.context.getString(R.string.d_questions, data.totalQuestion ?: "-")
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemBiohackTestBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    fun setData(list: List<BioHackProgressResponse.Data.TestPack>) {
        this.list = list as ArrayList<BioHackProgressResponse.Data.TestPack>
        notifyDataSetChanged()
    }
}