package cn.poco.watermarksync.watermarkstorage;

import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.poco.resource.BaseRes;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.IDownload;
import cn.poco.storagesystemlibs.AbsStorageService;
import cn.poco.storagesystemlibs.IStorage;
import cn.poco.storagesystemlibs.ServiceStruct;
import cn.poco.storagesystemlibs.StorageDownloadMgr;
import cn.poco.storagesystemlibs.StorageRes;
import cn.poco.storagesystemlibs.StorageStruct;

/**
 * Created by Shine on 2017/3/6.
 */

public abstract class WatermarkStorageService extends AbsStorageService{


    public static final String ACTION_ONE_COMPLETE_NOTIFY_ALL = "cn.poco.watermarksync.MSG2";

    protected ArrayList<WatermarkStorageService.UploadTask> mUploadArr = new ArrayList<>();
    protected ArrayList<WatermarkStorageService.DownloadTask> mDownloadArr = new ArrayList<>();



    @Override
    protected String GetStorageTempPath() {
        return DownloadMgr.getInstance().MY_LOGO_PATH + File.separator + ".temp";
    }

    @Override
    protected String GetStoragePath() {
        return DownloadMgr.getInstance().MY_LOGO_PATH;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        ACTION_NOTIFY_LOCAL = getApplicationContext().getPackageName() + ".MYMSG2";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent != null)
        {
            int type = intent.getIntExtra("type", 0);
            switch(type)
            {
                case UPLOAD :
                    ArrayList<ServiceStruct> str = intent.getParcelableArrayListExtra("str");
                    new WatermarkStorageService.UploadTask(getApplicationContext(), str, GetIStorage());
                    break;

                case DOWNLOAD :
                {
                    ServiceStruct strDownload = intent.getParcelableExtra("str");
                    new WatermarkStorageService.DownloadTask(strDownload, GetIStorage());
                    break;
                }

                default:
                    break;
            }
        }
        onStart(intent, startId);
        int result = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ECLAIR ? START_STICKY_COMPATIBILITY : START_STICKY;
        return result;
    }

    /**
     * 上传/下载单个文件进度
     *
     * @param type {@link AbsStorageService#UPLOAD_SINGLE_PROGRESS} or
     *             {@link AbsStorageService#DOWNLOAD_SINGLE_PROGRESS}
     */
    protected void OnSingleProgress(int type, ServiceStruct str, int progress)
    {
        Intent intent = new Intent(ACTION_NOTIFY_LOCAL);
        intent.putExtra("type", type);
        intent.putExtra("str", str);
        intent.putExtra("progress", progress);
        this.sendBroadcast(intent);
    }

    /**
     * 上传/下载单个文件成功
     *
     * @param type {@link AbsStorageService#UPLOAD_SINGLE_COMPLETE} or
     *             {@link AbsStorageService#DOWNLOAD_SINGLE_COMPLETE}
     */
    protected void OnSingleComplete(int type, ServiceStruct str)
    {
        Intent intent = new Intent(ACTION_ONE_COMPLETE_NOTIFY_ALL);
        intent.putExtra("type", type);
        intent.putExtra("str", str);
        this.sendBroadcast(intent);
    }

    /**
     * 上传/下载单个文件错误
     *
     * @param type {@link AbsStorageService#UPLOAD_SINGLE_FAIL} or
     *             {@link AbsStorageService#DOWNLOAD_SINGLE_FAIL}
     */
    protected void OnSingleFail(int type, ServiceStruct str)
    {
        Intent intent = new Intent(ACTION_NOTIFY_LOCAL);
        intent.putExtra("type", type);
        intent.putExtra("str", str);
        this.sendBroadcast(intent);
    }

    /**
     * 上传/下载发生未知错误
     *
     * @param type {@link AbsStorageService#UPLOAD_ERROR} or
     *             {@link AbsStorageService#DOWNLOAD_ERROR}
     */
    protected void OnError(int type, ServiceStruct str)
    {
        Intent intent = new Intent(ACTION_NOTIFY_LOCAL);
        intent.putExtra("type", type);
        intent.putExtra("str", str);
        this.sendBroadcast(intent);
    }

    protected WatermarkStorage MakeAliyunStorage(Context context, List<ServiceStruct> str, WatermarkStorage.Callback cb, IStorage iStorage)
    {
        return new WatermarkStorage(context, str, cb, iStorage);
    }

    protected class UploadTask
    {
        public ArrayList<ServiceStruct> mStr;
        public IStorage mIStorage;

        public WatermarkStorage mAli;


        public UploadTask(Context context, ArrayList<ServiceStruct> strList, IStorage iStorage)
        {
            mStr = strList;
            mIStorage = iStorage;

            if(strList != null)
            {
                mAli = MakeAliyunStorage(context, mStr, new WatermarkStorage.Callback()
                {
                    @Override
                    public void onProgress(int currentSize, int totalSize, StorageStruct str)
                    {
                        WatermarkStorageService.this.OnSingleProgress(UPLOAD_SINGLE_PROGRESS, (ServiceStruct) str, (int)((float)currentSize / (float)totalSize * 100));
                    }

                    @Override
                    public void onSuccess(StorageStruct str)
                    {
                        WatermarkStorageService.this.OnSingleComplete(UPLOAD_SINGLE_COMPLETE, (ServiceStruct) str);

                        IsAllComplete();
                    }

                    @Override
                    public void onFailure(StorageStruct str)
                    {
                        WatermarkStorageService.this.OnSingleFail(UPLOAD_SINGLE_FAIL, (ServiceStruct) str);

                        IsAllComplete();
                    }

                    @Override
                    public void onOtherFailure(List<StorageStruct> strList)
                    {
                        if (!strList.isEmpty()) {
                            for (StorageStruct item : strList) {
                                onFailure(item);
                            }
                        }
                    }

                    public void IsAllComplete()
                    {
                        mUploadArr.remove(UploadTask.this);
                    }
                }, mIStorage);

                //加入运行中的队列
                mUploadArr.add(UploadTask.this);
            }
            else
            {
                WatermarkStorageService.this.OnError(UPLOAD_ERROR, null);
            }
        }

        public void Cancel()
        {
            mAli.Cancel();
        }
    }

    protected class DownloadTask
    {
        public final ServiceStruct mStr;
        public final IStorage mIStorage;

        public int mDownloadId;

        public DownloadTask(ServiceStruct str, IStorage iStorage)
        {
            mStr = str;
            mIStorage = iStorage;
            if(mStr.mAliUrl != null)
            {
                StorageRes res = new StorageRes();
                res.m_type = BaseRes.TYPE_NETWORK_URL;
                res.url_thumb = mStr.mAliUrl;
                res.m_id = mStr.mAcId;
                mDownloadId = sDownloader.DownloadRes(res, false, true, false, new StorageDownloadMgr.Callback()
                {
                    @Override
                    public void OnProgress(int downloadId, IDownload res, int progress)
                    {
                        WatermarkStorageService.this.OnSingleProgress(DOWNLOAD_SINGLE_PROGRESS, mStr, progress);
                    }

                    @Override
                    public void OnComplete(int downloadId, IDownload res)
                    {
                        mStr.mPath = ((BaseRes)res).m_thumb.toString();
                        WatermarkStorageService.this.OnSingleComplete(DOWNLOAD_SINGLE_COMPLETE, mStr);

                        //从队列中移除
                        mDownloadArr.remove(DownloadTask.this);
                    }

                    @Override
                    public void OnFail(int downloadId, IDownload res)
                    {
                        WatermarkStorageService.this.OnSingleFail(DOWNLOAD_SINGLE_FAIL, mStr);
                        //从队列中移除
                        mDownloadArr.remove(DownloadTask.this);
                    }
                });
                //加入运行中的队列
                mDownloadArr.add(DownloadTask.this);
            }
            else
            {
                WatermarkStorageService.this.OnError(DOWNLOAD_ERROR, mStr);
            }
        }

        public void Cancel()
        {
            sDownloader.CancelDownload(mDownloadId);
        }
    }


    public abstract IStorage GetIStorage();

}
