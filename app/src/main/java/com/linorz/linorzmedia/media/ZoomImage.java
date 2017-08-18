package com.linorz.linorzmedia.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.customview.CustomSurfaceView;
import com.linorz.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/4/3.
 */
public class ZoomImage extends Activity {
    CustomSurfaceView sv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zoomimage);
        sv = (CustomSurfaceView) findViewById(R.id.zoom_image);
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
            if (bmp != null) sv.setBitmap(bmp);
            else sv.setBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.btn_start));
        }
    }
    //设置长按事件
//        sv.setLongTouch(new CustomSurfaceView.LongTouchEvent() {
//            @Override
//            public void longTouch() {
//
//            }
//        });
}