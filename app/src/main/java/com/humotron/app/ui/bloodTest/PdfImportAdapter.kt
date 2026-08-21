package com.humotron.app.ui.bloodTest

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.data.repository.ClinicalDocumentInfo
import com.humotron.app.databinding.ItemEmailFoundBinding
import java.text.SimpleDateFormat
import java.util.*

class PdfImportAdapter(
    private var items: List<ClinicalDocumentInfo>,
    private val onSelectionChanged: (Int) -> Unit,
) : RecyclerView.Adapter<PdfImportAdapter.PdfViewHolder>() {

    private var selectedIndex: Int? = null

    inner class PdfViewHolder(val binding: ItemEmailFoundBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val binding =
            ItemEmailFoundBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PdfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val item = items[position]

        holder.binding.tvResultName.text = item.fileName

        val sdf = SimpleDateFormat("EEE dd MMM", Locale.getDefault())
        holder.binding.tvResultMetadata.text = sdf.format(Date(item.timestamp))

        val isSelected = selectedIndex == position
        holder.binding.cbBloodTest.isChecked = isSelected

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

        holder.binding.cbBloodTest.setOnClickListener {
            holder.itemView.performClick()
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
