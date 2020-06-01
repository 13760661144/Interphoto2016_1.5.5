package cn.poco.resource;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import cn.poco.tianutils.NetCore2;
import cn.poco.utils.MyNetCore;

/**
 * Created by admin on 2017/7/13.
 */

public class ToggleMgr
{
	public static final String CLOUD_URL = "https://beauty-material.adnonstop.com/API/beauty_camera/json_api/switch_android.php";
	public static boolean m_showCircle = false;
	public static void InitCloudData2(Context context)
	{
		NetCore2 net = null;
		try
		{
			net = new MyNetCore(context);
			String url = CLOUD_URL;
			NetCore2.NetMsg msg = net.HttpGet(url);
			if(msg != null && msg.m_stateCode == HttpURLConnection.HTTP_OK && msg.m_data != null)
			{
				JSONArray jsonArr = new JSONArray(new String(msg.m_data));
				Object obj;
				int arrLen = jsonArr.length();
				for(int i = 0; i < arrLen; i++)
				{
					obj = jsonArr.get(i);
					if(obj instanceof JSONObject)
					{
						if(((JSONObject)obj).has("circle140"))
						{
							String str = ((JSONObject)obj).getString("circle140");
							if("on".equals(str))
							{
								m_showCircle = true;
							}
							break;
						}
					}
				}
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
	}
}
