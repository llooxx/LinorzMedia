package linorz.com.linorzmedia.main.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import linorz.com.linorzmedia.R;
import linorz.com.linorzmedia.customview.FloatingAction.FloatingActionMenu;
import linorz.com.linorzmedia.customview.FloatingAction.animation.DefaultAnimationHandler;
import linorz.com.linorzmedia.customview.MyPageTransformer;
import linorz.com.linorzmedia.customview.RandomFloatView;
import linorz.com.linorzmedia.main.AudioPlay;
import linorz.com.linorzmedia.main.service.LinorzService;
import linorz.com.linorzmedia.main.adapter.PagerAdapter;
import linorz.com.linorzmedia.main.adapter.PlayAudio;
import linorz.com.linorzmedia.main.fragment.AudioFragment;
import linorz.com.linorzmedia.main.fragment.ImageFragment;
import linorz.com.linorzmedia.main.fragment.MediaFragment;
import linorz.com.linorzmedia.main.fragment.VideoFragment;
import linorz.com.linorzmedia.media.PlayActivity;
import linorz.com.linorzmedia.mediatools.Audio;
import linorz.com.linorzmedia.tools.StaticMethod;

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;
    private List<MediaFragment> list;
    private VideoFragment videoFragment;
    private AudioFragment audioFragment;
    private ImageFragment imageFragment;
    private TextView audio_title, audio_author, audio_state;
    private ImageView play_btn;
    private SeekBar audio_seekbar;
    private Timer t;
    private TimerTask tt;
    private RotateAnimation animation;
    private AudioPlay audioPlay;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = StaticMethod.checkSelfPermissionArray(this, new String[]{
                    Manifest.permission.SYSTEM_ALERT_WINDOW,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
            if (permissions.length > 0) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
        setContentView(R.layout.activity_main);
        initView();
        viewPager.setCurrentItem(1);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                initFab();//开线程时，已经绘制完成，能获得view的宽高
//                audioPlay.init();
                audioPlay.current_num = audioPlay.getLastAudioNum();
                setAudio(audioPlay.current_num, false);
            }
        }, 500);
    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        audio_title = (TextView) findViewById(R.id.main_audio_title);
        audio_author = (TextView) findViewById(R.id.main_audio_author);
        audio_state = (TextView) findViewById(R.id.main_audio_state);
        audio_seekbar = (SeekBar) findViewById(R.id.audio_seekbar);
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setPageTransformer(true, new MyPageTransformer());
        viewPager.setOffscreenPageLimit(2);
        list = new ArrayList<>();
        List<String> list_title = new ArrayList<>();
        videoFragment = new VideoFragment();
        audioFragment = new AudioFragment();
        imageFragment = new ImageFragment();
        list.add(videoFragment);
        list.add(audioFragment);
        list.add(imageFragment);
        audioPlay = audioFragment.getAudioPlay();

        list_title.add("video");
        list_title.add("audio");
        list_title.add("image");
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), list, list_title);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);

        videoFragment.setPlayAudioListener(new PlayAudio() {
            @Override
            public void playAudio(int i) {
                if (audioPlay.isPlaying()) startOrPause(false);
            }

            @Override
            public void playAudioTwo(int i) {

            }
        });
        audioFragment.setPlayAudioListener(new PlayAudio() {
            @Override
            public void playAudio(int i) {
                if (i == audioPlay.current_num) return;
                setAudio(i, true);
            }

            @Override
            public void playAudioTwo(int i) {
                if (audioPlay.isPlaying()) startOrPause(false);
                Audio audio = audioPlay.getAudio(i);
                StaticMethod.currentDuration = -1;
                Intent intent = new Intent(MainActivity.this, PlayActivity.class);
                intent.putExtra("path", "file://" + audio.getPath());
                intent.putExtra("type", 2);
                MainActivity.this.startActivity(intent);
            }
        });
    }

    private void initFab() {
        FrameLayout frame = (FrameLayout) findViewById(R.id.fram);

        final RandomFloatView rfv = new RandomFloatView(frame.getContext(), 60, 60);
        rfv.setImageResource(R.drawable.current);
        rfv.setBackgroundResource(R.drawable.blue_circle);
        rfv.initView(frame, 1, 0.5);

        ImageView[] btns = getSubButton(frame);
        final FloatingActionMenu centerBottomMenu = new FloatingActionMenu.Builder(this)
                .setStartAngle(-30)
                .setAnimationHandler(new DefaultAnimationHandler()
                        .setDuration(100).setBetweenTime(10))
                .addSubActionView(btns[0])
                .addSubActionView(btns[1])
                .addSubActionView(btns[2])
                .addSubActionView(btns[3])
                .addSubActionView(btns[4])
                .addSubActionView(btns[5])
                .attachTo(rfv).build();

        rfv.setOnClickListener(null);
        rfv.setAction(new RandomFloatView.Action() {
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
                else
                    rfv.startAnimation(animation);
                last_time = time;
            }
        });
        initBtnListener(btns);
        play_btn = btns[5];
        //设置动画
        setAnimation(0);
    }

    private void setAnimation(float degree) {
        animation = new RotateAnimation(0f + degree, 359f + degree,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(-1);
        animation.setDuration(3000);
    }

    private ImageView[] getSubButton(ViewGroup parentView) {
        LayoutInflater inflater = LayoutInflater.from(this);
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
                        Toast.makeText(MainActivity.this, "后面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                } else if (view == btns[4]) {
                    //前一个
                    if (!setAudio(--audioPlay.current_num, true)) {
                        audioPlay.current_num++;
                        Toast.makeText(MainActivity.this, "后面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                } else if (view == btns[2]) {
                    //回到顶端
                    list.get(viewPager.getCurrentItem()).jumpTop();
                } else if (view == btns[1]) {
                    //增大音量
                    int currentVolume = StaticMethod.getCurrentVolume(MainActivity.this);
                    StaticMethod.setVolume(MainActivity.this, ++currentVolume);
                } else if (view == btns[3]) {
                    //减小音量
                    int currentVolume = StaticMethod.getCurrentVolume(MainActivity.this);
                    StaticMethod.setVolume(MainActivity.this, --currentVolume);
                }
            }
        };
        for (ImageView btn : btns) btn.setOnClickListener(onClickListener);
    }

    private boolean setAudio(int num, boolean play) {
        boolean result = audioPlay.setAudio(num, play);
        if (result) {
            Audio audio = audioPlay.getCurrentAudio();
            //跳转
            audioFragment.jumpToPositon(num);
            audioFragment.changeAudio(num);
            audio_title.setText(audio.getTitle());
            audio_author.setText(audio.getArtist());
            if (play) {
                audio_state.setText("正在播放");
                play_btn.setImageResource(R.drawable.btn_pause_white);
            } else {
                audio_state.setText("已暂停");
                play_btn.setImageResource(R.drawable.btn_play_white);
            }
            setVideoTimeTask();
        }
        return result;
    }

    private void setVideoTimeTask() {
        if (t != null) t.cancel();
        if (tt != null) tt.cancel();
        audio_seekbar.setMax(audioPlay.getDuration());
        t = new Timer();
        tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    audio_seekbar.setProgress(audioPlay.getCurrentPosition());
                } catch (Exception e) {
                    t.cancel();
                    tt.cancel();
                }
            }
        };
        t.schedule(tt, 0, 10);
    }

    private void startOrPause(boolean sp) {
        if (sp) {
            play_btn.setImageResource(R.drawable.btn_pause_white);
            audio_state.setText("正在播放");
            audioPlay.start();
        } else {
            play_btn.setImageResource(R.drawable.btn_play_white);
            audio_state.setText("已暂停");
            audioPlay.pause();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent2 = new Intent();
        switch (item.getItemId()) {
            case R.id.select_music:
                intent2.setType("audio/*");
                intent2.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
                break;
            case R.id.select_video:
                intent2.setType("video/*");
                intent2.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
                break;
            case R.id.start_floatbtn:
                //启动悬浮窗
                intent = new Intent(this, LinorzService.class);
                startService(intent);
                finish();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (intent != null) {
            stopService(intent);
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String file = StaticMethod.getRealPath(this, uri);
            Intent intent = new Intent(this, PlayActivity.class);
            intent.putExtra("path", file);
            intent.putExtra("type", requestCode);
            StaticMethod.currentDuration = -1;
            startActivity(intent);
        }
    }


    @SuppressLint("ShowToast")
    @Override
    public void onBackPressed() {
        Snackbar.make(viewPager, "是否退出应用？", Snackbar.LENGTH_SHORT).setAction("是", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (t != null) t.cancel();
                if (tt != null) tt.cancel();
                audioPlay.stop();
                finish();
            }
        }).show();
    }
}