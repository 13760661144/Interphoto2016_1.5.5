package cn.poco.resource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import cn.poco.framework.EventID;
import cn.poco.framework.MyFramework2App;
import cn.poco.resource.database.TableNames;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.utils.FileUtil;

/**
 * Created by admin on 2017/12/6.
 */

public class LightEffectResMgr2 extends DBBaseResMgr2<LightEffectRes, ArrayList<LightEffectRes>>
{
	private static LightEffectResMgr2 sInstance;
	public String LOCAL_REFRESH_DATABASE_FLAG;

	public String LOCAL_PATH;

	public String SDCARD_PATH; //资源集合
	public String ORDER_PATH; //显示的item&排序(不存在这里的id不会显示)

	public String CLOUD_CACHE_PATH;
	public String CLOUD_URL;

	private LightEffectResMgr2()
	{
		LOCAL_REFRESH_DATABASE_FLAG = "lighteffect_need_refresh_database_1.4.0";
		LOCAL_PATH = "data_json/light_effect.json";
		SDCARD_PATH = DownloadMgr.getInstance().LIGHT_EFFECT_PATH + "/ress.xxxx";
		ORDER_PATH = DownloadMgr.getInstance().LIGHT_EFFECT_PATH + "/order.xxxx";
		CLOUD_CACHE_PATH = DownloadMgr.getInstance().LIGHT_EFFECT_PATH + "/cache.xxxx";
		CLOUD_URL = "http://beauty-material.adnonstop.com/API/poco_camera/light/android.php?version=";
	}

