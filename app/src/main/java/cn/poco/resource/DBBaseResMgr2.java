package cn.poco.resource;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;

import com.adnonstop.resourcelibs.DBResMgr;
import com.adnonstop.resourcelibs.DataFilter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.framework.MyFramework2App;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.NetCore2;
import cn.poco.utils.MyNetCore;

/**
 * 数据库存储数据
 * @param <ResType>
 * @param <ResArrType>
 */
public abstract class DBBaseResMgr2<ResType extends BaseRes, ResArrType> extends DBResMgr<ResType, ResArrType>
{
	protected final ArrayList<Integer> mOrderArr = new ArrayList<>();		//(0)
	protected final HashMap<Integer, ArrayList<Integer>> mOrderArr1 = new HashMap<>(); //每种分类对应的顺序(1)
	protected boolean mInitOrderArr = false;

	public int m_oldID = 0;
	public boolean m_hasNewRes = false;

	@Override
	protected int GetLocalEventId()
	{
		return 0;
	}

	@Override
	protected int GetSdcardEventId()
	{
		return 0;
	}

	public void ReadOrderArr(Context context, String path)
	{
		try
		{
			byte[] data = CommonUtils.ReadFile(path);
			if(data != null)
			{
				mOrderArr.clear();

				JSONObject jsonObj = new JSONObject(new String(data));
				if(jsonObj.length() > 0)
				{
					JSONArray jsonArr = jsonObj.getJSONArray("order");
					if(jsonArr != null)
					{
						int arrLen = jsonArr.length();
						for(int i = 0; i < arrLen; i++)
						{
							mOrderArr.add(jsonArr.getInt(i));
						}
					}
				}
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public HashMap<Integer, ArrayList<Integer>> ReadOrderArr1(String path)
	{
		HashMap<Integer, ArrayList<Integer>> out = new HashMap<>();

		try
		{
			byte[] datas = CommonUtils.ReadFile(path);
			if(datas != null)
			{
				JSONObject jsonObj = new JSONObject(new String(datas));
				if(jsonObj != null && jsonObj.length() > 0)
				{
					JSONArray jsonArray = jsonObj.getJSONArray("data");
					if(jsonArray != null)
					{
						int len = jsonArray.length();
						JSONObject obj;
						for(int i = 0; i < len; i ++)
						{
							obj = jsonArray.getJSONObject(i);
							if(obj != null)
							{
								if(obj.has("key"))
								{
									Integer key = obj.getInt("key");
									ArrayList<Integer> orderArr = new ArrayList<>();
									JSONArray array = obj.getJSONArray("order");
									for(int k = 0; k < array.length(); k ++)
									{
										orderArr.add(array.getInt(k));
									}
									out.put(key, orderArr);
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

	public void SaveOrderArr(String path)
	{
		ResourceUtils.WriteOrderArr(path, mOrderArr);
	}

	public void SaveOrderArr1(String path)
	{
		ResourceUtils.WriteOrderArr(path, mOrderArr1);
	}

	protected void InitOrderArr(ArrayList<Integer> dstObj)
	{
	}

	protected void InitOrderArr(HashMap<Integer, ArrayList<Integer>> dstObj)
	{
	}

	public ArrayList<Integer> GetOrderArr()
	{
		if(!mInitOrderArr)
		{
			mInitOrderArr = true;
			InitOrderArr(mOrderArr);
		}
		return mOrderArr;
	}

	public HashMap<Integer, ArrayList<Integer>> GetOrderArr1()
	{
		if(!mInitOrderArr)
		{
			mInitOrderArr = true;
			InitOrderArr(mOrderArr1);
		}
		return mOrderArr1;
	}

	public boolean CheckIntact(ResType res)
	{
		return true;
	}

	public boolean CheckExist(ResType res)
	{
		return true;
	}

	public abstract int GetId(ResType res);

	public abstract ResType ReadResByIndex(ResArrType arrType, int index);

	public void ClearUnusedRes(SQLiteDatabase db)
	{
		if(db != null)
		{
			ResArrType resArr = ReadResArrByDB(db, null);
			int size =  GetResArrSize(resArr);
			ArrayList<Integer> ids = new ArrayList<>();
			if(resArr != null && size > 0)
			{
				ResType res;
				for(int i = 0 ; i < size; i ++)
				{
					res = ReadResByIndex(resArr, i);
					if(!CheckExist(res)){
						ids.add(GetId(res));
					}
				}
			}
			if(ids.size() > 0)
			{
				StringBuilder builder = new StringBuilder("delete from " + GetTableName() +" where " + GetKeyId() + " in(");
				for(int id : ids){
					builder.append(id);
					builder.append(",");
				}
				builder.deleteCharAt(builder.length() - 1);
				builder.append(")");
				db.execSQL(builder.toString());
			}
		}
	}

	/**
	 * 删除sd卡数据
	 * @param context
	 * @param res
	 */
	protected abstract void DeleteSDRes(Context context, ResType res);

	public void DeleteRes(Context context, ResType res)
	{
		boolean flag = false;
		synchronized(ResourceMgr.DATABASE_THREAD_LOCK)
		{
			String sql = "delete from " + GetTableName() + " where " + GetKeyId() + " = " + GetId(res);
			SQLiteDatabase db = GetDB();
			if(db != null)
			{
				try
				{
					flag = true;
					db.execSQL(sql);
				}catch(SQLException e){
					e.printStackTrace();
				}
				CloseDB();
			}
		}
		if(flag){
			DeleteSDRes(context, res);
		}
	}

	protected void DeleteLocalResArr(SQLiteDatabase db)
	{
		String sql = "delete from " + GetTableName() + " where store_type = 1";
		if(db != null)
		{
			db.execSQL(sql);
		}
	}

	@Override
	public SQLiteDatabase GetDB()
	{
		return ResourseDatabase.getInstance(MyFramework2App.getInstance().getApplicationContext()).openDatabase();
	}

	@Override
	public void CloseDB()
	{
		ResourseDatabase.getInstance(MyFramework2App.getInstance().getApplicationContext()).closeDatabase();
	}

	protected abstract ResType ReadResItem(JSONObject jsonObj, boolean isPath);

	protected abstract String GetSdcardPath(Context context);

	protected abstract void RecodeLocalItem(ResType item);

	protected abstract String GetLocalPath();

	protected String GetOldIdFlag()
	{
		return null;
	}

	public abstract ResType GetItem(ResArrType arr, int id);

	protected ResType GetCloudRes(int id)
	{
		ResType out = null;

		ResArrType arr;
		if((arr = sync_ar_GetCloudCacheRes(MyFramework2App.getInstance().getApplication(), null)) != null)
		{
			out = GetItem(arr, id);
		}

		return out;
	}

	@Override
	protected ResArrType sync_raw_GetLocalRes(Context context, DataFilter filter)
	{
		ResArrType out = MakeResArrObj();
		InputStreamReader inputReader = null;
		BufferedReader reader = null;
		try
		{
			InputStream is = context.getAssets().open(GetLocalPath());
			inputReader = new InputStreamReader(is);
			reader = new BufferedReader(inputReader);
			String inputLine = null;
			StringBuffer sb = new StringBuffer();
			while((inputLine = reader.readLine()) != null)
			{
				sb.append(inputLine).append("\n");
			}
			reader.close();
			inputReader.close();

			String datas = sb.toString();
			if(datas != null)
			{
				JSONArray jsonArr = new JSONArray(datas);
				if(jsonArr != null && jsonArr.length() > 0)
				{
					int arrLen = jsonArr.length();
					ResType item;
					Object obj;
					for(int i = 0; i < arrLen; i++)
					{
						obj = jsonArr.get(i);
						if(obj instanceof JSONObject)
						{
							item = ReadResItem((JSONObject)obj, true);
							if(item != null)
							{
								RecodeLocalItem(item);
								ResArrAddItem(out, item);
							}
						}
					}
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			try
			{
				if(reader != null)
				{
					reader.close();
				}
				if(inputReader != null)
				{
					inputReader.close();
				}
			}
			catch(IOException e1)
			{
			}
		}
		return out;
	}

	@Override
	public ResArrType sync_GetSdcardRes(Context context, DataFilter filter)
	{
		return super.sync_GetSdcardRes(context, filter);
	}

	@Override
	protected Object sync_raw_ReadSdcardData(Context context, DataFilter filter)
	{
		Object obj = null;
		try
		{
			obj = CommonUtils.ReadFile(GetSdcardPath(context));
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	protected ResArrType sync_DecodeSdcardRes(Context context, DataFilter filter, Object data)
	{
		ResArrType out = MakeResArrObj();//sdcard默认都有返回值

		try
		{
			if(data != null)
			{
				//out = MakeResArrObj();
				JSONObject jsonObj = new JSONObject(new String((byte[])data));
				if(jsonObj.length() > 0)
				{
					JSONArray jsonArr = jsonObj.getJSONArray("data");
					if(jsonArr != null)
					{
						int arrLen = jsonArr.length();
						ResType item;
						Object obj;
						for(int i = 0; i < arrLen; i++)
						{
							obj = jsonArr.get(i);
							if(obj instanceof JSONObject)
							{
								item = ReadResItem((JSONObject)obj, true);
								if(item != null && CheckIntact(item))
								{
									ResArrAddItem(out, item);
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

	@Override
	protected void sync_raw_SaveSdcardRes(Context context, ResArrType arr)
	{

	}

	protected abstract String GetCloudUrl(Context context);

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

	protected abstract String GetCloudCachePath(Context context);

	@Override
	protected Object sync_raw_ReadCloudCacheData(Context context, DataFilter filter)
	{
		Object obj = null;
		try
		{
			obj = CommonUtils.ReadFile(GetCloudCachePath(context));
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
		return obj;
	}

	@Override
	protected void sync_raw_WriteCloudData(Context context, DataFilter filter, Object data)
	{
		try
		{
			CommonUtils.SaveFile(GetCloudCachePath(context), (byte[])data);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	@Override
	protected ResArrType sync_DecodeCloudRes(Context context, DataFilter filter, Object data)
	{
		ResArrType out = null;

		try
		{
			if(data instanceof byte[] && ((byte[])data).length > 0)
			{
				JSONArray jsonArr = new JSONArray(new String((byte[])data));
				ResType item;
				Object obj;
				int arrLen = jsonArr.length();
				out = MakeResArrObj();
				for(int i = 0; i < arrLen; i++)
				{
					obj = jsonArr.get(i);
					if(obj instanceof JSONObject)
					{
						item = ReadResItem((JSONObject)obj, false);
						if(item != null)
						{
							ResArrAddItem(out, item);
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

	protected void UpdateOldId(ResArrType newArr)
	{
		final String FLAG = GetOldIdFlag();
		if(newArr != null && FLAG != null)
		{
			int maxID = 0;
			if(newArr instanceof ArrayList)
			{
				maxID = ResourceUtils.GetMaxID((ArrayList)newArr);
			}
			else if(newArr instanceof SparseArray)
			{
				maxID = ResourceUtils.GetMaxID((SparseArray)newArr);
			}
			else if(newArr instanceof HashMap)
			{
				maxID = ResourceUtils.GetMaxID((HashMap)newArr);
			}
			if(maxID > m_oldID && m_oldID != 0)
			{
				m_hasNewRes = true;
			}
			if(m_oldID == 0)
			{
				ResourceMgr.UpdateOldIDFlag(MyFramework2App.getInstance().getApplicationContext(), maxID, FLAG);
			}
			if(maxID > m_oldID)
			{
				m_oldID = maxID;
			}
		}
	}

	public void ClearOldId(Context context)
	{
		final String FLAG = GetOldIdFlag();
		if(FLAG != null)
		{
			ResourceMgr.UpdateOldIDFlag(context, m_oldID, FLAG);
			m_hasNewRes = false;
		}
	}

	public void ReadOldId(SharedPreferences sp)
	{
		final String FLAG = GetOldIdFlag();
		if(FLAG != null && sp != null)
		{
			m_oldID = sp.getInt(FLAG, 0);
		}
	}

	@Override
	public ResArrType sync_GetCloudCacheRes(Context context, DataFilter filter)
	{
		ResArrType arr = mCloudResArr;
		ResArrType arr2 = super.sync_GetCloudCacheRes(context, filter);

		synchronized(CLOUD_MEM_LOCK)
		{
			if(arr != arr2 && arr2 != null)
			{
				UpdateOldId(arr2);
			}
		}

		return arr2;
	}

	protected void RebuildNetResArr(ResArrType dst, ResArrType src)
	{
	}

	@Override
	protected void sync_ui_CloudResChange(ResArrType oldArr, ResArrType newArr)
	{
		if(oldArr != newArr)
		{
			RebuildNetResArr(newArr, oldArr);
		}

		if(newArr != null && GetResArrSize(newArr) > 0)
		{
			UpdateOldId(newArr);
		}
	}
}
