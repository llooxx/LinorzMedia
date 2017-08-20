package com.linorz.linorzmedia.mediatools;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class Video extends Audio {
    //视频类继承音频类
    public Video() {
        super();
    }

    public Video(int id, String title, String album, String artist,
                 String displayName, String mimeType, String path, long size,
                 long duration) {
        super(id, -1, title, album, artist, path, displayName, mimeType, duration, size);
    }


    public Bitmap getVideoThumbnail() {
//        MediaMetadataRetriever media = new MediaMetadataRetriever();
//        media.setDataSource(path);
//        return media.getFrameAtTime();
        return ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
    }
}