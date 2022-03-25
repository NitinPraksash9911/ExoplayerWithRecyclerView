package com.nitin.viewpagertest2.ui

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaDescriptionCompat
import android.util.Log
import android.view.View
import android.widget.Toast
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


    private lateinit var scrollListener: RecyclerViewScrollListener


    private lateinit var musicAdapter: MusicAdapter
    lateinit var binding: ViewPagerFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ViewPagerFragmentBinding.bind(view)
        setAdapter()

    }

    private fun setAdapter() {
        musicAdapter = MusicAdapter(onItemClickListener = {

        }, onItemClose = {
            Toast.makeText(requireContext(), "click close", Toast.LENGTH_LONG).show()
        })
        binding.recyclerView.setHasFixedSize(true)

        binding.recyclerView.adapter = musicAdapter

        PagerSnapHelper().attachToRecyclerView(binding.recyclerView)

        musicAdapter.submitList(getMusicList().map {
            it.asMetaDataDesc()
        })

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


    private fun Music.asMetaDataDesc(): MediaDescriptionCompat {
        return MediaDescriptionCompat.Builder()
            .setDescription(this.subTitle)
            .setMediaId(this.mediaId)
            .setMediaUri(Uri.parse(this.songUrl))
            .setTitle(this.title)
            .setIconUri(Uri.parse(this.defaultThumbnail))
            .build()

    }

    override fun onPause() {
        super.onPause()
        PlayerViewAdapter.pauseCurrentPlayingVideo()
    }


    override fun onDestroy() {
        super.onDestroy()
        PlayerViewAdapter.releaseAllPlayers()
    }


    private fun getMusicList() = listOf<Music>(
        Music(
            "1",
            "https://images.pexels.com/photos/4725133/pexels-photo-4725133.jpeg",
            "https://bitdash-a.akamaihd.net/content/MI201109210084_1/m3u8s/f08e80da-bf1d-4e3d-8899-f0f6155f6efa.m3u8",
            "SongId1",
            "Hls Video"
        ),
        Music(
            "2",
            "https://images.pexels.com/photos/2100063/pexels-photo-2100063.jpeg",

            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
            "SongId2",
            "Mp3 Audio"
        ),
        Music(
            "3",
            "https://images.pexels.com/photos/3348748/pexels-photo-3348748.jpeg",
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