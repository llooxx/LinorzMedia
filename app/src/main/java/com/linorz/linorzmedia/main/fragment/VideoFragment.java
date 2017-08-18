package com.linorz.linorzmedia.main.fragment;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.linorz.linorzmedia.main.adapter.PlayAudio;
import com.linorz.linorzmedia.main.adapter.VideoAdapter;
import com.linorz.linorzmedia.tools.StaticMethod;
import com.linorz.linorzmedia.mediatools.Video;
import com.linorz.linorzmedia.mediatools.VideoProvider;

/**
 * Created by linorz on 2016/5/5.
 */
public class VideoFragment extends MediaFragment {
    ArrayList<Video> videos;
    private PlayAudio playAudio;

    @Override
    protected RecyclerView.Adapter getAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        videos = (ArrayList<Video>) new VideoProvider(context).getList();
        VideoAdapter videoAdapter = new VideoAdapter(context, items);
        videoAdapter.setPlayAudioListener(new PlayAudio() {
            @Override
            public void playAudio(int i) {
                if (playAudio != null) playAudio.playAudio(i);
            }

            @Override
            public void playAudioTwo(int i) {
            }
        });
        return videoAdapter;
    }

    public void setPlayAudioListener(PlayAudio playAudio) {
        this.playAudio = playAudio;
    }

    @Override
    protected void load() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < videos.size(); i++) {
                    final int ii = i;
                    Map<String, Object> map = new HashMap<>();
                    map.put("img", StaticMethod.createVideoThumbnail(videos.get(i).getPath()));
                    map.put("name", videos.get(i).getTitle());
                    map.put("time", StaticMethod.getMusicTime(videos.get(i).getDuration()));
                    map.put("path", "file://" + videos.get(i).getPath());
                    items.add(map);
                    adapter.notifyItemInserted(ii);
                }
            }
        }).start();
    }
}
