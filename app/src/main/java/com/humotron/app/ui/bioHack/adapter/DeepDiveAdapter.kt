package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemDeepDiveBinding
import com.humotron.app.domain.modal.response.NuggetDetailResponse

class DeepDiveAdapter : RecyclerView.Adapter<DeepDiveAdapter.ViewHolder>() {

    var list = arrayListOf<NuggetDetailResponse.NuggetDetail.Nugget.DeepDive>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemDeepDiveBinding.inflate(
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
            tvSubTitle.text = data.subtitle
            tvContent.text = data.content
            tvSource.text = data.source
            tvPrompt1.text = data.prompt1
            tvPrompt2.text = data.prompt2
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemDeepDiveBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    fun setData(list: List<NuggetDetailResponse.NuggetDetail.Nugget.DeepDive>) {
        this.list = list as ArrayList<NuggetDetailResponse.NuggetDetail.Nugget.DeepDive>
        notifyDataSetChanged()
    }
}