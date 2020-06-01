package cn.poco.video.utils;

import android.os.HandlerThread;
import android.os.Looper;

/**
 * Created by Shine on 2017/7/18.
 */

public class TimingExecutor {
    public interface TimeToDoSomething {
        void onTimeDoThings();
    }

    private HandlerThread mCountDownThread;
    private RepeatPlayHandler mRepeatPlayHandler;
    private TimeToDoSomething mCallback;

    public TimingExecutor(TimeToDoSomething callback) {
        mCallback = callback;
        mCountDownThread = new HandlerThread("countDown");
        mCountDownThread.start();
        mRepeatPlayHandler = new RepeatPlayHandler(mCountDownThread.getLooper());
        AndroidUtil.init();
    }


    private long mRepeatDuration;
    public void executeItAfterSomeTime(long mills) {
        mRepeatDuration = mills;
        mCountDownStartTime = System.currentTimeMillis();
        reset();
        mRepeatPlayHandler.postDelayed(runnable, mills);
    }

    private long mCountDownStartTime;
    private long mElapsedTime;
    private long mCountDownStopTime;

    public void stopCountTime() {
        // 暂停的时候先把之前定时的事件取消掉
        mRepeatPlayHandler.removeCallbacksAndMessages(null);
        mCountDownStopTime = System.currentTimeMillis();
        mElapsedTime += mCountDownStopTime - mCountDownStartTime;
    }

    /**
     * 继续计时
     */
    public void continueCountTime() {
        mCountDownStartTime = System.currentTimeMillis();
        long delay = mRepeatDuration - mElapsedTime;
        mRepeatPlayHandler.postDelayed(runnable, delay);
    }

    public void reset() {
        mElapsedTime = 0;
        mRepeatPlayHandler.removeCallbacksAndMessages(null);
    }

    public void resetAndUpdateDuration(long duration) {
        mElapsedTime = 0;
        mRepeatPlayHandler.removeCallbacksAndMessages(null);
        if (duration > 0) {
            mRepeatDuration = duration;
        }
    }

    public void clear() {
        reset();
        runnable = null;
        mCountDownThread.quit();
        AndroidUtil.clear();
    }


    private schedule runnable = new schedule();
    private class schedule implements Runnable {
        @Override
        public void run() {
            AndroidUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mCallback != null) {
                        mCallback.onTimeDoThings();
                    }
                }
            });
        }
    }

    private static class RepeatPlayHandler extends android.os.Handler {

        public RepeatPlayHandler(Looper looper) {
            super(looper);
        }
    }



}
