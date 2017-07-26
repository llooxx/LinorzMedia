package linorz.com.linorzmedia.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.animation.DefaultAnimationHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import linorz.com.linorzmedia.R;
import linorz.com.linorzmedia.customview.MyPageTransformer;
import linorz.com.linorzmedia.customview.RandomFloatView;
import linorz.com.linorzmedia.media.PlayActivity;
import linorz.com.linorzmedia.mediatools.Audio;
import linorz.com.linorzmedia.tools.StaticMethod;

public class MainActivity extends AppCompatActivity {
    public static DisplayImageOptions mOptions;
    private ViewPager viewPager;
    private List<MediaFragment> list;
    private MediaPlayer main_player;
    private TextView audio_title, audio_author, audio_state;
    private ImageView play_btn;
    private SeekBar audio_seekbar;
    private Timer t;
    private TimerTask tt;
    private int current_audio_num = 0;
    private SharedPreferences mySharedPreferences;
    private SharedPreferences.Editor editor;
    private Audio current_audio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = StaticMethod.checkSelfPermissionArray(this, new String[]{
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION,
//                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            });
            if (permissions.length > 0) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            }
        }
        setContentView(R.layout.activity_main);
        //缓存
        mySharedPreferences = getSharedPreferences("LinorzMedia", Context.MODE_PRIVATE);
        editor = mySharedPreferences.edit();
        initImageLoader(this);
        initView();
        initFab();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                String path = mySharedPreferences.getString("lastAudioPath", null);
                if (path == null) {
                    Audio first_audio = ((AudioFragment) list.get(1)).getAudio(current_audio_num);
                    setAudio(first_audio, false);
                } else {
                    Audio first_audio = new Audio();
                    first_audio.setPath(path);
                    first_audio.setArtist(mySharedPreferences.getString("lastAudioAuthor", "获取失败"));
                    first_audio.setTitle(mySharedPreferences.getString("lastAudioTitle", "获取失败"));
                    current_audio_num = mySharedPreferences.getInt("lastAudioNum", 0);
                    setAudio(first_audio, false);
                }
            }
        }, 1000);
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
        list.add(new VideoFragment());
        list.add(new AudioFragment());
        list.add(new ImageFragment());

        list_title.add("video");
        list_title.add("audio");
        list_title.add("image");
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(), list, list_title);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setupWithViewPager(viewPager);

        ((VideoFragment) list.get(0)).setPlayAudioListener(new PlayAudio() {
            @Override
            public void playAudio(int i) {
                if (main_player != null && main_player.isPlaying()) startOrPause(false);
            }

            @Override
            public void playAudioTwo(int i) {

            }
        });
        ((AudioFragment) list.get(1)).setPlayAudioListener(new PlayAudio() {
            @Override
            public void playAudio(int i) {
                if (i == current_audio_num) return;
                Audio audio = ((AudioFragment) list.get(1)).getAudio(i);
                setAudio(audio, true);
                current_audio_num = i;
            }

            @Override
            public void playAudioTwo(int i) {
                if (main_player != null && main_player.isPlaying()) startOrPause(false);
                Audio audio = ((AudioFragment) list.get(1)).getAudio(i);
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

        final RandomFloatView rfv = new RandomFloatView(this, 60, 60);
        rfv.setImageResource(R.drawable.current);
        rfv.setBackgroundResource(R.drawable.blue_circle);
        rfv.initView(frame, 1, 0.2);

        ImageView[] btns = getSubButton(frame);

        final FloatingActionMenu centerBottomMenu = new FloatingActionMenu.Builder(this)
                .setStartAngle(-30)
                .setAnimationHandler(new DefaultAnimationHandler())
                .addSubActionView(btns[0])
                .addSubActionView(btns[1])
                .addSubActionView(btns[2])
                .addSubActionView(btns[3])
                .addSubActionView(btns[4])
                .addSubActionView(btns[5])
                .attachTo(rfv).build();

        rfv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rfv.canDo())
                    centerBottomMenu.toggle(true);
                else if (centerBottomMenu.isOpen())
                    centerBottomMenu.close(true);
            }
        });

        initBtnListener(btns);
        play_btn = btns[5];
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
                    if (main_player == null) {
                        Audio first_audio = ((AudioFragment) list.get(1)).getAudio(current_audio_num);
                        setAudio(first_audio, false);
                    } else {
                        if (main_player.isPlaying()) startOrPause(false);
                        else startOrPause(true);
                    }
                } else if (view == btns[0]) {
                    Audio next_audio = ((AudioFragment) list.get(1)).getAudio(++current_audio_num);
                    if (next_audio != null) setAudio(next_audio, true);
                    else {
                        current_audio_num--;
                        Toast.makeText(MainActivity.this, "后面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                } else if (view == btns[4]) {
                    Audio pre_audio = ((AudioFragment) list.get(1)).getAudio(--current_audio_num);
                    if (pre_audio != null) setAudio(pre_audio, true);
                    else {
                        current_audio_num++;
                        Toast.makeText(MainActivity.this, "前面没有歌啦", Toast.LENGTH_SHORT).show();
                    }
                } else if (view == btns[2])
                    list.get(viewPager.getCurrentItem()).jumpTop();
                else if (view == btns[1]) {
                    int currentVolume = StaticMethod.getCurrentVolume(MainActivity.this);
                    StaticMethod.setVolume(MainActivity.this, ++currentVolume);
                } else if (view == btns[3]) {
                    int currentVolume = StaticMethod.getCurrentVolume(MainActivity.this);
                    StaticMethod.setVolume(MainActivity.this, --currentVolume);
                }
            }
        };
        for (ImageView btn : btns) btn.setOnClickListener(onClickListener);
    }

    private void setAudio(Audio audio, boolean play) {
        current_audio = audio;
        editor.putString("lastAudioPath", current_audio.getPath());
        editor.putString("lastAudioAuthor", current_audio.getArtist());
        editor.putString("lastAudioTitle", current_audio.getTitle());
        editor.putInt("lastAudioNum", current_audio_num);
        editor.commit();
        try {
            audio_title.setText(audio.getTitle());
            audio_author.setText(audio.getArtist());
            if (main_player != null) main_player.release();
            main_player = new MediaPlayer();
            main_player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            main_player.setDataSource("file://" + audio.getPath());
            main_player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (play) {
            main_player.start();
            audio_state.setText("正在播放");
            play_btn.setImageResource(R.drawable.btn_pause_white);
        } else {
            audio_state.setText("已暂停");
            play_btn.setImageResource(R.drawable.btn_play_white);
        }
        setVideoTimeTask(main_player);
        main_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Audio next_audio = ((AudioFragment) list.get(1)).getAudio(++current_audio_num);
                if (next_audio != null) setAudio(next_audio, true);
                else current_audio_num--;
            }
        });
    }

    private void setVideoTimeTask(final MediaPlayer mediaPlayer) {
        if (t != null) t.cancel();
        if (tt != null) tt.cancel();
        audio_seekbar.setMax(mediaPlayer.getDuration());
        t = new Timer();
        tt = new TimerTask() {
            @Override
            public void run() {
                try {
                    audio_seekbar.setProgress(mediaPlayer.getCurrentPosition());
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
            main_player.start();
        } else {
            play_btn.setImageResource(R.drawable.btn_play_white);
            audio_state.setText("已暂停");
            main_player.pause();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch (item.getItemId()) {
            case R.id.select_music:
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 0);
                break;
            case R.id.select_video:
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, 1);
                break;
            default:
                break;
        }
        return true;
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

    private void initImageLoader(Context context) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(200 * 1024 * 1024) // 200 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        ImageLoader.getInstance().init(config);
        //统一使用
        mOptions = new DisplayImageOptions.Builder()
                .showImageOnLoading(R.color.white)   //加载过程中
                .showImageForEmptyUri(R.color.white) //uri为空时
                .showImageOnFail(R.color.white)      //加载失败时
                .cacheOnDisk(true)
                .cacheInMemory(true)                             //允许cache在内存和磁盘中
                .bitmapConfig(Bitmap.Config.RGB_565)             //图片压缩质量参数
                .build();
    }


    @SuppressLint("ShowToast")
    @Override
    public void onBackPressed() {
        Snackbar.make(viewPager, "是否退出应用？", Snackbar.LENGTH_SHORT).setAction("是", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (t != null) t.cancel();
                if (tt != null) tt.cancel();
                if (main_player != null) main_player.stop();
                finish();
            }
        }).show();
    }
}
