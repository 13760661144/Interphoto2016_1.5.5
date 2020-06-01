package cn.poco.video.threadPool;

import android.graphics.Bitmap;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Shine on 2017/8/28.
 */

public class DecodeVideoThreadPool {
    public interface ThreadPoolCallback {
        void getDecodedFrame(VideoTask videoTask, Bitmap bitmap, boolean isFinished);
    }

    public static class VideoTask {
        public String mVideoPath;
        public long mFrameDecodedTime;
        public int mLayoutPosition;
        public int mIndexOfGroup;
        public long mDurationOfSelectedVideo;
        public String mImgCachePath;
    }

    private ExecutorService mExcutorService;
    private int mThreadCount;
    private List<VideoTask> mVideoTaskList;
    private ThreadPoolCallback mCallback;
    private boolean mAllTaskFinished;

    public DecodeVideoThreadPool(int count, List<VideoTask> videoTaskList, ThreadPoolCallback callback) {
        this.mCallback = callback;
        this.mThreadCount = count;
        this.mVideoTaskList = videoTaskList;
        mTotalTaskCount = mVideoTaskList.size();
    }

    public void setVideoTaskList(List<VideoTask> list) {
        this.mVideoTaskList.clear();
        this.mVideoTaskList.addAll(list);
        mTotalTaskCount = mVideoTaskList.size();
        mDecodedSuccessfullyCount = 0;
    }

    private int mTotalTaskCount;
    private boolean mIsReverse;

    public void startDecode(boolean reverse) {
        mIsReverse = reverse;
        mExcutorService = Executors.newFixedThreadPool(mThreadCount);
        VideoTask videoTask;
        if (!reverse) {
            for (int i = 0; i < mVideoTaskList.size(); i++) {
                videoTask = mVideoTaskList.get(i);
                carryDecodeVideoTask(videoTask);
            }
        } else {
            for (int i = mVideoTaskList.size() - 1; i >= 0; i--) {
                videoTask = mVideoTaskList.get(i);
                carryDecodeVideoTask(videoTask);
            }
        }
    }

    private void carryDecodeVideoTask(VideoTask videoTask) {
        if (!mIsReverse) {
            decodeVideoFrame(videoTask);
        } else {
            decodeVideoFrame(videoTask);
        }
    }

    private void decodeVideoFrame(VideoTask videoTask) {
        DecodeVideoFrameThread decodeThread = new DecodeVideoFrameThread(videoTask);
        decodeThread.setDecodeFrameCallback(callback);
        mExcutorService.execute(decodeThread);
    }


    private int mDecodedSuccessfullyCount;
    private DecodeVideoFrameThread.DecodeFrameCallback callback = new DecodeVideoFrameThread.DecodeFrameCallback() {
        @Override
        public void getFrame(VideoTask videoTask, Bitmap bitmap) {
            synchronized (DecodeVideoThreadPool.class) {
                ++mDecodedSuccessfullyCount;
                if (mDecodedSuccessfullyCount == mTotalTaskCount) {
                    mAllTaskFinished = true;
                }
                if (mCallback != null) {
                    mCallback.getDecodedFrame(videoTask, bitmap, mAllTaskFinished);
                }
            }

        }
    };


    public void clear() {
        if (!mExcutorService.isShutdown()) {
            mExcutorService.shutdownNow();
        }
        callback = null;
    }

}
