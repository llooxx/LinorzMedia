package linorz.com.linorzmedia.main;

/**
 * Created by linorz on 2017/7/27.
 */

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

import linorz.com.linorzmedia.R;
import linorz.com.linorzmedia.customview.ServiceFloatView;
import linorz.com.linorzmedia.tools.StaticMethod;

public class LinorzService extends Service {

    private final IBinder mBinder = new LocalBinder();
    WindowManager.LayoutParams wml;
    ServiceFloatView rfv;

    @Override
    public void onCreate() {
        super.onCreate();
        createFloatView();
    }

    public class LocalBinder extends Binder {
        LinorzService getService() {
            return LinorzService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private void createFloatView() {
        wml = getDefaultSystemWindowParams(this, StaticMethod.dipTopx(this, 60));


        initButton();
    }

    public void initButton() {
        rfv = new ServiceFloatView(this, 60, 60);
        rfv.setImageResource(R.drawable.current);
        rfv.setBackgroundResource(R.drawable.blue_circle);
        rfv.initView(wml, 1, 0.5);

//        ImageView[] btns = getSubButton(null);

//        int wh = StaticMethod.dipTopx(this, 50);
//        final FloatingActionMenu centerBottomMenu = new FloatingActionMenu
//                .Builder(this, true)
//                .setStartAngle(-30)
//                .addSubActionView(btns[0], wh, wh)
//                .addSubActionView(btns[1], wh, wh)
//                .addSubActionView(btns[2], wh, wh)
//                .addSubActionView(btns[3], wh, wh)
//                .addSubActionView(btns[4], wh, wh)
//                .addSubActionView(btns[5], wh, wh)
//                .attachTo(rfv).build();

        rfv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if (rfv.canDo())
//                    centerBottomMenu.toggle(true);
//                else if (centerBottomMenu.isOpen())
//                    centerBottomMenu.close(true);
            }
        });

    }

    private ImageView[] getSubButton(ViewGroup parentView) {
        LayoutInflater inflater = LayoutInflater.from(getApplication());
        ImageView[] btns = new ImageView[6];
        for (int i = 0; i < btns.length; i++)
            btns[i] = (ImageView) inflater.inflate(R.layout.sub_btn, parentView, false).findViewById(R.id.sub_image);
        btns[0].setImageResource(R.drawable.next);
        btns[1].setImageResource(R.drawable.voice_up_white);
        btns[2].setImageResource(R.drawable.back_top);
        btns[3].setImageResource(R.drawable.voice_down_white);
        btns[4].setImageResource(R.drawable.pre);
        btns[5].setImageResource(R.drawable.btn_play_white);
        return btns;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    public static WindowManager.LayoutParams getDefaultSystemWindowParams(Context context, int size) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                size,
                size,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // z-ordering
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }
}