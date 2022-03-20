package com.nitin.viewpagertest2.ui

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.PagerSnapHelper
import com.nitin.viewpagertest2.R
import com.nitin.viewpagertest2.data.Music
import com.nitin.viewpagertest2.databinding.ViewPagerFragmentBinding
import com.nitin.viewpagertest2.utils.PlayerViewAdapter
import com.nitin.viewpagertest2.utils.RecyclerViewScrollListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewPagerFragment : Fragment(R.layout.view_pager_fragment) {

    lateinit var songs: List<MediaDescriptionCompat>

    private lateinit var scrollListener: RecyclerViewScrollListener


    lateinit var musicAdapter: MusicAdapter
    lateinit var binding: ViewPagerFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ViewPagerFragmentBinding.bind(view)
        fetchMediaData()

        setAdapter()

    }

    private fun setAdapter() {
        musicAdapter = MusicAdapter {

        }
        binding.recyclerView.setHasFixedSize(true)

        binding.recyclerView.adapter = musicAdapter

        PagerSnapHelper().attachToRecyclerView(binding.recyclerView)
        musicAdapter.submitList(songs)

        scrollListener = object : RecyclerViewScrollListener() {
            override fun onItemIsFirstVisibleItem(index: Int) {
                Log.d("visible item index", index.toString())
                // play just visible item
                if (index != -1)
                    PlayerViewAdapter.playIndexThenPausePreviousPlayer(index)
            }

        }
        binding.recyclerView.addOnScrollListener(scrollListener)

    }


    fun fetchMediaData() {
        val allSongs = setMusicList()
        songs = allSongs.map { song ->
            MediaDescriptionCompat.Builder()
                .setDescription(song.subTitle)
                .setMediaId(song.mediaId)
                .setMediaUri(Uri.parse(song.songUrl))
                .setTitle(song.title)
                .setIconUri(Uri.parse(song.defaultThumbnail))
                .build()

        }
    }

    override fun onPause() {
        super.onPause()
        PlayerViewAdapter.releaseAllPlayers()
    }


    private fun setMusicList() = listOf<Music>(
        Music(
            "1",
            "SoundHelix-Song-5",
            "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
            "SongId1",
            "Hls Video"
        ),
        Music(
            "2",
            "SoundHelix-Song-11",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
            "SongId2",
            "Mp3 Audio"
        ),
        Music(
            "3",
            "SoundHelix-Song-14",
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-14.mp3",
            "SongId3",
            "Mp3 Audio"
        ),
        Music(
            "4",
            "BigBuckBunny MP4-1",
            "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            "SongId4",
            "Mp4 Video"
        ),


        )

}