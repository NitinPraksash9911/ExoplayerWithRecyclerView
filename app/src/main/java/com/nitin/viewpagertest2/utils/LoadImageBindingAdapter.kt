package com.nitin.viewpagertest2.utils

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.nitin.viewpagertest2.R

class LoadImageBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter(value = ["thumbnail", "error"], requireAll = false)
        fun loadImage(view: ImageView, profileImage: Uri?, error: Int) {
            if (profileImage.toString().isNotEmpty()) {
                    Glide.with(view.context)
                        .setDefaultRequestOptions(
                            RequestOptions()
                                .placeholder(R.drawable.white_background)
//                                .error(R.drawable.white_background)
                        )
                        .load(profileImage)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(view)
                }
        }
    }
}