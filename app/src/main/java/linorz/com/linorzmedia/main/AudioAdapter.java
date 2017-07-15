package linorz.com.linorzmedia.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import linorz.com.linorzmedia.R;

/**
 * Created by linorz on 2016/5/5.
 */
public class AudioAdapter extends RecyclerView.Adapter<AudioAdapter.AudioItem> {
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> mapList;
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
        audioItem.time.setText((String) mapList.get(i).get("time"));
        audioItem.path = (String) mapList.get(i).get("path");
        audioItem.i = i;
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public void setPlayAudioListener(PlayAudio playAudio) {
        this.playAudio = playAudio;
    }

    public class AudioItem extends RecyclerView.ViewHolder {
        TextView name, time;
        String path;
        int i;

        public AudioItem(final View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.item_name);
            time = (TextView) view.findViewById(R.id.item_time);
            final ImageView play = (ImageView) view.findViewById(R.id.item_play);
            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (playAudio == null) return;
                    if (v == view) playAudio.playAudioTwo(i);
                    else if (v == play) playAudio.playAudio(i);
                }
            };
            view.setOnClickListener(onClickListener);
            play.setOnClickListener(onClickListener);
        }
    }
}
