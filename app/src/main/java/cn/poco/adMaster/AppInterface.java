package cn.poco.adMaster;

import android.content.Context;
import android.webkit.WebView;

import com.adnonstop.admasterlibs.IAd;

import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;

/**
 * Created by Raining on 2017/8/18.
 */

public class AppInterface implements IAd
{
	protected static AppInterface sInstance;
	protected static String sVer;
	protected static String sVer1;
	protected static String sIMEI;
	protected static String sUA;

	public synchronized static AppInterface GetInstance(Context context)
	{
		if(sInstance == null)
		{
			sInstance = new AppInterface();
		}
		if(context != null)
		{
			sVer = SysConfig.GetAppVer(context);
			sVer1 = SysConfig.GetAppVerNoSuffix(context);
			sIMEI = CommonUtils.GetIMEI(context);
			try
			{
				WebView view = new WebView(context);
				sUA = view.getSettings().getUserAgentString();
//				System.out.println("sUA: " + sUA);
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
		return sInstance;
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
	public String GetAdUrl(Context context)
	{
		if(SysConfig.GetAppVer(context).contains("88.8.8"))
		{
			return "http://tw.adnonstop.com/zt/web/index.php?r=api/tpad/data/list";
		}
		else
		{
			return "http://union.adnonstop.com/?r=api/tpad/data/list";
		}
	}

	@Override
	public String GetAdUserId(Context context)
	{
		return null;
	}

	@Override
	public String GetAdAppChannel(Context context)
	{
		String channel = "";
		String ver = SysConfig.GetAppVer(context);
		if(ver != null)
		{
			int index = ver.indexOf("_");
			if(index > 0 && index + 1 < ver.length())
			{
				channel = ver.substring(index + 1);
			}
		}
		return channel;
	}

	@Override
	public String GetUserAgentString(Context context)
	{
		return sUA;
	}

	@Override
	public String GetAdDefPostApi(Context context)
	{
		if(SysConfig.GetAppVer(context).contains("88.8.8"))
		{
			return "http://tw.adnonstop.com/zt/web/index.php?r=api/v1/appdata/add";
		}
		else
		{
			return "http://zt.adnonstop.com/index.php?r=api/v1/appdata/add";
		}
	}

	@Override
	public String GetAdAppVer()
	{
		return sVer1;
	}

	@Override
	public String GetAdAppName(Context context)
	{
		return "beauty_business";
	}
}
