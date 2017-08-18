package com.linorz.linorzmedia.media;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;

import java.io.IOException;
import java.util.ArrayList;

import com.linorz.linorzmedia.main.application.LinorzApplication;
import com.linorz.linorzmedia.mediatools.Audio;

/**
 * Created by linorz on 2017/8/14.
 */

public class AudioPlay {
    public static AudioPlay instance;//单例
    private MediaPlayer main_player;
    private Audio current_audio;
    private ArrayList<Audio> audios;
    private SharedPreferences mySharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;
    private MediaPlayer.OnCompletionListener onCompletionListener;
    private float current_volume = 1.0f;
    public int current_num = 0;
    //接口
    private ArrayList<AudioListener> audioListenerList;

    @SuppressLint("CommitPrefEdits")
    public AudioPlay() {
        this.context = LinorzApplication.getContext();
        //缓存
        mySharedPreferences = context.getSharedPreferences("LinorzMedia", Context.MODE_PRIVATE);
        editor = mySharedPreferences.edit();
        instance = this;
        audioListenerList = new ArrayList<>();
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

    public ArrayList<Audio> getAudios() {
        return audios;
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
        for (AudioListener audioListener : audioListenerList) audioListener.start();
    }

    public void pause() {
        if (main_player != null) main_player.pause();
        for (AudioListener audioListener : audioListenerList) audioListener.pause();
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
            main_player.setVolume(current_volume, current_volume);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (play) {
            main_player.start();
        }
        if (onCompletionListener != null)
            main_player.setOnCompletionListener(onCompletionListener);
        for (AudioListener audioListener : audioListenerList) audioListener.changeAudio(play);
    }

    public boolean setAudio(int num, boolean play) {
        Audio audio = getAudio(num);
        if (audio == null) return false;
        current_num = num;
        setAudio(audio, play);
        return true;
    }

    public void volumeUp() {
        current_volume += 0.1f;
        if (current_volume > 1.0f) current_volume = 1.0f;
        main_player.setVolume(current_volume, current_volume);
    }

    public void volumeDown() {
        current_volume -= 0.1f;
        if (current_volume < 0.1f) current_volume = 0.1f;
        main_player.setVolume(current_volume, current_volume);
    }

    public void setAudioPlayAction(MediaPlayer.OnCompletionListener ocl) {
        this.onCompletionListener = ocl;
    }


    public void addAudioListener(AudioListener audioListener) {
        audioListenerList.add(audioListener);
    }

    public boolean removeAudioListener(AudioListener audioListener) {
        return audioListenerList.remove(audioListener);
    }

    public interface AudioListener {
        void start();

        void pause();

        void changeAudio(boolean play);
    }
}
