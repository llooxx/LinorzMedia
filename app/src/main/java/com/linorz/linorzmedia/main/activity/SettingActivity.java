package com.linorz.linorzmedia.main.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.linorz.linorzmedia.R;
import com.linorz.linorzmedia.mediatools.AudioPlay;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by linorz on 2017/8/26.
 */

public class SettingActivity extends SwipeBackAppCompatActivity {
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        //缓存
        mSharedPreferences = getSharedPreferences("LinorzMedia", Context.MODE_PRIVATE);
        editor = mSharedPreferences.edit();

        setVolume();
        setMode();
        setSearch();
    }

    private void setVolume() {
        volumeBar.setMax(9);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            float volume;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                volume = i + 1;
                AudioPlay.instance.setVolume(volume / 10);
                volumeText.setText(String.valueOf(i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editor.putFloat("volume", volume / 10);
                editor.apply();
            }
        });
        volumeBar.setProgress((int) (mSharedPreferences.getFloat("volume", 0.5f) * 10) - 1);

    }

    private void setMode() {
        modeBar.setMax(2);
        modeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) modeText.setText("顺序");
                if (i == 1) modeText.setText("随机");
                if (i == 2) modeText.setText("循环");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioPlay.instance.setMode(seekBar.getProgress());
                editor.putInt("mode", seekBar.getProgress());
                editor.apply();
            }
        });
        modeBar.setProgress(mSharedPreferences.getInt("mode", AudioPlay.ORDER_MODE));
    }

    private void setSearch() {
        searchBar.setMax(1);
        searchBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i == 0) searchTv.setText("百度");
                if (i == 1) searchTv.setText("必应");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioPlay.instance.setMode(seekBar.getProgress());
                editor.putInt("search", seekBar.getProgress());
                editor.apply();
            }
        });
        searchBar.setProgress(mSharedPreferences.getInt("search", 0));
    }

    @BindView(R.id.setting_volume_bar)
    SeekBar volumeBar;
    @BindView(R.id.setting_volume_tv)
    TextView volumeText;
    @BindView(R.id.setting_mode_bar)
    SeekBar modeBar;
    @BindView(R.id.setting_mode_tv)
    TextView modeText;
    @BindView(R.id.setting_search_bar)
    SeekBar searchBar;
    @BindView(R.id.setting_search_tv)
    TextView searchTv;
}
