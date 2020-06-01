package cn.poco.watermarksync.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.CountDownTimer;
import android.text.TextUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.poco.beautify.BeautifyHandler;
import cn.poco.framework.FileCacheMgr;
import cn.poco.interphoto2.R;
import cn.poco.resource.FontRes;
import cn.poco.resource.MyLogoRes;
import cn.poco.resource.MyLogoResMgr;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.storagesystemlibs.CloudListener;
import cn.poco.storagesystemlibs.ServiceStruct;
import cn.poco.system.WaterInterface;
import cn.poco.utils.Utils;
import cn.poco.watermarksync.api.UserWatermarkListApi;
import cn.poco.watermarksync.api.WatermarkSyncApi;
import cn.poco.watermarksync.model.EditableWatermark;
import cn.poco.watermarksync.model.NotEditableWatermark;
import cn.poco.watermarksync.model.Watermark;
import cn.poco.watermarksync.model.WatermarkUpdateInfo;
import cn.poco.watermarksync.util.FileUtil;
import cn.poco.watermarksync.util.JsonHelper;
import cn.poco.watermarksync.util.MemorySyncHelper;
import cn.poco.watermarksync.watermarkstorage.StorageReceiver;
import cn.poco.watermarksync.watermarkstorage.StorageService;
import cn.poco.watermarksync.watermarkstorage.WatermarkStorageService;


/**
 * Created by Shine on 2017/2/27.
 */

public class WatermarkSyncManager implements CloudListener{
    private static final int SYNC_TIME_OUT_DURATION = 60000;

    private static Context sContext;
    private WaterInterface mApiInterface;

    private static String sUserId;
    private static String sAccessToken;
    private static boolean sIsUserInfoValid;

    private List<Integer> mWatermarkSyncAcidList = new ArrayList<>();
    private SyncManagerCallback mCallback;

    private boolean mIsBeingSync;
    private CountDownTimer mCountDownTimer;
    private boolean mIsSyncFail;


