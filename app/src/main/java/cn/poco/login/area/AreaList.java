package cn.poco.login.area;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import cn.poco.login.util.LoginOtherUtil;
import cn.poco.tianutils.CommonUtils;

public class AreaList
{
	private static final String TAG = "AreaList";
	public static String COUNTRY_PATH2 = "cities/location.json";
	public static String COUNTRY_PATH2EN = "cities/locationEn.json";

	public static class AreaInfo
	{
		public long m_id;
		public String m_name;
	}

	public static class AreaInfo2 extends AreaInfo
	{
		public AreaInfo2 m_parent;
		public AreaInfo2[] m_child;
	}

	private static AreaInfo2[] ReadLocationEnData(AreaInfo2 parent, JSONArray jsonArr)
	{
		AreaInfo2[] out = null;

		if(jsonArr != null && jsonArr.length() > 0)
		{
			int len = jsonArr.length();
			out = new AreaInfo2[len];
			AreaInfo2 temp;
			JSONObject jsonObj;

			//
			CharacterParser characterParser = CharacterParser.getInstance();

			for(int i = 0; i < len; i++)
			{
				try
				{
					temp = new AreaInfo2();
					jsonObj = jsonArr.getJSONObject(i);
					temp.m_parent = parent;
					temp.m_id = jsonObj.getLong("location_id");
					if(jsonObj.has("child"))
					{
						temp.m_child = ReadLocationEnData(temp, jsonObj.getJSONArray("child"));
					}
					out[i] = temp;

					if(parent == null)
					{
						temp.m_name = jsonObj.getString("location_name");
					}
					else
					{
						String name = jsonObj.getString("location_name");


						StringBuilder stringBuilder = new StringBuilder();
						String prefix, postfix;
						if(name.contains("自治区"))
						{
							prefix = characterParser.getSelling(name.substring(0, name.length() - 3));
							postfix = " Autonomous District";
						}
						else if(name.contains("地区"))
						{
							prefix = characterParser.getSelling(name.substring(0, name.length() - 2));
							postfix = " Area";
						}
						else
						{
							prefix = characterParser.getSelling(name.substring(0, name.length() - 1));
							postfix = name.substring(name.length() - 1, name.length());
							if(postfix.equals("省"))
							{
								postfix = "";
							}
							else if(postfix.equals("市"))
							{
								postfix = "";
							}
							else
							if(postfix.equals("区"))
							{
								postfix = " District";
							}
							else if(postfix.equals("县"))
							{
								postfix = " County";
							}
							else if(postfix.equals("州"))
							{
								postfix = " Prefecture";
							}
							else
							{
								prefix = characterParser.getSelling(name.substring(0, name.length()));
								postfix = "";
							}
						}
						if(prefix != null && prefix.length() >= 1)
						{
							stringBuilder.append(prefix.substring(0, 1).toUpperCase());
							stringBuilder.append(prefix.substring(1, prefix.length()));
						}
						if(postfix != null)
						{
							stringBuilder.append(postfix);
						}
						temp.m_name = stringBuilder.toString();
					}

				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}

		return out;
	}


	private static AreaInfo2[] ReadLocationData(AreaInfo2 parent, JSONArray jsonArr)
	{
		AreaInfo2[] out = null;

		if(jsonArr != null && jsonArr.length() > 0)
		{
			int len = jsonArr.length();
			out = new AreaInfo2[len];
			AreaInfo2 temp;
			JSONObject jsonObj;
			for(int i = 0; i < len; i++)
			{
				try
				{
					temp = new AreaInfo2();
					jsonObj = jsonArr.getJSONObject(i);
					temp.m_parent = parent;
					temp.m_id = jsonObj.getLong("location_id");
					temp.m_name = jsonObj.getString("location_name");
					if(jsonObj.has("child"))
					{
						temp.m_child = ReadLocationData(temp, jsonObj.getJSONArray("child"));
					}
					out[i] = temp;
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}

		return out;
	}

	public static AreaInfo2[] GetLocationLists(Context context)
	{
		AreaInfo2[] out = null;

		try
		{
			if(LoginOtherUtil.isChineseLanguage(context))
			{
				byte[] data = CommonUtils.ReadData(context.getAssets().open(COUNTRY_PATH2));
				JSONArray jsonArr = new JSONArray(new String(data));
				out = ReadLocationData(null, jsonArr);
			}
			else
			{
				byte[] data = CommonUtils.ReadData(context.getAssets().open(COUNTRY_PATH2EN));
				JSONArray jsonArr = new JSONArray(new String(data));
				out = ReadLocationEnData(null, jsonArr);

			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		return out;
	}

	public static AreaInfo2 GetLocation(AreaInfo2[] arr, long id)
	{
		AreaInfo2 out = null;

		if(arr != null && arr.length > 0)
		{
			for(int i = 0; i < arr.length; i++)
			{
				if(arr[i].m_id == id)
				{
					out = arr[i];
					break;
				}
				out = GetLocation(arr[i].m_child, id);
				if(out != null)
				{
					break;
				}
			}
		}

		return out;
	}

	public static String GetLocationStr(AreaInfo2[] arr, long id, String split)
	{
		String out = "";

		AreaInfo2 info = GetLocation(arr, id);
		if(info != null)
		{
			out = info.m_name;
			info = info.m_parent;
			while(info != null)
			{
				out = info.m_name + split + out;
				info = info.m_parent;
			}
		}

		return out;
	}
}
