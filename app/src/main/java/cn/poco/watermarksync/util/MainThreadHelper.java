package cn.poco.watermarksync.util;

import android.content.Context;
import android.os.Handler;

/**
 * Created by Shine on 2017/3/7.
 */

public class MainThreadHelper {
    private Handler mHandler;

    public MainThreadHelper(Context context) {
        mHandler = new Handler(context.getMainLooper());
    }

    public void runOnUiThread(final callBack callBack) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                callBack.runOnUiThread();
            }
        });
    }

    public void clearAll() {
        mHandler.removeCallbacksAndMessages(null);
    }


    public interface callBack {
        void runOnUiThread();
    }


}
