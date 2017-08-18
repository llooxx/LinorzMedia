package com.linorz.linorzmedia.main.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.media.PlayActivity;
import com.linorz.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/5/3.
 */
public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoItem> {
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> mapList;
    private PlayAudio playAudio;

    public VideoAdapter(Context context, List<Map<String, Object>> mapList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mapList = mapList;
    }

    @Override
    public VideoItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoItem(inflater.inflate(R.layout.video_item, parent, false));
    }

    @Override
    public void onBindViewHolder(VideoItem videoItem, int i) {
        Object object = mapList.get(i).get("img");
        if (object != null)
            videoItem.img.setImageBitmap((Bitmap) object);
        videoItem.name.setText((String) mapList.get(i).get("name"));
        videoItem.time.setText((String) mapList.get(i).get("time"));
        videoItem.path = (String) mapList.get(i).get("path");
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public void setPlayAudioListener(PlayAudio playAudio) {
        this.playAudio = playAudio;
    }

    public class VideoItem extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, time;
        String path;

        public VideoItem(View view) {
            super(view);
            img = (ImageView) view.findViewById(R.id.item_img);
            name = (TextView) view.findViewById(R.id.item_name);
            time = (TextView) view.findViewById(R.id.item_time);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (playAudio != null) playAudio.playAudio(0);
                    Intent intent = new Intent(context, PlayActivity.class);
                    intent.putExtra("path", path);
                    intent.putExtra("type", 1);
                    StaticMethod.currentDuration = -1;
                    context.startActivity(intent);
                }
            });
        }
    }
}