    private static volatile WatermarkSyncManager Instance = null;
    public synchronized static WatermarkSyncManager getInstacne(Context context) {
        WatermarkSyncManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (WatermarkSyncManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new WatermarkSyncManager(context);
                }
            }
        }
        sIsUserInfoValid = getUserInfo();
        return localInstance;
    }

    private WatermarkSyncManager(Context context) {
        sContext = context;
        mApiInterface = WaterInterface.GetInstance(context);
        StorageReceiver.AddCloudListener(this);
    }

    @Override
    public void OnProgress(int type, ServiceStruct str, int progress) {
    }


    @Override
    public void OnComplete(int type, ServiceStruct str) {
        final Watermark watermark = ((Watermark) str.mEx);
        if (type == WatermarkStorageService.UPLOAD_SINGLE_COMPLETE) {
            if (watermark.getOperateType() == Watermark.OperateType.UPLOAD || watermark.getOperateType() == Watermark.OperateType.SYNC) {
                watermark.setShouldModify(false);
                SyncDataManager.getInstance(sContext).updateWatermarkByLocally(watermark);
                checkSycnProgress(watermark, true);

                if (watermark.getOperateType() == Watermark.OperateType.UPLOAD) {
//                    Toast.makeText(sContext, "uploadFinish", Toast.LENGTH_SHORT).show();
                }

                if (watermark.getOperateType() == Watermark.OperateType.SYNC) {
//                    Toast.makeText(sContext, "syncFinish", Toast.LENGTH_SHORT).show();
                }

            } else if (watermark.getOperateType() == Watermark.OperateType.MODIFY_UPLOAD) {
                // 修改名字以及需要上传阿里云的修改
                ModifyWatermarkInfo info = (ModifyWatermarkInfo) ((Watermark)str.mEx).getCarrayData();
                info.mWatermarkServerUrl = str.mAliUrl;
                info.mVolume = watermark.getVolume();
                watermark.setUrl(info.mWatermarkServerUrl);
                // 调用修改的接口
                modifyWatermarkStep2(watermark, info);
            }
        } else if (type == WatermarkStorageService.DOWNLOAD_SINGLE_COMPLETE) {

            String waterMarkName = watermark.getTitle();

            // 假如没有名字，默认水印名字为自定义水印
            if (TextUtils.isEmpty(waterMarkName)) {
                waterMarkName = sContext.getString(R.string.diyLogo);
                watermark.setTitle(waterMarkName);
            }

            // 重命名文件
            final String originFileName = str.mPath;
            final String newPath = watermark.generateNewPathAfterDownload();
            FileUtil.renameFile(originFileName, newPath);
            watermark.setPath(newPath);

            //假如存在旧包, 先删除旧的zip包,以objectId以及userId相等为判断标准
            SyncDataManager.getInstance(sContext).deleteZipPackgeFromLocal(watermark.getObjectId(), watermark.getUserId());

            // 先判断下载的item是否已经存在(以本地的json文件为准，以防用户通过手动删除文件夹)，假如存在说明是更新的，需要做相应处理
            boolean isUpdateZip = SyncDataManager.getInstance(sContext).isNewWatermark(watermark);

            //新增水印
            if (!isUpdateZip) {
                // 添加本地id;
                SyncDataManager.getInstance(sContext).addWatermark(watermark);
            } else {
                // 更新水印情况
                watermark.setShouldModify(false);
                SyncDataManager.getInstance(sContext).updateWatermarkByObjectIds(watermark);
            }

            // 检查是否同步完成，若完成，则回调
            checkSycnProgress(watermark, true);

            if (watermark.getOperateType() == Watermark.OperateType.SYNC) {
//                Toast.makeText(sContext, "syncFinish", Toast.LENGTH_SHORT).show();
            }

            if (watermark.getOperateType() == Watermark.OperateType.DOWNLOAD) {
//                Toast.makeText(sContext, "downloadFinish", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void OnFail(int type, ServiceStruct str) {
        doSomeThingWhenShitHappen(type, (Watermark) str.mEx);
    }

    private void doSomeThingWhenShitHappen(int type, Watermark watermark) {
        Watermark.OperateType operateType = watermark.getOperateType();
        if (type == WatermarkStorageService.UPLOAD_SINGLE_FAIL) {
            if (operateType == Watermark.OperateType.UPLOAD) {
                SyncDataManager.getInstance(sContext).updateWatermarkByLocally(watermark);
//                Toast.makeText(sContext, "uploadFail", Toast.LENGTH_SHORT).show();
            } else if (operateType == Watermark.OperateType.MODIFY_UPLOAD) {
                watermark.setShouldModify(true);
                SyncDataManager.getInstance(sContext).updateWatermarkByLocally(watermark);
//                Toast.makeText(sContext, "modifyFail", Toast.LENGTH_SHORT).show();
            } else if (operateType == Watermark.OperateType.SYNC) {
//                Toast.makeText(sContext, "syncFail", Toast.LENGTH_SHORT).show();
            }

        } else if (type == WatermarkStorageService.DOWNLOAD_SINGLE_FAIL) {
            if (operateType == Watermark.OperateType.SYNC) {
//                Toast.makeText(sContext, "syncFail", Toast.LENGTH_SHORT).show();
            } else if (operateType == Watermark.OperateType.DOWNLOAD) {
//                Toast.makeText(sContext, "downloadFail", Toast.LENGTH_SHORT).show();
            }
        }
        checkSycnProgress(watermark, false);
    }

    @Override
    public void OnError(int type, ServiceStruct str) {
        doSomeThingWhenShitHappen(type, (Watermark) str.mEx);
    }




    // 根据不同的情况开始不同的信息同步
    public void startUpSyncDependOnSituation() {
        if (sIsUserInfoValid) {
            // 判断是否需要修正网络
            boolean needToCorrect = SyncDataManager.getInstance(sContext).isAnyDataNeedToCorrect(sUserId);

            // 先修正网络
            if (needToCorrect) {
                corretNetwork();
            }

            if (!(MyLogoResMgr.getInstance().GetMyLogoResArr().size() > 0)){
                //  当前没有任何水印
                syncWatermarkInfoWithServer();
            }
        }
    }

    // 修正网络
    private void corretNetwork() {
        NetWorkRequestManager.getInstacne(sContext).getUserWatermarkList(sUserId, sAccessToken, mApiInterface, new NetWorkRequestManager.NetWorkCallback() {
            @Override
            public void onSuccess() {


            }

            @Override
            public void onFailure() {

            }

            @Override
            public void onSuccessWithObject(Object object) {
                if (object instanceof UserWatermarkListApi) {
                    UserWatermarkListApi userWatermarkListApi = (UserWatermarkListApi) object;
                    List<Watermark> userWatermarkList = userWatermarkListApi.mWatermarkList;

                    HashMap<Integer, List<Watermark>> result = SyncDataManager.getInstance(sContext).categoryPendentHandleList(sUserId);
                    List<Watermark> uploadList = result.get(0);
                    List<Watermark> deleteList = result.get(1);
                    List<Watermark> modifyList = result.get(2);

                    // 过滤走可能产生重复的item;
                    List<Watermark> finalUploadList = filterCorretUploadList(uploadList, userWatermarkList);
                    uploadNewWatermark(finalUploadList);
                    modifyWatermarkStep1(modifyList);
                    for (Watermark item : deleteList) {
                        deleteWatermark(item);
                    }

                }
            }
        });
    }

    private List<Watermark> filterCorretUploadList(List<Watermark> uploadList, List<Watermark> currentWatermarkList) {
        List<Watermark> finalUploadList;
        List<Watermark> dropList = new ArrayList<>();
        for (Watermark item : uploadList) {
            for (Watermark item2 : currentWatermarkList) {
                if (item.getTitle().equals(item2) && item.getSaveTime().equals(item2.getCustomData())) {
                    dropList.add(item);
                }
            }
        }
        uploadList.removeAll(dropList);
        finalUploadList = uploadList;
        return finalUploadList;
    }


    // 与服务器同步水印的接口
    public void syncWatermarkInfoWithServer() {
        if (sIsUserInfoValid && !mIsBeingSync) {
            mIsBeingSync = true;
            mIsSyncFail = false;
            mWatermarkSyncAcidList.clear();

            List<Watermark> deleteList = SyncDataManager.getInstance(sContext).getAllDeleteWatermark(sUserId, Watermark.OperateType.SYNC);
            List<Watermark> syncList = SyncDataManager.getInstance(sContext).getAllWatermarkExceptMarkAsDelete(sUserId, Watermark.OperateType.SYNC);

            // 删除标记为应该删除的水印
            for (Watermark deleteItem : deleteList) {
                deleteWatermark(deleteItem);
            }

            String data = makeUpDataForSync(syncList);
            if (!TextUtils.isEmpty(data)) {
                getWatermarkSyncApi(data, new NetWorkRequestManager.NetWorkCallback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFailure() {
                        if (mCallback != null) {
                            mCallback.onFail();
                        }
                        mIsBeingSync = false;
                        setSyncCallback(null);
//                        Toast.makeText(sContext, "syncFail", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccessWithObject(Object object) {
                        if (object instanceof WatermarkSyncApi) {
                            WatermarkSyncApi watermarkSyncApi = (WatermarkSyncApi) object;
                            int count = watermarkSyncApi.getTotalCount();
                            if (!(count > 0)) {
                                if (mIsBeingSync) {
                                    if (mCallback != null) {
                                        mCallback.noNeedToSync();
                                        setSyncCallback(null);
                                    }
                                }
                                mIsBeingSync = false;
                            } else {
                                boolean callBackAfterCheckDelete = false;
                                if (!(watermarkSyncApi.mUploadList.size() + watermarkSyncApi.mDownLoadList.size() > 0)) {
                                    callBackAfterCheckDelete = true;
                                }

                                startCountDown();

                                // 需要删除
                                if (watermarkSyncApi.mDeleteList.size() > 0) {
                                    for (int i = 0; i < watermarkSyncApi.mDeleteList.size(); i++) {
                                        Watermark watermark = watermarkSyncApi.mDeleteList.get(i);

                                        // 在本地删除zip包，此时要用objectId和userId为条件
                                        SyncDataManager.getInstance(sContext).deleteWatermarkByObjectId(watermark);
                                    }
                                    if (callBackAfterCheckDelete) {
                                        if (mCallback != null) {
                                            mCallback.onFinishSuccessful();
                                            setSyncCallback(null);
                                        }
                                        mIsBeingSync = false;
                                        return;
                                    }
                                }

                                // 需要下载
                                if (watermarkSyncApi.mDownLoadList.size() > 0) {
                                    for (int i = 0; i < watermarkSyncApi.mDownLoadList.size(); i++) {
                                        Watermark watermark = watermarkSyncApi.mDownLoadList.get(i);
                                        watermark.setOperateType(Watermark.OperateType.SYNC);
                                        downLoadZipPackageForSync(sUserId, sAccessToken, watermark);
                                    }
                                }

                                // 需要上传
                                if (watermarkSyncApi.mUploadList.size() > 0) {
                                    List<Watermark> uploadList = new ArrayList<>();
                                    for (Watermark item : watermarkSyncApi.mUploadList) {
                                        // 上传的情况有可能只有objectId 和 savetime两个值是有效的
                                        MyLogoRes resItem = MemorySyncHelper.getInstance(sContext).getItemByObjectIds(MyLogoResMgr.getInstance().GetMyLogoResArr(), item.getObjectId(), sUserId);
                                        Watermark watermark;
                                        if (resItem.m_editable) {
                                            watermark = new EditableWatermark(resItem);
                                        } else {
                                            watermark = new NotEditableWatermark(resItem);
                                        }
                                        watermark.setOperateType(Watermark.OperateType.SYNC);
                                        uploadList.add(watermark);
                                    }
                                    uploadNewWatermark(uploadList);
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void getWatermarkSyncApi(String data, NetWorkRequestManager.NetWorkCallback callback) {
        if (!TextUtils.isEmpty(data)) {
            NetWorkRequestManager.getInstacne(sContext).syncWatermarkInfo(sUserId, sAccessToken, data, mApiInterface, callback);
        } else {
            if (callback != null) {
                callback.onFailure();
            }
        }
    }


    public void getUploadCallback(WatermarkUpdateInfo updateInfo, NetWorkRequestManager.NetWorkCallback callback) {
        if (sIsUserInfoValid) {
            NetWorkRequestManager.getInstacne(sContext).getWatermarkUploadCallback(updateInfo, mApiInterface, callback);
        }
    }

    // 上传接口(同时作为不覆盖的修改,以及修改图片水印的接口调用)
    public void uploadNewWatermark(List<Watermark> watermarkList) {
        if (sIsUserInfoValid) {
            ArrayList<ServiceStruct> structList = new ArrayList<>();
            for (Watermark watermark : watermarkList) {
                ServiceStruct struct = new ServiceStruct();
                String uploadPath = watermark.getPath();
                // 上传之前先做压缩处理
                if (watermark instanceof NotEditableWatermark) {
                    Bitmap compressedBitmap = BeautifyHandler.MakeBmp(sContext, watermark.getPath(), 2048, 2048);
                    final String suffix = ".png";
                    String uploadCachePath = FileCacheMgr.GetAppPath() + suffix;
                    uploadPath = Utils.SaveImg(sContext, compressedBitmap, uploadCachePath, 100, false);
                }

                long fileSize = FileUtil.getFileSize(uploadPath);
                watermark.setVolume(fileSize);

                struct.mPath = uploadPath;
                struct.mAccessToken = sAccessToken;
                struct.mUserId = sUserId;
                struct.mEx = watermark;
                structList.add(struct);
            }
            List<Integer> acidList = StorageService.PushUploadTask(sContext, structList);
            if (watermarkList.size() > 0) {
                if (watermarkList.get(0).getOperateType() == Watermark.OperateType.SYNC) {
                    mWatermarkSyncAcidList.addAll(acidList);
                }
            }
        }
    }

    // 下载同步所需要的zip包
    private void downLoadZipPackageForSync(String userId, String accessToken, Watermark watermark) {
        ServiceStruct downloadStruct = new ServiceStruct();
        downloadStruct.mUserId = userId;
        downloadStruct.mAccessToken = accessToken;
        downloadStruct.mAliUrl = watermark.getUrl();
        downloadStruct.mEx = watermark;
        downloadStruct.mPath = watermark.getPath();
        int acid = StorageService.PushDownloadTask(sContext, downloadStruct);
        if (watermark != null && watermark.getOperateType() == Watermark.OperateType.SYNC) {
            mWatermarkSyncAcidList.add(acid);
        }
    }

    // 删除水印接口
    public void deleteWatermark(final Watermark res) {
        // 假如objectId为负数，说明该水印的信息没有上传到服务器，只需要本地删除对应的数据即可
        final boolean isLocalDeleteOk = res.getObjectId() == Watermark.VALUE_NONE;

        // 首先，在本地删除zip包
        FileUtil.deleteFile(res.getPath());
        if (!isLocalDeleteOk && sIsUserInfoValid) {
            deleteWatermarkNetWork(res);
        } else {
            // 这种情况是现在删除的水印并没有存在服务器;
            SyncDataManager.getInstance(sContext).deleteResFileRecordByLocally(res);
        }
    }

    private void deleteWatermarkNetWork(final Watermark res) {
        NetWorkRequestManager.getInstacne(sContext).deleteWatermark(sUserId, sAccessToken, res, mApiInterface, new NetWorkRequestManager.NetWorkCallback() {
            @Override
            public void onSuccess() {
                SyncDataManager.getInstance(sContext).deleteResFileRecordByLocally(res);
//                Toast.makeText(sContext, "delete succed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure() {
                // 删除网络的请求失败
                res.setShouldDelete(true);
                SyncDataManager.getInstance(sContext).updateResFileRecordLocally(res);
//                Toast.makeText(sContext, "delete fail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccessWithObject(Object object) {

            }
        });
    }


    // 修改水印接口
    public void modifyTheWatermark(ModifyWatermarkInfo info, boolean needUploadImg) {
        // 假如用户已经登录了
        if (sIsUserInfoValid) {
            Watermark watermark;
            if (info.mEditable) {
                watermark = new EditableWatermark();
            } else {
                watermark = new NotEditableWatermark();
            }
            watermark.setUserId(info.mUserId);
            watermark.setLocalId(info.mLocalId);
            watermark.setObjectId(info.mObjectId);
            watermark.setPath(info.mLocalPath);
            watermark.setTitle(info.mWatermarkTitle);
            watermark.setSaveTime(info.mSaveTime);
            watermark.setCarrayData(info);
            watermark.setOperateType(info.mOperateType);
            watermark.setEditable(info.mEditable);
            watermark.setResArray(info.mFontInfo);

            List<Watermark> list = new ArrayList<>();
            list.add(watermark);
            if (needUploadImg) {
                // 需要重新申请阿里云token
                if (list != null && list.size() > 0) {
                    modifyWatermarkStep1(list);
                }
            } else {
                modifyWatermarkStep2(watermark, info);
            }
        }
    }

    private void modifyWatermarkStep1(List<Watermark> list) {
        // 需要重新申请阿里云token
        if (list != null && list.size() > 0) {
            uploadNewWatermark(list);
        }
    }

    private void modifyWatermarkStep2(final Watermark watermark, ModifyWatermarkInfo info) {
        // 调用修改的接口
        NetWorkRequestManager.getInstacne(sContext).modifyWatermark(sUserId, sAccessToken, watermark.getObjectId(), mApiInterface, new NetWorkRequestManager.NetWorkCallback() {
            @Override
            public void onSuccess() {
                watermark.setShouldModify(false);
                SyncDataManager.getInstance(sContext).updateWatermarkByLocally(watermark);
//                Toast.makeText(sContext, "modifyFinish", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure() {
                watermark.setShouldModify(true);
                SyncDataManager.getInstance(sContext).updateWatermarkByLocally(watermark);
//                Toast.makeText(sContext, "modifyFail", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccessWithObject(Object object) {

            }
        }, info);
    }

    private static boolean getUserInfo() {
        boolean result = false;
        sUserId = SettingInfoMgr.GetSettingInfo(sContext).GetPoco2Id(true);
        sAccessToken = SettingInfoMgr.GetSettingInfo(sContext).GetPoco2Token(true);
        if (!TextUtils.isEmpty(sUserId) && !TextUtils.isEmpty(sAccessToken)) {
            result = true;
        }
        return result;
    }


    private ArrayList<MyLogoRes> filterMyLogoRes() {
        ArrayList<MyLogoRes> resList = MyLogoResMgr.getInstance().GetMyLogoResArr();
        ArrayList<MyLogoRes> filterList = new ArrayList<>();
        for (MyLogoRes item : resList) {
            if (item.m_userId == MyLogoRes.USER_NONE_ID) {
                filterList.add(item);
            }
        }
        return filterList;
    }



    private String makeUpDataForSync(List<Watermark> list) {
        List<WatermarkSyncManager.SyncUploadInfo> objectIdList = new ArrayList<>();
        for (Watermark item : list) {
            if (!TextUtils.isEmpty(String.valueOf(item.getObjectId())) && item.getObjectId() != -1 && item.getUserId().equals(sUserId)) {
                WatermarkSyncManager.SyncUploadInfo uploadInfo = new WatermarkSyncManager.SyncUploadInfo();
                uploadInfo.mObjectId = item.getObjectId();
                uploadInfo.mSaveTime = item.getSaveTime();
                objectIdList.add(uploadInfo);
            }
        }
        String result = JsonHelper.getInstacne().makeUpSyncWaterJson(objectIdList);
        return result;
    }


    public void clear() {
        StorageReceiver.RemoveCloudListener(this);
    }


    public static class SyncUploadInfo {
        public String mSaveTime;
        public int mObjectId;
    }

    public interface SyncManagerCallback {
        void onFinishSuccessful();
        void onFail();
        void onTimeOut();
        void noNeedToSync();
    }

    public static class ModifyWatermarkInfo implements Serializable{
        public String mUserId;
        public int mObjectId;
        public String mWatermarkTitle;
        public String mWatermarkSubTitle;
        public String mWatermarkSummary;
        public String mWatermarkTags;
        public String mWaterContent;
        public String mWatermarkServerUrl;
        public int mLocationId;
        public String mCustomData;
        public String mSaveTime;
        public String mLocalPath;
        public long mVolume;

        // 字体信息
        public ArrayList<FontRes> mFontInfo;

        // 水印类型
        public boolean mEditable;
        public Watermark.OperateType mOperateType;

        // localPart
        public int mLocalId;
    }

    // 只有全部同步都成功，才算同步成功
    private void checkSycnProgress(Watermark watermark, boolean isSucced) {
        if (watermark.getOperateType() == Watermark.OperateType.SYNC) {
            if (!isSucced) {
                mIsSyncFail = true;
            }

            int result = mWatermarkSyncAcidList.indexOf(watermark.getAcid());
            if (result != -1) {
                mWatermarkSyncAcidList.remove((Integer)(watermark.getAcid()));
            }

            if (!(mWatermarkSyncAcidList.size() > 0)) {
                if (mIsBeingSync && mCallback != null) {
                    if (!mIsSyncFail) {
                        mCallback.onFinishSuccessful();
                    } else {
                        mCallback.onFail();
                    }
                    // 移除监听回调
                    setSyncCallback(null);
                }

                if (mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                    mCountDownTimer = null;
                }
                mIsBeingSync = false;
                mIsSyncFail = false;
            }
        }
    }

    public void setSyncCallback(SyncManagerCallback callBack) {
        this.mCallback = callBack;
    }



    // 判断是否正在同步
    public boolean isWatermarkBeingSynchronized() {
        return mIsBeingSync;
    }

    private void startCountDown() {
        mCountDownTimer = new CountDownTimer(SYNC_TIME_OUT_DURATION, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }
            @Override
            public void onFinish() {
                if (mIsBeingSync) {
                    if (mCallback != null) {
                        mCallback.onTimeOut();
                        setSyncCallback(null);
                    }
                }
                if (mCountDownTimer != null) {
                    mCountDownTimer.cancel();
                    mCountDownTimer = null;
                }
                mIsBeingSync = false;
            }
        }.start();
    }




}
