package cn.poco.video.helper;

import android.graphics.Bitmap;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.poco.utils.FileUtil;
import cn.poco.watermarksync.util.DispatchQueue;

/**
 * Created by Simon Meng on 2017/11/15.
 * Guangzhou Beauty Information Technology Co.,Ltd
 */

public class CacheManager {
    public interface CacheManagerCallback {
        void onSaveImgSuccessfully(String path);
        void failureToLoadImg(String path);
    }

    private android.os.Handler mHandler;
    private DispatchQueue mCacheThread;
    private List<String> mCacheFilePathList = new ArrayList<>();

    public CacheManager() {
        mHandler = new android.os.Handler(Looper.myLooper());
        mCacheThread = new DispatchQueue("cacheThread");
    }

    public void saveBitmapToSdCard(final String dstFileName, final Bitmap bitmap) {
        mCacheThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                final File bitmapFile = new File(dstFileName);
                FileOutputStream fileOutputStream = null;
                File parentFile = bitmapFile.getParentFile();
                if (!parentFile.exists()) {
                    parentFile.mkdirs();
                }
                try {
                    if (!bitmapFile.exists()) {
                        bitmapFile.createNewFile();
                    }
                    if (mCacheFilePathList.indexOf(dstFileName) == -1) {
                        mCacheFilePathList.add(dstFileName);
                    }
                    fileOutputStream = new FileOutputStream(bitmapFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
                    try {
                        if (fileOutputStream != null) {
                            fileOutputStream.flush();
                            fileOutputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.onSaveImgSuccessfully(bitmapFile.getAbsolutePath());
                            }
                        }
                    });
                } catch (IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mCallback != null) {
                                mCallback.failureToLoadImg(bitmapFile.getAbsolutePath());
                            }
                        }
                    });
                }
            }
        });
    }


    private CacheManagerCallback mCallback;
    public void setCacheCallback(CacheManagerCallback cacheCallback) {
        this.mCallback = cacheCallback;
    }

    public void clear() {
        this.mCallback = null;
        mHandler.removeCallbacksAndMessages(-1);
        mCacheThread.cleanupQueue();
        deleteCacheFile();
    }

    private void deleteCacheFile() {
        mCacheThread.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (String item : mCacheFilePathList) {
                    if (cn.poco.utils.FileUtil.isFileExists(item)) {
                        FileUtil.deleteSDFile(item);
                    }
                }
                mCacheThread.quit();
            }
        });
    }



}
