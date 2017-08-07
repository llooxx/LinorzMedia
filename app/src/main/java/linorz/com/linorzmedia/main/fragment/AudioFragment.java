package linorz.com.linorzmedia.main.fragment;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import linorz.com.linorzmedia.main.adapter.PlayAudio;
import linorz.com.linorzmedia.main.adapter.AudioAdapter;
import linorz.com.linorzmedia.mediatools.Audio;
import linorz.com.linorzmedia.mediatools.AudioProvider;
import linorz.com.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/5/5.
 */
public class AudioFragment extends MediaFragment {
    public ArrayList<Audio> audios;
    private PlayAudio playAudio;
    public int last_num = 0;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            int n = message.what;
            boolean k = (boolean) message.obj;

            Map<String, Object> map = items.get(n);
            map.put("isPlay", k);
            if (recyclerView.getScrollState() != RecyclerView.SCROLL_STATE_IDLE
                    || (!recyclerView.isComputingLayout())) {
                adapter.notifyItemChanged(n);
            }
            return false;
        }
    });

    public Audio getAudio(int i) {
        if (i >= 0 && i < audios.size()) {
            changeColor(last_num, false);
            last_num = i;
            changeColor(last_num, true);
            return audios.get(i);
        } else return null;
    }

    public void changeColor(int i, boolean isPlay) {
        Message message = new Message();
        message.what = i;
        message.obj = isPlay;
        handler.sendMessageDelayed(message, 0);
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
        for (int i = 0; i < audios.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("name", audios.get(i).getTitle());
            map.put("time", StaticMethod.getMusicTime(audios.get(i).getDuration()));
            map.put("path", "file://" + audios.get(i).getPath());
            map.put("isPlay", false);
            items.add(map);
        }
        adapter.notifyDataSetChanged();
    }
}
