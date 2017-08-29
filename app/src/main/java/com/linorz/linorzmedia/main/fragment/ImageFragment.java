package com.linorz.linorzmedia.main.fragment;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.linorz.linorzmedia.main.adapter.ImageAdapter;
import com.linorz.linorzmedia.mediatools.Image;
import com.linorz.linorzmedia.mediatools.ImageProvider;

/**
 * Created by linorz on 2016/5/8.
 */
public class ImageFragment extends MediaFragment {
    ArrayList<Image> images;

    @Override
    protected RecyclerView.Adapter getAdapter() {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        images = (ArrayList<Image>) new ImageProvider(context).getList();
        return new ImageAdapter(this, items);
    }

    @Override
    protected void load() {
        for (int i = 0; i < images.size(); i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("path", images.get(i).getPath());
            items.add(map);
        }
        adapter.notifyDataSetChanged();
    }
}
