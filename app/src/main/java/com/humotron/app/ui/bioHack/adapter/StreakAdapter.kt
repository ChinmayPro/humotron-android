package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.databinding.ItemBiohackStreakBinding
import com.humotron.app.domain.modal.response.BioHackProgressResponse

class StreakAdapter : RecyclerView.Adapter<StreakAdapter.ViewHolder>() {

    var list = arrayListOf<BioHackProgressResponse.Data.Streak>()


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            ItemBiohackStreakBinding.inflate(
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

            tvCount.text = "${data.count ?: 0}"
            tvDate.text = data.formatDate
            tvDay.text = data.day
            if((data.count?:0)==0){
                root.alpha = 0.5f
            }else{
                root.alpha = 1f
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(val binding: ItemBiohackStreakBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    fun setData(list: List<BioHackProgressResponse.Data.Streak>) {
        this.list = list as ArrayList<BioHackProgressResponse.Data.Streak>
        notifyDataSetChanged()
    }
}