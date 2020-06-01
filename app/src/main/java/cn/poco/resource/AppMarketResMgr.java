package cn.poco.resource;

import android.content.Context;
import android.icu.text.UFormat;
import android.util.Base64;
import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.login.util.UserMgr;
import cn.poco.loginlibs.info.UserInfo;
import cn.poco.setting.SettingInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.NetCore2;
import cn.poco.utils.MyNetCore;

public class AppMarketResMgr
{
	public static String CLOUD_BASE_URL = "http://zt.adnonstop.com/index.php?r=api/recommend/app/list";
	public static String CLOUD_BASE_URL_BETA = "http://tw.adnonstop.com/zt/web/index.php?r=api/recommend/app/list";

	protected static String MakeCloudUrl(Context context)
	{
		JSONObject postJson = new JSONObject();
		try
		{
			JSONObject paramJson = new JSONObject();
			UserInfo userInfo = UserMgr.ReadCache(context);
			SettingInfo settingInfo = SettingInfoMgr.GetSettingInfo(context);
			if(userInfo != null && settingInfo != null)
			{
				paramJson.put("user_id", userInfo.mUserId);
				paramJson.put("access_token", settingInfo.GetPoco2Token(false));
			}

			String param = new StringBuilder().append("poco_").append(paramJson.toString()).append("_app").toString();
			String signStr = CommonUtils.Encrypt("MD5", param);
			String signCode = signStr.substring(5, (signStr.length() - 8));

			postJson.put("version",SysConfig.GetAppVerNoSuffix(context));
			postJson.put("os_type", "android");
			postJson.put("ctime", System.currentTimeMillis());
			postJson.put("app_name", "beauty_business");
			postJson.put("is_enc", 0);
			postJson.put("imei", CommonUtils.GetIMEI(context));
			postJson.put("sign_code", signCode);
			postJson.put("param", paramJson);
			postJson.put("come_from", "interphoto");
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}


		String url;
		if(SysConfig.GetAppVer(context).contains("88.8.8"))
		{
			url = CLOUD_BASE_URL_BETA;
		}
		else
		{
			url = CLOUD_BASE_URL;
		}
		StringBuilder builder = new StringBuilder(url);
		builder.append("&req=");
		builder.append(new String(Base64.encode(postJson.toString().getBytes(), Base64.NO_WRAP)));
		return builder.toString();
	}

	public static ArrayList<AppMarketRes> ReadCloudResArr(Context context)
	{
		ArrayList<AppMarketRes> out = new ArrayList<AppMarketRes>();

		NetCore2 net = null;
		try
		{
			net = new MyNetCore(context);
			NetCore2.NetMsg msg = net.HttpGet(MakeCloudUrl(context));
			if(msg != null && msg.m_stateCode == HttpURLConnection.HTTP_OK && msg.m_data != null)
			{
				out.addAll(ReadResArr(msg.m_data));
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(net != null)
			{
				net.ClearAll();
			}
		}

		return out;
	}

	public static ArrayList<AppMarketRes> ReadResArr(byte[] data) throws JSONException
	{
//		String ddd = new String(data);
//		System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaa" + ddd);
		ArrayList<AppMarketRes> out = new ArrayList<AppMarketRes>();
		JSONObject json = new JSONObject(new String(data));
		JSONObject json1 = json.getJSONObject("data");
		JSONArray jsonArr = json1.getJSONArray("ret_data");
		AppMarketRes item = null;
		String temp = null;
		if(jsonArr != null)
		{
			for(int i = 0; i < jsonArr.length(); i ++)
			{
				JSONObject json2 = jsonArr.getJSONObject(i);
				temp = json2.getString("type");
				int classID = 0;
				if(temp != null && temp.length() > 0)
				{
					classID = Integer.parseInt(temp);
				}
				String className = json2.getString("type_name");
				JSONArray jsonArr1 = json2.getJSONArray("list");
				if(jsonArr1 != null)
				{
					JSONObject json3;
					for(int j = 0; j < jsonArr1.length(); j ++)
					{
						json3 = jsonArr1.getJSONObject(j);
						item = new AppMarketRes();
						item.m_name = json3.getString("app_name");
						item.m_info = json3.getString("description");
						item.m_downloadUrl = json3.getString("download_url");
						item.url_thumb = json3.getString("icon");
						item.m_classID = classID;
						item.m_className = className;
						out.add(item);
					}
				}
			}
		}
		return out;
	}
}
