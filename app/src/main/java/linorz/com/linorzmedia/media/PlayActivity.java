package linorz.com.linorzmedia.media;

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
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import linorz.com.linorzmedia.R;
import linorz.com.linorzmedia.tools.BrightTools;
import linorz.com.linorzmedia.tools.ScreenObserver;
import linorz.com.linorzmedia.tools.StaticMethod;

/**
 * Created by linorz on 2016/5/2.
 */
public class PlayActivity extends Activity {
    private GestureDetector mGesture;//手势控制
    private ScreenObserver screenObserver;//屏幕监听
    private SurfaceView surface;
    private SurfaceHolder surfaceHolder;
    private MediaPlayer mediaPlayer;
    private Button play_btn, start_pause_btn, left_btn, right_btn;
    private ImageView lock_btn, unlock_btn;
    private SeekBar seekBar, voiceBar, screenBar;
    private TextView currentTime_tv;
    private TextView time_tv;
    private TextView selectTime_tv;
    private ViewGroup layout_top;
    private ViewGroup layout_bottom;
    private ViewGroup voice_layout;
    private ViewGroup screen_layout;
    private Timer t;
    private TimerTask tt;
    //视频各参数
    int videolength = 0, screenWidth = 0, screenHeight = 0;
    boolean isChanging = false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
    boolean canDealUp = true;//是否能处理手指抬起事件
    boolean isScreenBarShow = false, isVoiceBarShow = false, isSeekBarShow = false;//seekbar的显示状况
    boolean isHMoving = false;//是否横向移动
    boolean isLock = false;//是否锁屏
    int dealUpCount = 0;//处理手指抬起的次数
    String path;//文件路径
    int type;//视频或者音乐

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    mediaPlayer.seekTo((int) message.obj);
                    break;
                case 2:
                    mediaPlayer.seekTo((int) message.obj);
                    if (isChanging) seekBar.setProgress((int) message.obj);
                    break;
                case 3:
                    voice_layout.setVisibility(View.VISIBLE);
                    voiceBar.setProgress((int) message.obj);
                    break;
                case 4:
                    screen_layout.setVisibility(View.VISIBLE);
                    screenBar.setProgress((int) message.obj);
                    break;
                case 5:
                    System.out.println("!!!!" + dealUpCount + canDealUp);
                    if (!isChanging & dealUpCount == 1 | dealUpCount-- == 1 & canDealUp)
                        setAllVisible(false);
                    if (dealUpCount == 1) canDealUp = true;
                    break;
                case 6:
                    voice_layout.setVisibility(View.INVISIBLE);
                    break;
                case 7:
                    screen_layout.setVisibility(View.INVISIBLE);
                    break;
                case 8:
                    canDealUp = true;
                    setAllVisible(false);
                    break;
                case 9:
                    unlock_btn.setVisibility(View.INVISIBLE);
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_activity);
        seekBar = (SeekBar) findViewById(R.id.seekbar);
        currentTime_tv = (TextView) findViewById(R.id.currenttime);
        time_tv = (TextView) findViewById(R.id.time);
        surface = (SurfaceView) findViewById(R.id.surfaceview);
        surfaceHolder = surface.getHolder();//SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.setFixedSize(320, 220);//显示的分辨率,不设置为视频默认
        layout_top = (ViewGroup) findViewById(R.id.layout_top);
        layout_bottom = (ViewGroup) findViewById(R.id.layout_bottom);
        voice_layout = (ViewGroup) findViewById(R.id.voice_layout);
        screen_layout = (ViewGroup) findViewById(R.id.screen_layout);
        play_btn = (Button) findViewById(R.id.video_btn_play);
        start_pause_btn = (Button) findViewById(R.id.start_pause_btn);
        left_btn = (Button) findViewById(R.id.left_btn);
        right_btn = (Button) findViewById(R.id.right_btn);
        ImageView changeScreen = (ImageView) findViewById(R.id.change_screen_btn);
        lock_btn = (ImageView) findViewById(R.id.lock_screen_btn);
        unlock_btn = (ImageView) findViewById(R.id.unlock_screen_btn);
        selectTime_tv = (TextView) findViewById(R.id.select_time);
        TextView title_tv = (TextView) findViewById(R.id.top_title);
        voiceBar = (SeekBar) findViewById(R.id.voice_seekbar);
        screenBar = (SeekBar) findViewById(R.id.screen_seekbar);

        screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        System.out.println("!!!3:" + screenWidth + "/" + screenHeight);
        voiceBar.setMax(StaticMethod.getMaxVolume(this));
        voiceBar.setProgress(StaticMethod.getCurrentVolume(this));
        screenBar.setMax(BrightTools.getMaxScreenBrightness());
        screenBar.setProgress(BrightTools.getScreenBrightness(this));

        voiceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        screenBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
        changeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticMethod.currentDuration = seekBar.getProgress();
                System.out.println("!!!!!1:" + StaticMethod.currentDuration);
                t.cancel();
                tt.cancel();
                if (StaticMethod.mScreen)
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                else setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                StaticMethod.mScreen = !StaticMethod.mScreen;
            }
        });
        setScreenListener();//屏幕状态监听器
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
            String realPath = StaticMethod.getRealPath(this,uri);
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
        setSeekListener(seekBar);
        mGesture = new GestureDetector(this, new MyGestureDetector());
        View.OnClickListener startOrPause = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer.isPlaying()) videoPause();
                else videoStart();
            }
        };
        play_btn.setOnClickListener(startOrPause);
        start_pause_btn.setOnClickListener(startOrPause);
        left_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null && mediaPlayer.getCurrentPosition() >= 5000)
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() - 5000);
            }
        });
        right_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mediaPlayer != null && mediaPlayer.getCurrentPosition() <= videolength - 5000)
                    mediaPlayer.seekTo(mediaPlayer.getCurrentPosition() + 5000);
            }
        });
        lock_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLock = true;
                setAllVisible(false);
            }
        });
        unlock_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLock = false;
                setAllVisible(true);
                unlock_btn.setVisibility(View.INVISIBLE);
            }
        });
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
            handler.sendMessageDelayed(Message.obtain(handler, 9), 3000);
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
                    dealUpCount++;
                    canDealUp = false;
                    handler.sendMessageDelayed(Message.obtain(handler, 5), 4000);
                }
                if (isVoiceBarShow) handler.sendMessageDelayed(Message.obtain(handler, 6), 500);
                if (isScreenBarShow) handler.sendMessageDelayed(Message.obtain(handler, 7), 500);
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

    private void setSeekListener(SeekBar sb) {
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int count = 0;

            @Override
            public void onProgressChanged(final SeekBar seekBar, int i, boolean b) {
                int currentTime = seekBar.getProgress();
                String time = StaticMethod.getMusicTime(seekBar.getProgress());
                currentTime_tv.setText(time);
                selectTime_tv.setText(time);
                if (isChanging && ++count >= 5) {
                    Message.obtain(handler, 1, currentTime).sendToTarget();
                    count = 0;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                selectTime_tv.setVisibility(View.VISIBLE);
                mediaPlayer.pause();
                isChanging = true;
                canDealUp = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                selectTime_tv.setVisibility(View.INVISIBLE);
                if (isChanging) Message.obtain(handler, 1, seekBar.getProgress()).sendToTarget();
                if (isSeekBarShow) handler.sendMessageDelayed(Message.obtain(handler, 8), 3000);
                videoStart();
                isChanging = false;
            }
        });
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

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        int lastSeekProgress = -1;//上次播放进度条的位置
        int lastVoiceProgress = -1;//上次音乐进度条的位置
        int lastScreenProgress = -1;//上次亮度进度条的位置

        @Override
        public boolean onDown(MotionEvent motionEvent) {
            if (!isSeekBarShow) setAllVisible(true);
            lastSeekProgress = seekBar.getProgress();
            lastVoiceProgress = voiceBar.getProgress();
            lastScreenProgress = screenBar.getProgress();
            isVoiceBarShow = false;
            isScreenBarShow = false;
            canDealUp = false;
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            canDealUp = false;
            if (mediaPlayer.isPlaying()) videoPause();
            else videoStart();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            canDealUp = false;
            if (screenWidth - e1.getX() < screenWidth / 2 && Math.abs(distanceY) > Math.abs(distanceX) * 5) {//音乐调节
                Message.obtain(handler, 3,
                        lastVoiceProgress + (int) ((e1.getY() - e2.getY()) / screenHeight * voiceBar.getMax() * 2)
                ).sendToTarget();
                isVoiceBarShow = true;
            } else if (e1.getX() < screenWidth / 2 && Math.abs(distanceY) > Math.abs(distanceX) * 5) {//亮度调节
                Message.obtain(handler, 4,
                        lastScreenProgress + (int) ((e1.getY() - e2.getY()) / screenHeight * screenBar.getMax() * 2)
                ).sendToTarget();
                isScreenBarShow = true;
            } else if (Math.abs(distanceY) * 5 < Math.abs(distanceX)) { //进度调节
                if (mediaPlayer.isPlaying() && Math.abs(e1.getX() - e2.getX()) > 30) {
                    mediaPlayer.pause();
                    selectTime_tv.setVisibility(View.VISIBLE);
                    isChanging = true;
                    isHMoving = true;
                }
                if (isChanging && !mediaPlayer.isPlaying())
                    Message.obtain(handler, 2,
                            lastSeekProgress + (int) ((e2.getX() - e1.getX()) / screenWidth * 60000)
                    ).sendToTarget();
            }
            return true;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {
            canDealUp = false;
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
