package cn.poco.resource;

import android.content.Context;
import android.text.TextUtils;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import cn.poco.framework.MyFramework;
import cn.poco.framework.MyFramework2App;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.R;
import cn.poco.tianutils.CommonUtils;
import cn.poco.watermarksync.util.Constant;

public class MyLogoResMgr
{
	private static MyLogoResMgr sInstance;
	public static int NEW_JSON_VER = 1;

	public static int BASE_RES_ID = 0xf000;

	public static String SDCARD_PATH= DownloadMgr.getInstance().MY_LOGO_PATH + "/ress.xxxx";//资源集合
	public static ArrayList<MyLogoRes> m_sdcardArr = null;
	public static int CURRENT_RES_JSON_VER = 1;

	private MyLogoResMgr()
	{
	}

	public synchronized static MyLogoResMgr getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new MyLogoResMgr();
		}
		return sInstance;
	}

	public ArrayList<MyLogoRes> ReadSDCardResArr()
	{
		ArrayList<MyLogoRes> out = new ArrayList<MyLogoRes>();

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
						MyLogoRes item;
						Object obj;
						for(int i = 0; i < arrLen; i++)
						{
							obj = jsonArr.get(i);
							if(obj instanceof JSONObject)
							{
								item = ReadResItem(out, (JSONObject)obj, true);
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


	public boolean CheckIntact(MyLogoRes res)
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


	public ArrayList<MyLogoRes> GetMyLogoResArr()
	{
		ArrayList<MyLogoRes> out = new ArrayList<MyLogoRes>();

		if(m_sdcardArr != null)
		{
			out.addAll(m_sdcardArr);
		}

		return out;
	}


	protected MyLogoRes ReadResItem(ArrayList<MyLogoRes> outArr, JSONObject jsonObj, boolean isPath)
	{
		MyLogoRes out = null;

		if(jsonObj != null)
		{
			try
			{
				out = new MyLogoRes();
				if(isPath)
				{
					out.m_type = BaseRes.TYPE_LOCAL_PATH;
				}
				else
				{
					out.m_type = BaseRes.TYPE_NETWORK_URL;
				}
				if(jsonObj.has("user_id"))
				{
					out.m_userId = jsonObj.getInt("user_id");
				}
				out.m_id = jsonObj.getInt("id");
				//本地分配的id保证唯一
				if((out.m_id == BaseRes.NONE_ID || out.m_id == 0) && out.m_userId == MyLogoRes.USER_NONE_ID)
				{
					int id = BASE_RES_ID;
					while(ResourceUtils.HasItem(outArr, id) != -1)
					{
						id++;
					}
					out.m_id = id;
				}
				out.m_name = jsonObj.getString("title");
				if(isPath)
				{
					out.m_res = jsonObj.getString("url");
				}
				else
				{
					out.url_res = jsonObj.getString("url");
				}
				if(jsonObj.has("editable"))
				{
					out.m_editable = jsonObj.getBoolean("editable");
				}

				if (jsonObj.has("res_arr")) {
					Object obj = jsonObj.get("res_arr");
					if(obj != null && obj instanceof JSONArray)
					{
						JSONArray res_arr = (JSONArray)obj;
						out.m_resArr = FontResMgr.ReadResArr(res_arr);
					}
				}

				if (jsonObj.has(Constant.KEY_OBJECTID)) {
					out.mUniqueObjectId = jsonObj.getInt("objectId");
				}

				if (jsonObj.has(Constant.KEY_SAVETIME)) {
					out.mSaveTime = jsonObj.getString("saveTime");
				}

				if (jsonObj.has(Constant.KEY_SHOULD_DELETE)) {
					out.mShouldDelete = jsonObj.getBoolean("shouldDelete");
				}

				if (jsonObj.has(Constant.KEY_SHOULD_MODIFY)) {
					out.mShouldModify = jsonObj.getBoolean("shouldModify");
				}

				if(jsonObj.has("scale"))
				{
					out.m_scale = (float)jsonObj.getDouble("scale");
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

	/**
	 *
	 * @param resArr
	 * @param userId	用户id，未登录的时候是-1
	 * @param id	资源id
	 * @return
	 */
	public int HasItem(ArrayList<MyLogoRes> resArr, int userId, int id)
	{
		int out = -1;

		if(resArr != null)
		{
			int len = resArr.size();
			MyLogoRes res;
			for(int i = 0; i < len; i++)
			{
				res = resArr.get(i);
				if(res.m_userId == userId && res.m_id == id)
				{
					out = i;
					break;
				}
			}
		}

		return out;
	}

	public void WriteSDCardResArr(ArrayList<MyLogoRes> arr)
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
						MyLogoRes res;
						JSONObject jsonObj;
						int len = arr.size();
						for(int i = 0; i < len; i++)
						{
							res = arr.get(i);
							if(res != null)
							{
								jsonObj = new JSONObject();
								jsonObj.put("id", res.m_id);
								jsonObj.put(Constant.KEY_USER_ID, res.m_userId);
								jsonObj.put(Constant.KEY_OBJECTID, res.mUniqueObjectId);

								if(res.m_name != null)
								{
									jsonObj.put("title", res.m_name);
								}
								else
								{
									jsonObj.put("title", "");
								}
								if(res.m_res instanceof String)
								{
									jsonObj.put("url", res.m_res);
								}
								else
								{
									jsonObj.put("url", "");
								}
								jsonObj.put("editable", res.m_editable);

								if (!TextUtils.isEmpty(res.mSaveTime)) {
									jsonObj.put(Constant.KEY_SAVETIME, res.mSaveTime);
								}
								jsonObj.put(Constant.KEY_SHOULD_DELETE, res.mShouldDelete);
								jsonObj.put(Constant.KEY_SHOULD_MODIFY, res.mShouldModify);
								JSONArray res_arr = new JSONArray();
								if(res.m_resArr != null)
								{
									int arrLen = res.m_resArr.size();
									JSONObject arrObj;
									FontRes fontRes;
									for(int k = 0; k < arrLen; k++)
									{
										fontRes = res.m_resArr.get(k);
										arrObj = new JSONObject();
										arrObj.put("id", fontRes.m_id);
										arrObj.put("size", fontRes.m_size);
										arrObj.put("zip_url", fontRes.url_res);
										res_arr.put(arrObj);
									}
								}
								jsonObj.put("res_arr", res_arr);

								jsonObj.put("scale", res.m_scale);


								jsonArr.put(jsonObj);
							}
						}
					}
				}
				json.put("data", jsonArr);
			}

			fos = new FileOutputStream(SDCARD_PATH);
			fos.write(json.toString().getBytes());
			fos.flush();
		}
		catch(Throwable e)
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

	/**
	 * 本地添加我的LOGO
	 *
	 * @param res
	 * @return
	 */
	public boolean AddMyLogoResItem(MyLogoRes res)
	{
		boolean out = false;

		if(res != null && res.m_res instanceof String && ((String)res.m_res).length() > 0)
		{
			File srcFile = new File((String)res.m_res);
			if(srcFile.exists())
			{
				res.mSaveTime = String.valueOf(System.currentTimeMillis() / 1000);
				String parentPath = srcFile.getParent();
				if(parentPath != null && !parentPath.equals(res.GetSaveParentPath()))
				{
					String newPath = res.GetSaveParentPath() + File.separator;
					if(res.m_editable)
					{
						newPath += "." + System.currentTimeMillis() + (int)(Math.random() * 10000) + ".zip";
					}
					else
					{
						newPath += System.currentTimeMillis() + (int)(Math.random() * 10000) + ".img";
					}
					try
					{
						FileUtils.copyFile(srcFile, new File(newPath));
						res.m_res = newPath;
						if(res.m_name == null || res.m_name.equals(""))
						{
							res.m_name = MyFramework2App.getInstance().getApplicationContext().getResources().getString(R.string.diyLogo);
						}
						int id = BASE_RES_ID;
						while(ResourceUtils.HasItem(m_sdcardArr, id) != -1)
						{
							id++;
						}
						res.m_id = id;
						//添加到资源数组

						res.OnDownloadComplete(null, true);

						out = true;
					}
					catch(Throwable e)
					{
						e.printStackTrace();
					}
				}
				else
				{
					res.OnDownloadComplete(null, true);
				}
			}
		}

		return out;
	}

	public void DeleteMyLogoResItem(int id)
	{
		if(id == MyLogoRes.NONE_ID) return;

		ArrayList<Integer> ids = new ArrayList<Integer>();
		int size = m_sdcardArr.size();
		for(int i = 0; i < size; i++)
		{
			if(m_sdcardArr.get(i).m_id == id)
			{
				ids.add(id);
			}
		}
		int[] deleteIds = new int[ids.size()];
		for(int i = 0; i < deleteIds.length; i++)
		{
			deleteIds[i] = ids.get(i);
		}
		ResourceUtils.DeleteItems(m_sdcardArr, deleteIds);
	}

	public synchronized MyLogoRes DeleteMyLogoRes(Context context, MyLogoRes res)
	{
		MyLogoRes out = null;
		int index = HasItem(m_sdcardArr, res.m_userId, res.m_id);
		if(index != -1)
		{
			m_sdcardArr.remove(index);
			WriteSDCardResArr(m_sdcardArr);
		}
		return out;
	}

	public void UpdateMyLogoRes(MyLogoRes res)
	{
		int index = HasItem(m_sdcardArr, res.m_userId, res.m_id);
		if(index != -1)
		{
			m_sdcardArr.remove(index);
			if(res.m_name == null || res.m_name.equals(""))
			{
				res.m_name = MyFramework2App.getInstance().getApplicationContext().getResources().getString(R.string.diyLogo);
			}

			m_sdcardArr.add(index, res);
			WriteSDCardResArr(m_sdcardArr);
		}
	}

	public boolean isNameExist(String name)
	{
		if(m_sdcardArr != null)
		{
			int size = m_sdcardArr.size();
			for(int i = 0; i < size; i++)
			{
				if(name.equals(m_sdcardArr.get(i).m_name))
				{
					return true;
				}
			}
		}
		return false;
	}

	public MyLogoRes OnlyDeleteMyLogoResInMemory(MyLogoRes res)
	{
		MyLogoRes out = null;
		int index = HasItem(m_sdcardArr, res.m_userId, res.m_id);
		if(index != -1)
		{
			m_sdcardArr.remove(index);
		}
		return out;
	}

	/**
	 * 包括本地和SD卡的资源
	 */
	public void InitLocalData2()
	{
		m_sdcardArr = ReadSDCardResArr();
	}
}
