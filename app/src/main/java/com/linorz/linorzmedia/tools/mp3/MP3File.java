package com.linorz.linorzmedia.tools.mp3;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Vector;

@SuppressWarnings("serial")
public class MP3File {
    // Encoding variables
    private byte[] finalImageData;
    private byte[] imageDataBytes;
    private byte finalImageDataEncoding;
    private static final byte[][] textTerminators = {{0}, {0, 0}, {0, 0}, {0}};

    // Holds the header
    private byte[] header = new byte[10];
    // Holds all the tags
    private Vector<ID3TextFrame> tags = new Vector<ID3TextFrame>();

    // Holds the tags that we are able to change and the picture
    private String title;
    private String artist;
    private String album;
    private String year;
    private ID3PicFrame pframe;
    private File file;
    // Holds the audioPart of the MP3 file (music)
    private byte[] audioPart;

    private boolean hasCover = false;
    private boolean cached = false;
    private boolean isID3v2Tag = true;
    private boolean isParsed = false;
    private boolean isChanged = false;
    private boolean needCover = false;

    public MP3File(String path) {
        this.file = new File(path);
        parse();
    }

    public MP3File(String path, boolean needCover) {
        this.file = new File(path);
        this.needCover = needCover;
        parse();
    }

    public MP3File(File file) {
        this.file = file;
        parse();
    }

    public MP3File(File file, boolean needCover) {
        this.file = file;
        this.needCover = needCover;
        parse();
    }

    public MP3File(String artist, String album, String title, String year, String path) {
        this.cached = true;
        this.isParsed = false;
        this.setAlbum(album);
        this.setArtist(artist);
        this.setTitle(title);
        this.setYear(year);
        this.file = new File(path);
    }

    public byte[] getHeader() {
        return this.header;
    }

    public Vector<ID3TextFrame> getTags() {
        return this.tags;
    }

    public boolean isCached() {
        return this.cached;
    }

    public int getSize() {
        return audioPart.length + finalImageData.length;
    }

    public byte[] getAudioPart() {
        return this.audioPart;
    }

    public void setHasCover(boolean b) {
        this.hasCover = b;
    }

    public boolean hasCover() {
        return this.hasCover;
    }

    public ID3PicFrame getPicFrame() {
        return this.pframe;
    }

