package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemBiohackProgressBinding
import com.humotron.app.domain.modal.response.BioHackProgressResponse

class LearningProgressAdapter : RecyclerView.Adapter<LearningProgressAdapter.ViewHolder>() {

    var list = arrayListOf<BioHackProgressResponse.Data.PrimaryTagScore>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemBiohackProgressBinding.inflate(
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

            tvTitle.text = data.primaryTagName
            tvDesc.text = data.categoryName
            tvLevel.text = "${data.percentage ?: 0}"
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemBiohackProgressBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    fun setData(list: List<BioHackProgressResponse.Data.PrimaryTagScore>) {
        this.list = list as ArrayList<BioHackProgressResponse.Data.PrimaryTagScore>
        notifyDataSetChanged()
    }
}