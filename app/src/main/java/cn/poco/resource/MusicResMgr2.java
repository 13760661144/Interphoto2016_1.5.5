package cn.poco.resource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.adnonstop.resourcelibs.DataFilter;

import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import cn.poco.framework.EventID;
import cn.poco.framework.MyFramework2App;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.resource.database.TableNames;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.utils.FileUtil;
import cn.poco.zip.Zip;

/**
 * Created by admin on 2017/12/6.
 */

public class MusicResMgr2 extends DBBaseResMgr2<MusicRes, ArrayList<MusicRes>>
{
	private static MusicResMgr2 sInstance;
	private String CLEAR_DATABASE_FLAG = "music_clear";
	public String LOCAL_REFRESH_DATABASE_FLAG = "music_need_refresh_database_1.5.0";
	public String LOCAL_PATH = "data_json/music.json";

	public String ORDER_PATH = DownloadMgr.getInstance().MUSIC_PATH + "/order.xxxx"; //显示的item&排序(不存在这里的id不会显示)

	public String CLOUD_CACHE_PATH = DownloadMgr.getInstance().MUSIC_PATH + "/cache.xxxx";
	public String CLOUD_URL = "http://beauty-material.adnonstop.com/API/interphoto/music/android.php?version=";

	private MusicResMgr2()
	{
		CLEAR_DATABASE_FLAG = "music_clear";
		LOCAL_REFRESH_DATABASE_FLAG = "music_need_refresh_database_1.5.0";
		LOCAL_PATH = "data_json/music.json";

		ORDER_PATH = DownloadMgr.getInstance().MUSIC_PATH + "/order.xxxx"; //显示的item&排序(不存在这里的id不会显示)

		CLOUD_CACHE_PATH = DownloadMgr.getInstance().MUSIC_PATH + "/cache.xxxx";
		CLOUD_URL = "http://beauty-material.adnonstop.com/API/interphoto/music/android.php?version=";
	}

