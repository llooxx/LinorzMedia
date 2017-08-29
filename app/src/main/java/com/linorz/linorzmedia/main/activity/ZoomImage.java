package com.linorz.linorzmedia.main.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/4/3.
 */
public class ZoomImage extends SwipeBackActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoomimage);
        imageView = (ImageView) findViewById(R.id.zoom_image);
        findViewById(R.id.zoom_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        //Bitmap设置
        Intent intent = getIntent();
        String path = intent.getStringExtra("path");
        if (path != null) {
            Bitmap bmp = StaticMethod.getDiskBitmap(path);
            if (bmp != null) imageView.setImageBitmap(bmp);
            else imageView.setImageResource(R.drawable.btn_start);
        }
    }
}