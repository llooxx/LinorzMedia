package com.linorz.linorzmedia.mediatools;

public class Audio extends Media {
    //音频类继承媒体类
    protected String album;//唱片
    protected String artist;//艺术家
    protected long duration;//时长

    public Audio() {
        super();
    }

    public Audio(int id, String title, String album, String artist,
                 String path, String displayName, String mimeType, long duration,
                 long size) {
        super(id, title, displayName, mimeType, path, size);
        this.album = album;
        this.artist = artist;
        this.duration = duration;
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

}