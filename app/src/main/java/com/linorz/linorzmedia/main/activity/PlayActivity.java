package com.linorz.linorzmedia.main.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.tools.BrightTools;
import com.linorz.linorzmedia.tools.ScreenObserver;
import com.linorz.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/5/2.
 */
public class PlayActivity extends Activity {
    private GestureDetector mGesture;//手势控制
    private ScreenObserver screenObserver;//屏幕监听
    private SurfaceView surface;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private Button play_btn, start_pause_btn;
    private ImageView lock_btn, unlock_btn;
    private SeekBar seekBar, volumeBar, brightnessBar;
    private TextView currentTime_tv, time_tv, selectTime_tv;
    private ViewGroup layout_top, layout_bottom, volume_layout, brightness_layout;
    private Timer t;
    private TimerTask tt;

    String path;//文件路径
    int videolength = 0, screenWidth = 0, screenHeight = 0//视频各参数
            , type;//视频或者音乐
    final int SEEKTO = 1, VOLUME_SHOW = 2, BRIGHTNESS_SHOW = 3, VOLUME_HIDE = 4,
            BRIGHTNESS_HIDE = 5, ALL_HIDE = 6, LOCK_HIDE = 7;//一些参数
    boolean isChanging = false//互斥变量，防止定时器与SeekBar拖动时进度冲突
            , isBrightnessBarShow = false, isVolumeBarShow = false, isSeekBarShow = false//seekbar的显示状况
            , isHMoving = false//是否横向移动
            , isLock = false;//是否锁屏
    long lockTime = 0//最后一次点击锁屏的时间
            , fingerUpTime = 0;//最后一次手指抬起的时间


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case SEEKTO:
                    //进度跳转
                    mediaPlayer.seekTo((int) message.obj);
                    break;
                case VOLUME_SHOW:
                    //音量调节滑动条显示
                    volume_layout.setVisibility(View.VISIBLE);
                    volumeBar.setProgress((int) message.obj);
                    break;
                case BRIGHTNESS_SHOW:
                    //屏幕亮度滑动条显示
                    brightness_layout.setVisibility(View.VISIBLE);
                    brightnessBar.setProgress((int) message.obj);
                    break;
                case VOLUME_HIDE:
                    //音量调节滑动条隐藏
                    volume_layout.setVisibility(View.INVISIBLE);
                    break;
                case BRIGHTNESS_HIDE:
                    //屏幕亮度滑动条显示
                    brightness_layout.setVisibility(View.INVISIBLE);
                    break;
                case ALL_HIDE:
                    //整体操作面板隐藏
                    if (!isChanging && isSeekBarShow && System.currentTimeMillis() >= fingerUpTime + 4000)
                        setAllVisible(false);
                    break;
                case LOCK_HIDE:
                    //锁定按钮隐藏
                    if (System.currentTimeMillis() >= lockTime + 3000)
                        unlock_btn.setVisibility(View.INVISIBLE);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // 隐藏状态栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        StaticMethod.hideBottomUIMenu(this);
        setContentView(R.layout.play_activity);
        layout_top = (ViewGroup) findViewById(R.id.layout_top);
        layout_bottom = (ViewGroup) findViewById(R.id.layout_bottom);
        volume_layout = (ViewGroup) findViewById(R.id.voice_layout);
        brightness_layout = (ViewGroup) findViewById(R.id.screen_layout);
        play_btn = (Button) findViewById(R.id.video_btn_play);
        start_pause_btn = (Button) findViewById(R.id.start_pause_btn);
        lock_btn = (ImageView) findViewById(R.id.lock_screen_btn);
        unlock_btn = (ImageView) findViewById(R.id.unlock_screen_btn);
        selectTime_tv = (TextView) findViewById(R.id.select_time);
        volumeBar = (SeekBar) findViewById(R.id.voice_seekbar);
        brightnessBar = (SeekBar) findViewById(R.id.screen_seekbar);
        TextView title_tv = (TextView) findViewById(R.id.top_title);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        currentTime_tv = (TextView) findViewById(R.id.currenttime);
        time_tv = (TextView) findViewById(R.id.time);
        surface = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceHolder = surface.getHolder();//SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.setFixedSize(320, 220);//显示的分辨率,不设置为视频默认

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        System.out.println("!!!3:" + screenWidth + "/" + screenHeight);

