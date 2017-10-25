package com.linorz.linorzmedia.mediatools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.linorz.linorzmedia.main.application.LinorzApplication;
import com.linorz.linorzmedia.tools.StaticMethod;
import com.linorz.linorzmedia.tools.mp3.MP3File;

public class AudioProvider extends AbstractProvider {
    //获得音频列表
    public AudioProvider(Context context) {
        super(context);
    }

    @Override
    public List<Audio> getList() {
        List<Audio> list = null;
        if (context != null) {
            Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    null,
                    null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                list = new ArrayList<>();
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                    int albumId = cursor
                            .getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                    String title = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                    String album = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                    String artist = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                    String path = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                    String displayName = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                    String mimeType = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
                    long duration = cursor.getInt(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                    long size = cursor.getLong(cursor
                            .getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
                    Audio audio = new Audio(id, albumId, title, album, artist, path,
                            displayName, mimeType, duration, size);
                    list.add(audio);
                }
                cursor.close();
            }
        }
        SharedPreferences preferences = LinorzApplication.getContext().
                getSharedPreferences("LinorzMedia", Context.MODE_PRIVATE);
        String ja_str = preferences.getString("mp3s", "[]");
        Gson gson = new Gson();
        JsonParser jp = new JsonParser();
        JsonArray ja = jp.parse(ja_str).getAsJsonArray();
        for (int i = 0; i < ja.size(); i++) {
            Audio audio = gson.fromJson(ja.get(i), Audio.class);
            list.add(audio);
        }
//        list.addAll(ScanSDCard(context));
        return list;
    }

    public static void ScanSDCard() {
        Gson gson = new Gson();
        String[] sdpath = StaticMethod.getSDPath();
        // 判断是否存在SD
        Queue<File> dirFiles = new LinkedList<>();
        File root = new File(sdpath[sdpath.length - 1]);
        dirFiles.add(root);
        LinkedList<Audio> mp3s = new LinkedList<>();
        int num = 0;
        while (!dirFiles.isEmpty()) {
            File file = dirFiles.poll();
            File[] listFiles = file.listFiles();
            if (listFiles != null)
                Collections.addAll(dirFiles, listFiles);
            if (file.getPath().endsWith(".mp3")
                    || file.getPath().endsWith(".MP3")) {
                Log.e("MP3_NO", ++num + ":" + file.getName());
                Audio audio = new Audio();
                MP3File mp3 = new MP3File(file);
                audio.setPath(file.getPath());
                String title = mp3.getTitle();
                audio.setTitle(title != null ? title : file.getName().replace(".mp3", ""));
                audio.setAlbum(mp3.getAlbum());
                audio.setArtist(mp3.getArtist());
                audio.setDuration(0);
                mp3s.add(audio);
            }

        }
        Log.e("MP3_NO", "扫描结束");
        SharedPreferences.Editor editor = LinorzApplication.getContext().
                getSharedPreferences("LinorzMedia", Context.MODE_PRIVATE).edit();
        editor.putString("mp3s", gson.toJson(mp3s));
        editor.apply();
        Toast.makeText(LinorzApplication.getContext(), "扫描结束", Toast.LENGTH_LONG);
    }

}