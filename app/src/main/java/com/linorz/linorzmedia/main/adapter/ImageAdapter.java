package com.linorz.linorzmedia.main.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.main.application.LinorzApplication;
import com.linorz.linorzmedia.main.activity.ZoomImage;
import com.linorz.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/5/8.
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageItem> {
    private Context context;
    private LayoutInflater inflater;
    private List<Map<String, Object>> mapList;
    private BitmapFactory.Options options;
    private int screenWidth;
    //    private int lastPosition = -1;
    private ArrayList<String> urls;

    public ImageAdapter(Context context, List<Map<String, Object>> mapList) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.mapList = mapList;
        options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        screenWidth = ((Activity) context).getWindowManager().getDefaultDisplay().getWidth();
        this.urls = new ArrayList<>();
    }

    @Override
    public ImageItem onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageItem(inflater.inflate(R.layout.image_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageItem imageItem, final int i) {
        imageItem.path = (String) mapList.get(i).get("path");
        BitmapFactory.decodeFile(imageItem.path, options);
        setWH(imageItem.img, options.outWidth, options.outHeight);
        ImageLoader.getInstance().displayImage("file://" + imageItem.path, imageItem.img, LinorzApplication.getOptions());
        urls.add("file://" + imageItem.path);

//        imageItem.img.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(context, MoreImagesActivity.class);
//                intent.putExtra("urls", urls);
//                intent.putExtra("num", i);
//                context.startActivity(intent);
//            }
//        });
    }

    private void setWH(ImageView img, int ow, int oh) {
        ViewGroup.LayoutParams lp = img.getLayoutParams();
        lp.width = StaticMethod.pxTodip(context, screenWidth - 30);
        lp.height = lp.width * oh / ow;
        img.setLayoutParams(lp);
    }


    @Override
    public int getItemCount() {
        return mapList.size();
    }

    public class ImageItem extends RecyclerView.ViewHolder {
        ImageView img;
        String path;

        public ImageItem(View view) {
            super(view);
            img = (ImageView) view.findViewById(R.id.item_img);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ZoomImage.class);
                    intent.putExtra("path", path);
                    context.startActivity(intent);
                }
            });
        }
    }
}