        if (path != null) dealPath(type, path);
        Intent intent = getIntent();
        path = intent.getStringExtra("path");
        if (path != null) {
            int start = path.lastIndexOf("/");
            int end = path.lastIndexOf(".");
            if (start != -1 && end != -1) title_tv.setText(path.substring(start + 1, end));
            else title_tv.setText("");
            type = intent.getIntExtra("type", 0);
            dealPath(type, path);
        } else {
            Uri uri = intent.getData();
            String realPath = StaticMethod.getRealPath(this, uri);
            int start = realPath.lastIndexOf("/");
            int end = realPath.lastIndexOf(".");
            if (start != -1 && end != -1) title_tv.setText(realPath.substring(start + 1, end));
            else title_tv.setText("");
            type = 1;
            dealPath(type, realPath);
        }
    }

    private void dealPath(int requestCode, String file) {
        setMedia(requestCode, file);
        setSeekListener();//进度条监听器
        setButtonListener();//按钮监听器
        setVolumeBrightnessListener();//声音亮度监听器
        setScreenListener();//屏幕状态监听器
        mGesture = new GestureDetector(this, new MyGestureDetector());//手势操作
    }

    private void setMedia(int media_select, final String filepath) {
        if (mediaPlayer != null) mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        //音频
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        if (filepath == null) return;
        try {
            mediaPlayer.setDataSource(filepath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        videolength = mediaPlayer.getDuration();
        seekBar.setMax(videolength);
        currentTime_tv.setText("00:00");
        time_tv.setText(StaticMethod.getMusicTime(videolength));
        setVideoTimeTask();//定时器记录播放进度
        //视频
        if (media_select == 1) {
            fixVideoSize();//重新调整界面大小与比例
            surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder surfaceHolder) {
                    mediaPlayer.setDisplay(surfaceHolder);//设置显示视频显示在SurfaceView上
                }

                @Override
                public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                    if (mediaPlayer != null && mediaPlayer.isPlaying())
                        mediaPlayer.stop();
                    mediaPlayer.release();
                }
            });
        }
        System.out.println("!!!!!2:" + StaticMethod.currentDuration);
        if (StaticMethod.currentDuration != -1) {
            mediaPlayer.seekTo(StaticMethod.currentDuration);
            StaticMethod.currentDuration = -1;
        }
        mediaPlayer.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isLock) {
            unlock_btn.setVisibility(View.VISIBLE);
            lockTime = System.currentTimeMillis();
            handler.sendMessageDelayed(Message.obtain(handler, LOCK_HIDE), 3000);
            return true;
        }
        boolean result = mGesture.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                result = true;
                if (isHMoving && !mediaPlayer.isPlaying()) {
                    selectTime_tv.setVisibility(View.INVISIBLE);
                    videoStart();
                    isHMoving = false;
                    isChanging = false;
                }
                if (isSeekBarShow) {
                    fingerUpTime = System.currentTimeMillis();
                    handler.sendMessageDelayed(Message.obtain(handler, ALL_HIDE), 4000);
                }
                if (isVolumeBarShow)
                    handler.sendMessageDelayed(Message.obtain(handler, VOLUME_HIDE), 500);
                if (isBrightnessBarShow)
                    handler.sendMessageDelayed(Message.obtain(handler, BRIGHTNESS_HIDE), 500);
                break;
            default:
                break;
        }
        return result;
    }

    private void fixVideoSize() {
        int width = mediaPlayer.getVideoWidth();
        int height = mediaPlayer.getVideoHeight();
        System.out.println("!!!!" + width + "/" + height);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        ViewGroup.LayoutParams lp = surface.getLayoutParams();
        lp.width = dm.widthPixels;
        lp.height = lp.width * height / width;
        surface.setLayoutParams(lp);
    }

    private void setVideoTimeTask() {
        t = new Timer();
        tt = new TimerTask() {
            @Override
            public void run() {
                if (!isChanging) seekBar.setProgress(mediaPlayer.getCurrentPosition());
            }
        };
        t.schedule(tt, 0, 10);
    }

    private void setAllVisible(boolean visible) {
        int canVisile = visible ? View.VISIBLE : View.INVISIBLE;
        layout_bottom.setVisibility(canVisile);
        layout_top.setVisibility(canVisile);
        if (visible) {
            layout_bottom.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_in_bottom));
            layout_top.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_in_top));
        } else {
            layout_bottom.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_out_bottom));
            layout_top.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.slide_out_top));
        }
        isSeekBarShow = visible;
    }

    private void videoStart() {
        play_btn.setVisibility(View.INVISIBLE);
        StaticMethod.isPlayBtnShow = false;
        start_pause_btn.setBackgroundResource(R.drawable.btn_pause);
        mediaPlayer.start();
    }

    private void videoPause() {
        play_btn.setVisibility(View.VISIBLE);
        StaticMethod.isPlayBtnShow = true;
        start_pause_btn.setBackgroundResource(R.drawable.btn_start);
        mediaPlayer.pause();
    }

    private void setButtonListener() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.change_screen_btn:
                        //横竖屏切换
                        StaticMethod.currentDuration = seekBar.getProgress();
                        System.out.println("!!!!!1:" + StaticMethod.currentDuration);
                        t.cancel();
                        tt.cancel();
                        if (StaticMethod.mScreen)
                            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        StaticMethod.mScreen = !StaticMethod.mScreen;
                        break;
                    case R.id.video_btn_play:
                    case R.id.start_pause_btn:
                        //暂停播放
                        if (mediaPlayer.isPlaying()) videoPause();
                        else videoStart();
                        break;
                    case R.id.right_btn:
                        //前进5秒
                        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() <= videolength - 5000)
                            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
                        break;
                    case R.id.left_btn:
                        //后退5秒
                        if (mediaPlayer != null && mediaPlayer.getCurrentPosition() >= 5000)
                            mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
                        break;
                    case R.id.lock_screen_btn:
                        //锁屏
                        isLock = true;
                        setAllVisible(false);
                        break;
                    case R.id.unlock_screen_btn:
                        //解除锁屏
                        isLock = false;
                        unlock_btn.setVisibility(View.INVISIBLE);
                        setAllVisible(true);
                        break;
                }
                fingerUpTime = System.currentTimeMillis();
                handler.sendMessageDelayed(Message.obtain(handler, ALL_HIDE), 4000);
            }
        };
        findViewById(R.id.change_screen_btn).setOnClickListener(listener);
        findViewById(R.id.left_btn).setOnClickListener(listener);
        findViewById(R.id.right_btn).setOnClickListener(listener);
        play_btn.setOnClickListener(listener);
        start_pause_btn.setOnClickListener(listener);
        lock_btn.setOnClickListener(listener);
        unlock_btn.setOnClickListener(listener);
    }

    private void setVolumeBrightnessListener() {
        volumeBar.setMax(StaticMethod.getMaxVolume(this));
        volumeBar.setProgress(StaticMethod.getCurrentVolume(this));
        brightnessBar.setMax(BrightTools.getMaxScreenBrightness());
        brightnessBar.setProgress(BrightTools.getScreenBrightness(this));

        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                StaticMethod.setVolume(PlayActivity.this, seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                BrightTools.setBrightness(PlayActivity.this, seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void setScreenListener() {
        screenObserver = new ScreenObserver(this);
        ScreenObserver.ScreenStateListener stateListener = new ScreenObserver.ScreenStateListener() {
            @Override
            public void onScreenOn() {//屏幕亮
            }

            @Override
            public void onScreenOff() {//屏幕暗
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    t.cancel();
                    tt.cancel();
                }
            }

            @Override
            public void onUserPresent() {//屏幕解锁
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    setVideoTimeTask();
                    if (!StaticMethod.isPlayBtnShow) mediaPlayer.start();
                }
            }
        };
        screenObserver.startObserver(stateListener);
    }

    private void setSeekListener() {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int count = 0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, int i, boolean b) {
                int currentTime = seekBar.getProgress();
                String time = StaticMethod.getMusicTime(seekBar.getProgress());
                currentTime_tv.setText(time);
                selectTime_tv.setText(time);
                if (isChanging && ++count >= 5) {
                    Message.obtain(handler, SEEKTO, currentTime).sendToTarget();
                    count = 0;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                selectTime_tv.setVisibility(View.VISIBLE);
                mediaPlayer.pause();
                isChanging = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                selectTime_tv.setVisibility(View.INVISIBLE);
                if (isChanging)
                    Message.obtain(handler, SEEKTO, seekBar.getProgress()).sendToTarget();
                if (isSeekBarShow) {
                    fingerUpTime = System.currentTimeMillis();
                    handler.sendMessageDelayed(Message.obtain(handler, ALL_HIDE), 4000);
                }
                videoStart();
                isChanging = false;
            }
        });
    }

    private class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        int lastSeekProgress = -1;//上次播放进度条的位置
        int lastVoiceProgress = -1;//上次音乐进度条的位置
        int lastScreenProgress = -1;//上次亮度进度条的位置

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            if (!isSeekBarShow) setAllVisible(true);
            lastSeekProgress = seekBar.getProgress();
            lastVoiceProgress = volumeBar.getProgress();
            lastScreenProgress = brightnessBar.getProgress();
            isVolumeBarShow = false;
            isBrightnessBarShow = false;
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mediaPlayer.isPlaying()) videoPause();
            else videoStart();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (screenWidth - e1.getX() < screenWidth / 2 && Math.abs(distanceY) > Math.abs(distanceX) * 5) {//音乐调节
                Message.obtain(handler, VOLUME_SHOW,
                        lastVoiceProgress + (int) ((e1.getY() - e2.getY()) / screenHeight * volumeBar.getMax() * 2)
                ).sendToTarget();
                isVolumeBarShow = true;
            } else if (e1.getX() < screenWidth / 2 && Math.abs(distanceY) > Math.abs(distanceX) * 5) {//亮度调节
                Message.obtain(handler, BRIGHTNESS_SHOW,
                        lastScreenProgress + (int) ((e1.getY() - e2.getY()) / screenHeight * brightnessBar.getMax() * 2)
                ).sendToTarget();
                isBrightnessBarShow = true;
            } else if (Math.abs(distanceY) * 5 < Math.abs(distanceX)) { //进度调节
                if (mediaPlayer.isPlaying() && Math.abs(e1.getX() - e2.getX()) > 30) {
                    mediaPlayer.pause();
                    selectTime_tv.setVisibility(View.VISIBLE);
                    isChanging = true;
                    isHMoving = true;
                }
                if (isChanging && !mediaPlayer.isPlaying()) {
                    int k = lastSeekProgress + (int) ((e2.getX() - e1.getX()) / screenWidth * 60000);
                    Message.obtain(handler, SEEKTO, k).sendToTarget();
                    seekBar.setProgress(k);
                }
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            if (mediaPlayer.isPlaying()) videoPause();
            else videoStart();
        }
    }

    //退出按钮计时
    long lastBackPressed = 0;

    @SuppressLint("ShowToast")
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastBackPressed < 1000) {
            t.cancel();
            tt.cancel();
            mediaPlayer.stop();
            finish();
            StaticMethod.currentDuration = -1;
        } else Toast.makeText(this, "再按一次退出", 1000).show();
        lastBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onDestroy() {
        screenObserver.shutdownObserver();
        t.cancel();
        tt.cancel();
        StaticMethod.currentDuration = seekBar.getProgress();
        super.onDestroy();
    }
}
