package linorz.com.linorzmedia.main;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import linorz.com.linorzmedia.mediatools.Image;
import linorz.com.linorzmedia.mediatools.ImageProvider;

/**
 * Created by linorz on 2016/5/8.
 */
public class ImageFragment extends MediaFragment {
    ArrayList<Image> images;

    @Override
    protected RecyclerView.Adapter getAdapter() {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        images = (ArrayList<Image>) new ImageProvider(context).getList();
        return new ImageAdapter(context, items);
    }

    @Override
    protected void load() {
        int ii;
        if (num + 20 < images.size()) ii = 20;
        else {
            ii = images.size() % 20;
            isEnd = true;
        }
        for (int i = num; i < num + ii; i++) {
            Map<String, Object> map = new HashMap<>();
            map.put("path", images.get(i).getPath());
            items.add(map);
        }
        recyclerView.notifyMoreFinish(true);
        num = num + 20;
    }
}
