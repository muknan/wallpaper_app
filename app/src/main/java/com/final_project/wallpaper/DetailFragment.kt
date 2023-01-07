package com.final_project.wallpaper

import android.app.DownloadManager
import android.app.WallpaperManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.final_project.wallpaper.databinding.FragmentDetailBinding


class DetailFragment : Fragment(), View.OnClickListener {

    private var image: String? = null
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        image = DetailFragmentArgs.fromBundle(requireArguments()).wallpaperImage

        //Button to set wallpaper
        binding.dtlSetBtn.setOnClickListener(this)

        //Button to download wallpaper
        binding.dtlDownBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.dtl_set_btn -> setWallpaper()
            R.id.dtl_down_btn -> downloadWallpaper()
        }
    }

    private fun setWallpaper() {
        //Change text on set wallpaper button and disable it
        binding.dtlSetBtn.isEnabled = false
        binding.dtlSetBtn.text = "Set Wallpaper"
        binding.dtlSetBtn.setTextColor(resources.getColor(R.color.colorDark, null))

        val bitmap: Bitmap = binding.dtlImage.drawable.toBitmap()
        val task = SetWallpaperTask(requireContext(), bitmap)
        task.execute(true)
    }

    private fun downloadWallpaper() {
        //Change text on download wallpaper button and disable it
        binding.dtlDownBtn.isEnabled = false
        binding.dtlDownBtn.text = "Downloaded"
        binding.dtlDownBtn.setTextColor(resources.getColor(R.color.colorDark, null))

        val uri: String? = image
        val task = DownloadWallpaperTask(requireContext(), uri)
        task.execute(true)
    }


    //Set wallpaper
    companion object {
        class SetWallpaperTask internal constructor(private val context: Context, private val bitmap: Bitmap) :
            AsyncTask<Boolean, String, String>() {
            override fun doInBackground(vararg params: Boolean?): String? {
                val wallpaperManager: WallpaperManager = WallpaperManager.getInstance(context)
                wallpaperManager.setBitmap(bitmap)
                return "Wallpaper Set"
            }
        }

        //Download wallpaper
        class DownloadWallpaperTask internal constructor(private val context: Context, private val downloadUrl: String?):
            AsyncTask<Boolean, String, String>() {
            override fun doInBackground(vararg params: Boolean?): String? {
                val request = DownloadManager.Request(
                    Uri.parse(downloadUrl)
                )
                request.allowScanningByMediaScanner()
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "wallpaper.jpg")
                val dm = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager?
                dm!!.enqueue(request)
                return "Downloaded"
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(image != null){
            //Set image as wallpaper
            Glide.with(requireContext()).load(image).listener(
                object: RequestListener<Drawable> {
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
                        //Show Set Wallpaper and Download Buttons after image is fully loaded
                        binding.dtlSetBtn.visibility = View.VISIBLE
                        binding.dtlDownBtn.visibility = View.VISIBLE

                        //Hide Progress
                        binding.dtlWpProgress.visibility = View.INVISIBLE

                        return false
                    }

                }
            ).into(binding.dtlImage)
        }
    }

    override fun onStop() {
        super.onStop()
        Glide.with(requireContext()).clear(binding.dtlImage)
    }
}