package cn.poco.system;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;

import java.util.HashMap;

import cn.poco.blogcore.Tools;
import cn.poco.loginlibs.ILogin;
import cn.poco.loginlibs.LoginUtils;
import cn.poco.loginlibs.info.UploadToken;
import cn.poco.setting.SettingInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statisticlibs.IStat;
import cn.poco.storagesystemlibs.IStorage;
import cn.poco.storagesystemlibs.StorageStruct;
import cn.poco.storagesystemlibs.StorageUtils;
import cn.poco.storagesystemlibs.UpdateInfo;
import cn.poco.storagesystemlibs.UploadInfo;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.NetState;

public class AppInterface implements  ILogin,IStat,IStorage
{
//	private static final String DEV = "http://tw.adnonstop.com/beauty/app/api/interphoto/biz/dev/api/public/index.php";
	private static final String BETA = "http://tw.adnonstop.com/beauty/app/api/interphoto/biz/beta/api/public/index.php";
	private static final String PROD = "http://open.adnonstop.com/interphoto/biz/prod/api/public/index.php";

	protected static AppInterface sInstance;
	protected static String sVer;
	protected static String sVer2;//带渠道号
	private static String sIMEI;

	private String mBaseUrl;

	private AppInterface()
	{
		if (SysConfig.IsDebug()) {
			mBaseUrl = BETA;
		} else {
			mBaseUrl = PROD;
		}
	}

