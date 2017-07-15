package linorz.com.linorzmedia.main;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import linorz.com.linorzmedia.tools.StaticMethod;
import linorz.com.linorzmedia.mediatools.Video;
import linorz.com.linorzmedia.mediatools.VideoProvider;

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
        int ii;
        if (num + 20 < videos.size()) ii = 20;
        else {
            ii = videos.size() % 20;
            isEnd = true;
        }
        for (int i = num; i < num + ii; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("img", StaticMethod.createVideoThumbnail(videos.get(i).getPath()));
            map.put("name", videos.get(i).getTitle());
            map.put("time", StaticMethod.getMusicTime(videos.get(i).getDuration()));
            map.put("path", "file://" + videos.get(i).getPath());
            items.add(map);
        }
        recyclerView.notifyMoreFinish(true);
        num = num + 20;
    }
}
