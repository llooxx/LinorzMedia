package com.linorz.linorzmedia.main.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.main.adapter.WebAdapter;
import com.linorz.linorzmedia.main.application.LinorzApplication;
import com.linorz.linorzmedia.tools.MessageTools;

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
        LinearLayout layout = (LinearLayout) rootView.findViewById(R.id.media_layout);
        View view = LayoutInflater.from(context).inflate(R.layout.web_top, layout, false);
        layout.addView(view, 0);
        final EditText editText = (EditText) view.findViewById(R.id.web_top_edit);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String url1 = editText.getText().toString();
                    if (url1.contains("http://") || url1.contains("https://"))
                        MessageTools.ToWebActivityURL(context, url1);
                    else MessageTools.ToWebActivityURL(context, "http://" + url1);
                    return true;
                }
                return false;
            }
        });
        Button button = (Button) view.findViewById(R.id.web_top_btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url1 = editText.getText().toString();
                if (url1.contains("http://") || url1.contains("https://"))
                    MessageTools.ToWebActivityURL(context, url1);
                else MessageTools.ToWebActivityURL(context, "http://" + url1);
            }
        });

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
