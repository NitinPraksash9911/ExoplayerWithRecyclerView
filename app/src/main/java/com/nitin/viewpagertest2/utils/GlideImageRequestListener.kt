package com.nitin.viewpagertest2.utils

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition

private const val ANIM_DURATION = 1000

class GlideImageRequestListener(private val callback: Callback? = null) : RequestListener<Drawable> {

    interface Callback {
        fun onFailure(message: String?)
        fun onSuccess(dataSource: String, resource: Drawable)
    }

    override fun onLoadFailed(
        e: GlideException?,
        model: Any?,
        target: Target<Drawable>?,
        isFirstResource: Boolean
    ): Boolean {
        callback?.onFailure(e?.message)
        return false
    }

    override fun onResourceReady(
        resource: Drawable?,
        model: Any?,
        target: Target<Drawable>?,
        dataSource: DataSource?,
        isFirstResource: Boolean
    ): Boolean {
        resource?.let {
            target?.onResourceReady(
                it,
                DrawableCrossFadeTransition(ANIM_DURATION, isFirstResource)
            )
        }
        callback?.onSuccess(dataSource.toString(), resource!!)
        return true
    }
}