    @SuppressWarnings("resource")
    public boolean parse() {
        boolean hasTagsLeft = true;
        try {
            DataInputStream data =
                    new DataInputStream(new FileInputStream(this.file));
            data.read(header);
            // Point to the file

            // Check if we have a header???
            if (data.available() < 10) {
                this.isID3v2Tag = false;
                data.close();
                return false;
            }

            // Read header and check if it is ID3v2
            if (this.header[0] == 73//49
                    && this.header[1] == 68//44
                    && this.header[2] == 51//33
                    && this.header[3] < 255//FF
                    && this.header[4] < 255
                    && this.header[6] < 128//80
                    && this.header[7] < 128
                    && this.header[8] < 128
                    && this.header[9] < 128) {
                this.isID3v2Tag = true;
            } else {
                this.isID3v2Tag = false;
                data.close();
                return false;
            }

            while (hasTagsLeft) {
                // Read first for bytes to determine which tag it is
                byte[] keyArr = new byte[4];
                data.read(keyArr);
                // Save tag name
                String keyword = new String(keyArr);

                // Read frame body size
                int frameBodySize = data.readInt();
                // Read tag flags
                short flags = data.readShort();

                // Read audio part of the file and finish parsing
                if (frameBodySize == 0) {
                    this.audioPart = new byte[data.available() + 10];
                    data.read(this.audioPart, 9, data.available());
                    data.close();
                    hasTagsLeft = false;
                    this.isParsed = true;
                    return true;
                }

                // Read content of tag
                byte[] textBuffer = new byte[frameBodySize];
                data.read(textBuffer);

                // Parse text tags
                if (keyword.startsWith("T")) {
                    this.parseText(textBuffer, keyword, frameBodySize, flags);
                }
                // Parse image
                if (needCover && keyword.equals("APIC")) {
                    this.imageDataBytes = textBuffer;
                    this.parseCover(textBuffer, keyword, frameBodySize, flags);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void parseText(byte[] frame, String keyword, int size, short flags) {
        String s;
        Byte type = frame[0];
        ID3TextFrame id3frame;

        // Check encoding of text and read accordingly
        if (type == 1) {
            s = new String(Arrays.copyOfRange(frame, 1, frame.length), Charset.forName("UTF-16"));

        } else {
            s = new String(Arrays.copyOfRange(frame, 1, frame.length), Charset.forName("ISO-8859-1"));
        }

        // Save
        id3frame = new ID3TextFrame(keyword, s, type, size, flags);
        boolean contains = false;
        for (ID3TextFrame f : tags) {
            if (f.getKeyword().equals(id3frame.getKeyword())) {
                contains = true;
            }
        }
        if (!contains) {
            // Save content of tag to appropriate tag
            if (keyword.equals("TPE1"))
                this.setArtist(s);
            if (keyword.equals("TALB"))
                this.setAlbum(s);
            if (keyword.equals("TIT2"))
                this.setTitle(s);
            if (keyword.equals("TYER"))
                this.setYear(s);
            tags.add(id3frame);
        }
    }

    public void parseCover(byte[] bytes, String keyword, int frameBodySize, short flags) {
        int pointer;
        String mimeType;

        byte pictureType;

        // Read empty stuff
        for (pointer = 1; pointer < bytes.length; pointer++) {
            if (bytes[pointer] == 0)
                break;
        }

        // Determine image encoding
        try {
            if (bytes[0] == 0)
                mimeType = new String(bytes, 1, pointer - 1, "ISO-8859-1");
            else
                mimeType = new String(bytes, 1, pointer - 1, "UTF-16");
        } catch (UnsupportedEncodingException e) {
            mimeType = "image/unknown";
        }

        // Determine image type
        pictureType = bytes[pointer + 1];

        // Read more empty stuff
        pointer += 2;
        int pointer2;
        for (pointer2 = pointer; pointer2 < bytes.length; pointer2++) {
            if (bytes[pointer2] == 0)
                break;
        }

        // Get image data
        int length = pointer2 - pointer;
        byte[] copy = new byte[length];
        if (length > 0) {
            System.arraycopy(bytes, pointer, copy, 0, length);
        }

        // Extract only needed data
        this.finalImageDataEncoding = bytes[0];
        this.finalImageData = copy;
        stripBomAndTerminator();
        pointer2 += getTerminator().length;

        // Save data
        length = bytes.length - pointer2;
        System.arraycopy(bytes, pointer2, imageDataBytes, 0, length);
        this.pframe = new ID3PicFrame(mimeType, pictureType,
                finalImageDataEncoding, finalImageData, imageDataBytes,
                keyword, frameBodySize, flags);
        this.hasCover = true;
    }

    public byte[] getTerminator() {
        return textTerminators[finalImageDataEncoding];
    }

    private void stripBomAndTerminator() {
        int leadingCharsToRemove = 0;

        // Determine encoding and how long the BOM is
        if (this.finalImageData.length >= 2
                && ((this.finalImageData[0] == (byte) 0xfe && this.finalImageData[1] == (byte) 0xff)
                || (this.finalImageData[0] == (byte) 0xff && this.finalImageData[1] == (byte) 0xfe))) {
            leadingCharsToRemove = 2;
        } else if (this.finalImageData.length >= 3 && (this.finalImageData[0] == (byte) 0xef
                && this.finalImageData[1] == (byte) 0xbb && this.finalImageData[2] == (byte) 0xbf)) {
            leadingCharsToRemove = 3;
        }

        // Determine how long the terminator is
        int trailingCharsToRemove = 0;
        for (int i = 1; i <= 2; i++) {
            if ((this.finalImageData.length - leadingCharsToRemove - trailingCharsToRemove) >= i
                    && this.finalImageData[this.finalImageData.length - i] == 0) {
                trailingCharsToRemove++;
            } else {
                break;
            }
        }

        // Remove BOM and terminator if present
        if (leadingCharsToRemove + trailingCharsToRemove > 0) {
            int newLength = this.finalImageData.length - leadingCharsToRemove - trailingCharsToRemove;
            byte[] newValue = new byte[newLength];
            if (newLength > 0) {
                System.arraycopy(this.finalImageData, leadingCharsToRemove, newValue, 0, newValue.length);
            }
            this.finalImageData = newValue;
        }
    }

    public String toString() {
        if (this.file != null) {
            String name = this.file.getName();
            if (isChanged)
                name = "*" + name;

            return name;
        }
        return null;

    }

    public String getAbsolutePath() {
        return this.file.getAbsolutePath();
    }

    public ID3TextFrame getTag(String keyword) {
        for (int k = 0; k < tags.size(); k++) {
            if (tags.get(k).getKeyword().equals(keyword)) {
                return tags.get(k);
            }
        }

        return null;
    }

    public String getTitle() {
        return this.title;
    }

    public String getArtist() {
        return this.artist;
    }

    public String getAlbum() {
        return this.album;
    }

    public String getYear() {
        return this.year;
    }

    public void setTitle(String title) {
        if (tags.size() > 0) {
            ID3TextFrame tag = getTag("TIT2");
            if (tag != null)
                tag.setData(title);
        }
        this.title = title;
    }

    public void setArtist(String artist) {
        if (tags.size() > 0) {
            ID3TextFrame tag = getTag("TPE1");
            if (tag != null)
                tag.setData(artist);
        }
        this.artist = artist;
    }

    public void setAlbum(String album) {
        if (tags.size() > 0) {
            ID3TextFrame tag = getTag("TALB");
            if (tag != null)
                tag.setData(album);
        }
        this.album = album;
    }

    public void setYear(String year) {
        if (year.length() > 4) {
            year = year.substring(0, 4);
        }
        if (tags.size() > 0) {
            ID3TextFrame tag = getTag("TYER");
            if (tag != null)
                tag.setData(year);
        }

        this.year = year;
    }

    public boolean isID3v2Tag() {
        return this.isID3v2Tag;
    }

    public boolean isParsed() {
        return this.isParsed;
    }

    public void changed() {
        this.isChanged = true;
    }

    public byte[] getImageData() {
        return this.imageDataBytes;
    }
}
