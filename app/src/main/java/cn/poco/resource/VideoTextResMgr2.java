package cn.poco.resource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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

public class VideoTextResMgr2 extends DBBaseResMgr2<VideoTextRes, ArrayList<VideoTextRes>>
{
	private static VideoTextResMgr2 sInstance;
	private final static String CLEAR_DATABASE_FLAG = "video_text_clear";
	public final static String LOCAL_REFRESH_DATABASE_FLAG = "videotext_need_refresh_database_1.5.0";
	public final static String LOCAL_PATH = "data_json/video_text.json";

	public final static String ORDER_PATH = DownloadMgr.getInstance().VIDEO_TEXT_PATH + "/order.xxxx"; //显示的item&排序(不存在这里的id不会显示)

	public final static String CLOUD_CACHE_PATH = DownloadMgr.getInstance().VIDEO_TEXT_PATH + "/cache.xxxx";
	public static String CLOUD_URL = "http://beauty-material.adnonstop.com/API/interphoto/watermark/android.php?version=";// + "&random=" + Math.random();

	private VideoTextResMgr2()
	{
	}

	public synchronized static VideoTextResMgr2 getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new VideoTextResMgr2();
		}
		return sInstance;
	}

	@Override
	public int GetResArrSize(ArrayList<VideoTextRes> arr)
	{
		if(arr != null){
			return arr.size();
		}
		return 0;
	}

	@Override
	public ArrayList<VideoTextRes> MakeResArrObj()
	{
		return new ArrayList<>();
	}

	@Override
	public boolean ResArrAddItem(ArrayList<VideoTextRes> arr, VideoTextRes item)
	{
		if(arr != null && item != null){
			arr.add(item);
		}
		return false;
	}

	@Override
	protected String GetTableName()
	{
		return TableNames.VIDEO_TEXT;
	}

	@Override
	protected String GetKeyId()
	{
		return "id";
	}

	@Override
	protected VideoTextRes ReadResByDB(Cursor cursor)
	{
		VideoTextRes out = new VideoTextRes();
		out.m_id = cursor.getInt(cursor.getColumnIndex("id"));
		out.m_tjId = cursor.getInt(cursor.getColumnIndex("statisitcalID"));
		out.m_name = cursor.getString(cursor.getColumnIndex("name"));
		out.m_resTypeName = cursor.getString(cursor.getColumnIndex("type"));
		out.author = cursor.getString(cursor.getColumnIndex("author"));
		out.coverColor = cursor.getString(cursor.getColumnIndex("coverColor"));
		out.m_coverPic = cursor.getString(cursor.getColumnIndex("cover_pic"));
		out.m_thumb = cursor.getString(cursor.getColumnIndex("thumbnail"));
		out.m_res = cursor.getString(cursor.getColumnIndex("res_path"));
		out.lockType = cursor.getInt(cursor.getColumnIndex("isLock"));
		out.shareImg = cursor.getString(cursor.getColumnIndex("shareImg"));
		out.shareTitle = cursor.getString(cursor.getColumnIndex("shareTitle"));
		out.shareIntroduce = cursor.getString(cursor.getColumnIndex("shareIntroduce"));
		out.m_shareLink = cursor.getString(cursor.getColumnIndex("shareLink"));
		int hide = cursor.getInt(cursor.getColumnIndex("is_hide"));
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
		out.editType = cursor.getInt(cursor.getColumnIndex("editType"));
		out.m_resTypeID = cursor.getInt(cursor.getColumnIndex("type_id"));
		out.m_isHide = hide == 1 ? true : false;
		out.m_type = cursor.getInt(cursor.getColumnIndex("store_type"));
		return out;
	}

	@Override
	public boolean SaveResByDB(SQLiteDatabase db, VideoTextRes item)
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
			values.put("cover_pic", item.m_coverPic);
			values.put("thumbnail", item.m_thumb instanceof String ? (String)item.m_thumb : "");
			values.put("res_path", item.m_res);
			values.put("editType", item.editType);
			values.put("type_id", item.m_resTypeID);
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
			values.put("isLock", item.lockType);
			values.put("shareImg", item.shareImg);
			values.put("shareTitle", item.shareTitle);
			values.put("shareIntroduce", item.shareIntroduce);
			values.put("shareLink", item.m_shareLink);
			values.put("is_hide", item.m_isHide ? 1 : 0);
			values.put("store_type", item.m_type);
			long flag = db.insertWithOnConflict(TableNames.VIDEO_TEXT, null, values, SQLiteDatabase.CONFLICT_REPLACE);
			if(flag < 0)
				return false;
			return true;
		}
		return false;
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
	protected void InitLocalData(SQLiteDatabase db)
	{
		ArrayList<VideoTextRes> localArr = null;
		Context context = MyFramework2App.getInstance().getApplicationContext();

		if(db != null)
		{
			boolean flag1 = false;		//判断是否有改变

			if(TagMgr.CheckTag(context, CLEAR_DATABASE_FLAG))
			{
				String sql = "delete from " + TableNames.VIDEO_TEXT;
				if(db != null)
				{
					db.execSQL(sql);
				}
				FileUtil.deleteSDFile(DownloadMgr.getInstance().VIDEO_TEXT_PATH);
				TagMgr.SetTag(context, CLEAR_DATABASE_FLAG);
			}
			if(TagMgr.CheckTag(context, LOCAL_REFRESH_DATABASE_FLAG))
			{
				localArr = sync_GetLocalRes(context, null);
			}
			if(localArr != null)
			{
				DeleteLocalResArr(db);
				for(VideoTextRes res : localArr){
					SaveResByDB(db, res);
					ArrayList<Integer> orderArr = GetOrderArr1().get(res.m_resTypeID);
					orderArr.add(res.m_id);
					flag1 = true;
				}
				TagMgr.SetTag(context, LOCAL_REFRESH_DATABASE_FLAG);
			}

			for (Iterator it = GetOrderArr1().entrySet().iterator(); it.hasNext();) {
				Map.Entry e = (Map.Entry) it.next();
				ArrayList<Integer> orderArr = (ArrayList<Integer>)e.getValue();
				int key = (Integer)e.getKey();
				String[] selectIds = new String[]{"type_id"};
				String[] selectValues = new String[]{key + ""};
				ArrayList<VideoTextRes> resArr = ReadResArrByDB(db, null, selectIds, selectValues);
				boolean change = ResourceUtils.RebuildOrder(resArr, orderArr);
				if(!flag1)
				{
					flag1 = change;
				}
			}
			if(flag1)
			{
				SaveOrderArr();
			}

			ClearUnusedRes(db);
		}
	}

	public void SaveOrderArr()
	{
		SaveOrderArr1(ORDER_PATH);
	}

	@Override
	public int GetId(VideoTextRes res)
	{
		if(res != null){
			return res.m_id;
		}
		return 0;
	}

	@Override
	public VideoTextRes ReadResByIndex(ArrayList<VideoTextRes> videoTextRes, int index)
	{
		if(videoTextRes != null && index >= 0 && index < videoTextRes.size())
		{
			return videoTextRes.get(index);
		}
		return null;
	}

	@Override
	protected void DeleteSDRes(Context context, VideoTextRes res)
	{
		VideoTextRes tempRes = GetCloudRes(res.m_id);
		int index = ResourceUtils.HasId(GetOrderArr1().get(res.m_resTypeID), res.m_id);
		if(index >= 0){
			GetOrderArr1().get(res.m_resTypeID).remove(index);
			SaveOrderArr();
		}
		if(tempRes != null){
			tempRes.m_type = BaseRes.TYPE_NETWORK_URL;
//			tempRes.shareImg = null;
//			tempRes.m_coverPic = null;
//			tempRes.m_thumb = null;
			tempRes.m_res = null;
		}
//		FileUtil.deleteSDFile(res.shareImg);
//		FileUtil.deleteSDFile(res.m_coverPic);
//		FileUtil.deleteSDFile((String)res.m_thumb);
		FileUtil.deleteSDFile(res.m_res);
	}

	@Override
	protected VideoTextRes ReadResItem(JSONObject jsonObj, boolean isPath)
	{
		VideoTextRes out = null;
		try
		{
			out = new VideoTextRes();
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
			if(isPath)
			{
				out.m_coverPic = jsonObj.getString("cover_pic");
			}
			else
			{
				out.url_coverPic = jsonObj.getString("cover_pic");
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
			if(jsonObj.has("shareImg"))
			{
				if(isPath)
				{
					out.shareImg = jsonObj.getString("shareImg");
				}
				else
				{
					out.url_shareImg = jsonObj.getString("shareImg");
				}
			}
			if(jsonObj.has("shareTitle"))
			{
				out.shareTitle = jsonObj.getString("shareTitle");
			}
			if(jsonObj.has("shareIntroduce"))
			{
				out.shareIntroduce = jsonObj.getString("shareIntroduce");
			}
			if(jsonObj.has("shareLink"))
			{
				out.m_shareLink = jsonObj.getString("shareLink");
			}
			if(jsonObj.has("isLock"))
			{
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
			Object obj = jsonObj.get("res_arr");
			if(obj != null && obj instanceof JSONArray)
			{
				JSONArray res_arr = (JSONArray)obj;
				out.m_resArr = FontResMgr.ReadResArr(res_arr);
			}
			String editable = jsonObj.getString("editType");
			if(editable != null && editable.length() > 0)
			{
				out.editType = Integer.parseInt(editable);
			}
			String type_id = jsonObj.getString("typeid");
			if(editable != null && editable.length() > 0)
			{
				out.m_resTypeID = Integer.parseInt(type_id);
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
	protected void RecodeLocalItem(VideoTextRes res)
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
			String musicPath = "video_text/" + res.m_res;
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
		res.m_thumb = "video_text/" + res.m_thumb;
		res.shareImg = "video_text/" + res.shareImg;
		res.m_coverPic = "video_text/" + res.m_coverPic;
	}

	@Override
	protected String GetLocalPath()
	{
		return LOCAL_PATH;
	}

	@Override
	public VideoTextRes GetItem(ArrayList<VideoTextRes> arr, int id)
	{
		return ResourceUtils.GetItem(arr, id);
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
			url = CLOUD_URL + "1.4.1";
		}
		return url;
	}

	@Override
	protected String GetCloudCachePath(Context context)
	{
		return CLOUD_CACHE_PATH;
	}

	@Override
	public boolean CheckIntact(VideoTextRes res)
	{
		boolean out = false;

		if(res != null)
		{
			if(ResourceUtils.HasIntact(res.m_thumb, res.m_res))
			{
				out = true;
			}
		}

		return out;
	}

	@Override
	public boolean CheckExist(VideoTextRes res)
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

	public int GetClassifyIdByResId(int id)
	{
		int typeID = -1;
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			SQLiteDatabase db = GetDB();
			VideoTextRes res = ReadResByDB(db, id);
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
	public ArrayList<VideoTextRes> GetResArr(SQLiteDatabase db, int[] ids, boolean onlyLocal)
	{
		ArrayList<VideoTextRes> out = new ArrayList<VideoTextRes>();
		if(ids != null)
		{
			ArrayList<VideoTextRes> temp = ReadResArrByDB(db, ids);
			if(onlyLocal || ids.length == temp.size()){
				out = temp;
			}
			else
			{
				VideoTextRes res;
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

	public ArrayList<VideoTextRes> GetResArr(SQLiteDatabase db, int[] ids)
	{
		return GetResArr(db, ids, false);
	}

	/**
	 *
	 * @param db
	 * @param typeID
	 * @return
	 */
	public ArrayList<VideoTextRes> GetLocalResArr(SQLiteDatabase db, int typeID)
	{
		ArrayList<VideoTextRes> out = new ArrayList<VideoTextRes>();
		ArrayList<Integer> order_arr = GetOrderArr1().get(typeID);
		if(order_arr == null || order_arr.size() < 1)
			return out;
		if(db != null)
		{
			String[] selectIds = null;
			String[] selectValues = null;
			if(typeID != -1)
			{
				selectIds = new String[]{"type_id"};
				selectValues = new String[]{typeID + ""};
			}
			ArrayList<VideoTextRes> resArr = ReadResArrByDB(db, null, selectIds, selectValues);
			VideoTextRes res;
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
	protected void RebuildNetResArr(ArrayList<VideoTextRes> dst, ArrayList<VideoTextRes> src)
	{
		if(dst != null && src != null)
		{
			VideoTextRes srcTemp;
			VideoTextRes dstTemp;
			Class cls = VideoTextRes.class;
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
		return EventID.VIDEO_TEXT_CLOUD_OK;
	}
}