	public synchronized static LightEffectResMgr2 getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new LightEffectResMgr2();
		}
		return sInstance;
	}

	@Override
	public int GetResArrSize(ArrayList<LightEffectRes> arr)
	{
		return arr.size();
	}

	@Override
	public ArrayList<LightEffectRes> MakeResArrObj()
	{
		return new ArrayList<>();
	}

	@Override
	public boolean ResArrAddItem(ArrayList<LightEffectRes> arr, LightEffectRes item)
	{
		if(arr != null && item != null)
		{
			arr.add(item);
			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	protected String GetTableName()
	{
		return TableNames.LIGHT_EFFECT;
	}

	@Override
	protected String GetKeyId()
	{
		return "id";
	}

	@Override
	protected LightEffectRes ReadResByDB(Cursor cursor)
	{
		LightEffectRes out = new LightEffectRes();
		out.m_id = cursor.getInt(cursor.getColumnIndex("id"));
		out.m_name = cursor.getString(cursor.getColumnIndex("name"));
		out.m_className = cursor.getString(cursor.getColumnIndex("class"));
		out.m_size = cursor.getInt(cursor.getColumnIndex("size"));
		out.m_thumb = cursor.getString(cursor.getColumnIndex("thumb_120"));
		out.m_coverImg = cursor.getString(cursor.getColumnIndex("cover_pic"));
		out.m_orderType = cursor.getInt(cursor.getColumnIndex("type"));
		out.m_tjId = cursor.getInt(cursor.getColumnIndex("tracking_code"));
		out.m_location = cursor.getString(cursor.getColumnIndex("location"));
		out.m_compose = cursor.getInt(cursor.getColumnIndex("compose"));
		out.m_color = cursor.getString(cursor.getColumnIndex("color"));
		out.m_lockTypeName = cursor.getString(cursor.getColumnIndex("lockType"));
		out.m_showContent = cursor.getString(cursor.getColumnIndex("lockIntroduce"));
		out.m_showImg = cursor.getString(cursor.getColumnIndex("lockPage"));
		out.m_res = cursor.getString(cursor.getColumnIndex("iamge_info"));
		out.m_scale = cursor.getFloat(cursor.getColumnIndex("scale"));
		out.m_minScale = cursor.getFloat(cursor.getColumnIndex("min_scale"));
		out.m_maxScale = cursor.getFloat(cursor.getColumnIndex("max_scale"));
		out.m_shareContent = cursor.getString(cursor.getColumnIndex("shareContent"));
		out.m_shareThumb = cursor.getString(cursor.getColumnIndex("shareThumb"));
		out.m_shareUrl = cursor.getString(cursor.getColumnIndex("shareURL"));
		out.m_headTitle = cursor.getString(cursor.getColumnIndex("head_title"));
		out.m_headLink = cursor.getString(cursor.getColumnIndex("head_link"));
		out.m_headImg = cursor.getString(cursor.getColumnIndex("head_img"));
		int hide = cursor.getInt(cursor.getColumnIndex("is_hide"));
		out.m_isHide = hide == 1 ? true : false;
		out.m_type = cursor.getInt(cursor.getColumnIndex("store_type"));
		return out;
	}

	@Override
	public boolean SaveResByDB(SQLiteDatabase db, LightEffectRes item)
	{
		if(db != null && item != null)
		{
			ContentValues values = new ContentValues();
			values.put("id", item.m_id);
			values.put("name", item.m_name);
			values.put("class", item.m_className);
			values.put("size", item.m_size);
			values.put("thumb_120", item.m_thumb instanceof String ? (String)item.m_thumb : "");
			values.put("cover_pic", item.m_coverImg);
			values.put("type", item.m_orderType);
			values.put("tracking_code", item.m_tjId);
			values.put("location", item.m_location);
			values.put("compose", item.m_compose);
			values.put("color", item.m_color);
			values.put("lockType", item.m_lockTypeName);
			values.put("lockIntroduce", item.m_showContent);
			values.put("lockPage", item.m_showImg);
			values.put("iamge_info", item.m_res instanceof String ? (String)item.m_res : "");
			values.put("scale", item.m_scale);
			values.put("min_scale", item.m_minScale);
			values.put("max_scale", item.m_maxScale);
			values.put("shareContent", item.m_shareContent);
			values.put("shareThumb", item.m_shareThumb);
			values.put("shareURL", item.m_shareUrl);
			values.put("head_title", item.m_headTitle);
			values.put("head_link", item.m_headLink);
			values.put("head_img", item.m_headImg);
			values.put("is_hide", item.m_isHide ? 1: 0);
			values.put("store_type", item.m_type);

			long flag = db.insertWithOnConflict(TableNames.LIGHT_EFFECT, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if(flag < 0){
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	protected void InitLocalData(SQLiteDatabase db)
	{
		ArrayList<LightEffectRes> localArr = null;
		ArrayList<LightEffectRes> sdcardArr = null;
		Context context = MyFramework2App.getInstance().getApplication();

		if(TagMgr.CheckTag(context, "lighteffect_has_insert_into_database"))
		{
			sdcardArr = sync_GetSdcardRes(context, null);
		}

		if(TagMgr.CheckTag(context, LOCAL_REFRESH_DATABASE_FLAG))
		{
			localArr = sync_raw_GetLocalRes(context, null);
		}
		if(db != null)
		{
			boolean flag1 = false;
			if(localArr != null || sdcardArr != null)
			{
				if(localArr != null)
				{
					DeleteLocalResArr(db);
					flag1 = ResourceUtils.RebuildLocalOrder(localArr, GetOrderArr());
					for(LightEffectRes res : localArr)
					{
						SaveResByDB(db, res);
					}
					TagMgr.SetTag(context, LOCAL_REFRESH_DATABASE_FLAG);
				}
				if(sdcardArr != null)
				{
					for(LightEffectRes res : sdcardArr)
					{
						SaveResByDB(db, res);
					}
					TagMgr.SetTag(context, "lighteffect_has_insert_into_database");
				}
			}
			boolean flag2 = ResourceUtils.RebuildOrder(ReadResArrByDB(db, null), GetOrderArr());
			if(flag1 || flag2)
			{
				SaveOrderArr(ORDER_PATH);
			}

			ClearUnusedRes(db);
		}
	}

	@Override
	public boolean CheckIntact(LightEffectRes res)
	{
		boolean out = false;

		if(res != null)
		{
			if(ResourceUtils.HasIntact(res.m_thumb) && ResourceUtils.HasIntact(res.m_res))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	public boolean CheckExist(LightEffectRes res)
	{
		boolean out = false;

		if(CheckIntact(res))
		{
			if(ResourceUtils.IsResExist(MyFramework2App.getInstance().getApplicationContext(), (String)res.m_thumb, (String)res.m_res))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	public int GetId(LightEffectRes res)
	{
		if(res != null){
			return res.m_id;
		}
		return 0;
	}

	@Override
	public LightEffectRes ReadResByIndex(ArrayList<LightEffectRes> arr, int index)
	{
		if(arr != null && index >= 0 && index < arr.size())
		{
			return arr.get(index);
		}
		return null;
	}

	@Override
	protected void DeleteSDRes(Context context, LightEffectRes res)
	{
		LightEffectRes tempRes = GetCloudRes(res.m_id);
		int index = ResourceUtils.HasId(GetOrderArr(), res.m_id);
		if(index >= 0){
			GetOrderArr().remove(index);
			SaveOrderArr(ORDER_PATH);
		}
		if(tempRes != null){
			tempRes.m_type = BaseRes.TYPE_NETWORK_URL;
//			tempRes.m_coverImg = null;
//			tempRes.m_headImg = null;
//			tempRes.m_thumb = null;
			tempRes.m_res = null;
//			tempRes.m_shareThumb = null;
		}
//		FileUtil.deleteSDFile(res.m_coverImg);
//		FileUtil.deleteSDFile(res.m_shareThumb);
		FileUtil.deleteSDFile((String)res.m_res);
//		FileUtil.deleteSDFile((String)res.m_thumb);
//		FileUtil.deleteSDFile(res.m_headImg);
	}

	@Override
	protected LightEffectRes ReadResItem(JSONObject jsonObj, boolean isPath)
	{
		LightEffectRes out = null;

		if(jsonObj != null)
		{
			try
			{
				out = new LightEffectRes();
				if(isPath)
				{
					out.m_type = BaseRes.TYPE_LOCAL_PATH;
				}
				else
				{
					out.m_type = BaseRes.TYPE_NETWORK_URL;
				}
				String temp = jsonObj.getString("id");
				if(temp != null && temp.length() > 0)
				{
					out.m_id = (int)Long.parseLong(temp, 10);
				}
				else
				{
					out.m_id = (int)(Math.random() * 10000000);
				}
				out.m_name = jsonObj.getString("name");
				out.m_className = jsonObj.getString("class");
				temp = jsonObj.getString("size");
				if(temp != null && temp.length() > 0)
				{
					out.m_size = Integer.parseInt(temp);
				}
				if(isPath)
				{
					out.m_thumb = jsonObj.getString("thumb_120");
				}
				else
				{
					out.url_thumb = jsonObj.getString("thumb_120");
				}
				if(jsonObj.has("cover_pic"))
				{
					if(isPath)
					{
						out.m_coverImg = jsonObj.getString("cover_pic");
					}
					else
					{
						out.url_coverImg = jsonObj.getString("cover_pic");
					}
				}
				temp = jsonObj.getString("type");
				if(temp != null && temp.length() > 0)
				{
					out.m_orderType = Integer.parseInt(temp);
				}
				temp = jsonObj.getString("tracking_code");
				if(temp != null && temp.length() > 0)
				{
					out.m_tjId = Integer.parseInt(temp);
				}
				out.m_location = jsonObj.getString("location");
				temp = jsonObj.getString("compose");
				if(temp != null && temp.length() > 0)
				{
					out.m_compose = Integer.parseInt(temp);
				}
				out.m_color = jsonObj.getString("color");
				temp = jsonObj.getString("scale");
				if(temp != null && temp.length() > 0)
				{
					out.m_scale = Float.parseFloat(temp);
				}
				temp = jsonObj.getString("min_scale");
				if(temp != null && temp.length() > 0)
				{
					out.m_minScale = Float.parseFloat(temp);
				}
				temp = jsonObj.getString("max_scale");
				if(temp != null && temp.length() > 0)
				{
					out.m_maxScale = Float.parseFloat(temp);
				}
				if(!isPath)
				{
					out.url_res = jsonObj.getString("iamge_info");
				}
				else
				{
					out.m_res = jsonObj.getString("iamge_info");
				}

				temp = jsonObj.getString("lockType");
				out.m_lockTypeName = temp;
				if(temp.equals("comment"))
				{
					out.m_shareType = LockRes.SHARE_TYPE_MARKET;
				}
				else if(temp.equals("weixin"))
				{
					out.m_shareType = LockRes.SHARE_TYPE_WEIXIN;
				}
				out.m_showContent = jsonObj.getString("lockIntroduce");
				if(!isPath)
				{
					out.url_showImg = jsonObj.getString("lockPage");
				}
				else
				{
					out.m_showImg = jsonObj.getString("lockPage");
				}
				if(jsonObj.has("shareContent")){
					out.m_shareContent = jsonObj.getString("shareContent");
				}
				if(jsonObj.has("shareThumb")){
					if(!isPath)
					{
						out.url_shareThumb = jsonObj.getString("shareThumb");
					}
					else
					{
						out.m_shareThumb = jsonObj.getString("shareThumb");
					}
				}
				if(jsonObj.has("shareURL")){
					out.m_shareUrl = jsonObj.getString("shareURL");
				}
				out.m_headTitle = jsonObj.getString("head_title");
				out.m_headLink = jsonObj.getString("head_link");
				if(!isPath)
				{
					out.url_headImg = jsonObj.getString("head_img");
				}
				else
				{
					out.m_headImg = jsonObj.getString("head_img");
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

	@Override
	protected String GetSdcardPath(Context context)
	{
		return SDCARD_PATH;
	}

	@Override
	protected void RecodeLocalItem(LightEffectRes res)
	{
		res.m_type = BaseRes.TYPE_LOCAL_RES;
		res.m_res = "lighteffect_imgs/" + res.m_res;
		res.m_thumb = "lighteffect_imgs/" + res.m_thumb;
		res.m_headImg = "lighteffect_imgs/" + res.m_headImg;
		res.m_coverImg = "lighteffect_imgs/" + res.m_coverImg;
	}

	@Override
	protected String GetLocalPath()
	{
		return LOCAL_PATH;
	}

	@Override
	public LightEffectRes GetItem(ArrayList<LightEffectRes> arr, int id)
	{
		if(arr != null)
		{
			return ResourceUtils.GetItem(arr, id);
		}
		return null;
	}

	@Override
	protected String GetCloudUrl(Context context)
	{
		String url;
		if(SysConfig.GetAppVer(context).contains("88.8.8"))
		{
			url = CLOUD_URL + "88.8.8";
		}
		else
		{
			url = CLOUD_URL + "1.2.0";
		}
		return url;
	}

	@Override
	protected String GetCloudCachePath(Context context)
	{
		return CLOUD_CACHE_PATH;
	}

	@Override
	protected void RebuildNetResArr(ArrayList<LightEffectRes> dst, ArrayList<LightEffectRes> src)
	{
		if(dst != null && src != null)
		{
			LightEffectRes srcTemp;
			LightEffectRes dstTemp;
			Class cls = LightEffectRes.class;
			Field[] fields = cls.getDeclaredFields();
			int size = dst.size();
			for(int i = 0; i < size; i ++)
			{
				dstTemp = dst.get(i);
				srcTemp = GetItem(src, dstTemp.m_id);
				if(srcTemp != null)
				{
					dstTemp.m_type = srcTemp.m_type;
					dstTemp.m_isHide = srcTemp.m_isHide;
					dstTemp.m_thumb = srcTemp.m_thumb;
					dstTemp.m_headImg = srcTemp.m_headImg;
					dstTemp.m_coverImg = srcTemp.m_coverImg;
					dstTemp.m_shareThumb = srcTemp.m_shareThumb;

					for(int k = 0; k < fields.length; k++)
					{
						try
						{
							if(!Modifier.isFinal(fields[k].getModifiers()))
							{
								Object value = fields[k].get(dstTemp);
								fields[k].set(srcTemp, value);
							}
						}
						catch(Exception e1)
						{
						}
					}
					dst.set(i, srcTemp);
				}
			}
		}
	}

	public void SaveOrderArr()
	{
		SaveOrderArr(ORDER_PATH);
	}

	@Override
	protected void InitOrderArr(ArrayList<Integer> dstObj)
	{
		super.InitOrderArr(dstObj);
		ReadOrderArr(MyFramework2App.getInstance().getApplicationContext(), ORDER_PATH);
	}

	/**
	 * 需要统一控制数据库的时候调用
	 * @param db
	 * @param ids
	 * @return
	 */
	public ArrayList<LightEffectRes> GetResArr(SQLiteDatabase db, int[] ids, boolean onlyLocal)
	{
		ArrayList<LightEffectRes> out = new ArrayList<LightEffectRes>();
		if(ids != null)
		{
			ArrayList<LightEffectRes> temp = ReadResArrByDB(db, ids);
			if(onlyLocal || ids.length == temp.size()){
				out = temp;
			}
			else
			{
				LightEffectRes res;
				for(int i = 0; i < ids.length; i++)
				{
					res = ResourceUtils.GetItem(temp, ids[i]);
					if(res != null)
					{
						out.add(res);
					}
					else
					{
						res = GetCloudRes(ids[i]);
						if(res != null)
						{
							out.add(res);
						}
					}
				}
			}
		}
		return out;
	}

	public ArrayList<LightEffectRes> GetResArr(SQLiteDatabase db, int[] ids)
	{
		return GetResArr(db, ids, false);
	}

	public ArrayList<LightEffectRes> GetLocalResArr(SQLiteDatabase db)
	{
		ArrayList<LightEffectRes> out = new ArrayList<LightEffectRes>();
		ArrayList<Integer> order_arr = GetOrderArr();
		if(order_arr == null || order_arr.size() < 1)
			return out;
		if(db != null)
		{
			ArrayList<LightEffectRes> resArr = ReadResArrByDB(db, null);
			LightEffectRes res;
			for(int id : order_arr)
			{
				res = ResourceUtils.GetItem(resArr, id);
				if(res != null){
					out.add(res);
				}
			}
		}
		return out;
	}

	@Override
	protected int GetCloudEventId()
	{
		return EventID.LIGHT_EFFECT_CLOUD_OK;
	}
}
