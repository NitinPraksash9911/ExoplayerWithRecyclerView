package com.nitin.viewpagertest2.utils

import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.nitin.viewpagertest2.R


@BindingAdapter(value = ["imageUri", "successCallback"], requireAll = false)
fun ImageView.loadImage(
    imgUrl: String?,
    onLoadSuccess: (resource: Drawable) -> Unit = {},
) {

    val imageToBeLoad = if (imgUrl.isNullOrEmpty()) {
        R.drawable.default_audio_image
    } else {
        imgUrl
    }


    Glide.with(this.context)
        .load(imageToBeLoad)
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        .transition(DrawableTransitionOptions.withCrossFade())
        .listener(GlideImageRequestListener(object : GlideImageRequestListener.Callback {
            override fun onFailure(message: String?) {


                Handler(Looper.getMainLooper()).post {

                    Glide.with(this@loadImage.context)
                        .load(R.drawable.default_audio_image)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .listener(GlideImageRequestListener(object :
                            GlideImageRequestListener.Callback {
                            override fun onFailure(message: String?) {

                            }

                            override fun onSuccess(dataSource: String, resource: Drawable) {
                                onLoadSuccess(resource)
                            }

                        }))
                        .into(this@loadImage)

                }
            }

            override fun onSuccess(dataSource: String, resource: Drawable) {
                Log.d("loadImage", "onSuccess:-> load from $dataSource")
                onLoadSuccess(resource)
            }

        }))
        .into(this)
}