	public synchronized static AppInterface GetInstance(Context context)
	{
		if(sInstance == null)
		{
			sInstance = new AppInterface();
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


	@Override
	public String GetVerifyUrl()
	{
		return mBaseUrl+"?r=MessageVerify/SendSmsVerifyCode";
	}

	@Override
	public String GetCheckVerifyUrl()
	{
		return mBaseUrl+"?r=MessageVerify/CheckSmsVerifyCode";
	}

	@Override
	public String GetMobileRegisterUrl()
	{
		return mBaseUrl+"?r=OAuth/Register";
	}

	@Override
	public String GetFillRegisterInfoUrl()
	{
		return mBaseUrl+"?r=OAuth/RegisterUserInfo";
	}

	@Override
	public String GetUserLoginUrl()
	{
		return mBaseUrl+"?r=OAuth/Login";
	}

	@Override
	public String GetForgetPassWordUrl()
	{
		return mBaseUrl+"?r=OAuth/Forget";
	}

	@Override
	public String GetChangePasswordUrl()
	{
		return mBaseUrl+"?r=OAuth/ChangePassword";
	}

	@Override
	public String GetBindMobileUrl()
	{
		return mBaseUrl+"?r=OAuth/BindMobile";
	}

	@Override
	public String GetRefreshTokenUrl()
	{
		return mBaseUrl+"?r=OAuth/RefreshToken";
	}

	@Override
	public String GetUserInfoUrl()
	{
		return mBaseUrl+"?r=User/GetUserInfo";
	}

	@Override
	public String GetUpdateUserInfoUrl()
	{
		return mBaseUrl+"?r=User/UpdateUserInfo";
	}

	@Override
	public String GetTPLoginUrl()
	{
		return mBaseUrl+"?r=TPOAuth/Auth";
	}

	@Override
	public String GetUploadHeadThumbUrl(Context context)
	{
		if(ConnectivityManager.TYPE_WIFI == NetState.GetConnectNet(context))
		{
			return "http://os-upload-wifi.poco.cn/poco/upload";
		}
		else
		{
			return "http://os-upload.poco.cn/poco/upload";
		}
	}

	@Override
	public String GetUploadHeadThumbTokenUrl()
	{
		return mBaseUrl + "?r=Common/AliyunOSSToken";
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
	public String GetTokenUrl() {
//		return mBaseUrl + "?r=Common/AliyunOSSToken";
		return "http://open.adnonstop.com/interphoto/apidoc/#api-Common-AliyunOSSToken ";
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
//		return "http://open.adnonstop.com/interphoto/apidoc/#api-Common-AliyunOSSToken ";
		return null;
	}

	@Override
	public String GetStatOldLiveUrl()
	{
		return "http://phtj.poco.cn/phone_tj.php";
	}

	@Override
	public String GetStatOldOfflineUrl()
	{
		return "http://phtjp.poco.cn/phone_tj_post.php";
	}

	@Override
	public String GetStatOnlineUrl()
	{
		String out = null;

		if(SysConfig.IsDebug())
		{
			out = "http://optimus.adnonstop.com/collect-beta";
		}
		else
		{
			out = "http://optimus.adnonstop.com/collect";
		}
		return out;
	}

	@Override

	public String GetStatOfflineUrl()
	{
		String out = null;

		if(SysConfig.IsDebug())
		{
			out = "http://optimus.adnonstop.com/collect-multi-beta";
		}
		else
		{
			out = "http://optimus.adnonstop.com/collect-multi";
		}
		return out;
	}

	@Override
	public String GetStatIMEI()
	{
		return sIMEI;
	}

	@Override
	public String GetStatTJVer()
	{
		return "3";
	}

	@Override
	public String GetStatAppVer()
	{
		return sVer2;
	}

	@Override
	public String GetStatAppId()
	{
		return "129_3";
	}

	@Override
	public String GetStatUserId(Context context)
	{
		return SettingInfo.GetPoco2Id(SettingInfoMgr.GetSettingSP(context), true);
	}

	@Override
	public String GetStatUserToken(Context context)
	{
		return SettingInfo.GetPoco2Token(SettingInfoMgr.GetSettingSP(context), true);
	}

	@Override
	public synchronized UploadToken GetStatNearToken(Context context)
	{
		UploadToken out = null;

		String userId = GetStatUserId(context);
		String userToken = GetStatUserToken(context);

		if(userId != null && userId.length() > 0 && userToken != null && userToken.length() > 0)
		{
			HashMap<String, String> data = new HashMap<>();
			CommonUtils.SP_ReadSP(context, LoginUtils.GPS_CONFIG_SP_NAME, data);

			String identify = data.get(LoginUtils.GPS_TOKEN_IDENTIFY);
			String expire = data.get(LoginUtils.GPS_TOKEN_EXPIRE);
			String accessKey = data.get(LoginUtils.GPS_TOKEN_ACCESS_KEY);
			String accessToken = data.get(LoginUtils.GPS_TOKEN_ACCESS_TOKEN);

			if(identify == null || identify.length() <= 0 || expire == null || expire.length() <= 0 || accessKey == null || accessKey.length() <= 0 || accessToken == null || accessToken.length() <= 0 || Tools.isBindExpired(expire, System.currentTimeMillis() / 1000, 3600))
			{
				out = LoginUtils.getUploadHeadThumbToken(userId, userToken, "jpg", this);
				if(out != null)
				{
					data.put(LoginUtils.GPS_TOKEN_IDENTIFY, out.mIdentify);
					data.put(LoginUtils.GPS_TOKEN_EXPIRE, out.mExpireTime);
					data.put(LoginUtils.GPS_TOKEN_ACCESS_KEY, out.mAccessKey);
					data.put(LoginUtils.GPS_TOKEN_ACCESS_TOKEN, out.mAccessToken);
					CommonUtils.SP_SaveMap(context, LoginUtils.GPS_CONFIG_SP_NAME, data);
				}
			}
		}

		if(out == null)
		{
			out = new UploadToken();
			out.mAccessKey = "ed3a70f144ca4d9e88af1ba4da26f8de5aea9d0d";
			out.mAccessToken = "f0c80d5ed6533fa51ae9442e5c440a00b6bd42e8";
			out.mExpireTime = "2114352000";
			out.mIdentify = "anonymous";
		}

		return out;
	}

	@Override
	public String GetStatUploadGpsUrl(Context context)
	{
		return "http://near-api.adnonstop.com/location/record";
	}

	@Override
	public long GetMemoryMB(Context context)
	{
		return Runtime.getRuntime().maxMemory() / 1048576;
	}

	@Override
	public String GetPhoneName(Context context)
	{
		return Build.MODEL;
	}

	@Override
	public boolean IsDebug(Context context)
	{
		return SysConfig.IsDebug();
	}
}
