package com.humotron.app.ui.onboarding.personalize.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemStartAreaBinding

data class StartAreaItem(
    val id: String,
    val title: String,
    val iconResId: Int,
    var isSelected: Boolean,
    val description: String
)

class StartAreasAdapter : RecyclerView.Adapter<StartAreasAdapter.ViewHolder>() {

    private var list = listOf<StartAreaItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemStartAreaBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = list[position]
        holder.binding.apply {
            tvTitle.text = data.title
            ivIcon.setImageResource(data.iconResId)
            root.isActivated = data.isSelected

            val context = root.context
            val tintColor = if (data.isSelected) {
                ContextCompat.getColor(context, R.color.colorBgBtn) // #C4F23E
            } else {
                ContextCompat.getColor(context, R.color.white)
            }
            ivIcon.imageTintList = ColorStateList.valueOf(tintColor)

            root.setOnClickListener {
                data.isSelected = !data.isSelected
                notifyItemChanged(position)
            }

            root.setOnLongClickListener {
                Toast.makeText(context, data.description, Toast.LENGTH_SHORT).show()
                true
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<StartAreaItem>) {
        this.list = list
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<StartAreaItem> {
        return list.filter { it.isSelected }
    }

    class ViewHolder(val binding: ItemStartAreaBinding) : RecyclerView.ViewHolder(binding.root)
}
