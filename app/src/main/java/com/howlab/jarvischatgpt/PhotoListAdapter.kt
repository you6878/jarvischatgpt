package com.howlab.jarvischatgpt

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.howlab.jarvischatgpt.databinding.ItemPhotoBinding

class PhotoListAdapter: RecyclerView.Adapter<PhotoListAdapter.PhotoViewHolder>() {

    val items = ArrayList<Uri>()

    fun setImages(mediaList: List<Uri>) {
        items.clear()
        items.addAll(mediaList)
        notifyDataSetChanged()
    }

    fun addImage(uri: Uri) {
        items.add(uri)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PhotoViewHolder(
            ItemPhotoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    class PhotoViewHolder(private val binding: ItemPhotoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(media: Uri) {
            binding.photo.load(media)
        }
    }

}