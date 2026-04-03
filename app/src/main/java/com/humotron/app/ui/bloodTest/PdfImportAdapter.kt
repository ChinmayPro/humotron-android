package com.humotron.app.ui.bloodTest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.data.repository.ClinicalDocumentInfo
import com.humotron.app.databinding.ItemPdfImportBinding
import java.text.SimpleDateFormat
import java.util.*

class PdfImportAdapter(
    private var items: List<ClinicalDocumentInfo>,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<PdfImportAdapter.PdfViewHolder>() {

    private var selectedIndex: Int? = null

    inner class PdfViewHolder(val binding: ItemPdfImportBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val binding = ItemPdfImportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PdfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val item = items[position]
        
        holder.binding.tvFileName.text = item.fileName
        
        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
        holder.binding.tvDate.text = sdf.format(Date(item.timestamp))
        
        val isSelected = selectedIndex == position
        holder.binding.rootView.setBackgroundResource(
            if (isSelected) R.drawable.bg_pdf_item_selected else R.drawable.bg_pdf_item_unselected
        )
        
        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (selectedIndex == pos) {
                selectedIndex = null
            } else {
                val previous = selectedIndex
                selectedIndex = pos
                previous?.let { notifyItemChanged(it) }
            }
            notifyItemChanged(pos)
            onSelectionChanged(if (selectedIndex != null) 1 else 0)
        }
    }

    override fun getItemCount(): Int = items.size

    fun getSelectedItems(): List<ClinicalDocumentInfo> {
        return selectedIndex?.let { listOf(items[it]) } ?: emptyList()
    }

    fun updateData(newItems: List<ClinicalDocumentInfo>) {
        this.items = newItems
        this.selectedIndex = null
        notifyDataSetChanged()
        onSelectionChanged(0)
    }
}
