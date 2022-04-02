package com.nitin.viewpagertest2.utils

import android.net.Uri

sealed class MusicType {
    object Mp3OrMp4 : MusicType()
    object HLS : MusicType()
    object Dash : MusicType()
}

fun getMusicType(mediaUri: Uri): MusicType {
    return if (mediaUri.lastPathSegment!!.contains("mp3")
        || mediaUri.lastPathSegment!!.contains(
            "mp4"
        )
    ) {
        MusicType.Mp3OrMp4

    } else if (mediaUri.lastPathSegment!!.contains("m3u8")) {
        MusicType.HLS
    } else {
        MusicType.Dash
    }

}