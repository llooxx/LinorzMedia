package linorz.com.linorzmedia.main;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import linorz.com.linorzmedia.mediatools.Audio;
import linorz.com.linorzmedia.mediatools.AudioProvider;
import linorz.com.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/5/5.
 */
public class AudioFragment extends MediaFragment {
    public ArrayList<Audio> audios;
    private PlayAudio playAudio;

    public Audio getAudio(int i) {
        if (i >= 0 && i < audios.size()) return audios.get(i);
        else return null;
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        audios = (ArrayList<Audio>) new AudioProvider(context).getList();
        AudioAdapter audioAdapter = new AudioAdapter(context, items);
        audioAdapter.setPlayAudioListener(new PlayAudio() {
            @Override
            public void playAudio(int i) {
                if (playAudio != null) playAudio.playAudio(i);
            }

            @Override
            public void playAudioTwo(int i) {
                if (playAudio != null) playAudio.playAudioTwo(i);
            }
        });
        return audioAdapter;
    }

    public void setPlayAudioListener(PlayAudio playAudio) {
        this.playAudio = playAudio;
    }

    @Override
    protected void load() {
        int ii;
        if (num + 20 < audios.size()) ii = 20;
        else {
            ii = audios.size() % 20;
            isEnd = true;
        }
        for (int i = num; i < num + ii; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", audios.get(i).getTitle());
            map.put("time", StaticMethod.getMusicTime(audios.get(i).getDuration()));
            map.put("path", "file://" + audios.get(i).getPath());
            items.add(map);
        }
        recyclerView.notifyMoreFinish(true);
        num = num + 20;
    }
}
