package linorz.com.linorzmedia.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

import linorz.com.linorzmedia.main.application.LinorzApplication;
import linorz.com.linorzmedia.mediatools.Audio;

/**
 * Created by linorz on 2017/8/14.
 */

public class AudioPlay {
    private MediaPlayer main_player;
    private Audio current_audio;
    private ArrayList<Audio> audios;
    private SharedPreferences mySharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    public int current_num = 0;
    public static AudioPlay instance;

    @SuppressLint("CommitPrefEdits")
    public AudioPlay() {
        this.context = LinorzApplication.getContext();
        //缓存
        mySharedPreferences = context.getSharedPreferences("LinorzMedia", Context.MODE_PRIVATE);
        editor = mySharedPreferences.edit();
        instance = this;
    }

    //get
    public int getLastAudioNum() {
        return mySharedPreferences.getInt("lastAudioNum", 0);
    }

    public Audio getAudio(int i) {
        if (i >= 0 && i < audios.size()) {
            return audios.get(i);
        } else return null;
    }

    public Audio getCurrentAudio() {
        return current_audio;
    }

    public int getDuration() {
        return main_player.getDuration();
    }

    public int getCurrentPosition() {
        return main_player.getCurrentPosition();
    }

    //action
    public boolean isPlaying() {
        return main_player != null && main_player.isPlaying();
    }

    public void init() {
        current_num = getLastAudioNum();
        setAudio(getAudio(current_num), false);
    }

    public void start() {
        if (main_player != null) main_player.start();
    }

    public void pause() {
        if (main_player != null) main_player.pause();
    }

    public void stop() {
        if (main_player != null) main_player.stop();
    }

    //set
    public void setAudios(ArrayList<Audio> audios) {
        this.audios = audios;
    }

    public void setAudio(Audio audio, boolean play) {
        current_audio = audio;
        editor.putString("lastAudioPath", current_audio.getPath());
        editor.putString("lastAudioAuthor", current_audio.getArtist());
        editor.putString("lastAudioTitle", current_audio.getTitle());
        editor.putInt("lastAudioNum", current_num);
        editor.commit();
        try {
            if (main_player != null) main_player.release();
            main_player = new MediaPlayer();
            main_player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            main_player.setDataSource("file://" + current_audio.getPath());
            main_player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (play) {
            main_player.start();
        }
        main_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                //随机
                current_num = (int) (audios.size() * Math.random());
                Audio next_audio = getAudio(current_num);
                setAudio(next_audio, true);
            }
        });
    }

    public boolean setAudio(int num, boolean play) {
        Audio audio = getAudio(num);
        if (audio == null) return false;
        current_num = num;
        setAudio(audio, play);
        return true;
    }
}
