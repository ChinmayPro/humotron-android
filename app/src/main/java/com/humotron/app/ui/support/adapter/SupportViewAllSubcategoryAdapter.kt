package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemSupportViewAllSubcategoryBinding
import com.humotron.app.domain.modal.response.SupportSubcategory
import com.humotron.app.util.loadImage

class SupportViewAllSubcategoryAdapter(
    private val onSubcategoryClick: (SupportSubcategory) -> Unit
) : RecyclerView.Adapter<SupportViewAllSubcategoryAdapter.ViewHolder>() {

    private val items = mutableListOf<SupportSubcategory>()

    fun setData(newItems: List<SupportSubcategory>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSupportViewAllSubcategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(
        private val binding: ItemSupportViewAllSubcategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SupportSubcategory, position: Int) {
            binding.tvSubcategoryTitle.text = item.label ?: ""
            binding.tvSubcategoryDesc.text = item.description ?: ""
            binding.ivSubcategoryIcon.loadImage(item.icon, R.drawable.ic_sheet_document)
            binding.ivSubcategoryIcon.setColorFilter(
                ContextCompat.getColor(binding.root.context, R.color.colorBgBtn1)
            )

            // Hide divider for the last item in the list
            binding.vDivider.visibility = if (position == itemCount - 1) View.GONE else View.VISIBLE

            binding.root.setOnClickListener {
                onSubcategoryClick(item)
            }
        }
    }
}
