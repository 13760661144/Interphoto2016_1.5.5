package cn.poco.watermarksync.watermarkstorage;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.poco.storagesystemlibs.AbsAliyunBase;
import cn.poco.storagesystemlibs.IStorage;
import cn.poco.storagesystemlibs.ServiceStruct;
import cn.poco.storagesystemlibs.StorageStruct;
import cn.poco.storagesystemlibs.UploadInfo;
import cn.poco.tianutils.NetCore2;
import cn.poco.watermarksync.api.UploadWatermarkCallbackApi;
import cn.poco.watermarksync.delegate.WaterStorage;
import cn.poco.watermarksync.manager.NetWorkRequestManager;
import cn.poco.watermarksync.manager.WatermarkSyncManager;
import cn.poco.watermarksync.model.Watermark;
import cn.poco.watermarksync.model.WatermarkUpdateInfo;

public class WatermarkStorage extends AbsAliyunBase
{
    private static final String TAG = "WatermarkStoraqge";
	public static final int PROGRESS = 0x1;
	public static final int SUCCESS = 0x2;
	public static final int FAILURE = 0x4;
	public static final int OTHER_FAILURE = 0x8;

	protected final List<ServiceStruct> mStrList;
	protected final WatermarkStorage.Callback mCb;
	protected final WaterStorage mIStorage;
	protected OSSAsyncTask<PutObjectResult> mTask;

	protected boolean mIsCancel;
	private Context mContext;

	protected UploadInfo info;
	private Map<String, ServiceStruct> urlStructDictionary = new HashMap<>();

