package cn.poco.resource;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;

import com.adnonstop.resourcelibs.DataFilter;
import com.adnonstop.resourcelibs.MemCache4UISyncBaseResMgr;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import cn.poco.framework.EventID;
import cn.poco.framework.MyFramework2App;
import cn.poco.system.SysConfig;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.NetCore2;
import cn.poco.utils.MyNetCore;

/**
 * Created by admin on 2017/12/27.
 */

public class SwitchResMgr2 extends MemCache4UISyncBaseResMgr<SwitchRes, ArrayList<SwitchRes>>
{
	private static SwitchResMgr2 sInstance;

	private SwitchResMgr2()
	{
	}

	public synchronized static SwitchResMgr2 getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new SwitchResMgr2();
		}
		return sInstance;
	}

	@Override
	public int GetResArrSize(ArrayList<SwitchRes> arr)
	{
		if(arr != null)
			return arr.size();
		return 0;
	}

	@Override
	public ArrayList<SwitchRes> MakeResArrObj()
	{
		return new ArrayList<>();
	}

	@Override
	public boolean ResArrAddItem(ArrayList<SwitchRes> arr, SwitchRes item)
	{
		if(arr != null && item != null)
		{
			arr.add(item);
			return true;
		}
		return false;
	}

	@Override
	protected ArrayList<SwitchRes> sync_raw_GetLocalRes(Context context, DataFilter filter)
	{
		return null;
	}

	@Override
	protected int GetLocalEventId()
	{
		return 0;
	}

	@Override
	protected Object sync_raw_ReadSdcardData(Context context, DataFilter filter)
	{
		return null;
	}

	@Override
	protected ArrayList<SwitchRes> sync_DecodeSdcardRes(Context context, DataFilter filter, Object data)
	{
		return null;
	}

	@Override
	protected int GetSdcardEventId()
	{
		return 0;
	}

	@Override
	protected void sync_raw_SaveSdcardRes(Context context, ArrayList<SwitchRes> arr)
	{

	}

	@Override
	protected Object sync_raw_ReadCloudData(Context context, DataFilter filter)
	{
		byte[] data = null;

		MyNetCore net = null;
		try
		{
			net = new MyNetCore(context);
			NetCore2.NetMsg msg = net.HttpGet(GetCloudUrl(context));
			if(msg != null && msg.m_stateCode == HttpURLConnection.HTTP_OK && msg.m_data != null)
			{
				data = msg.m_data;
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

		return data;
	}

	protected String GetCloudUrl(Context context) throws Exception
	{
		String url;
		if(SysConfig.GetAppVer(context).contains("88.8.8"))
		{
			url = "http://tw.adnonstop.com/beauty/app/api/interphoto/biz/beta/api/public/index.php?r=switch/init/getdata";
		}
		else
		{
			url = "http://open.adnonstop.com/interphoto/biz/prod/api/public/index.php?r=switch/init/getdata";
		}
		JSONObject json = new JSONObject();
		JSONObject paramJson = new JSONObject();
		String param = new StringBuilder().append("poco_").append(paramJson.toString()).append("_app").toString();
		String signStr = CommonUtils.Encrypt("MD5", param);
		String signCode = signStr.substring(5, (signStr.length() - 8));
		json.put("sign_code", signCode);
		json.put("version", SysConfig.GetAppVer(context));
		json.put("os_type", "android");
		json.put("ctime", System.currentTimeMillis());
		json.put("is_enc", 0);
		json.put("app_name","interphoto_app_android");
		StringBuffer buffer = new StringBuffer();
		buffer.append(url);
		if(url.contains("?"))
		{
			buffer.append('&');
		}
		else
		{
			buffer.append('?');
		}
		buffer.append("req=");
		buffer.append(new String(Base64.encode(json.toString().getBytes(), Base64.NO_WRAP | Base64.URL_SAFE)));
		return buffer.toString();
	}

	@Override
	protected Object sync_raw_ReadCloudCacheData(Context context, DataFilter filter)
	{
		Object obj = null;
		try
		{
			obj = CommonUtils.ReadFile(GetCloudCachePath());
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return obj;
	}

	protected String GetCloudCachePath()
	{
		return DownloadMgr.getInstance().OTHER_PATH + "/switchCache.xxxx";
	}

	@Override
	protected void sync_raw_WriteCloudData(Context context, DataFilter filter, Object data)
	{
		try
		{
			CommonUtils.SaveFile(GetCloudCachePath(), (byte[])data);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected ArrayList<SwitchRes> sync_DecodeCloudRes(Context context, DataFilter filter, Object data)
	{
		ArrayList<SwitchRes> out = null;

		try
		{
			if(data instanceof byte[] && ((byte[])data).length > 0)
			{
				JSONObject jsonObject = new JSONObject(new String((byte[])data));
				if(jsonObject != null)
				{
					int code = jsonObject.getInt("code");
					String message = jsonObject.getString("message");
					if(code == 200)
					{
						jsonObject = jsonObject.getJSONObject("data");
						if(jsonObject != null)
						{
							jsonObject = jsonObject.getJSONObject("ret_data");
							SwitchRes item;
							Object obj;
							if(jsonObject != null)
							{
								JSONArray jsonArr = jsonObject.getJSONArray("sidebar");
								if(jsonArr != null)
								{
									out = MakeResArrObj();
									int arrLen = jsonArr.length();
									for(int i = 0; i < arrLen; i ++)
									{
										obj = jsonArr.get(i);
										if(obj instanceof JSONObject)
										{
											item = ReadResItem((JSONObject)obj, "sidebar");
											if(item != null)
											{
												ResArrAddItem(out, item);
											}
										}
									}
								}
								jsonArr = jsonObject.getJSONArray("other");
								if(jsonArr != null)
								{
									if(out == null)
										out = MakeResArrObj();
									int arrLen = jsonArr.length();
									for(int i = 0; i < arrLen; i ++)
									{
										obj = jsonArr.get(i);
										if(obj instanceof JSONObject)
										{
											item = ReadResItem((JSONObject)obj, "other");
											if(item != null)
											{
												ResArrAddItem(out, item);
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		return out;
	}

	protected SwitchRes ReadResItem(JSONObject jsonObj, String classifyName) throws JSONException
	{
		SwitchRes res = new SwitchRes();
		if(jsonObj != null)
		{
			res.mClassify = classifyName;
			res.mId = jsonObj.getString("id");
			res.mDescribe = jsonObj.getString("describe");
			String time = jsonObj.getString("time");
			if(!TextUtils.isEmpty(time))
			{
				res.mTime = Integer.parseInt(time);
			}
			res.mTip = jsonObj.getString("tips");
			res.mTitle = jsonObj.getString("title");
			String unlock = jsonObj.getString("unlock");
			if("yes".equals(unlock))
			{
				res.mUnlock = true;
			}
		}
		return res;
	}

	@Override
	protected int GetCloudEventId()
	{
		return EventID.SWITCH_CLOUD_OK;
	}

	public ArrayList<SwitchRes> GetAllResArr()
	{
		ArrayList<SwitchRes> out = sync_ar_GetCloudCacheRes(MyFramework2App.getInstance().getApplication(), null);
		return out;
	}


	@Override
	protected void sync_ui_CloudResChange(ArrayList<SwitchRes> oldArr, ArrayList<SwitchRes> newArr)
	{

	}
}
