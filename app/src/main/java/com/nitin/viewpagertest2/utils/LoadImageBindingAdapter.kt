package com.nitin.viewpagertest2.utils

import android.graphics.drawable.Drawable
import android.net.Uri
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
        .centerInside()

    Glide.with(this.context)
        .load(imgUrl)
        .transition(DrawableTransitionOptions.withCrossFade())
        .centerCrop()
        .apply(requestOption)
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .listener(MyImageRequestListener(object : MyImageRequestListener.Callback {
            override fun onFailure(message: String?) {
//                Toast.makeText(this@loadImage.context, message, Toast.LENGTH_LONG).show()
            }

            override fun onSuccess(dataSource: String, resource: Drawable) {
//                Toast.makeText(this@loadImage.context, dataSource, Toast.LENGTH_LONG).show()
                onLoadSuccess(resource)
            }

        }))
        .into(this)
}