	public synchronized static MusicResMgr2 getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new MusicResMgr2();
		}
		return sInstance;
	}

	@Override
	public int GetResArrSize(ArrayList<MusicRes> arr)
	{
		if(arr != null)
			return arr.size();
		return 0;
	}

	@Override
	public ArrayList<MusicRes> MakeResArrObj()
	{
		return new ArrayList<>();
	}

	@Override
	public boolean ResArrAddItem(ArrayList<MusicRes> arr, MusicRes item)
	{
		if(arr != null && item != null)
		{
			arr.add(item);
			return true;
		}
		return false;
	}

	@Override
	protected String GetTableName()
	{
		return TableNames.MUSIC;
	}

	@Override
	protected String GetKeyId()
	{
		return "id";
	}

	@Override
	protected MusicRes ReadResByDB(Cursor cursor)
	{
		MusicRes out = new MusicRes();
		out.m_id = cursor.getInt(cursor.getColumnIndex("id"));
		out.m_tjId = cursor.getInt(cursor.getColumnIndex("statisitcalID"));
		out.m_name = cursor.getString(cursor.getColumnIndex("name"));
		out.m_resTypeName = cursor.getString(cursor.getColumnIndex("type"));
		out.author = cursor.getString(cursor.getColumnIndex("author"));
		out.coverColor = cursor.getString(cursor.getColumnIndex("coverColor"));
		out.m_thumb = cursor.getString(cursor.getColumnIndex("thumbnail"));
		out.format = cursor.getString(cursor.getColumnIndex("format"));
		out.duration = cursor.getInt(cursor.getColumnIndex("duration"));
		out.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
		out.m_res = cursor.getString(cursor.getColumnIndex("res_path"));
		out.lockType = cursor.getInt(cursor.getColumnIndex("isLock"));
		out.shareImg = cursor.getString(cursor.getColumnIndex("shareImg"));
		out.shareTitle = cursor.getString(cursor.getColumnIndex("shareTitle"));
		out.m_shareLink = cursor.getString(cursor.getColumnIndex("shareLink"));
		int hide = cursor.getInt(cursor.getColumnIndex("is_hide"));
		out.m_isHide = hide == 1 ? true : false;
		out.m_type = cursor.getInt(cursor.getColumnIndex("store_type"));
		return out;
	}

	@Override
	public boolean SaveResByDB(SQLiteDatabase db, MusicRes item)
	{
		if(db != null && item != null)
		{
			ContentValues values = new ContentValues();
			values.put("id", item.m_id);
			values.put("statisitcalID", item.m_tjId);
			values.put("name", item.m_name);
			values.put("type", item.m_resTypeName);
			values.put("author", item.author);
			values.put("coverColor", item.coverColor);
			values.put("thumbnail", item.m_thumb instanceof String ? (String)item.m_thumb : "");
			values.put("format", item.format);
			values.put("duration", item.duration);
			values.put("fileName", item.fileName);
			values.put("res_path", item.m_res);
			values.put("isLock", item.lockType);
			values.put("shareImg", item.shareImg);
			values.put("shareTitle", item.shareTitle);
			values.put("shareLink", item.m_shareLink);
			values.put("is_hide", item.m_isHide ? 1 : 0);
			values.put("store_type", item.m_type);
			long flag = db.insertWithOnConflict(TableNames.MUSIC, null, values, SQLiteDatabase.CONFLICT_REPLACE);
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
		ArrayList<MusicRes> localArr = null;
		Context context = MyFramework2App.getInstance().getApplicationContext();

		if(db != null)
		{
			if(TagMgr.CheckTag(context, CLEAR_DATABASE_FLAG))
			{
				String sql = "delete from " + TableNames.MUSIC;
				if(db != null)
				{
					db.execSQL(sql);
				}
				FileUtil.deleteSDFile(DownloadMgr.getInstance().MUSIC_PATH);
				TagMgr.SetTag(context, CLEAR_DATABASE_FLAG);
			}
			if(TagMgr.CheckTag(context, LOCAL_REFRESH_DATABASE_FLAG))
			{
				localArr = sync_GetLocalRes(context, null);
			}
			boolean flag1 = false;
			if(localArr != null)
			{
				DeleteLocalResArr(db);
				flag1 = ResourceUtils.RebuildLocalOrder(localArr, GetOrderArr());
				for(MusicRes res : localArr){
					SaveResByDB(db, res);
				}
				TagMgr.SetTag(context, LOCAL_REFRESH_DATABASE_FLAG);
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
	public ArrayList<MusicRes> sync_GetCloudCacheRes(Context context, DataFilter filter)
	{
		ArrayList<MusicRes> res2 = super.sync_GetCloudCacheRes(context, filter);
		if(res2 != null)
		{
			for(MusicRes res : res2)
			{
				DownloadMgr.getInstance().DownloadRes(res, true, null);
			}
		}
		return res2;
	}

	@Override
	protected void sync_ui_CloudResChange(ArrayList<MusicRes> oldArr, ArrayList<MusicRes> newArr)
	{
		super.sync_ui_CloudResChange(oldArr, newArr);
		if(newArr != null)
		{
			for(MusicRes res : newArr)
			{
				DownloadMgr.getInstance().DownloadRes(res, true, null);
			}
		}
	}

	@Override
	public boolean CheckExist(MusicRes res)
	{
		boolean out = false;

		if(CheckIntact(res))
		{
			if(ResourceUtils.IsResExist(MyFramework2App.getInstance().getApplicationContext(), (String)res.m_thumb, res.m_res))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	public boolean CheckIntact(MusicRes res)
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
	public int GetId(MusicRes res)
	{
		if(res != null)
			return res.m_id;
		return 0;
	}

	@Override
	public MusicRes ReadResByIndex(ArrayList<MusicRes> musicRes, int index)
	{
		if(musicRes != null && index >= 0 && index < musicRes.size())
			return musicRes.get(index);
		return null;
	}

	@Override
	protected void DeleteSDRes(Context context, MusicRes res)
	{
		MusicRes tempRes = GetCloudRes(res.m_id);
		int index =  ResourceUtils.HasId(GetOrderArr(), res.m_id);
		if(index >= 0){
			GetOrderArr().remove(index);
			SaveOrderArr(ORDER_PATH);
		}
		if(tempRes != null){
			tempRes.m_type = BaseRes.TYPE_NETWORK_URL;
			tempRes.m_res = null;
		}
		FileUtil.deleteSDFile(res.m_res);
	}

	@Override
	protected MusicRes ReadResItem(JSONObject jsonObj, boolean isPath)
	{
		MusicRes out = null;
		try
		{
			out = new MusicRes();
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
			String tongjiId = jsonObj.getString("statisitcalID");
			if(tongjiId != null && tongjiId.length() > 0)
			{
				out.m_tjId = Integer.parseInt(tongjiId);
			}
			out.m_resTypeName = jsonObj.getString("type");
			out.m_name = jsonObj.getString("name");
			out.author = jsonObj.getString("author");
			out.coverColor = jsonObj.getString("coverColor");
			out.fileName = jsonObj.getString("fileName");
			String duration = jsonObj.getString("duration");
			if(duration != null && duration.length() > 0)
			{
				out.duration = Integer.parseInt(duration);
			}
			if(isPath)
			{
				out.m_thumb = jsonObj.getString("thumbnail");
			}
			else
			{
				out.url_thumb = jsonObj.getString("thumbnail");
			}
			if(isPath)
			{
				out.m_res = jsonObj.getString("downloadUrl");
			}
			else
			{
				out.url_res = jsonObj.getString("downloadUrl");
			}
			if (jsonObj.has("audition")){
				Log.i("auditionaudition", "ReadResItem: " + jsonObj.getString("audition"));
				out.m_auditionURL = jsonObj.getString("audition");
			}

			out.shareTitle = jsonObj.getString("shareTitle");
			if(jsonObj.has("shareLink"))
			{
				out.m_shareLink = jsonObj.getString("shareLink");
			}
			if(jsonObj.has("isLock"))			{
				String lockType = jsonObj.getString("isLock");
				if("weixin".equals(lockType))
				{
					out.lockType = LockRes.SHARE_TYPE_WEIXIN;
				}
				else if("comment".equals(lockType))
				{
					out.lockType = LockRes.SHARE_TYPE_MARKET;
				}
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
		return null;
	}

	@Override
	protected void RecodeLocalItem(MusicRes res)
	{
		res.m_type = BaseRes.TYPE_LOCAL_RES;
		String name = res.m_res;
		String parentPath = res.GetSaveParentPath();
		String path = parentPath + File.separator + name;
		//zip包会解压成文件夹，为了不重复下载，如果与zip包同名文件夹存在，我这边就认为素材是下载好了的；
		if(path.endsWith(".zip"))
		{
			String temp = FileUtil.GetImgFilePathNoSuffix(path) + "/.nomeida";
			File file = new File(temp);
			if(file.exists() && file.isDirectory())
			{
				file.delete();
			}
			String musicPath = "music/" + res.m_res;
			try
			{
				Zip.UnZipAssetsFolder(MyFramework2App.getInstance().getApplicationContext(), musicPath, temp, false);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			res.m_res = temp;
		}
		res.m_thumb = "music/" + res.m_thumb;
		res.shareImg = "music/" + res.shareImg;
	}

	@Override
	protected String GetLocalPath()
	{
		return LOCAL_PATH;
	}

	@Override
	public MusicRes GetItem(ArrayList<MusicRes> arr, int id)
	{
		if(arr != null)
			return ResourceUtils.GetItem(arr, id);
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
			url = CLOUD_URL + "1.5.0";
		}
		return url;
	}

	@Override
	protected String GetCloudCachePath(Context context)
	{
		return CLOUD_CACHE_PATH;
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
	public ArrayList<MusicRes> GetResArr(SQLiteDatabase db, int[] ids, boolean onlyLocal)
	{
		ArrayList<MusicRes> out = new ArrayList<MusicRes>();
		if(ids != null)
		{
			ArrayList<MusicRes> temp = ReadResArrByDB(db, ids);
			if(onlyLocal || ids.length == temp.size()){
				out = temp;
			}
			else
			{
				MusicRes res;
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

	public ArrayList<MusicRes> GetResArr(SQLiteDatabase db, int[] ids)
	{
		return GetResArr(db, ids, false);
	}

	public ArrayList<MusicRes> GetAllResArr(Context context, SQLiteDatabase db)
	{
		ArrayList<MusicRes> out = new ArrayList<MusicRes>();
		ArrayList<MusicRes> LocalResArr = GetLocalResArr(db);
		out.addAll(LocalResArr);

		ArrayList<MusicRes> resArr = sync_ar_GetCloudCacheRes(context, null);

		if(resArr != null)
		{
			for(MusicRes res : resArr)
			{
				if(ResourceUtils.HasItem(LocalResArr, res.m_id) == -1)
				{
					if(res.lockType != LockRes.SHARE_TYPE_NONE)
					{
						out.add(0, res);
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

	public ArrayList<MusicRes> GetLocalResArr(SQLiteDatabase db)
	{
		ArrayList<MusicRes> out = new ArrayList<MusicRes>();
		if(db != null)
		{
			ArrayList<MusicRes> resArr = ReadResArrByDB(db, null);
			ArrayList<Integer> order_arr = GetOrderArr();
			if(order_arr == null || order_arr.size() < 1)
				return out;
			MusicRes res;
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
	protected void RebuildNetResArr(ArrayList<MusicRes> dst, ArrayList<MusicRes> src)
	{
		if(dst != null && src != null)
		{
			MusicRes srcTemp;
			MusicRes dstTemp;
			Class cls = MusicRes.class;
			Field[] fields = cls.getDeclaredFields();
			int len = dst.size();
			for(int i = 0; i < len; i ++)
			{
				dstTemp = dst.get(i);
				srcTemp = GetItem(src, dstTemp.m_id);
				if(srcTemp != null)
				{
					dstTemp.m_type = srcTemp.m_type;
					dstTemp.m_thumb = srcTemp.m_thumb;
					dstTemp.m_res = srcTemp.m_res;
					dstTemp.shareImg = srcTemp.shareImg;

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

	@Override
	protected int GetCloudEventId()
	{
		return EventID.MUSIC_CLOUD_OK;
	}
}
