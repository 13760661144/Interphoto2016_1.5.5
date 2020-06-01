package cn.poco.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;

import java.util.HashMap;
import java.util.List;

import cn.poco.tianutils.NetCore2;
import cn.poco.tianutils.NetState;

public class MyNetCore extends NetCore2
{
	protected Context m_context;

	public MyNetCore(Context context)
	{
		m_context = context;
	}

	@Override
	public synchronized NetMsg HttpGet(String url, HashMap<String, String> headerParams, String savePath, int bufSize, Handler progressCallback)
	{
		return super.HttpGet(MyNetCore.GetPocoUrl(m_context, url), headerParams, savePath, bufSize, progressCallback);
	}

	@Override
	public synchronized NetMsg HttpGet(String url, HashMap<String, String> headerParams)
	{
		return super.HttpGet(MyNetCore.GetPocoUrl(m_context, url), headerParams);
	}

	@Override
	public synchronized NetMsg HttpPost(String url, HashMap<String, String> headerParams, HashMap<String, String> bodyParams, List<FormData> datas)
	{
		return super.HttpPost(MyNetCore.GetPocoUrl(m_context, url), headerParams, bodyParams, datas);
	}

	protected static final String POCO_URL_TAG_M = "//img-m[.]";
	protected static final String POCO_URL_VAL_M = "//img-m.";
	protected static final String POCO_URL_TAG_WIFI = "//img-wifi[.]";
	protected static final String POCO_URL_VAL_WIFI = "//img-wifi.";
	protected static final String POCO_URL_TAG_M_IP = "//img-m-ip[.]";
	protected static final String POCO_URL_VAL_M_IP = "//img-m-ip.";
	protected static final String POCO_URL_TAG_WIFI_IP = "//img-wifi-ip[.]";
	protected static final String POCO_URL_VAL_WIFI_IP = "//img-wifi-ip.";

	public static String GetPocoUrl(Context context, String url)
	{
		if(context != null && url != null && url.length() > 0)
		{
			if(ConnectivityManager.TYPE_WIFI == NetState.GetConnectNet(context))
			{
				url = url.replaceAll(POCO_URL_TAG_M, POCO_URL_VAL_WIFI);
				url = url.replaceAll(POCO_URL_TAG_M_IP, POCO_URL_VAL_WIFI_IP);
			}
			else
			{
				url = url.replaceAll(POCO_URL_TAG_WIFI, POCO_URL_VAL_M);
				url = url.replaceAll(POCO_URL_TAG_WIFI_IP, POCO_URL_VAL_M_IP);
			}
		}

		return url;
	}
}
