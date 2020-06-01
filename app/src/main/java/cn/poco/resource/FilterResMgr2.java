package cn.poco.resource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.adnonstop.resourcelibs.DataFilter;

import org.json.JSONArray;
import org.json.JSONException;
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
 * 滤镜素材管理
 */

public class FilterResMgr2 extends DBBaseResMgr2<FilterRes, ArrayList<FilterRes>>
{
	private static FilterResMgr2 sInstance;

	public String LOCAL_REFRESH_DATABASE_FLAG;
	private String LOCAL_PATH;

	public String SDCARD_PATH; //资源集合,在用数据库存储下载数据之前，是用sd卡存储的
	public String ORDER_PATH; //显示的item&排序(不存在这里的id不会显示)

	public String CLOUD_CACHE_PATH;
	public String CLOUD_URL;

	private FilterResMgr2()
	{
		LOCAL_REFRESH_DATABASE_FLAG = "filter_need_refresh_database_1.5.0";
		LOCAL_PATH = "data_json/filter.json";

		SDCARD_PATH = DownloadMgr.getInstance().FILTER_PATH + "/ress.xxxx"; //资源集合,在用数据库存储下载数据之前，是用sd卡存储的
		ORDER_PATH = DownloadMgr.getInstance().FILTER_PATH + "/order.xxxx"; //显示的item&排序(不存在这里的id不会显示)

		CLOUD_CACHE_PATH = DownloadMgr.getInstance().FILTER_PATH + "/cache.xxxx";
		CLOUD_URL = "http://beauty-material.adnonstop.com/API/interphoto/filter/android.php?version=";
	}

