package cn.poco.exception;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.support.multidex.MultiDexApplication;

import com.tencent.bugly.crashreport.CrashReport;

import cn.poco.framework.MyFramework2App;
import cn.poco.resource.DownloadMgr;
import cn.poco.system.ConfigIni;
import cn.poco.system.FolderMgr;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;

public class MyApplication extends MultiDexApplication {

	protected static MyApplication sApp;

	public synchronized static MyApplication getInstance()
	{
		return sApp;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		sApp = this;

		CaughtExceptionHandler exceptionHandler = new CaughtExceptionHandler();
		exceptionHandler.Init(getApplicationContext());

		CrashReport.initCrashReport(getApplicationContext(), "900057629", false);

		MyFramework2App.getInstance().onCreate(this);

		TagMgr.Init(FolderMgr.OTHER_CONFIG_SP_NAME);
		SysConfig.Read(this); //读取系统配
		ConfigIni.readConfig(this);

		DownloadMgr.InitInstance(this);
	}

	public static boolean isApkDebugable(Context context) {
		try {
			ApplicationInfo info= context.getApplicationInfo();
			return (info.flags & ApplicationInfo.FLAG_DEBUGGABLE)!=0;
		} catch (Exception e) {

		}
		return false;
	}
}
