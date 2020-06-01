package cn.poco.video.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.widget.ProgressBar;

import java.lang.reflect.Field;

/**
 * Created by admin on 2016/10/19.
 */

public class AndroidUtil {
    private static volatile Handler mHandler;

    public static void init() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
    }

    public static void runOnUiThread(Runnable runnable) {
        if (mHandler != null) {
             mHandler.post(runnable);
        }
    }

    public static void cancelRunOnUiThread(Runnable runnable) {
        if (mHandler != null) {
            mHandler.removeCallbacks(runnable);
        }
    }

    public static void assertMainThread() {
        if (!isOnMainThread()) {
            throw new IllegalArgumentException("You must call this method on the main thread");
        }
    }

    public static void assertBackgroundThread() {
        if (!isOnBackgroundThread()) {
            throw new IllegalArgumentException("You must call this method on the background thread");
        }
    }

    public static boolean isOnMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public static boolean isOnBackgroundThread() {
       return !isOnMainThread();
    }

    // 通过反射设置动画的时效
    public static void setProgressBarAnimationDuration(ProgressBar progressBar, int duration) {
        if (progressBar == null) {
            return;
        }

        try {
            Field mCursorDrawableRes = ProgressBar.class.getDeclaredField("mDuration");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.setInt(progressBar, duration);
        } catch (Throwable e) {

        }
    }

    public static int convertDpToPixel(Context context, float dp) {
        int pixel = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
        return pixel;
    }



    public static void clear() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }




}
