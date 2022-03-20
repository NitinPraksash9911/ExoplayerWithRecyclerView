package com.nitin.viewpagertest2.utils

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
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
import com.google.android.exoplayer2.ui.StyledPlayerView
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

    fun playIndexThenPausePreviousPlayer(index: Int) {
        if (playersMap.get(index)?.playWhenReady == false) {
            pauseCurrentPlayingVideo()
            playersMap.get(index)?.playWhenReady = true
            currentPlayingVideo = Pair(index, playersMap.get(index)!!)
        }

    }

    /*
    *  url is a url of stream video
    *  progressbar for show when start buffering stream
    * thumbnail for show before video start
    * */
    @JvmStatic
    @BindingAdapter(
        value = ["video_url", "on_state_change", "progressbar", "thumbnail", "item_index", "autoPlay"],
        requireAll = false
    )
    fun StyledPlayerView.loadVideo(
        mediaUri: Uri,
        callback: PlayerStateCallback,
        progressbar: ProgressBar,
        thumbnail: ImageView,
        item_index: Int? = null,
        autoPlay: Boolean = false
    ) {
        val player = ExoPlayer.Builder(context).build()

        player.playWhenReady = autoPlay
        player.repeatMode = Player.REPEAT_MODE_ALL
        // When changing track, retain the latest frame instead of showing a black screen
        setKeepContentOnPlayerReset(true)
        // We'll show the controller, change to true if want controllers as pause and start
        this.useController = false
        // Provide url to load the video from here
        val mediaSource = buildMediaSource(mediaUri, DefaultDataSource.Factory(context))
        player.setMediaSource(mediaSource)

        player.prepare()
        this.player = player

        // add player with its index to map
        if (playersMap.containsKey(item_index))
            playersMap.remove(item_index)
        if (item_index != null)
            playersMap[item_index] = player

        this.player!!.addListener(object : Player.Listener {

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                this@loadVideo.context.toast("Oops! Error occurred while playing media.")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                when (playbackState) {
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_BUFFERING -> {
                        callback.onVideoBuffering(player)
                        // Buffering..
                        // set progress bar visible here
                        // set thumbnail visible
                        thumbnail.visibility = View.VISIBLE
                        progressbar.visibility = View.VISIBLE
                    }
                    Player.STATE_READY -> {
                        if (playbackState == Player.STATE_READY && player.playWhenReady) {
                            // [PlayerView] has started playing/resumed the video
                            callback.onStartedPlaying(player)
                        } else {
                            // [PlayerView] has fetched the video duration so this is the block to hide the buffering progress bar
                            progressbar.visibility = View.GONE
                            // set thumbnail gone
                            thumbnail.visibility = View.GONE
                            callback.onVideoDurationRetrieved(
                                this@loadVideo.player!!.duration,
                                player
                            )
                        }

                    }
                    Player.STATE_ENDED -> {
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

    // extension function for show toast
    fun Context.toast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

}