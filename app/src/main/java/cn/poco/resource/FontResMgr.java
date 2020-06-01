package cn.poco.resource;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;

import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.NetCore2;
import cn.poco.utils.MyNetCore;

public class FontResMgr
{
	public final static int NEW_JSON_VER = 1;

	public final String SDCARD_PATH = DownloadMgr.getInstance().FONT_PATH + "/ress.xxxx"; //资源集合
	public int CURRENT_RES_JSON_VER = 1;

	public ArrayList<FontRes> ReadSDCardResArr()
	{
		ArrayList<FontRes> out = new ArrayList<FontRes>();

		try
		{
			byte[] datas = CommonUtils.ReadFile(SDCARD_PATH);
			if(datas != null)
			{
				JSONObject jsonObj = new JSONObject(new String(datas));
				if(jsonObj != null && jsonObj.length() > 0)
				{
					if(jsonObj.has("ver"))
					{
						CURRENT_RES_JSON_VER = jsonObj.getInt("ver");
					}
					JSONArray jsonArr = jsonObj.getJSONArray("data");
					if(jsonArr != null)
					{
						int arrLen = jsonArr.length();
						FontRes item;
						Object obj;
						for(int i = 0; i < arrLen; i++)
						{
							obj = jsonArr.get(i);
							if(obj instanceof JSONObject)
							{
								item = ReadResItem((JSONObject)obj, true);
								if(item != null && CheckIntact(item))
								{
									out.add(item);
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

	public boolean CheckIntact(FontRes res)
	{
		boolean out = false;

		if(res != null)
		{
			if(ResourceUtils.HasIntact(res.m_res))
			{
				out = true;
			}
		}

		return out;
	}

	public ArrayList<FontRes> ReadCloudResArr(Context context, String url)
	{
		ArrayList<FontRes> out = new ArrayList<FontRes>();

		NetCore2 net = null;
		try
		{
			net = new MyNetCore(context);
			NetCore2.NetMsg msg = net.HttpGet(url);
			if(msg != null && msg.m_stateCode == HttpURLConnection.HTTP_OK && msg.m_data != null)
			{
				JSONArray jsonArr = new JSONArray(new String(msg.m_data));
				FontRes item;
				Object obj;
				int arrLen = jsonArr.length();
				for(int i = 0; i < arrLen; i++)
				{
					obj = jsonArr.get(i);
					if(obj instanceof JSONObject)
					{
						item = ReadResItem((JSONObject)obj, false);
						if(item != null)
						{
							out.add(item);
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

		return out;
	}

	public static ArrayList<FontRes> ReadResArr(JSONArray resArr)
	{
		ArrayList<FontRes> out = new ArrayList<FontRes>();
		if(resArr != null)
		{
			FontRes item;
			Object obj;
			int arrLen = resArr.length();
			for(int i = 0; i < arrLen; i++)
			{
				try
				{
					obj = resArr.get(i);
					if(obj instanceof JSONObject)
					{
						item = ReadResItem((JSONObject)obj, false);
						if(item != null)
						{
							out.add(item);
						}
					}
				}
				catch(JSONException e)
				{
					e.printStackTrace();
				}
			}
		}
		return out;
	}

	protected static FontRes ReadResItem(JSONObject jsonObj, boolean isPath)
	{
		FontRes out = null;

		if(jsonObj != null)
		{
			try
			{
				out = new FontRes();
				if(isPath)
				{
					out.m_type = BaseRes.TYPE_LOCAL_PATH;
				}
				else
				{
					out.m_type = BaseRes.TYPE_NETWORK_URL;
				}
				String temp;
				if(jsonObj.has("id"))
				{
					temp = jsonObj.getString("id");
					if(temp != null && temp.length() > 0)
					{
						out.m_id = (int)Long.parseLong(temp, 10);
					}
					else
					{
						out.m_id = (int)(Math.random() * 10000000);
					}
				}
				if(jsonObj.has("size"))
				{
					temp = jsonObj.getString("size");
					if(temp != null && temp.length() > 0)
					{
						out.m_size = Integer.parseInt(temp);
					}
				}
				if(jsonObj.has("zip_url"))
				{
					if(isPath)
					{
						out.m_res = jsonObj.getString("zip_url");
					}
					else
					{
						out.url_res = jsonObj.getString("zip_url");
					}
				}

			}
			catch(Throwable e)
			{
				e.printStackTrace();
				out = null;
			}
		}
		return out;
	}

	public void WriteSDCardResArr(ArrayList<FontRes> arr)
	{
		FileOutputStream fos = null;

		try
		{
			JSONObject json = new JSONObject();
			{
				json.put("ver", NEW_JSON_VER);

				JSONArray jsonArr = new JSONArray();
				{
					if(arr != null)
					{
						JSONObject jsonObject;
						FontRes res;
						int len = arr.size();
						for(int i = 0; i < len; i++)
						{
							res = arr.get(i);
							if(res != null)
							{
								jsonObject = new JSONObject();
								jsonObject.put("id", Integer.toHexString(res.m_id));
								if(res.m_res instanceof String)
								{
									jsonObject.put("zip_url", res.m_res);
								}
								else
								{
									jsonObject.put("zip_url", "");
								}
								jsonArr.put(jsonObject);
							}
						}
					}
				}
				json.put("data", jsonArr);
				//System.out.println(json.toString());
			}
			fos = new FileOutputStream(SDCARD_PATH);
			fos.write(json.toString().getBytes());
			fos.flush();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(fos != null)
			{
				try
				{
					fos.close();
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}
