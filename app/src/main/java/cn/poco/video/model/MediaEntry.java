package cn.poco.video.model;

/**
 * Created by Shine on 2017/5/16.
 */

public abstract class MediaEntry {
    public String mMediaPath;
    public String mOriginPath;
    public int mWidth;
    public int mHeight;
    public int mRotation;

    public MediaEntry(String path) {
        mMediaPath = path;
        mOriginPath = path;
    }



}
