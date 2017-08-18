package com.linorz.linorzmedia.mediatools;

/**
 * Created by linorz on 2016/5/10.
 */
public abstract class Media {
    //抽象媒体类
    protected int id;//唯一id
    protected String title;//文件名
    protected String displayName;//显示的名字
    protected String mimeType;//格式
    protected String path;//路径
    protected long size;//大小

    public Media() {
        super();
    }

    public Media(int id, String title, String displayName, String mimeType,
                 String path, long size) {
        super();
        this.id = id;
        this.title = title;
        this.displayName = displayName;
        this.mimeType = mimeType;
        this.path = path;
        this.size = size;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
