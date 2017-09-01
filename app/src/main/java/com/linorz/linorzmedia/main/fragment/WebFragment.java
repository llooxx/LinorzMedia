package com.linorz.linorzmedia.main.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.linorz.linorzmedia.main.adapter.WebAdapter;
import com.linorz.linorzmedia.main.application.LinorzApplication;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by linorz on 2017/9/2.
 */

public class WebFragment extends MediaFragment {
    public static WebFragment instance;

    public WebFragment() {
        instance = this;
    }

    @Override
    protected RecyclerView.Adapter getAdapter() {
        recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        WebAdapter webAdapter = new WebAdapter(context, items);
        return webAdapter;
    }

    @Override
    protected void load() {
        try {
            JSONArray lovelist = LinorzApplication.lovelist;
            for (int i = 0; i < lovelist.length(); i++) {
                JSONObject jo = lovelist.getJSONObject(i);
                Map<String, Object> map = new HashMap<>();
                map.put("name", jo.getString("name"));
                map.put("url", jo.getString("url"));
                items.add(map);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void updateDate() {
        instance.items.clear();
        instance.load();
    }
}
