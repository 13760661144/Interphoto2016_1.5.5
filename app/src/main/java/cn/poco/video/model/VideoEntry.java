package cn.poco.video.model;

import cn.poco.resource.FilterRes;
import cn.poco.video.sequenceMosaics.VideoInfo;

/**
 * Created by Shine on 2017/5/15.
 */

public abstract class VideoEntry extends MediaEntry{
    public int mImageId;
    public int mBucketId;
    public String mBucketName;
    public long mDateToken;
    public boolean mPlayable;
    public boolean mPlaying;

    // 单位为毫秒
    public long mDuration;

    // 当前滤镜类型
    public VideoFilterRes mVideoFilter;

    public VideoMemory mVideoMemory;

    public VideoEntry(String path, long duration) {
        super(path);
        mDuration = duration;
    }

    public abstract VideoEntry copy(VideoEntry videoEntry);


    public VideoInfo transferToVideoInfo() {
        VideoInfo info = new VideoInfo();
        info.mHasEdit = true;
        info.mSelectStartTime = 0;
        info.mSelectEndTime = mDuration;
        info.mDuration = mDuration;
        info.mParentPath  = info.mPath = info.mClipPath = mOriginPath;
        return info;
    }

    public static class VideoNormal extends VideoEntry{
        public VideoNormal(String path, long duration) {
            super(path, duration);
            mPlayable = true;
        }

        @Override
        public VideoEntry copy(VideoEntry videoEntry) {
            VideoNormal dst = null;
            if (videoEntry instanceof VideoNormal) {
                VideoNormal src = (VideoNormal) videoEntry;
                dst = new VideoNormal(src.mMediaPath, src.mDuration);
                dst.mVideoMemory = src.mVideoMemory;
                dst.mVideoFilter = src.mVideoFilter;
                dst.mHeight = src.mHeight;
                dst.mWidth = src.mWidth;
                dst.mBucketId = src.mBucketId;
                dst.mBucketName = src.mBucketName;
                dst.mDateToken = src.mDateToken;
                dst.mImageId = src.mImageId;
                dst.mPlayable = src.mPlayable;
                dst.mPlaying = src.mPlaying;
                dst.mRotation = src.mRotation;
                return dst;
            }
            return dst;
        }
    }

    public static class VideoExceedLimit extends VideoEntry {
        public VideoExceedLimit(String path, long duration) {
            super(path, duration);
            mPlayable = false;
        }

        @Override
        public VideoEntry copy(VideoEntry videoEntry) {
            VideoExceedLimit dst = null;
            if (videoEntry instanceof VideoExceedLimit) {
                VideoExceedLimit src = (VideoExceedLimit) videoEntry;
                dst = new VideoExceedLimit(src.mMediaPath, src.mDuration);
                dst.mVideoMemory = src.mVideoMemory;
                dst.mVideoFilter = src.mVideoFilter;
                dst.mHeight = src.mHeight;
                dst.mWidth = src.mWidth;
                dst.mBucketId = src.mBucketId;
                dst.mBucketName = src.mBucketName;
                dst.mDateToken = src.mDateToken;
                dst.mImageId = src.mImageId;
                dst.mPlayable = src.mPlayable;
                dst.mPlaying = src.mPlaying;
                dst.mRotation = src.mRotation;
                return dst;
            }
            return dst;

        }
    }

    public static class VideoCorrupt extends VideoEntry{
        public VideoCorrupt(String path, long duration) {
            super(path, duration);
            mPlayable = false;
        }

        @Override
        public VideoEntry copy(VideoEntry videoEntry) {
            VideoCorrupt dst = null;
            if (videoEntry instanceof VideoCorrupt) {
                VideoCorrupt src = (VideoCorrupt) videoEntry;
                dst = new VideoCorrupt(src.mMediaPath, src.mDuration);
                dst.mVideoMemory = src.mVideoMemory;
                dst.mVideoFilter = src.mVideoFilter;
                dst.mHeight = src.mHeight;
                dst.mWidth = src.mWidth;
                dst.mBucketId = src.mBucketId;
                dst.mBucketName = src.mBucketName;
                dst.mDateToken = src.mDateToken;
                dst.mImageId = src.mImageId;
                dst.mPlayable = src.mPlayable;
                dst.mPlaying = src.mPlaying;
                dst.mRotation = src.mRotation;
                return dst;
            }
            return dst;
        }
    }

    public static class VideoFilterRes{
        public int mUri;
        public FilterRes mFilterRes;
    }

    public static class VideoMemory {
        public long mStartTime; //毫秒
        public long mEndTime; //毫秒
        public long mDuration;
    }
}
