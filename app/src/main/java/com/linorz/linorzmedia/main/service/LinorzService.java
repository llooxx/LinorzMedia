package com.linorz.linorzmedia.main.service;

/**
 * Created by linorz on 2017/7/27.
 */

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.customview.FloatingAction.FloatingActionMenu;
import com.linorz.linorzmedia.customview.FloatingAction.animation.DefaultAnimationHandler;
import com.linorz.linorzmedia.customview.ServiceFloatView;
import com.linorz.linorzmedia.mediatools.AudioPlay;
import com.linorz.linorzmedia.tools.StaticMethod;

public class LinorzService extends Service {
    private WindowManager.LayoutParams wml;
    private WindowManager wm;
    private ActivityManager am;
    private static ServiceFloatView rfv;
    private ImageView play_btn;
    private AudioPlay audioPlay;
    private AudioPlay.AudioListener audioListener;
    public static LinorzService instance;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    initButton();
                    break;
                case 2:
                    wm.removeView(rfv);
                    break;
            }
            return false;
        }
    });

    public void sendMessage(int what) {
        instance.handler.sendMessage(Message.obtain(instance.handler, what));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wml = getDefaultSystemWindowParams(this, StaticMethod.dipTopx(this, 60));
        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        initButton();
        audioPlay = AudioPlay.instance;
        audioListener = new AudioPlay.AudioListener() {
            @Override
            public void start() {
                play_btn.setImageResource(R.drawable.btn_pause_white);
            }

            @Override
            public void pause() {
                play_btn.setImageResource(R.drawable.btn_play_white);
            }

            @Override
            public void changeAudio(boolean play) {

            }

            @Override
            public void changeVolume(float volume) {

            }
        };
        audioPlay.addAudioListener(audioListener);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void initButton() {
        rfv = new ServiceFloatView(this, 60, 60);
        rfv.setImageResource(R.drawable.current);
        rfv.setBackgroundResource(R.drawable.blue_circle);
        rfv.initView(wml, 1, 0.5);

        ImageView[] btns = getSubButton(null);

        int wh = StaticMethod.dipTopx(this, 50);
        final FloatingActionMenu centerBottomMenu = new FloatingActionMenu
                .Builder(this, true)
                .setStartAngle(-18)
                .setAnimationHandler(new DefaultAnimationHandler()
                        .setDuration(100).setBetweenTime(10))
                .addSubActionView(btns[0], wh, wh)
                .addSubActionView(btns[1], wh, wh)
                .addSubActionView(btns[3], wh, wh)
                .addSubActionView(btns[4], wh, wh)
                .addSubActionView(btns[5], wh, wh)
                .attachTo(rfv).build();

        rfv.setOnClickListener(null);
        rfv.setAction(new ServiceFloatView.Action() {
            long time = 0, last_time = 0;

            @Override
            public void up() {
                time = System.currentTimeMillis();
                if ((time - last_time) < 1000 && rfv.canDo() && !centerBottomMenu.isOpen())
                    centerBottomMenu.open(true);
                last_time = time;
            }

            @Override
            public void down() {
                time = System.currentTimeMillis();
                if (rfv.canDo() && centerBottomMenu.isOpen())
                    centerBottomMenu.close(true);
                last_time = time;
            }
        });
        initBtnListener(btns);
        play_btn = btns[5];
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

    private void initBtnListener(final ImageView[] btns) {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == btns[5]) {
                    //播放暂停
                    if (audioPlay.isPlaying()) audioPlay.pause();
                    else audioPlay.start();
                } else if (view == btns[0]) {
                    //下一个
                    audioPlay.playNext();
                } else if (view == btns[4]) {
                    //前一个
                    audioPlay.playPrevious();
                } else if (view == btns[2]) {
                    //回到顶端
                } else if (view == btns[1]) {
                    //增大音量
                    audioPlay.volumeUp();
                } else if (view == btns[3]) {
                    //减小音量
                    audioPlay.volumeDown();
                }
            }
        };
        for (ImageView btn : btns) btn.setOnClickListener(onClickListener);
    }

    public WindowManager.LayoutParams getDefaultSystemWindowParams(Context context, int size) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                size,
                size,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT, // z-ordering
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        params.windowAnimations = android.R.style.Animation_Translucent;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }

    @Override
    public void onDestroy() {
        handler.sendMessage(Message.obtain(handler, 2));
        audioPlay.removeAudioListener(audioListener);
        super.onDestroy();
    }
}