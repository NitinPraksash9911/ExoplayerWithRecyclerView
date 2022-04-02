package com.nitin.viewpagertest2.utils

import android.animation.LayoutTransition
import android.content.Context
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
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
import com.google.android.exoplayer2.ui.R
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource


object PlayerViewAdapter {
    private const val CONTROLS_HIDE_SHOW_DURATION = 1000

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
        if (playersMap[index]?.playWhenReady == false) {
            pauseCurrentPlayingVideo()
            playersMap[index]?.playWhenReady = true
            currentPlayingVideo = Pair(index, playersMap[index]!!)
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
    fun StyledPlayerView.loadVideo(
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


        this.controllerShowTimeoutMs = CONTROLS_HIDE_SHOW_DURATION

        this.addLayoutTransitionLister(songNameTV)

        this.loadArtWorkIfMp3(mediaUri, thumbnailUri)

        this.player!!.addPlayerListener(this, progressbar, callback, exoPlayer)

    }


    private fun StyledPlayerView.loadArtWorkIfMp3(mediaUri: Uri, thumbnailUri: Uri) {
        try {
            val imageView = this.findViewById<ImageView>(R.id.exo_artwork)
            if (mediaUri.lastPathSegment!!.contains("mp3")) {
                this.useArtwork = true
                imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
                imageView.loadImage(
                    thumbnailUri.toString(),
                    onLoadSuccess = {
                        this.defaultArtwork = it
                    }
                )
            }
        } catch (e: Resources.NotFoundException) {
        }
    }

    private fun Player.addPlayerListener(
        playerView: StyledPlayerView,
        progressbar: ProgressBar,
        callback: PlayerStateCallback,
        exoPlayer: ExoPlayer
    ) {
        this.addListener(object : Player.Listener {

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)

                playerView.keepScreenOn = isPlaying

                if (isPlaying) {

                    callback.onStartedPlaying(this@addPlayerListener)
                    playerView.controllerHideOnTouch = true

                } else {
                    playerView.controllerHideOnTouch = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                playerView.keepScreenOn = false
                progressbar.visibility = View.GONE
                if (error.errorCode == PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED) {
                    playerView.context.toast("Oops! No Network")
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_IDLE -> {
                        playerView.keepScreenOn = false
                    }
                    Player.STATE_BUFFERING -> {
                        callback.onVideoBuffering(exoPlayer)
                        playerView.keepScreenOn = true
                        progressbar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        playerView.keepScreenOn = true
                        progressbar.visibility = View.GONE
                        callback.onVideoDurationRetrieved(
                            playerView.player!!.duration,
                            exoPlayer
                        )

                    }
                    Player.STATE_ENDED -> {
                        playerView.keepScreenOn = false
                        progressbar.visibility = View.GONE
                        callback.onFinishedPlaying(this@addPlayerListener)
                        pause()
                        seekTo(0)
                    }
                }
            }
        })
    }

    private fun StyledPlayerView.addLayoutTransitionLister(songNameTV: TextView) {

        this.layoutTransition.addTransitionListener(object : LayoutTransition.TransitionListener {
            override fun startTransition(
                p0: LayoutTransition?,
                p1: ViewGroup?,
                p2: View?,
                transitionType: Int
            ) {
                if (transitionType == LayoutTransition.DISAPPEARING) {
                    songNameTV.animate().alpha(0F)
                        .setDuration(CONTROLS_HIDE_SHOW_DURATION.toLong() - 500).start()
                }
            }

            override fun endTransition(p0: LayoutTransition?, p1: ViewGroup?, p2: View?, p3: Int) {

            }

        })

        this.setControllerVisibilityListener {
            if (it == View.VISIBLE) {
                songNameTV.animate().alpha(1F)
                    .setDuration(CONTROLS_HIDE_SHOW_DURATION.toLong() - 500).start()
            }
        }
    }


    private fun buildMediaSource(
        uri: Uri,
        defaultDataSource: DefaultDataSource.Factory
    ): MediaSource {
        return when (getMusicType(uri)) {
            MusicType.Dash -> {
                DashMediaSource.Factory(defaultDataSource)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
            MusicType.HLS -> {
                HlsMediaSource.Factory(defaultDataSource)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
            MusicType.Mp3OrMp4 -> {
                ProgressiveMediaSource.Factory(defaultDataSource)
                    .createMediaSource(MediaItem.fromUri(uri))
            }
        }

    }


    fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }
}