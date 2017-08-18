package com.linorz.linorzmedia.mediatools;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;

public class Audio extends Media {
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    //音频类继承媒体类
    protected String album;//唱片
    protected String artist;//艺术家
    protected long duration;//时长
    protected int albumId;//唱片ID


    public Audio() {
        super();
    }

    public Audio(int id, int albumId, String title, String album, String artist,
                 String path, String displayName, String mimeType, long duration,
                 long size) {
        super(id, title, displayName, mimeType, path, size);
        this.album = album;
        this.artist = artist;
        this.duration = duration;
        this.albumId = albumId;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }


    public Bitmap getArtwork(Context context) {
        Bitmap bm = null;
        if (albumId < 0 && id < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            FileDescriptor fd = null;
            if (albumId < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + id + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumId);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
            bm = BitmapFactory.decodeFileDescriptor(fd);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }
}