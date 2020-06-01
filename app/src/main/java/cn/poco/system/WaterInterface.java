package cn.poco.system;

import android.content.Context;

import cn.poco.storagesystemlibs.StorageStruct;
import cn.poco.storagesystemlibs.StorageUtils;
import cn.poco.storagesystemlibs.UpdateInfo;
import cn.poco.storagesystemlibs.UploadInfo;
import cn.poco.tianutils.CommonUtils;
import cn.poco.watermarksync.api.IWatermarkSync;
import cn.poco.watermarksync.delegate.WaterStorage;

public class WaterInterface implements  IWatermarkSync, WaterStorage
{
//	private static final String DEV = "http://tw.adnonstop.com/beauty/app/api/interphoto/biz/dev/api/public/index.php";
	private static final String BETA = "http://tw.adnonstop.com/beauty/app/api/interphoto/biz/beta/api/public/index.php";
	private static final String PROD = "http://open.adnonstop.com/interphoto/biz/prod/api/public/index.php";

	protected static WaterInterface sInstance;
	protected static String sVer;
	protected static String sVer2;//带渠道号
	private static String sIMEI;

	private String mBaseUrl;

	private WaterInterface()
	{
		if (SysConfig.IsDebug()) {
			mBaseUrl = BETA;
		} else {
			mBaseUrl = PROD;
		}
	}

	public synchronized static WaterInterface GetInstance(Context context)
	{
		if(sInstance == null)
		{
			sInstance = new WaterInterface();
		}
		//不把context保存到静态变量的目的是避免多activity造成泄漏
		if(context != null)
		{
			sIMEI = CommonUtils.GetIMEI(context);
			sVer2 = SysConfig.GetAppVer(context);
			sVer = CommonUtils.GetAppVer(context);
		}
		return sInstance;
	}


	public String GetIMEI()
	{
		return sIMEI;
	}


	@Override
	public String GetAppName()
	{
		return "interphoto_app_android";
	}

	@Override
	public String GetAppVer()
	{
		return sVer;
	}

	@Override
	public String GetMKey()
	{
		return sIMEI;
	}

	@Override
	public String getWatermarkSyncData() {
		return mBaseUrl + "?r=WaterMark/GetWaterMarkData";
	}

	@Override
	public String deleteWatermark() {
		return mBaseUrl + "?r=WaterMark/Del";
	}

	@Override
	public String modifyWatermark() {
		return mBaseUrl + "?r=WaterMark/Update";
	}


	@Override
	public String GetTokenUrl() {
		return mBaseUrl + "?r=Common/AliyunOSSToken";
	}

	@Override
	public String MakeUpdateMyWebData(UpdateInfo info) {
		return null;
	}

	@Override
	public UploadInfo GetUploadInfo(StorageStruct str, int num) {
		return StorageUtils.GetTokenInfo(str.mUserId, str.mAccessToken, num, str.mIsAlbum, this);
	}

	@Override
	public String GetUpdateMyWebUrl() {
		return mBaseUrl + "?r=WaterMark/Save";
	}

	@Override
	public String getUserWatermarkList() {
		return mBaseUrl + "?r=WaterMark/GetList";
	}
}
