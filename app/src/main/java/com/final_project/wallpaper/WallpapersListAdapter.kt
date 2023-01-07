package com.final_project.wallpaper

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.finalproject.model.Image
import com.final_project.wallpaper.databinding.ListSingleItemBinding

class WallpapersListAdapter(var wallpapersList: List<Image>, private val clickListener: (Image) -> Unit): RecyclerView.Adapter<WallpapersListAdapter.WallpapersViewHolder>() {

    class WallpapersViewHolder(private var binding: ListSingleItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(wallpapers: Image, clickListener: (Image) -> Unit) {
            //Load Image
            Glide.with(binding.root.context).load(wallpapers.previewUrl).listener(
                object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.listPrg.visibility = View.GONE
                        return false
                    }

                }
            ).into(binding.listImg)
            binding.root.setOnClickListener {
                clickListener(wallpapers)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WallpapersViewHolder {
        return WallpapersViewHolder(
            ListSingleItemBinding.inflate(
            LayoutInflater.from(
                parent.context
            ))
        )
    }

    override fun getItemCount(): Int {
        return wallpapersList.size
    }

    override fun onBindViewHolder(holder: WallpapersViewHolder, position: Int) {
        holder.bind(wallpapersList[position], clickListener)
    }

}