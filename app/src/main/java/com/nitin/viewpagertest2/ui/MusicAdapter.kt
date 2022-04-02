package com.nitin.viewpagertest2.ui

import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.Player
import com.nitin.viewpagertest2.databinding.MusicItemBinding
import com.nitin.viewpagertest2.utils.PlayerStateCallback
import com.nitin.viewpagertest2.utils.PlayerViewAdapter

const val TAG = "MusicAdapter"
class MusicAdapter(
    private val onItemClickListener: (MediaDescriptionCompat) -> Unit,
    private val onItemClose: () -> Unit
) : ListAdapter<MediaDescriptionCompat, MusicAdapter.MusicViewHolder>(MusicDiff),
    PlayerStateCallback {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = MusicItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MusicViewHolder(binding, onItemClickListener, onItemClose)
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        val music = getItem(position)
        holder.bind(music)
    }

    inner class MusicViewHolder(
        private val musicItemBinding: MusicItemBinding,
        private val onItemClickListener: (MediaDescriptionCompat) -> Unit,
        private val onItemClose: () -> Unit,
    ) : RecyclerView.ViewHolder(musicItemBinding.root) {

        init {
            musicItemBinding.root.setOnClickListener {
                onItemClickListener(musicItemBinding.dataModel!!)
            }
            musicItemBinding.closeIconIV
                .setOnClickListener {
                    onItemClose.invoke()
                }
        }

        fun bind(music: MediaDescriptionCompat) {
            musicItemBinding.apply {
                dataModel = music
                callback = this@MusicAdapter
                index = absoluteAdapterPosition

                executePendingBindings()
            }
        }
    }

    override fun onViewRecycled(holder: MusicViewHolder) {
        val position = holder.absoluteAdapterPosition
        PlayerViewAdapter.releaseRecycledPlayers(position)
        super.onViewRecycled(holder)
    }

    object MusicDiff : DiffUtil.ItemCallback<MediaDescriptionCompat>() {
        override fun areItemsTheSame(
            oldItem: MediaDescriptionCompat,
            newItem: MediaDescriptionCompat
        ): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(
            oldItem: MediaDescriptionCompat,
            newItem: MediaDescriptionCompat
        ): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }
    }

    override fun onVideoDurationRetrieved(duration: Long, player: Player) {
        Log.d(TAG, "onVideoDurationRetrieved: $duration")
    }

    override fun onVideoBuffering(player: Player) {
        Log.d(TAG, "onVideoBuffering: is buffuring")
    }

    override fun onStartedPlaying(player: Player) {
        Log.d(TAG, "onStartedPlaying: is started playing")
    }

    override fun onFinishedPlaying(player: Player) {
        Log.d(TAG, "onFinishedPlaying: finish")
    }
}
