package cn.poco.resource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.poco.Text.Painter;
import cn.poco.framework.EventID;
import cn.poco.framework.MyFramework2App;
import cn.poco.resource.database.TableNames;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.utils.FileUtil;

/**
 * Created by admin on 2017/12/6.
 */

public class TextResMgr2 extends DBBaseResMgr2<TextRes, ArrayList<TextRes>>
{
	private static TextResMgr2 sInstance;
	public final static String LOCAL_REFRESH_DATABASE_FLAG = "text_need_refresh_database_1.5.5";
	public final static String LOCAL_PATH = "data_json/text.json";

	public final static String SDCARD_PATH = DownloadMgr.getInstance().TEXT_PATH + "/ress.xxxx"; //资源集合

	public final static String ORDER_PATH = DownloadMgr.getInstance().TEXT_PATH + "/order.xxxx"; //显示的item&排序(不存在这里的id不会显示)

	public final static String CLOUD_CACHE_PATH = DownloadMgr.getInstance().TEXT_PATH + "/cache.xxxx";
	public static String CLOUD_URL = "http://beauty-material.adnonstop.com/API/interphoto/font/android.php?version=";// + "&random=" + Math.random();

	private TextResMgr2()
	{
	}

	public synchronized static TextResMgr2 getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new TextResMgr2();
		}
		return sInstance;
	}

	@Override
	public int GetResArrSize(ArrayList<TextRes> arr)
	{
		if(arr != null)
			return arr.size();
		return 0;
	}

	@Override
	public ArrayList<TextRes> MakeResArrObj()
	{
		return new ArrayList<>();
	}

	@Override
	public boolean ResArrAddItem(ArrayList<TextRes> arr, TextRes item)
	{
		if(arr != null && item != null){
			arr.add(item);
		}
		return false;
	}

	@Override
	protected String GetTableName()
	{
		return TableNames.TEXT;
	}

	@Override
	protected String GetKeyId()
	{
		return "id";
	}

	@Override
	protected TextRes ReadResByDB(Cursor cursor)
	{
		TextRes out = new TextRes();
		out.m_id = cursor.getInt(cursor.getColumnIndex("id"));
		out.m_editable = cursor.getInt(cursor.getColumnIndex("editable"));
		out.m_resTypeID = cursor.getInt(cursor.getColumnIndex("restype_id"));
		out.m_resTypeName = cursor.getString(cursor.getColumnIndex("restype"));
		out.m_order = cursor.getInt(cursor.getColumnIndex("order"));
		out.m_titleColor = cursor.getString(cursor.getColumnIndex("title_color"));
		out.m_thumb = cursor.getString(cursor.getColumnIndex("thumb_120"));
		out.m_tjId = cursor.getInt(cursor.getColumnIndex("tracking_code"));
		String res_arr = cursor.getString(cursor.getColumnIndex("res_arr"));
		if(!TextUtils.isEmpty(res_arr))
		{
			try
			{
				JSONArray array = new JSONArray(res_arr);
				out.m_resArr = FontResMgr.ReadResArr(array);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		out.m_imageZip = cursor.getString(cursor.getColumnIndex("image_zip"));
		out.m_orderType = cursor.getInt(cursor.getColumnIndex("type"));
		out.m_name = cursor.getString(cursor.getColumnIndex("previewTitle"));
		out.m_headLink = cursor.getString(cursor.getColumnIndex("head_link"));
		out.m_headImg = cursor.getString(cursor.getColumnIndex("head_img"));
		out.m_coverImg = cursor.getString(cursor.getColumnIndex("cover_pic"));
		out.m_pic = cursor.getString(cursor.getColumnIndex("pic"));
		out.m_align = cursor.getString(cursor.getColumnIndex("align"));
		out.m_offsetX = cursor.getFloat(cursor.getColumnIndex("margin_x"));
		out.m_offsetY = cursor.getFloat(cursor.getColumnIndex("margin_y"));
		int hide = cursor.getInt(cursor.getColumnIndex("is_hide"));
		out.m_isHide = hide == 1 ? true : false;
		out.m_type = cursor.getInt(cursor.getColumnIndex("store_type"));
		return out;
	}

	@Override
	public boolean SaveResByDB(SQLiteDatabase db, TextRes item)
	{
		if(db != null && item != null)
		{
			ContentValues values = new ContentValues();
			values.put("id", item.m_id);
			values.put("editable", item.m_editable);
			values.put("restype_id", item.m_resTypeID);
			values.put("restype", item.m_resTypeName);
			values.put("'order'", item.m_order);
			values.put("title_color", item.m_titleColor);
			values.put("thumb_120", item.m_thumb instanceof String ? (String)item.m_thumb : "");
			values.put("tracking_code", item.m_tjId);
			JSONArray res_arr = new JSONArray();
			if(item.m_resArr != null)
			{
				int arrLen = item.m_resArr.size();
				JSONObject arrObj;
				FontRes fontRes;
				for(int k = 0; k < arrLen; k++)
				{
					fontRes = item.m_resArr.get(k);
					arrObj = new JSONObject();
					try
					{
						arrObj.put("id", fontRes.m_id);
						arrObj.put("size", fontRes.m_size);
						arrObj.put("zip_url", fontRes.url_res);
						res_arr.put(arrObj);
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			values.put("res_arr", res_arr.toString());
			values.put("image_zip", item.m_imageZip);
			values.put("type", item.m_orderType);
			values.put("previewTitle", item.m_name);
			values.put("head_link", item.m_headLink);
			values.put("head_img", item.m_headImg);
			values.put("cover_pic", item.m_coverImg instanceof String ? (String)item.m_coverImg : "");
			values.put("pic", item.m_pic);
			values.put("align", item.m_align);
			values.put("margin_x", item.m_offsetX);
			values.put("margin_y", item.m_offsetY);
			values.put("is_hide", item.m_isHide ? 1 : 0);
			values.put("store_type", item.m_type);
			long flag = db.insertWithOnConflict(TableNames.TEXT, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if(flag < 0)
				return false;
			return true;
		}
		return false;
	}

	@Override
	public int GetId(TextRes res)
	{
		if(res != null)
			return res.m_id;
		return 0;
	}

	@Override
	public TextRes ReadResByIndex(ArrayList<TextRes> textRes, int index)
	{
		if(textRes != null && index >= 0 && index < textRes.size()){
			return textRes.get(index);
		}
		return null;
	}

	@Override
	protected void InitLocalData(SQLiteDatabase db)
	{
		if(db != null)
		{
			ArrayList<TextRes> localArr = null;
			ArrayList<TextRes> sdcardArr = null;

			Context context =  MyFramework2App.getInstance().getApplicationContext();
			if(TagMgr.CheckTag(context, "text_has_insert_into_database"))
			{
				sdcardArr = sync_GetSdcardRes(context, null);
			}

			if(TagMgr.CheckTag(context, LOCAL_REFRESH_DATABASE_FLAG))
			{
				localArr = sync_GetLocalRes(context, null);
			}
			boolean flag = false;
			if(localArr != null)
			{
				DeleteLocalResArr(db);
				for(TextRes res : localArr)
				{
					SaveResByDB(db, res);
					ArrayList<Integer> orderArr = GetOrderArr1().get(res.m_resTypeID);
					orderArr.add(res.m_id);
					flag = true;
				}
				TagMgr.SetTag(context, LOCAL_REFRESH_DATABASE_FLAG);
			}
			if(sdcardArr != null)
			{
				for(TextRes res : sdcardArr)
				{
					SaveResByDB(db, res);
				}
				TagMgr.SetTag(context, "text_has_insert_into_database");
			}

			for (Iterator it = GetOrderArr1().entrySet().iterator(); it.hasNext();) {
				Map.Entry e = (Map.Entry) it.next();
				ArrayList<Integer> orderArr = (ArrayList<Integer>)e.getValue();
				int key = (Integer)e.getKey();
				String[] selectIds = new String[]{"restype_id"};
				String[] selectValues = new String[]{key + ""};
				ArrayList<TextRes> resArr = ReadResArrByDB(db, null, selectIds, selectValues);
				boolean change = ResourceUtils.RebuildOrder(resArr, orderArr);
				if(!flag)
				{
					flag = change;
				}
			}
			if(flag)
			{
				SaveOrderArr();
			}
			ClearUnusedRes(db);

		}
	}

	@Override
	public boolean CheckExist(TextRes res)
	{
		boolean out = false;

		if(CheckIntact(res))
		{
			if(ResourceUtils.IsResExist(MyFramework2App.getInstance().getApplicationContext(), res.m_imageZip, (String)res.m_thumb)
					&& Painter.isFontExists(MyFramework2App.getInstance().getApplicationContext(), res.m_resArr))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	public boolean CheckIntact(TextRes res)
	{
		boolean out = false;

		if(res != null)
		{
			if(ResourceUtils.HasIntact(res.m_imageZip, res.m_thumb))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	protected void DeleteSDRes(Context context, TextRes res)
	{
		TextRes tempRes = GetCloudRes(res.m_id);
		int index = ResourceUtils.HasId(GetOrderArr1().get(res.m_resTypeID), res.m_id);
		if(index >= 0){
			GetOrderArr1().get(res.m_resTypeID).remove(index);
			SaveOrderArr();
		}
		if(tempRes != null){
			tempRes.m_type = BaseRes.TYPE_NETWORK_URL;
//			tempRes.m_coverImg = null;
//			tempRes.m_headImg = null;
			tempRes.m_pic = null;
			tempRes.m_imageZip = null;
//			tempRes.m_thumb = null;
		}
//		FileUtil.deleteSDFile(res.m_coverImg);
		FileUtil.deleteSDFile(res.m_pic);
//		FileUtil.deleteSDFile(res.m_headImg);
//		FileUtil.deleteSDFile((String)res.m_thumb);
		FileUtil.deleteSDFile(res.m_imageZip);
	}

	public void SaveOrderArr()
	{
		SaveOrderArr1(ORDER_PATH);
	}

	@Override
	protected void InitOrderArr(HashMap<Integer, ArrayList<Integer>> dstObj)
	{
		super.InitOrderArr(dstObj);
		ReadOrderArr1(ORDER_PATH);
		if(GetOrderArr1().get(1) == null)
		{
			//水印
			ArrayList<Integer> orderArr = new ArrayList<>();
			GetOrderArr1().put(1, orderArr);
		}
		if(GetOrderArr1().get(2) == null)
		{
			//创意
			ArrayList<Integer> orderArr = new ArrayList<>();
			GetOrderArr1().put(2, orderArr);
		}
	}

	@Override
	protected TextRes ReadResItem(JSONObject jsonObj, boolean isPath)
	{
		TextRes out = null;
		try
		{
			out = new TextRes();
			if(isPath)
			{
				out.m_type = BaseRes.TYPE_LOCAL_PATH;
			}
			else
			{
				out.m_type = BaseRes.TYPE_NETWORK_URL;
			}
			String id = jsonObj.getString("id");
			if(id != null && id.length() > 0)
			{
				out.m_id = Integer.parseInt(id);
			}
			String resTypeId = jsonObj.getString("restype_id");
			if(resTypeId != null && resTypeId.length() > 0)
			{
				out.m_resTypeID = Integer.parseInt(resTypeId);
			}
			out.m_resTypeName = jsonObj.getString("restype");
			String order = jsonObj.getString("order");
			if(order != null && order.length() > 0)
			{
				out.m_order = Integer.parseInt(order);
			}
			String editable = jsonObj.getString("editable");
			if(editable != null && editable.length() > 0)
			{
				out.m_editable = Integer.parseInt(editable);
			}
			out.m_titleColor = jsonObj.getString("title_color");
			if(isPath)
			{
				out.m_thumb = jsonObj.getString("thumb_120");
			}
			else
			{
				out.url_thumb = jsonObj.getString("thumb_120");
			}
			if(jsonObj.has("head_img"))
			{
				if(isPath)
				{
					out.m_headImg = jsonObj.getString("head_img");
				}
				else
				{
					out.url_headImg = jsonObj.getString("head_img");
				}
			}
			if(jsonObj.has("head_link"))
			{
				out.m_headLink = jsonObj.getString("head_link");
			}
			String tongjiId = jsonObj.getString("tracking_code");
			if(tongjiId != null && tongjiId.length() > 0)
			{
				out.m_tjId = Integer.parseInt(tongjiId);
			}
			if(isPath)
			{
				out.m_imageZip = jsonObj.getString("image_zip");
			}
			else
			{
				out.url_imageZip = jsonObj.getString("image_zip");
			}
			String type1 = jsonObj.getString("type");
			if(type1 != null && type1.length() > 0)
			{
				out.m_orderType = Integer.parseInt(type1);
			}
			out.m_name = jsonObj.getString("previewTitle");
			if(isPath)
			{
				out.m_pic = jsonObj.getString("pic");
			}
			else
			{
				out.url_pic = jsonObj.getString("pic");
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
			out.m_align = jsonObj.getString("align");
			String offsetX = jsonObj.getString("margin_x");
			if(offsetX != null && offsetX.length() > 0)
			{
				out.m_offsetX = Integer.parseInt(offsetX);
			}
			String offsetY = jsonObj.getString("margin_y");
			if(offsetY != null && offsetY.length() > 0)
			{
				out.m_offsetY = Integer.parseInt(offsetY);
			}
			Object obj = jsonObj.get("res_arr");
			if(obj != null && obj instanceof JSONArray)
			{
				JSONArray res_arr = (JSONArray)obj;
				out.m_resArr = FontResMgr.ReadResArr(res_arr);
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
	protected void RecodeLocalItem(TextRes item)
	{
		item.m_type = BaseRes.TYPE_LOCAL_RES;
		if(item.m_imageZip != null && item.m_imageZip.length() > 0)
		{
			item.m_imageZip = "zip/" + item.m_imageZip;
		}
		if(item.m_thumb != null)
		{
			item.m_thumb = "text_imgs/" + item.m_thumb;
		}
		if(item.m_headImg != null && item.m_headImg.length() > 0)
		{
			item.m_headImg = "text_imgs/" + item.m_headImg;
		}
		if(!TextUtils.isEmpty(item.m_coverImg))
		{
			item.m_coverImg = "text_imgs/" + item.m_coverImg;
		}
	}

	@Override
	protected String GetLocalPath()
	{
		return LOCAL_PATH;
	}

	@Override
	public TextRes GetItem(ArrayList<TextRes> arr, int id)
	{
		return ResourceUtils.GetItem(arr, id);
	}

	@Override
	protected int GetCloudEventId()
	{
		return EventID.TEXT_CLOUD_OK;
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
	protected void RebuildNetResArr(ArrayList<TextRes> dst, ArrayList<TextRes> src)
	{
		if(dst != null && src != null)
		{
			TextRes srcTemp;
			TextRes dstTemp;
			Class cls = TextRes.class;
			Field[] fields = cls.getDeclaredFields();
			int len = dst.size();
			for(int i = 0; i < len; i ++)
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
					dstTemp.m_pic = srcTemp.m_pic;
					dstTemp.m_imageZip = srcTemp.m_imageZip;

					for(int k = 0; k < fields.length; k ++)
					{
						try
						{
							if(!Modifier.isFinal(fields[k].getModifiers()))
							{
								Object value = fields[k].get(dstTemp);
								fields[k].set(srcTemp, value);
							}
						}catch(Exception e){
						}
					}
					dst.set(i, srcTemp);
				}
			}
		}
	}

	public int GetClassifyIdByResId(int id)
	{
		int typeID = -1;
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			SQLiteDatabase db = GetDB();
			TextRes res = ReadResByDB(db, id);
			CloseDB();
			if(res != null){
				typeID = res.m_resTypeID;
			}
		}
		return typeID;
	}

	/**
	 * 需要统一控制数据库的时候调用
	 * @param db
	 * @param ids
	 * @return
	 */
	public ArrayList<TextRes> GetResArr(SQLiteDatabase db, int[] ids, boolean onlyLocal)
	{
		ArrayList<TextRes> out = new ArrayList<TextRes>();
		if(ids != null)
		{
			ArrayList<TextRes> temp = ReadResArrByDB(db, ids);
			if(onlyLocal || ids.length == temp.size()){
				out = temp;
			}
			else
			{
				TextRes res;
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

	public ArrayList<TextRes> GetResArr(SQLiteDatabase db, int[] ids)
	{
		return GetResArr(db, ids, false);
	}

	/**
	 *
	 * @param db
	 * @param typeID
	 * @return
	 */
	public ArrayList<TextRes> GetLocalResArr(SQLiteDatabase db, int typeID)
	{
		ArrayList<TextRes> out = new ArrayList<TextRes>();
		ArrayList<Integer> order_arr = GetOrderArr1().get(typeID);

		if(order_arr == null || order_arr.size() < 1)
			return out;
		if(db != null)
		{
			String[] selectIds = null;
			String[] selectValues = null;
			if(typeID != -1)
			{
				selectIds = new String[]{"restype_id"};
				selectValues = new String[]{typeID + ""};
			}
			ArrayList<TextRes> resArr = ReadResArrByDB(db, null, selectIds, selectValues);
			TextRes res;
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
	protected String GetCloudCachePath(Context context)
	{
		return CLOUD_CACHE_PATH;
	}
}
