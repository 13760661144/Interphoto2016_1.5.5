package cn.poco.resource;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;

import cn.poco.adMaster.AdMaster;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.resource.database.ResourseDatabase;
import cn.poco.resource.database.TableNames;
import cn.poco.system.FolderMgr;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.CommonUtils;
import cn.poco.utils.FileUtil;

public class ResourceMgr
{
	public static final Object DATABASE_THREAD_LOCK = new Object(); //数据库读写的线程锁
	public static boolean BUSINESS_RES_LOCK_FLAG = true;
	public static final Object BUSINESS_RES_LOCK = new Object(); //商业素材锁,配合BUSINESS_RES_LOCK_FLAG使用
	public static final String NEW_FLAG_DB_NAME = "resource_new_flag";

	/**
	 * 主线程运行
	 *
	 * @param context
	 */
	public static void PreInit(Context context)
	{
		ReadAllOldIDFalg(context);

		AdMaster.getInstance(context).Init(context);
		MyLogoResMgr.getInstance().InitLocalData2();
	}

	/**
	 * 有网络访问需要在线程执行
	 *
	 * @param context
	 */
	public static void AsyncInit(Context context)
	{
		ReadCloudCacheRes(context);
		ReadCloudRes(context);
	}

	protected static void ReadCloudCacheRes(Context context)
	{
		//用的时候才初始化会导致页面很卡
		LightEffectResMgr2.getInstance().InitLocalData();
		TextResMgr2.getInstance().InitLocalData();
		FilterResMgr2.getInstance().InitLocalData();
		MusicResMgr2.getInstance().InitLocalData();
		VideoTextResMgr2.getInstance().InitLocalData();

		LightEffectResMgr2.getInstance().sync_ar_GetCloudCacheRes(context, null);
		TextResMgr2.getInstance().sync_ar_GetCloudCacheRes(context, null);
		FilterResMgr2.getInstance().sync_ar_GetCloudCacheRes(context, null);
		MusicResMgr2.getInstance().sync_GetCloudCacheRes(context, null);
		VideoTextResMgr2.getInstance().sync_ar_GetCloudCacheRes(context, null);
		ThemeResMgr2.getInstance().sync_ar_GetCloudCacheRes(context, null);
	}

	protected static void ReadCloudRes(Context context)
	{
		SwitchResMgr2.getInstance().sync_GetCloudRes(context, null, true);
		LightEffectResMgr2.getInstance().sync_GetCloudRes(context, null, true);
		TextResMgr2.getInstance().sync_GetCloudRes(context, null, true);
		FilterResMgr2.getInstance().sync_GetCloudRes(context, null, true);
		MusicResMgr2.getInstance().sync_GetCloudRes(context, null, true);
		VideoTextResMgr2.getInstance().sync_GetCloudRes(context, null, true);
		ThemeResMgr2.getInstance().sync_GetCloudRes(context, null, true);

		synchronized(BUSINESS_RES_LOCK)
		{
			BUSINESS_RES_LOCK_FLAG = false;
			BUSINESS_RES_LOCK.notifyAll();
		}
	}

	/**
	 * 非UI线程调用,线程阻塞
	 *
	 * @param context
	 */
	public void ReloadCloudRes(Context context)
	{
		ReadCloudRes(context);
	}

	public static void clearDatabaseData(Context context)
	{

	}

	public synchronized static void UpdateOldIDFlag(Context context, int id, String flag)
	{
		SharedPreferences sp = context.getSharedPreferences(NEW_FLAG_DB_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(flag, id);
		editor.commit();
	}

	protected synchronized static void ReadAllOldIDFalg(Context context)
	{
		try
		{
			SharedPreferences sp = context.getSharedPreferences(NEW_FLAG_DB_NAME, Context.MODE_PRIVATE);
			FilterResMgr2.getInstance().ReadOldId(sp);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
