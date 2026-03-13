package com.humotron.app.ui.bioHack.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.humotron.app.R

class TagAdapter : RecyclerView.Adapter<TagAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate your tag item layout here
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Bind your tag data to the view holder here
    }

    override fun getItemCount(): Int {
        // Return the number of tags
        return 3 // Replace with actual count
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Initialize your tag views here
    }
}