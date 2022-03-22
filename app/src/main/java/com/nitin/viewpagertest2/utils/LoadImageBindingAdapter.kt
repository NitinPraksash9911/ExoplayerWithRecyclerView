package com.nitin.viewpagertest2.utils

import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.nitin.viewpagertest2.R


@BindingAdapter(value = ["imageUri", "successCallback"], requireAll = false)
fun ImageView.loadImage(imgUrl: Uri?, onLoadSuccess: (resource: Drawable) -> Unit = {}) {

    val requestOption = RequestOptions()
        .placeholder(R.drawable.ic_music)
        .error(R.drawable.ic_music)

    Glide.with(this.context)
        .load(imgUrl)
        .transition(DrawableTransitionOptions.withCrossFade())
        .apply(requestOption)
        .centerInside()
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .listener(GlideImageRequestListener(object : GlideImageRequestListener.Callback {
            override fun onFailure(message: String?) {
                Log.d("loadImage", "onFailure:-> $message")
            }

            override fun onSuccess(dataSource: String, resource: Drawable) {
                Log.d("loadImage", "onSuccess:-> load from $dataSource")
                onLoadSuccess(resource)
            }

        }))
        .into(this)
}