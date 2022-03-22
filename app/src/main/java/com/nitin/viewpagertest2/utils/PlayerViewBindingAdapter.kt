package com.nitin.viewpagertest2.utils

import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.R
import com.google.android.exoplayer2.upstream.DefaultDataSource


object PlayerViewAdapter {

    // for hold all players generated
    private var playersMap: MutableMap<Int, ExoPlayer> = mutableMapOf()

    // for hold current player
    private var currentPlayingVideo: Pair<Int, ExoPlayer>? = null
    fun releaseAllPlayers() {
        playersMap.map {
            it.value.release()
        }
    }

    // call when item recycled to improve performance
    fun releaseRecycledPlayers(index: Int) {
        playersMap[index]?.release()
    }

    // call when scroll to pause any playing player
    fun pauseCurrentPlayingVideo() {
        if (currentPlayingVideo != null) {
            currentPlayingVideo?.second?.playWhenReady = false
        }
    }

    fun playCurrentVideo() {
        if (currentPlayingVideo != null) {
            currentPlayingVideo?.second?.playWhenReady = true
        }
    }

    fun playIndexThenPausePreviousPlayer(index: Int) {
        if (playersMap.get(index)?.playWhenReady == false) {
            pauseCurrentPlayingVideo()
            playersMap.get(index)?.playWhenReady = true
            currentPlayingVideo = Pair(index, playersMap.get(index)!!)
        }

    }


    /**
     * @param mediaUri is uri of media to be played
     * @param callback is a playerState callback which state of player such as ready, idle etc...
     * @param progressbar it shows the progress bar while buffering video/audio
     * @param thumbnailUri is used to show image while video/audio is buffering
     * @param item_index is current item index in viewHolder
     * @param autoPlay is used to set if we want auto play or not
     * @param songNameTV is the TextVew which show the title of media
     * */
    @JvmStatic
    @BindingAdapter(
        value = ["video_url", "on_state_change", "progressbar", "thumbnailUri", "item_index", "autoPlay", "songName"],
        requireAll = false
    )
    fun PlayerView.loadVideo(
        mediaUri: Uri,
        callback: PlayerStateCallback,
        progressbar: ProgressBar,
        thumbnailUri: Uri,
        item_index: Int? = null,
        autoPlay: Boolean = false,
        songNameTV: TextView
    ) {
        val exoPlayer = ExoPlayer.Builder(context).build()

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N) {
            exoPlayer.setVideoTextureView(TextureView(context))
        } else {
            exoPlayer.setVideoSurfaceView(SurfaceView(context))
        }

        exoPlayer.playWhenReady = autoPlay
//        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL
        // When changing track, retain the latest frame instead of showing a black screen
        setKeepContentOnPlayerReset(true)
        // Provide url to load the video from here
        val mediaSource = buildMediaSource(mediaUri, DefaultDataSource.Factory(context))
        exoPlayer.setMediaSource(mediaSource)

        exoPlayer.prepare()

        this.player = exoPlayer

        // add player with its index to map
        if (playersMap.containsKey(item_index))
            playersMap.remove(item_index)
        if (item_index != null)
            playersMap[item_index] = exoPlayer


        this.controllerShowTimeoutMs = 1500

        this.setControllerVisibilityListener {
            if (it == View.VISIBLE) {
                songNameTV.visibility = View.VISIBLE
            } else {
                songNameTV.visibility = View.GONE
            }
        }

        //set the art work for audio only
        val imageView = this.findViewById<ImageView>(R.id.exo_artwork)
        if (mediaUri.lastPathSegment!!.contains("mp3")) {
            this.useArtwork = true
            imageView.loadImage(thumbnailUri) {
                this.defaultArtwork = it
            }
        }

        this.player!!.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                this@loadVideo.keepScreenOn = isPlaying
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                this@loadVideo.context.toast("Oops! Error occurred while playing media.")
                this@loadVideo.keepScreenOn = false
                progressbar.visibility = View.GONE
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        this@loadVideo.keepScreenOn = false
                    }
                    Player.STATE_BUFFERING -> {
                        callback.onVideoBuffering(exoPlayer)
                        this@loadVideo.keepScreenOn = true
                        progressbar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        this@loadVideo.keepScreenOn = true
                        progressbar.visibility = View.GONE
                        callback.onVideoDurationRetrieved(
                            this@loadVideo.player!!.duration,
                            exoPlayer
                        )

                    }
                    Player.STATE_ENDED -> {
                        this@loadVideo.keepScreenOn = false
                        progressbar.visibility = View.GONE
                    }
                }
            }
        })

    }

    private fun buildMediaSource(
        uri: Uri,
        defaultDataSource: DefaultDataSource.Factory
    ): MediaSource {
        return if (uri.lastPathSegment!!.contains("mp3") || uri.lastPathSegment!!.contains("mp4")
        ) {
            ProgressiveMediaSource.Factory(defaultDataSource)
                .createMediaSource(MediaItem.fromUri(uri))
        } else if (uri.lastPathSegment!!.contains("m3u8")) {
            HlsMediaSource.Factory(defaultDataSource)
                .createMediaSource(MediaItem.fromUri(uri))
        } else {
            DashMediaSource.Factory(defaultDataSource)
                .createMediaSource(MediaItem.fromUri(uri));
        }
    }

    fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}