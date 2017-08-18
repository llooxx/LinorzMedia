package com.linorz.linorzmedia.main.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.customview.LinorzRecyclerView;

/**
 * Created by linorz on 2016/5/9.
 */
public abstract class MediaFragment extends Fragment {
    protected View rootView;
    protected Context context;
    protected LinorzRecyclerView recyclerView;
    protected RecyclerView.Adapter adapter;
    protected List<Map<String, Object>> items;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (null != rootView) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (null != parent) parent.removeView(rootView);
        } else {
            rootView = inflater.inflate(R.layout.media_fragment, container, false);
            context = rootView.getContext();
            initAllView(rootView);
        }
        return rootView;
    }

    protected void initAllView(View view) {
        recyclerView = (LinorzRecyclerView) view.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setOnPauseListenerParams(ImageLoader.getInstance(), false, true);
        items = new ArrayList<>();
        adapter = getAdapter();
        recyclerView.setAdapter(adapter);
        load();
    }

    public void jumpTop() {
        recyclerView.smoothScrollToPosition(0);
    }

    public void jumpToPositon(int i) {
        recyclerView.scrollToPosition(i);
    }

    protected abstract RecyclerView.Adapter getAdapter();

    protected abstract void load();
}
