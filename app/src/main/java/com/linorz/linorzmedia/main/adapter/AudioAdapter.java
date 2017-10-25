package com.linorz.linorzmedia.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.tools.MessageTools;

/**
 * Created by linorz on 2016/5/5.
 */
public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioItem> {
    private Context context;
    private LayoutInflater inflater;
    public List<Map<String, Object>> mapList;
    private PlayAudio playAudio;

    public AudioAdapter(Context context, List<Map<String, Object>> mapList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mapList = mapList;
    }

    @Override
    public AudioItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioItem(inflater.inflate(R.layout.audio_item, parent, false));
    }

    @Override
    public void onBindViewHolder(AudioItem audioItem, int i) {
        audioItem.name.setText((String) mapList.get(i).get("name"));
        audioItem.artist.setText((String) mapList.get(i).get("artist"));
        audioItem.path = (String) mapList.get(i).get("path");
        audioItem.i = i;
        if ((boolean) mapList.get(i).get("isPlay")) {
            audioItem.view.setBackgroundResource(R.color.transparentBlue);
            audioItem.name.setTextColor(0xffffffff);
        } else {
            audioItem.view.setBackgroundResource(R.color.transparent);
            audioItem.name.setTextColor(0xff696969);
        }
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public void setPlayAudioListener(PlayAudio playAudio) {
        this.playAudio = playAudio;
    }

    public class AudioItem extends RecyclerView.ViewHolder {
        TextView name,artist;
        String path;
        View view;
        int i;

        public AudioItem(final View view) {
            super(view);
            this.view = view;
            name = (TextView) view.findViewById(R.id.item_name);
            artist = (TextView) view.findViewById(R.id.item_artist);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (playAudio != null) {
                        playAudio.playAudio(i);
                    }

                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MessageTools.ToWebActivity(context,name.getText());
                    return false;
                }
            });
        }
    }
}
