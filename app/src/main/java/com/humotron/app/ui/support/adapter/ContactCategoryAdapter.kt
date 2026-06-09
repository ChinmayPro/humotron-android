package com.humotron.app.ui.support.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.databinding.ItemContactCategoryBinding
import com.humotron.app.domain.modal.response.SupportCategory
import com.humotron.app.util.loadImage

class ContactCategoryAdapter(
    private val onCategorySelected: (SupportCategory) -> Unit
) : ListAdapter<SupportCategory, ContactCategoryAdapter.ViewHolder>(DiffCallback) {

    private var selectedCategoryKey: String? = null

    fun getSelectedCategoryKey(): String? = selectedCategoryKey

    fun setSelectedCategoryKey(key: String?) {
        val oldKey = selectedCategoryKey
        if (oldKey == key) return
        selectedCategoryKey = key

        val currentList = currentList
        for (i in currentList.indices) {
            val itemKey = currentList[i].key
            if (itemKey == oldKey || itemKey == key) {
                notifyItemChanged(i)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemContactCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemContactCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SupportCategory) {
            binding.tvTitle.text = item.label ?: ""
            binding.tvDescription.text = item.description ?: ""
            binding.ivIcon.loadImage(item.icon)

            val isSelected = item.key == selectedCategoryKey
            if (isSelected) {
                binding.root.setBackgroundResource(R.drawable.bg_contact_category_selected)
                binding.rbSelector.isChecked = true
            } else {
                binding.root.setBackgroundResource(R.drawable.bg_contact_category_unselected)
                binding.rbSelector.isChecked = false
            }

            binding.root.setOnClickListener {
                val itemKey = item.key
                if (itemKey != null) {
                    setSelectedCategoryKey(itemKey)
                    onCategorySelected(item)
                }
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<SupportCategory>() {
            override fun areItemsTheSame(oldItem: SupportCategory, newItem: SupportCategory): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: SupportCategory, newItem: SupportCategory): Boolean {
                return oldItem == newItem
            }
        }
    }
}
