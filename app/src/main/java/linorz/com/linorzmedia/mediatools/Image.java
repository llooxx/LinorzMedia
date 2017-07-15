package linorz.com.linorzmedia.mediatools;

public class Image extends Media {
    //图片继承媒体类
    public Image() {
        super();
    }

    public Image(int id, String title, String displayName, String mimeType,
                 String path, long size) {
        super(id, title, displayName, mimeType, path, size);
    }

}