	public synchronized static FilterResMgr2 getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new FilterResMgr2();
		}
		return sInstance;
	}

	@Override
	protected String GetTableName()
	{
		return TableNames.FILTER;
	}

	@Override
	protected String GetKeyId()
	{
		return "color_ID";
	}

	@Override
	protected FilterRes ReadResByDB(Cursor cursor)
	{
		FilterRes out = new FilterRes();
		out.m_id = cursor.getInt(cursor.getColumnIndex("color_ID"));
		out.m_name = cursor.getString(cursor.getColumnIndex("color_name"));
		out.m_coverImg = cursor.getString(cursor.getColumnIndex("color_coverImage"));
		out.m_thumb = cursor.getString(cursor.getColumnIndex("color_thumbnailImage"));
		out.m_alpha = cursor.getInt(cursor.getColumnIndex("color_alpha"));
		out.m_coverColor = cursor.getString(cursor.getColumnIndex("color_coverColor"));
		out.m_orderType = cursor.getInt(cursor.getColumnIndex("color_locationType"));
		out.m_order = cursor.getInt(cursor.getColumnIndex("color_location"));
		out.m_tjId = cursor.getInt(cursor.getColumnIndex("color_statisticalID"));
		out.m_masterType = cursor.getInt(cursor.getColumnIndex("color_masterType"));
		out.m_authorImg = cursor.getString(cursor.getColumnIndex("color_authorImage"));
		out.m_authorName = cursor.getString(cursor.getColumnIndex("color_introductionAuthor"));
		out.m_authorInfo = cursor.getString(cursor.getColumnIndex("color_introductionAuthorTitle"));
		out.m_filterDetail = cursor.getString(cursor.getColumnIndex("color_introductionDetail"));
		out.m_filterIntroUrl = cursor.getString(cursor.getColumnIndex("color_introductionURL"));
		out.m_shareImg = cursor.getString(cursor.getColumnIndex("color_unlockWeixinImage"));
		out.m_shareUrl = cursor.getString(cursor.getColumnIndex("share_link"));
		out.m_filterData = cursor.getString(cursor.getColumnIndex("detail"));
		int hide = cursor.getInt(cursor.getColumnIndex("is_hide"));
		out.m_isHide = hide == 1 ? true : false;
		out.m_alpha = cursor.getInt(cursor.getColumnIndex("color_alpha"));
		out.m_type = cursor.getInt(cursor.getColumnIndex("store_type"));

		out.m_tablePic = cursor.getString(cursor.getColumnIndex("table_pic"));
		String temp = cursor.getString(cursor.getColumnIndex("blend_info"));
		if(!TextUtils.isEmpty(temp))
		{
			try
			{
				JSONArray jsonArr = new JSONArray(temp);
				if(jsonArr != null && jsonArr.length() > 0)
				{
					out.m_compose = new FilterComposeInfo[jsonArr.length()];
					JSONObject json;
					for(int i = 0; i < jsonArr.length(); i ++)
					{
						json = jsonArr.getJSONObject(i);
						out.m_compose[i] = new FilterComposeInfo();
						out.m_compose[i].blend_pic = json.getString("blend_pic");
						out.m_compose[i].blend_alpha = Float.parseFloat(json.getString("blend_alpha"));
						out.m_compose[i].blend_type = json.getInt("blend_type");
					}
				}
			}catch(JSONException e){
				e.printStackTrace();
			}
		}
		out.m_filterType = cursor.getInt(cursor.getColumnIndex("filter_type"));
		return out;
	}

	@Override
	public boolean SaveResByDB(SQLiteDatabase db, FilterRes item)
	{
		if(db != null && item != null)
		{
			ContentValues values = new ContentValues();
			values.put("color_ID", item.m_id);
			values.put("color_name", item.m_name);
			values.put("color_coverImage", item.m_coverImg);
			values.put("color_thumbnailImage", item.m_thumb instanceof String ? (String)item.m_thumb : "");
			values.put("color_alpha", item.m_alpha);
			values.put("color_coverColor", item.m_coverColor);
			values.put("color_locationType", item.m_orderType);
			values.put("color_location", item.m_order);
			values.put("color_statisticalID", item.m_tjId);
			values.put("color_masterType", item.m_masterType);
			values.put("color_authorImage", item.m_authorImg instanceof String ? (String)item.m_authorImg : "");
			values.put("color_introductionAuthor", item.m_authorName);
			values.put("color_introductionAuthorTitle", item.m_authorInfo);
			values.put("color_introductionDetail", item.m_filterDetail);
			values.put("color_introductionURL", item.m_filterIntroUrl);
			values.put("color_unlockWeixinImage", item.m_shareImg instanceof String ? (String)item.m_shareImg : "");
			values.put("color_unlockWeixinTitle", item.m_shareTitle);
			values.put("share_link", item.m_shareUrl);
			values.put("detail", item.m_filterData);
			values.put("is_hide", item.m_isHide ? 1 : 0);
			values.put("store_type", item.m_type);

			values.put("table_pic", item.m_tablePic);
			if(item.m_compose != null)
			{
				try{
					JSONArray jsonArr = new JSONArray();
					JSONObject json;
					for(int i = 0; i < item.m_compose.length; i ++)
					{
						json = new JSONObject();
						json.put("blend_pic", item.m_compose[i].blend_pic);
						json.put("blend_alpha", item.m_compose[i].blend_alpha);
						json.put("blend_type", item.m_compose[i].blend_type);
						jsonArr.put(json);
					}
					values.put("blend_info", jsonArr.toString());
				}catch(JSONException e){
					e.printStackTrace();
				}
			}
			values.put("filter_type", item.m_filterType);

			long flag = db.insertWithOnConflict(TableNames.FILTER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if(flag < 0){
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public int GetResArrSize(ArrayList<FilterRes> arr)
	{
		return arr.size();
	}

	@Override
	public int GetId(FilterRes res)
	{
		if(res != null){
			return res.m_id;
		}
		return 0;
	}

	@Override
	public FilterRes ReadResByIndex(ArrayList<FilterRes> arr, int index)
	{
		if(arr != null && index >= 0 && index < arr.size())
		{
			return arr.get(index);
		}
		return null;
	}

	@Override
	protected void DeleteSDRes(Context context, FilterRes res)
	{
		FilterRes baseRes = GetCloudRes(res.m_id);
		int index = ResourceUtils.HasId(GetOrderArr(), res.m_id);
		if(index >= 0){
			GetOrderArr().remove(index);
			SaveOrderArr(ORDER_PATH);
		}
		if(baseRes != null){
			baseRes.m_type = BaseRes.TYPE_NETWORK_URL;
//			baseRes.m_coverImg = null;
//			baseRes.m_shareImg = null;
			baseRes.m_tablePic = null;
//			baseRes.m_thumb = null;
//			baseRes.m_authorImg = null;
			if(baseRes.m_compose != null)
			{
				for(int i = 0; i < baseRes.m_compose.length; i ++)
				{
					baseRes.m_compose[i].blend_pic = null;
				}
			}
		}
//		FileUtil.deleteSDFile(res.m_coverImg);
//		FileUtil.deleteSDFile((String)res.m_shareImg);
		FileUtil.deleteSDFile(res.m_tablePic);
//		FileUtil.deleteSDFile((String)res.m_thumb);
//		FileUtil.deleteSDFile((String)res.m_authorImg);
		if(res.m_compose != null)
		{
			for(int i = 0; i < res.m_compose.length; i ++)
			{
				FileUtil.deleteSDFile(res.m_compose[i].blend_pic);
			}
		}
	}

	@Override
	public ArrayList<FilterRes> MakeResArrObj()
	{
		return new ArrayList<>();
	}

	@Override
	public boolean ResArrAddItem(ArrayList<FilterRes> arr, FilterRes item)
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
	protected FilterRes ReadResItem(JSONObject jsonObj, boolean isPath)
	{
		FilterRes out = null;
		try
		{
			out = new FilterRes();
			if(isPath)
			{
				out.m_type = BaseRes.TYPE_LOCAL_PATH;
			}
			else
			{
				out.m_type = BaseRes.TYPE_NETWORK_URL;
			}
			String temp = jsonObj.getString("color_ID");
			if(temp != null && temp.length() > 0)
			{
				out.m_id = Integer.parseInt(temp);
			}
			out.m_name = jsonObj.getString("color_name");
			if(isPath)
			{
				out.m_coverImg = jsonObj.getString("color_coverImage");
				if(jsonObj.has("color_thumbnailImage"))
				{
					out.m_thumb = jsonObj.getString("color_thumbnailImage");
				}
			}
			else
			{
				out.url_coverImg = jsonObj.getString("color_coverImage");
				if(jsonObj.has("color_thumbnailImage"))
				{
					out.url_thumb = jsonObj.getString("color_thumbnailImage");
				}
			}
			temp = jsonObj.getString("color_alpha");
			if(temp != null && temp.length() > 0)
			{
				out.m_alpha = Integer.parseInt(temp);
			}
			out.m_coverColor = jsonObj.getString("color_coverColor");
			temp = jsonObj.getString("color_locationType");
			if(temp != null && temp.length() > 0)
			{
				out.m_orderType = Integer.parseInt(temp);
			}
			temp = jsonObj.getString("color_location");
			if(temp != null && temp.length() > 0)
			{
				out.m_order = Integer.parseInt(temp);
			}
			temp = jsonObj.getString("color_statisticalID");
			if(temp != null && temp.length() > 0)
			{
				out.m_tjId = Integer.parseInt(temp);
			}
			if(jsonObj.has("color_masterType"))
			{
				out.m_masterType = jsonObj.getInt("color_masterType");
			}
			if(isPath)
			{
				out.m_authorImg = jsonObj.getString("color_authorImage");
			}
			else
			{
				out.url_authorImg = jsonObj.getString("color_authorImage");
			}
			out.m_authorName = jsonObj.getString("color_introductionAuthor");
			out.m_authorInfo = jsonObj.getString("color_introductionAuthorTitle");
			out.m_filterDetail = jsonObj.getString("color_introductionDetail");
			out.m_filterIntroUrl = jsonObj.getString("color_introductionURL");
			if(jsonObj.has("color_unlockWeixinImage"))
			{
				if(isPath)
				{
					out.m_shareImg = jsonObj.getString("color_unlockWeixinImage");
				}
				else
				{
					out.url_shareImg = jsonObj.getString("color_unlockWeixinImage");
				}
			}
			if(jsonObj.has("color_unlockWeixinTitle"))
			{
				out.m_shareTitle = jsonObj.getString("color_unlockWeixinTitle");
			}
			if(jsonObj.has("share_link"))
			{
				out.m_shareUrl = jsonObj.getString("share_link");
			}
			if(jsonObj.has("detail"))
			{
				out.m_filterData = jsonObj.getString("detail");
			}
			if(jsonObj.has("color_filter"))
			{
				JSONObject json1 = jsonObj.getJSONObject("color_filter");
				if(json1 != null)
				{
					temp = json1.getString("filter_type");
					if(temp != null && temp.length() > 0)
					{
						out.m_filterType = Integer.parseInt(temp);
					}
					if(isPath)
					{
						out.m_tablePic = json1.getString("table_pic");
					}
					else
					{
						out.url_tablePic = json1.getString("table_pic");
					}
					JSONArray jsonArr1 = json1.getJSONArray("blend");
					if(jsonArr1 != null && jsonArr1.length() > 0)
					{
						out.m_compose = new FilterComposeInfo[jsonArr1.length()];
						JSONObject json2;
						for(int i = 0; i < jsonArr1.length(); i ++)
						{
							json2 = jsonArr1.getJSONObject(i);
							out.m_compose[i] = new FilterComposeInfo();
							if(isPath)
							{
								out.m_compose[i].blend_pic = json2.getString("blend_pic");
							}
							else
							{
								out.m_compose[i].url_blend_pic = json2.getString("blend_pic");
							}
							temp = json2.getString("blend_alpha");
							if(temp != null && temp.length() > 0)
							{
								out.m_compose[i].blend_alpha = Float.parseFloat(temp);
							}
							temp = json2.getString("blend_type");
							if(temp != null && temp.length() > 0)
							{
								out.m_compose[i].blend_type = Integer.parseInt(temp);
							}
						}
					}
				}
				out.m_filterData = jsonObj.getString("detail");
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return out;
	}

	@Override
	protected String GetSdcardPath(Context context)
	{
		return SDCARD_PATH;
	}

	@Override
	protected void RecodeLocalItem(FilterRes item)
	{
		item.m_type = BaseRes.TYPE_LOCAL_RES;
		item.m_authorImg = "filter_imgs/" + item.m_authorImg;
		item.m_thumb = "filter_imgs/" + item.m_thumb;
		item.m_coverImg = "filter_imgs/" + item.m_coverImg;
		item.m_tablePic = "filter_imgs/" + item.m_tablePic;
		if(item.m_compose != null)
		{
			for(int j = 0; j < item.m_compose.length; j ++)
			{
				item.m_compose[j].blend_pic = "filter_imgs/" + item.m_compose[j].blend_pic;
			}
		}
	}

	@Override
	protected void RebuildNetResArr(ArrayList<FilterRes> dst, ArrayList<FilterRes> src)
	{
		if(dst != null && src != null)
		{
			FilterRes srcTemp;
			FilterRes dstTemp;
			Class cls = FilterRes.class;
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
					dstTemp.m_authorImg = srcTemp.m_authorImg;
					dstTemp.m_coverImg = srcTemp.m_coverImg;
					dstTemp.m_shareImg = srcTemp.m_shareImg;

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

	@Override
	protected String GetLocalPath()
	{
		return LOCAL_PATH;
	}

	@Override
	public FilterRes GetItem(ArrayList<FilterRes> arr, int id)
	{
		if(arr != null)
		{
			return ResourceUtils.GetItem(arr, id);
		}
		return null;
	}

	@Override
	protected void InitOrderArr(ArrayList<Integer> dstObj)
	{
		super.InitOrderArr(dstObj);
		ReadOrderArr(MyFramework2App.getInstance().getApplicationContext(), ORDER_PATH);
	}

	@Override
	protected void InitLocalData(SQLiteDatabase db)
	{
		ArrayList<FilterRes> localArr = null;
		ArrayList<FilterRes> sdcardArr = null;
		Context context = MyFramework2App.getInstance().getApplication();

		if(TagMgr.CheckTag(context, "filter_has_insert_into_database"))
		{
			sdcardArr = sync_GetSdcardRes(context, null);
		}

		if(TagMgr.CheckTag(context, LOCAL_REFRESH_DATABASE_FLAG))
		{
			localArr = sync_GetLocalRes(context, null);
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
					for(FilterRes res : localArr)
					{
						SaveResByDB(db, res);
					}
					TagMgr.SetTag(context, LOCAL_REFRESH_DATABASE_FLAG);
				}
				if(sdcardArr != null)
				{
					for(FilterRes res : sdcardArr)
					{
						SaveResByDB(db, res);
					}
					TagMgr.SetTag(context, "filter_has_insert_into_database");
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
	public boolean CheckExist(FilterRes res)
	{
		boolean out = false;

		if(CheckIntact(res))
		{
			if(ResourceUtils.IsResExist(MyFramework2App.getInstance().getApplicationContext(), (String)res.m_thumb))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	public boolean CheckIntact(FilterRes res)
	{
		boolean out = false;

		if(res != null)
		{
			if(ResourceUtils.HasIntact(res.m_thumb) && ResourceUtils.HasIntact(res.m_filterData))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	public ArrayList<FilterRes> sync_GetCloudCacheRes(Context context, DataFilter filter)
	{
		ArrayList<FilterRes> arr = mCloudResArr;
		ArrayList<FilterRes> arr2 = super.sync_GetCloudCacheRes(context, filter);

		synchronized(CLOUD_MEM_LOCK)
		{
			if(arr != arr2 && arr2 != null)
			{
				UpdateOldId(arr2);
			}
		}
		if(arr != arr2 && arr2 != null)
		{
			synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
			{
				ReDownload(GetDB());
				CloseDB();
			}
		}

		return arr2;
	}

	/**
	 * 下载1.4.0版本缺失的素材
	 * @param db
	 */
	private void ReDownload(SQLiteDatabase db)
	{
		ArrayList<FilterRes> temp = ReadResArrByDB(db, null);
		for(FilterRes res : temp){
			if(TextUtils.isEmpty(res.m_tablePic))
			{
				FilterRes res1 = GetCloudRes(res.m_id);
				if(res1 != null)
				{
					DownloadMgr.getInstance().DownloadRes(res1, null);
				}
			}
		}
	}

	public void SaveOrderArr()
	{
		SaveOrderArr(ORDER_PATH);
	}

	/**
	 * 需要统一控制数据库的时候调用
	 * @param db
	 * @param ids
	 * @return
	 */
	public ArrayList<FilterRes> GetResArr(SQLiteDatabase db, int[] ids, boolean onlyLocal)
	{
		ArrayList<FilterRes> out = new ArrayList<FilterRes>();
		if(ids != null)
		{
			ArrayList<FilterRes> temp = ReadResArrByDB(db, ids);
			if(onlyLocal || ids.length == temp.size()){
				out = temp;
			}
			else
			{
				FilterRes res;
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

	public ArrayList<FilterRes> GetResArr(SQLiteDatabase db, int[] ids)
	{
		return GetResArr(db, ids, false);
	}

	public ArrayList<FilterRes> GetLocalResArr(SQLiteDatabase db, boolean isVideo)
	{
		ArrayList<FilterRes> out = new ArrayList<FilterRes>();
		ArrayList<Integer> order_arr = GetOrderArr();
		if(order_arr == null || order_arr.size() < 1)
			return out;
		if(db != null)
		{
			ArrayList<FilterRes> resArr = ReadResArrByDB(db, null);
			FilterRes res;
			for(int id : order_arr)
			{
				res = ResourceUtils.GetItem(resArr, id);
				if(res != null){
					if(isVideo){
						if(!TextUtils.isEmpty(res.m_tablePic))
						{
							out.add(res);
						}
					}
					else
					{
						out.add(res);
					}
				}
			}
		}
		return out;
	}

	@Override
	protected int GetCloudEventId()
	{
		return EventID.FILTER_CLOUD_OK;
	}

	@Override
	protected void sync_ui_CloudResChange(ArrayList<FilterRes> oldArr, ArrayList<FilterRes> newArr)
	{
		super.sync_ui_CloudResChange(oldArr, newArr);
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			ReDownload(GetDB());
			CloseDB();
		}
	}
}
