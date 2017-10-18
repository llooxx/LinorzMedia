package com.linorz.linorzmedia.mediatools;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.linorz.linorzmedia.tools.StaticMethod;

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
        list.addAll(ScanSDCard());
        return list;
    }

    private List<Audio> ScanSDCard() {
        String[] sdpath = StaticMethod.getSDPath(context);
        // 判断是否存在SD
        LinkedList<Audio> mp3Files = new LinkedList<>();
        Queue<File> dirFiles = new LinkedList<>();
        File root = new File(sdpath[sdpath.length - 1]);
        dirFiles.add(root);
        while (!dirFiles.isEmpty()) {
            File file = dirFiles.poll();
            System.out.println(file.getPath());

            File[] listFiles = file.listFiles();
            if (listFiles != null)
                Collections.addAll(dirFiles, listFiles);

            if (file.getPath().endsWith(".mp3")
                    || file.getPath().endsWith(".MP3")) {
                Audio audio = new Audio();
                audio.setPath(file.getPath());
                audio.setTitle(file.getName());
                audio.setDuration(0);
                mp3Files.add(audio);
            }

        }
        return mp3Files;
    }

}