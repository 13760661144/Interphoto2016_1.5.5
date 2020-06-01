package cn.poco.system;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

import java.io.File;
import java.util.HashMap;

import cn.poco.setting.SettingInfoMgr;
import cn.poco.tianutils.CommonUtils;

/**
 * 系统内部配置信息
 *
 * @author POCO
 */
public class SysConfig
{
	protected static final HashMap<String, String> s_data = new HashMap<>();
	protected static boolean s_init = false;
	protected static boolean s_change = false;

	protected static final String SDCARD_PATH = "SDCARD_PATH";//SDCARD的路径,以后扩展用户可修改
	protected static final String APP_FILE_NAME = "APP_FILE_NAME"; //程序文件夹名
	protected static final String IS_DEBUG = "IS_DEBUG";

	public static void Read(Context context)
	{
		if(!s_init)
		{
			CommonUtils.SP_ReadSP(context, FolderMgr.SYSTEM_CONFIG_SP_NAME, s_data);
			s_init = true;
		}
	}

	public static void Save(Context context)
	{
		if(s_init && s_change)
		{
			CommonUtils.SP_SaveMap(context, FolderMgr.SYSTEM_CONFIG_SP_NAME, s_data);
		}
	}

	public static String GetSDCardPath()
	{
		String out = s_data.get(SDCARD_PATH);

		if(out == null || out.length() <= 0)
		{
			out = Environment.getExternalStorageDirectory().getAbsolutePath();
		}

		return out;
	}

	public static String GetAppFileName()
	{
		String out = s_data.get(APP_FILE_NAME);

		if(out == null || out.length() <= 0)
		{
			out = "interphoto";
		}

		return out;
	}

	/**
	 * @return 程序文件夹路径
	 */
	public static String GetAppPath()
	{
		return GetSDCardPath() + File.separator + GetAppFileName();
	}

	public static boolean IsDebug()
	{
		boolean out = false;

		String temp = s_data.get(IS_DEBUG);
		if(temp != null)
		{
			out = true;
		}

		return out;
	}

	public static void SetDebug(boolean debug)
	{
		if(debug)
		{
			s_data.put(IS_DEBUG, "1");
		}
		else
		{
			s_data.remove(IS_DEBUG);
		}
		s_change = true;
	}

	public static String GetAppVer(Context context)
	{
		String out = null;

		if(IsDebug())
		{
			out = "88.8.8";
		}
		else if(context != null)
		{
			PackageManager pm = context.getApplicationContext().getPackageManager();
			if(pm != null)
			{
				PackageInfo pi;
				try
				{
					pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
					if(pi != null)
					{
						out = pi.versionName;
						String miniver = ConfigIni.getMiniVer();
						if(miniver != null)
						{
							out += miniver;
						}
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

	public static String GetAppVer2(Context context)
	{
		String out = null;

		if(IsDebug())
		{
			String miniver = ConfigIni.getMiniVer();
			if(miniver != null)
			{
				out = "88.8.8" + miniver;
			}
			else
			{
				out = "88.8.8";
			}
		}
		else if(context != null)
		{
			PackageManager pm = context.getApplicationContext().getPackageManager();
			if(pm != null)
			{
				PackageInfo pi;
				try
				{
					pi = pm.getPackageInfo(context.getApplicationContext().getPackageName(), 0);
					if(pi != null)
					{
						out = pi.versionName;
						String miniver = ConfigIni.getMiniVer();
						if(miniver != null)
						{
							out += miniver;
						}
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

	/**
	 * 有debug版本号判断
	 *
	 * @param context
	 * @return
	 */
	public static String GetAppVerNoSuffix(Context context)
	{
		String out = null;

		if(IsDebug())
		{
			out = "88.8.8";
		}
		else
		{
			out = CommonUtils.GetAppVer(context);
		}

		return out;
	}

	public static int GetPhotoSize(Context context)
	{
		return GetPhotoSize(context, SettingInfoMgr.GetSettingInfo(context).GetQualityState());
	}

	public static int GetPhotoSize(Context context, boolean quality)
	{
		int out = (int)Math.sqrt(Runtime.getRuntime().maxMemory() / 36);

		if(!quality)
		{
			out = out / 2;
		}

		if(out < 640)
		{
			out = 640;
		}

		return out;
	}
}