	public WatermarkStorage(final Context context, final List<ServiceStruct> str, WatermarkStorage.Callback cb, IStorage iStorage)
	{
		mContext = context;
		mStrList = str;
		mCb = cb;
		mIStorage = (WaterStorage) iStorage;
		if(mStrList != null && mStrList.size() > 0)
		{
			final MyHandler handler = new MyHandler(Looper.getMainLooper(), mCb);
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					info = mIStorage.GetUploadInfo(mStrList.get(0), mStrList.size());
					if(info != null && info.mUrls != null && info.mUrls.length > 0)
					{
						OSSClient oss = GetOSS(context);
						synchronized(WatermarkStorage.this)
						{
							if(oss != null && !mIsCancel)
							{
								//构造上传请求
								for (ServiceStruct item : mStrList) {
									final ServiceStruct mCurrentStr = item;
									if (item.mPath != null) {
										String objectKey = info.mKeys[mStrList.indexOf(item)] + NetCore2.GetFileSuffix(item.mPath);
										PutObjectRequest put = new PutObjectRequest(info.mBucketName, objectKey, item.mPath);
//								mStr.mUrl = "http://" + info.mBucketName + "." + ALIYUN_ENDPOINT + "/" + objectKey;
										mCurrentStr.mAliUrl = info.mUrls[mStrList.indexOf(item)] + NetCore2.GetFileSuffix(item.mPath);
										urlStructDictionary.put(item.mPath, mCurrentStr);

										//回调服务器
										final Watermark watermark = (Watermark) mCurrentStr.mEx;
										// 异步上传时可以设置进度回调
										put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>()
										{
											@Override
											public void onProgress(PutObjectRequest request, final long currentSize, final long totalSize)
											{
												Message msg = handler.obtainMessage();
												msg.what = PROGRESS;
												msg.arg1 = (int)currentSize;
												msg.arg2 = (int)totalSize;
												msg.obj = mCurrentStr;
												handler.sendMessage(msg);
											}
										});
										mTask = oss.asyncPutObject(put, new OSSCompletedCallback<PutObjectRequest, PutObjectResult>()
										{
											@Override
											public void onSuccess(PutObjectRequest putObjectRequest, PutObjectResult putObjectResult)
											{
												String key = putObjectRequest.getUploadFilePath();
												final ServiceStruct struct = urlStructDictionary.get(key);

												if (watermark.getObjectId() == Watermark.VALUE_NONE) {
													final Message msg = handler.obtainMessage();
													WatermarkUpdateInfo updateInfo = new WatermarkUpdateInfo();
													updateInfo.mAccessToken = struct.mAccessToken;
													updateInfo.mUserId = struct.mUserId;
													final Watermark watermark = (Watermark) struct.mEx;
													updateInfo.mTitle = watermark.getTitle();
													updateInfo.mCoverImgUrl = struct.mAliUrl;
													updateInfo.mSaveTime = watermark.getSaveTime();
													updateInfo.mFontInfo = watermark.getResArray();
													updateInfo.mFileVolume = watermark.getVolume();
													WatermarkSyncManager.getInstacne(mContext).getUploadCallback(updateInfo, new NetWorkRequestManager.NetWorkCallback() {
														@Override
														public void onSuccess() {

														}

														@Override
														public void onFailure() {
															msg.what = FAILURE;
															handler.sendMessage(msg);
														}

														@Override
														public void onSuccessWithObject(Object object) {
															msg.what = SUCCESS;
															UploadWatermarkCallbackApi callBackApi = null;
															if (object instanceof UploadWatermarkCallbackApi) {
																callBackApi = (UploadWatermarkCallbackApi) object;
															}
															if (callBackApi != null) {
																if (watermark.getObjectId() == Watermark.VALUE_NONE) {
																	((Watermark)struct.mEx).setObjectId(callBackApi.mObjectId);
																}
																msg.obj = struct;
																handler.sendMessage(msg);
															}
														}
													});
												} else {
													Message msg = handler.obtainMessage();
													msg.what = SUCCESS;
													msg.obj = struct;
													handler.sendMessage(msg);
												}
											}

											@Override
											public void onFailure(PutObjectRequest putObjectRequest, ClientException e, ServiceException e1)
											{
												Message msg = handler.obtainMessage();
												msg.what = FAILURE;
												msg.obj = mCurrentStr;
												handler.sendMessage(msg);
											}
										});

									} else {
										Log.i(TAG, "上传失败，上传的水印路径为空");
									}
								}

							}
							return;
						}
					}
					// 请求token都失败
					Message msg = handler.obtainMessage();
					msg.what = OTHER_FAILURE;
					msg.obj = mStrList;
					handler.sendMessage(msg);
				}
			}).start();
		}
	}

	@Override
	protected OSSFederationToken GetFederationToken()
	{
		if(info != null)
		{
			return new OSSFederationToken(info.mAccessKeyId, info.mAccessKeySecret, info.mSecurityToken, info.mExpire);
		}
		return null;
	}

	public synchronized void Cancel()
	{
		mIsCancel = true;

		if(mTask != null)
		{
			try
			{
				mTask.cancel();
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
			mTask = null;
		}
	}

	protected static class MyHandler extends Handler
	{
		protected WatermarkStorage.Callback m_cb;

		public MyHandler(Looper looper, WatermarkStorage.Callback cb)
		{
			super(looper);

			m_cb = cb;
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case PROGRESS:
					if(m_cb != null && msg.obj instanceof StorageStruct)
					{
						m_cb.onProgress(msg.arg1, msg.arg2, (StorageStruct)msg.obj);
					}
					break;

				case SUCCESS:
					if(m_cb != null && msg.obj instanceof StorageStruct)
					{
						m_cb.onSuccess((StorageStruct)msg.obj);
					}
					break;

				case FAILURE:
					if(m_cb != null && msg.obj instanceof StorageStruct)
					{
						m_cb.onFailure((StorageStruct)msg.obj);
					}
					break;

				case OTHER_FAILURE:
					if(m_cb != null)
					{
						m_cb.onOtherFailure((List<StorageStruct>)msg.obj);
						ClearAll();
					}
					break;


				default:
					break;
			}
		}

		public void ClearAll()
		{
			m_cb = null;
		}
	}

	public interface Callback
	{

		void onProgress(int currentSize, int totalSize, StorageStruct str);

		void onSuccess(StorageStruct str);

		void onFailure(StorageStruct str);

		void onOtherFailure(List<StorageStruct> strList);

	}
}
