package com.humotron.app.ui.support.adapter

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.humotron.app.databinding.ItemReviewAttachmentBinding
import java.io.File

class ReviewAttachmentAdapter :
    ListAdapter<Uri, ReviewAttachmentAdapter.ViewHolder>(UriDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReviewAttachmentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemReviewAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(uri: Uri) {
            val context = binding.root.context
            
            // Load preview using Glide
            Glide.with(context)
                .load(uri)
                .centerCrop()
                .into(binding.ivAttachmentPreview)

            // Extract file metadata (name and size)
            val fileInfo = getFileInfo(context, uri)
            binding.tvFileName.text = fileInfo.first
            binding.tvFileSize.text = fileInfo.second
        }
    }

    private fun getFileInfo(context: Context, uri: Uri): Pair<String, String> {
        var name = "attachment"
        var sizeStr = "0.0 MB"
        
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        name = it.getString(nameIndex)
                    }
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex != -1) {
                        val sizeBytes = it.getLong(sizeIndex)
                        val sizeMb = sizeBytes.toDouble() / (1024 * 1024)
                        sizeStr = String.format("%.1f MB", sizeMb)
                    }
                }
            }
        } else if (uri.scheme == "file") {
            uri.path?.let { path ->
                val file = File(path)
                name = file.name
                val sizeBytes = file.length()
                val sizeMb = sizeBytes.toDouble() / (1024 * 1024)
                sizeStr = String.format("%.1f MB", sizeMb)
            }
        } else if (uri.scheme == "http" || uri.scheme == "https") {
            val path = uri.path.orEmpty()
            name = path.substringAfterLast("/")
            sizeStr = "Cloud File"
        }
        
        // Truncate name if it's too long
        if (name.length > 18) {
            val extension = name.substringAfterLast(".", "")
            val nameWithoutExt = name.substringBeforeLast(".")
            if (nameWithoutExt.length > 10) {
                name = nameWithoutExt.take(8) + "..." + nameWithoutExt.takeLast(4) + if (extension.isNotEmpty()) ".$extension" else ""
            }
        }
        
        return Pair(name, sizeStr)
    }

    class UriDiffCallback : DiffUtil.ItemCallback<Uri>() {
        override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean =
            oldItem.toString() == newItem.toString()
    }
}
