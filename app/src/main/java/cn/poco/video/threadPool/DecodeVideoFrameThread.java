package cn.poco.video.threadPool;

import android.graphics.Bitmap;

import cn.poco.video.utils.VideoUtils;

/**
 * Created by Shine on 2017/8/25.
 */

public class DecodeVideoFrameThread implements Runnable{
    public interface DecodeFrameCallback {
        void getFrame(DecodeVideoThreadPool.VideoTask videoTask, Bitmap bitmap);
    }


    private DecodeFrameCallback mCallback;
    private DecodeVideoThreadPool.VideoTask mVideoTask;

    public DecodeVideoFrameThread(DecodeVideoThreadPool.VideoTask videoTask) {
        mVideoTask = videoTask;
    }

    public void setDecodeFrameCallback(DecodeFrameCallback callback) {
        this.mCallback = callback;
    }

    @Override
    public void run() {
        if (!Thread.currentThread().isInterrupted()) {
            Bitmap bitmap = decodeFrameByTime(mVideoTask.mVideoPath, mVideoTask.mFrameDecodedTime);
            if (mCallback != null) {
                mCallback.getFrame(mVideoTask, bitmap);
            }
        }
    }

    public Bitmap decodeFrameByTime(String videoPath, long decodeTime) {
        synchronized (DecodeFrameCallback.class) {
            Bitmap bitmap = VideoUtils.decodeFrameByTimeAndroidApi(videoPath, decodeTime);
            return bitmap;
        }
    }

}
