package com.humotron.app.ui.decode.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R
import com.humotron.app.domain.modal.response.SuggestedAction

class DecodeSuggestedActionsAdapter(
    private var items: List<SuggestedAction>
) : RecyclerView.Adapter<DecodeSuggestedActionsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvActionTitle)
        val tvDesc: TextView = view.findViewById(R.id.tvActionDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_suggested_action, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title ?: ""
        holder.tvDesc.text = item.description ?: ""
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<SuggestedAction>) {
        this.items = newItems
        notifyDataSetChanged()
    }
}
