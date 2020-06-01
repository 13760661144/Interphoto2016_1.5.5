package cn.poco.watermarksync.manager;

import android.content.Context;

import cn.poco.system.WaterInterface;
import cn.poco.watermarksync.api.DeleteWatermarkApi;
import cn.poco.watermarksync.api.ModifyWatermarkApi;
import cn.poco.watermarksync.api.UploadWatermarkCallbackApi;
import cn.poco.watermarksync.api.UserWatermarkListApi;
import cn.poco.watermarksync.api.WatermarkSyncApi;
import cn.poco.watermarksync.api.WatermarkSyncWebUtil;
import cn.poco.watermarksync.model.Watermark;
import cn.poco.watermarksync.model.WatermarkUpdateInfo;
import cn.poco.watermarksync.util.Constant;
import cn.poco.watermarksync.util.DispatchQueue;
import cn.poco.watermarksync.util.MainThreadHelper;

/**
 * Created by Shine on 2017/3/7.
 */

public class NetWorkRequestManager {

    private static NetWorkRequestManager sInstance;
    private Context mContext;
    private MainThreadHelper mMainthreadHelper;
    private DispatchQueue mUploadQueue = new DispatchQueue("uploadQueue");
    private DispatchQueue mDeleteQueue = new DispatchQueue("deleteQueue");
    private DispatchQueue mModifyQueue = new DispatchQueue("modifyQueue");


    public static NetWorkRequestManager getInstacne(Context context) {
        NetWorkRequestManager localInstance = sInstance;
        if (localInstance == null) {
            synchronized (WatermarkSyncManager.class) {
                localInstance = sInstance;
                if (localInstance == null) {
                    sInstance = localInstance = new NetWorkRequestManager(context);
                }
            }
        }
        return localInstance;
    }

    private NetWorkRequestManager(Context context) {
        mContext = context;
        mMainthreadHelper = new MainThreadHelper(context);
    }

    public void getWatermarkUploadCallback(final WatermarkUpdateInfo updateInfo, final WaterInterface api, final NetWorkCallback callback) {
        mUploadQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                final UploadWatermarkCallbackApi uploadWatermarkCallbackApi = WatermarkSyncWebUtil.getUploadWatermarkCallback(updateInfo, api);
                mMainthreadHelper.runOnUiThread(new MainThreadHelper.callBack() {
                    @Override
                    public void runOnUiThread() {
                        if (uploadWatermarkCallbackApi != null && uploadWatermarkCallbackApi.mProtocolCode == Constant.NET_WORK_SUCCESSFUL && uploadWatermarkCallbackApi.mIsUploadSucceed) {
                            callback.onSuccessWithObject(uploadWatermarkCallbackApi);
                        } else {
                            callback.onFailure();
                        }
                    }
                });
            }
        });
    }


    public void deleteWatermark(final String userId, final String accessToken, final Watermark res, final WaterInterface api, final NetWorkCallback callback) {
        mDeleteQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                final DeleteWatermarkApi deleteWatermarkApi = WatermarkSyncWebUtil.deleteUserWatermark(userId, accessToken, res.getObjectId(), api);
                mMainthreadHelper.runOnUiThread(new MainThreadHelper.callBack() {
                    @Override
                    public void runOnUiThread() {
                        if (deleteWatermarkApi != null && deleteWatermarkApi.mProtocolCode == Constant.NET_WORK_SUCCESSFUL && deleteWatermarkApi.mIsDeleteSucceed) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure();
                        }
                    }
                });

            }
        });

    }

    public void modifyWatermark(final String userId, final String accessToken, final int objectId, final WaterInterface api, final NetWorkCallback callback, final WatermarkSyncManager.ModifyWatermarkInfo info) {
        mModifyQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                final ModifyWatermarkApi modifyWatermarkApi = WatermarkSyncWebUtil.modifyWatermarkApi(userId, accessToken, objectId, api, info);
                mMainthreadHelper.runOnUiThread(new MainThreadHelper.callBack() {
                    @Override
                    public void runOnUiThread() {
                        if (modifyWatermarkApi != null && modifyWatermarkApi.mProtocolCode == Constant.NET_WORK_SUCCESSFUL && modifyWatermarkApi.mIsModifySucceed) {
                            callback.onSuccess();
                        } else {
                            callback.onFailure();
                        }
                    }
                });
            }
        });
    }


    public void getUserWatermarkList(final String userId, final String accessToken, final WaterInterface api, final NetWorkCallback callBack) {
        Thread watermarkThread = new Thread() {
            @Override
            public void run() {
                final UserWatermarkListApi userWatermarkListApi = WatermarkSyncWebUtil.getUserWatermarkList(userId, accessToken,api);
                mMainthreadHelper.runOnUiThread(new MainThreadHelper.callBack() {
                    @Override
                    public void runOnUiThread() {
                        if (userWatermarkListApi != null && userWatermarkListApi.mProtocolCode == Constant.NET_WORK_SUCCESSFUL && userWatermarkListApi.mIsGetUserWatermarkSucceed) {
                            callBack.onSuccessWithObject(userWatermarkListApi);
                        } else {
                            callBack.onFailure();
                        }
                    }
                });
            }
        };
        watermarkThread.start();
    }

    public void syncWatermarkInfo(final String userId, final String accessToken, final String data, final WaterInterface api, final NetWorkCallback callback) {
        Thread syncThread = new Thread() {
            @Override
            public void run() {
                final WatermarkSyncApi watermarkSyncApi = WatermarkSyncWebUtil.getWatermarkSyncData(userId, accessToken, data, api);
                mMainthreadHelper.runOnUiThread(new MainThreadHelper.callBack() {
                    @Override
                    public void runOnUiThread() {
                        if (watermarkSyncApi != null && watermarkSyncApi.mProtocolCode == Constant.NET_WORK_SUCCESSFUL && watermarkSyncApi.mIsSyncSucceed) {
                            callback.onSuccessWithObject(watermarkSyncApi);
                        } else {
                            callback.onFailure();
                        }
                    }
                });
            }
        };
        syncThread.start();

    }

    public interface NetWorkCallback {
        void onSuccess();
        void onFailure();
        void onSuccessWithObject(Object object);
    }

}
