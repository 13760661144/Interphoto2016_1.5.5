package cn.poco.resource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.adnonstop.resourcelibs.DataFilter;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

import cn.poco.framework.EventID;
import cn.poco.framework.MyFramework2App;
import cn.poco.resource.database.TableNames;
import cn.poco.system.SysConfig;

/**
 * Created by admin on 2017/12/6.
 */

public class ThemeResMgr2 extends DBBaseResMgr2<ThemeRes, ArrayList<ThemeRes>>
{
	private static ThemeResMgr2 sInstance;
	public final static int NEW_JSON_VER = 5;
	public final static String SDCARD_PATH = DownloadMgr.getInstance().THEME_PATH + "/ress.xxxx";

	public final static String CLOUD_CACHE_PATH = DownloadMgr.getInstance().THEME_PATH + "/cache.xxxx";
	public static String CLOUD_URL = "http://beauty-material.adnonstop.com/API/poco_camera/theme/android.php?version=";

	public final static String OLD_ID_FLAG = "theme_id"; //判断是否有新素材更新

	private ThemeResMgr2()
	{
	}

	public synchronized static ThemeResMgr2 getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new ThemeResMgr2();
		}
		return sInstance;
	}

	@Override
	public int GetResArrSize(ArrayList<ThemeRes> arr)
	{
		if(arr != null)
			return arr.size();
		return 0;
	}

	@Override
	public ArrayList<ThemeRes> MakeResArrObj()
	{
		return new ArrayList<>();
	}

	@Override
	public boolean ResArrAddItem(ArrayList<ThemeRes> arr, ThemeRes item)
	{
		if(arr != null && item != null){
			arr.add(item);
		}
		return false;
	}

	@Override
	public int GetId(ThemeRes res)
	{
		if(res != null)
			return res.m_id;
		return 0;
	}

	@Override
	public ThemeRes ReadResByIndex(ArrayList<ThemeRes> themeRes, int index)
	{
		if(themeRes != null && index >= 0 && index < themeRes.size())
			return themeRes.get(index);
		return null;
	}

	@Override
	protected void DeleteSDRes(Context context, ThemeRes res)
	{

	}

	@Override
	protected ThemeRes ReadResItem(JSONObject jsonObj, boolean isPath)
	{
		ThemeRes out = null;

		if (jsonObj != null) {
			try {
				Object obj;

				out = new ThemeRes();
				if (isPath) {
					out.m_type = BaseRes.TYPE_LOCAL_PATH;
				} else {
					out.m_type = BaseRes.TYPE_NETWORK_URL;
				}
				String temp = jsonObj.getString("id");
				if (temp != null && temp.length() > 0) {
					out.m_id = (int) Long.parseLong(temp, 10);
				} else {
					out.m_id = (int) (Math.random() * 10000000);
				}
				out.m_name = jsonObj.getString("name");
				out.m_subTitle = jsonObj.getString("subtitle");

				if (jsonObj.has("recommend")) {
					out.m_recommend =  (int) Long.parseLong(jsonObj.getString("recommend"), 10);
				}

				temp = jsonObj.getString("tj_id");
				if (temp != null && temp.length() > 0) {
					out.m_tjId = (int) Long.parseLong(temp, 10);
				}
				out.m_version = jsonObj.getString("version");
				out.m_detail = jsonObj.getString("detail");
				if (isPath) {
					out.m_icon = jsonObj.getString("icon");
					out.m_dashiIcon = jsonObj.getString("dashi_icon");
					out.m_thumb = jsonObj.getString("icon_200");
				} else {
					out.url_icon = jsonObj.getString("icon");
					out.url_dashiIcon = jsonObj.getString("dashi_icon");
					temp = jsonObj.getString("unlock");
					if ("comment".equals(temp)) {
						out.m_shareType = LockRes.SHARE_TYPE_MARKET;
					} else if ("weixin".equals(temp)) {
						out.m_shareType = LockRes.SHARE_TYPE_WEIXIN;
					}
					out.url_thumb = jsonObj.getString("icon_200");
				}
				out.m_shareTitle = jsonObj.getString("share_title");
				out.m_dashiType = jsonObj.getString("dashi_type");
				out.m_dashiName = jsonObj.getString("dashi_name");
				out.m_dashiRank = jsonObj.getString("dashi_rank");
				if (jsonObj.has("share_link")) {
					out.m_shareUrl = jsonObj.getString("share_link");
				}
				if (jsonObj.has("title_color")) {
					out.m_titleColor = jsonObj.getString("title_color");
				}
				if (jsonObj.has("tj_link") && (obj = jsonObj.get("tj_link")) instanceof String) {
					out.m_tjLink = (String) obj;
				}
				temp = jsonObj.getString("is_hide");
				if (temp != null && temp.length() > 0) {
					int value = (int) Long.parseLong(temp);
					out.m_isHide = (value == 0) ? false : true;
				}
				temp = jsonObj.getString("is_business");
				if (temp != null && temp.length() > 0) {
					int value = (int) Long.parseLong(temp);
					out.m_isBusiness = (value == 0) ? false : true;
				}


				obj = jsonObj.get("content");
				if (obj instanceof JSONObject) {
					JSONObject jsonObj2 = (JSONObject) obj;
					if (jsonObj2.has("text")) {
						out.m_textIDArr = ParseIds(jsonObj2.getString("text"), 10);
					}
					if (jsonObj2.has("filter")) {
						out.m_filterIDArr = ParseIds(jsonObj2.getString("filter"), 10);
					}
					if (jsonObj2.has("light_effect")) {
						out.m_lightEffectIDArr = ParseIds(jsonObj2.getString("light_effect"), 10);
					}
					if (jsonObj2.has("music")) {

						out.m_musicIDArr = ParseIds(jsonObj2.getString("music"), 10);
						Log.i("musicmusicmusic", "ReadResItem: " +  jsonObj2.getString("music"));
					}
					if (jsonObj2.has("watermark")) {

						out.m_watermarkIDArr = ParseIds(jsonObj2.getString("watermark"), 10);
						Log.i("watermarkwatermark", "ReadResItem: " + jsonObj2.getString("watermark"));
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
				out = null;
			}
		}

		return out;
	}

	/**
	 * @param str
	 * @param radix 多少进制10/16
	 * @return
	 */
	public int[] ParseIds(String str, int radix) {
		int[] out = null;

		if (str != null && str.length() > 0 && str != "null") {
			String[] strIds = str.split(",");
			out = new int[strIds.length];
			for (int i = 0; i < strIds.length; i++) {
				out[i] = (int) Long.parseLong(strIds[i], radix);
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
	protected void RecodeLocalItem(ThemeRes item)
	{

	}

	@Override
	protected String GetLocalPath()
	{
		return null;
	}

	public String MakeStr(int[] arr, int radix) {
		String out = null;

		if (arr != null && arr.length > 0) {
			out = "";
			for (int i = 0; i < arr.length; i++) {
				if (i != 0) {
					out += "," + Integer.toString(arr[i], radix);
				} else {
					out += Integer.toString(arr[i], radix);
				}
			}
		}

		return out;
	}

	@Override
	protected String GetCloudUrl(Context context)
	{
		String url;
		if (SysConfig.GetAppVer(context).contains("88.8.8")) {
			url = CLOUD_URL + "88.8.8";
		} else {
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
	protected int GetCloudEventId()
	{
		return EventID.THEME_CLOUD_OK;
	}

	@Override
	public ThemeRes GetItem(ArrayList<ThemeRes> arr, int id)
	{
		return ResourceUtils.GetItem(arr, id);
	}

	@Override
	protected String GetTableName()
	{
		return TableNames.THEME;
	}

	@Override
	protected String GetKeyId()
	{
		return "id";
	}

	@Override
	protected ThemeRes ReadResByDB(Cursor cursor)
	{
		ThemeRes out = new ThemeRes();
		out.m_id = cursor.getInt(cursor.getColumnIndex("id"));
		out.m_name = cursor.getString(cursor.getColumnIndex("name"));
		out.m_subTitle = cursor.getString(cursor.getColumnIndex("subtitle"));
		out.m_detail = cursor.getString(cursor.getColumnIndex("detail"));
		out.m_tjId = cursor.getInt(cursor.getColumnIndex("tj_id"));
		out.m_thumb = cursor.getString(cursor.getColumnIndex("icon_200"));
		out.m_icon = cursor.getString(cursor.getColumnIndex("icon"));
		out.m_version = cursor.getString(cursor.getColumnIndex("version"));
		out.m_shareTitle = cursor.getString(cursor.getColumnIndex("share_title"));
		out.m_dashiType = cursor.getString(cursor.getColumnIndex("dashi_type"));
		out.m_dashiName = cursor.getString(cursor.getColumnIndex("dashi_name"));
		out.m_dashiRank = cursor.getString(cursor.getColumnIndex("dashi_rank"));
		out.m_shareUrl = cursor.getString(cursor.getColumnIndex("share_link"));
		out.m_dashiIcon = cursor.getString(cursor.getColumnIndex("dashi_icon"));
		out.m_titleColor = cursor.getString(cursor.getColumnIndex("title_color"));
		out.m_tjLink = cursor.getString(cursor.getColumnIndex("tj_link"));
		out.m_tjShowId = cursor.getInt(cursor.getColumnIndex("show_id"));
		out.m_order = cursor.getInt(cursor.getColumnIndex("order"));
		int ishide = cursor.getInt(cursor.getColumnIndex("is_hide"));
		out.m_isHide = ishide == 1 ? true: false;
		int isBusiness = cursor.getInt(cursor.getColumnIndex("is_business"));
		out.m_isBusiness = isBusiness == 1 ? true: false;
		String str = cursor.getString(cursor.getColumnIndex("text"));
		out.m_textIDArr = ParseIds(str, 10);
		str = cursor.getString(cursor.getColumnIndex("filter"));
		out.m_filterIDArr = ParseIds(str, 10);
		str = cursor.getString(cursor.getColumnIndex("light_effect"));
		out.m_lightEffectIDArr = ParseIds(str, 10);
		str = cursor.getString(cursor.getColumnIndex("music"));
		out.m_musicIDArr = ParseIds(str, 10);
		str = cursor.getString(cursor.getColumnIndex("watermark"));
		out.m_watermarkIDArr = ParseIds(str, 10);
		out.m_recommend = cursor.getInt(cursor.getColumnIndex("recoment"));
		out.m_type = cursor.getInt(cursor.getColumnIndex("store_type"));
		return out;
	}

	@Override
	public boolean SaveResByDB(SQLiteDatabase db, ThemeRes item)
	{
		if(db != null && item != null)
		{
			ContentValues values = new ContentValues();
			values.put("id", item.m_id);
			values.put("name", item.m_name);
			values.put("subtitle", item.m_subTitle);
			values.put("detail", item.m_detail);
			values.put("tj_id", item.m_tjId);
			values.put("icon_200", item.m_thumb instanceof String ? (String)item.m_thumb : null);
			values.put("icon", item.m_icon);
			values.put("version", item.m_version);
			values.put("share_title", item.m_shareTitle);
			values.put("dashi_type", item.m_dashiType);
			values.put("dashi_name", item.m_dashiName);
			values.put("dashi_rank", item.m_dashiRank);
			values.put("share_link", item.m_shareUrl);
			values.put("dashi_icon", item.m_dashiIcon);
			values.put("title_color", item.m_titleColor);
			values.put("tj_link", item.m_tjLink);
			values.put("show_id", item.m_tjShowId);
			values.put("'order'", item.m_order);
			values.put("is_hide", item.m_isHide ? 1: 0);
			values.put("is_business", item.m_isBusiness ? 1 : 0);
			values.put("recoment", item.m_recommend);
			values.put("text", MakeStr(item.m_textIDArr, 10));
			values.put("filter", MakeStr(item.m_filterIDArr, 10));
			values.put("light_effect", MakeStr(item.m_lightEffectIDArr, 10));
			values.put("music", MakeStr(item.m_musicIDArr, 10));
			values.put("watermark", MakeStr(item.m_watermarkIDArr, 10));
			values.put("store_type", item.m_type);

			db.insertWithOnConflict(TableNames.THEME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			return true;
		}
		return false;
	}

	public static String getOldIdFlag()
	{
		return OLD_ID_FLAG;
	}

	@Override
	public ArrayList<ThemeRes> sync_GetCloudCacheRes(Context context, DataFilter filter)
	{
		ArrayList<ThemeRes> arr = mCloudResArr;
		ArrayList<ThemeRes> arr2 = super.sync_GetCloudCacheRes(context, filter);

		synchronized(CLOUD_MEM_LOCK)
		{
			if(arr != arr2 && arr2 != null)
			{
				//数据有刷新,通常是第一次
				RebuildNetResArr(arr2, arr);
				BuildThemeArr(arr2, sync_GetSdcardRes(context, filter));
			}
		}

		return arr2;
	}

	protected static void BuildThemeArr(ArrayList<ThemeRes> dst, ArrayList<ThemeRes> src)
	{
		if(dst != null && src != null)
		{
			ThemeRes srcTemp;
			ThemeRes dstTemp;
			int len = src.size();
			for(int i = 0; i < len; i++)
			{
				srcTemp = src.get(i);
				dstTemp = ResourceUtils.GetItem(dst, srcTemp.m_id);
				if(dstTemp != null)
				{
					dstTemp.m_type = srcTemp.m_type;
					dstTemp.m_thumb = srcTemp.m_thumb;
					dstTemp.m_icon = srcTemp.m_icon;
					dstTemp.m_dashiIcon = srcTemp.m_dashiIcon;
				}
			}
		}
	}

	@Override
	protected void RebuildNetResArr(ArrayList<ThemeRes> dst, ArrayList<ThemeRes> src)
	{
		if (dst != null && src != null) {
			ThemeRes srcTemp;
			ThemeRes dstTemp;
			Class cls = ThemeRes.class;
			Field[] fields = cls.getDeclaredFields();
			int size = dst.size();
			for (int i = 0; i < size; i++) {
				dstTemp = dst.get(i);
				srcTemp = GetItem(src, dstTemp.m_id);
				if (srcTemp != null) {
					dstTemp.m_type = srcTemp.m_type;
					dstTemp.m_thumb = srcTemp.m_thumb;
					dstTemp.m_icon = srcTemp.m_icon;
					dstTemp.m_dashiIcon = srcTemp.m_dashiIcon;

					for (int k = 0; k < fields.length; k++) {
						try {
							Object value = fields[k].get(dstTemp);
							fields[k].set(srcTemp, value);
						} catch (Exception e1) {
						}
					}
					dst.set(i, srcTemp);
				}
			}
		}
	}

	/**
	 * 包含内置和网络资源
	 *
	 * @return
	 */
	public ArrayList<ThemeRes> GetAllThemeResArr(SQLiteDatabase db)
	{
		ArrayList<ThemeRes> out = new ArrayList<>();
		ArrayList<ThemeRes> resArr = ReadResArrByDB(db, null);
		ArrayList<ThemeRes> cloudResArr = sync_ar_GetCloudCacheRes(MyFramework2App.getInstance().getApplication(), null);
		if(cloudResArr != null)
		{
			for(ThemeRes res : cloudResArr)
			{
				if(res.m_type == BaseRes.TYPE_NETWORK_URL)
				{
					ThemeRes temp = ResourceUtils.GetItem(resArr, res.m_id);
					if(temp != null)
					{
						res.m_type = temp.m_type;
						res.m_icon = temp.m_icon;
						res.m_dashiIcon = temp.m_dashiIcon;
						res.m_thumb = temp.m_thumb;
					}
				}
				out.add(res);
			}
		}
		return out;
	}

	@Override
	protected void InitLocalData(SQLiteDatabase db)
	{

	}
}
