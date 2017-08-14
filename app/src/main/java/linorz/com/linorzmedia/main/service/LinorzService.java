package linorz.com.linorzmedia.main.service;

/**
 * Created by linorz on 2017/7/27.
 */

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Binder;
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


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import linorz.com.linorzmedia.R;
import linorz.com.linorzmedia.customview.FloatingAction.FloatingActionMenu;
import linorz.com.linorzmedia.customview.FloatingAction.animation.DefaultAnimationHandler;
import linorz.com.linorzmedia.customview.ServiceFloatView;
import linorz.com.linorzmedia.main.AudioPlay;
import linorz.com.linorzmedia.main.activity.MainActivity;
import linorz.com.linorzmedia.mediatools.Audio;
import linorz.com.linorzmedia.tools.StaticMethod;

public class LinorzService extends Service {
    private WindowManager.LayoutParams wml;
    private WindowManager wm;
    private ActivityManager am;
    private static ServiceFloatView rfv;
    private ImageView play_btn;
    private AudioPlay audioPlay;
    private boolean isShowing = true;
    private Timer timer;    // 定时器，定时进行检测当前应该创建还是移除悬浮窗。
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    initButton();
                    break;
                case 2:
                    wm.removeView(rfv);
                    isShowing = false;
                    break;
            }
            return false;
        }
    });


    @Override
    public void onCreate() {
        super.onCreate();
        audioPlay = AudioPlay.instance;
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wml = getDefaultSystemWindowParams(this, StaticMethod.dipTopx(this, 60));
        am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        initButton();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔0.5秒刷新一次
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (isHome() && !isWindowShowing()) {
                        // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗。
                        handler.sendMessage(Message.obtain(handler, 1));
                    } else if (!isHome() && isWindowShowing()) {
                        // 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗。
                        handler.sendMessage(Message.obtain(handler, 2));
                    }

                }
            }, 0, 500);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean isWindowShowing() {
        return isShowing;
    }

    public void initButton() {
        isShowing = true;
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
                    if (audioPlay.isPlaying()) startOrPause(false);
                    else startOrPause(true);
                } else if (view == btns[0]) {
                    //下一个
                    if (!setAudio(++audioPlay.current_num, true)) {
                        audioPlay.current_num--;
                        Toast.makeText(LinorzService.this, "后面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                } else if (view == btns[4]) {
                    //前一个
                    if (!setAudio(--audioPlay.current_num, true)) {
                        audioPlay.current_num++;
                        Toast.makeText(LinorzService.this, "后面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                } else if (view == btns[2]) {
                    //回到顶端
                } else if (view == btns[1]) {
                    //增大音量
                    int currentVolume = StaticMethod.getCurrentVolume(LinorzService.this);
                    StaticMethod.setVolume(LinorzService.this, ++currentVolume);
                } else if (view == btns[3]) {
                    //减小音量
                    int currentVolume = StaticMethod.getCurrentVolume(LinorzService.this);
                    StaticMethod.setVolume(LinorzService.this, --currentVolume);
                }
            }
        };
        for (ImageView btn : btns) btn.setOnClickListener(onClickListener);
    }

    private boolean setAudio(int num, boolean play) {
        boolean result = audioPlay.setAudio(num, play);
        if (result) {
            if (play) {
                play_btn.setImageResource(R.drawable.btn_pause_white);
            } else {
                play_btn.setImageResource(R.drawable.btn_play_white);
            }
        }
        return result;
    }

    private void startOrPause(boolean sp) {
        if (sp) {
            play_btn.setImageResource(R.drawable.btn_pause_white);
            audioPlay.start();
        } else {
            play_btn.setImageResource(R.drawable.btn_play_white);
            audioPlay.pause();
        }
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

    // 判断当前界面是否是桌面
    @SuppressWarnings("deprecation")
    private boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    @Override
    public void onDestroy() {
        // Service被终止的同时也停止定时器继续运行
        timer.cancel();
        timer = null;
        super.onDestroy();
    }
}