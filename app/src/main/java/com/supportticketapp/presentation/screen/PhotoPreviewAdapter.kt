package com.supportticketapp.presentation.screen

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.supportticketapp.R

class PhotoPreviewAdapter(
    private val onRemove: (Uri) -> Unit
) : ListAdapter<Uri, PhotoPreviewAdapter.PhotoViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.ivPhoto)
        private val btnRemove: View = itemView.findViewById(R.id.btnRemove)

        fun bind(uri: Uri) {
            ivPhoto.setImageURI(uri)

            btnRemove.setOnClickListener {
                onRemove(uri)
            }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Uri>() {
            override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean = oldItem == newItem
            override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean = oldItem == newItem
        }
    }
}
