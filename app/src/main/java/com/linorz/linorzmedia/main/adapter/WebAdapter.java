package com.linorz.linorzmedia.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.main.application.LinorzApplication;
import com.linorz.linorzmedia.tools.MessageTools;

import java.util.List;
import java.util.Map;

import static com.linorz.linorzmedia.main.application.LinorzApplication.lovelist;

/**
 * Created by linorz on 2017/9/2.
 */

public class WebAdapter extends RecyclerView.Adapter<WebAdapter.WebItem> {
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> mapList;

    public WebAdapter(Context context, List<Map<String, Object>> mapList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mapList = mapList;
    }

    @Override
    public WebAdapter.WebItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new WebAdapter.WebItem(inflater.inflate(R.layout.web_item, parent, false));
    }

    @Override
    public void onBindViewHolder(WebAdapter.WebItem item, int i) {
        item.url = (String) mapList.get(i).get("url");
        item.name.setText((String) mapList.get(i).get("name"));
        item.i = i;
    }

    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public void remove(int positon) {
        lovelist.remove(positon);
        LinorzApplication.getInstance().saveLoveList();
        mapList.remove(positon);
        notifyItemRemoved(positon);
    }


    public class WebItem extends RecyclerView.ViewHolder {
        TextView name;
        String url;
        int i;

        public WebItem(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.item_name);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MessageTools.ToWebActivityURL(context, url);
                }
            });
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    remove(i);
                    return false;
                }
            });
        }
    }
}
