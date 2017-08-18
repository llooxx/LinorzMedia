package com.linorz.linorzmedia.mediatools;

public class Video extends Audio {
    //视频类继承音频类
    public Video() {
        super();
    }

    public Video(int id, String title, String album, String artist,
                 String displayName, String mimeType, String path, long size,
                 long duration) {
        super(id, title, album, artist, path, displayName, mimeType, duration, size);
    }
}