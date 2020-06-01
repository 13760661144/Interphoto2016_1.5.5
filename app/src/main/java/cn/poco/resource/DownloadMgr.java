package cn.poco.resource;

import android.content.Context;

import cn.poco.resource.DownloadTaskThread.CallbackHandler;
import cn.poco.system.FolderMgr;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.NetCore2;
import cn.poco.utils.MyNetCore;

public class DownloadMgr extends AbsDownloadMgr
{
	private static DownloadMgr sInstance;

	public static synchronized DownloadMgr getInstance()
	{
		return sInstance;
	}

	public static synchronized void InitInstance(Context context)
	{
		if(sInstance == null)
		{
			sInstance = new DownloadMgr(context);
		}
	}

	//不用静态变量，确保已赋值
	public String THEME_PATH;
	public String TEXT_PATH;
	public String MUSIC_PATH;
	public String VIDEO_TEXT_PATH;
	public String LIGHT_EFFECT_PATH;
	public String LOCK_PATH;
	public String BANNER_PATH;
	public String FONT_PATH;
	public String BUSINESS_PATH;
	public String MY_LOGO_PATH;
	public String APP_MARKET_PATH;
	public String FILTER_PATH;
	public String OTHER_PATH;

	public DownloadMgr(Context context)
	{
		super(context, FolderMgr.getInstance().RESOURCE_TEMP_PATH);
	}

	public void initPathData(Context context)
	{
		CommonUtils.MakeFolder(FolderMgr.getInstance().RESOURCE_TEMP_PATH);
		InitData(context);
	}

	@Override
	protected void InitData(Context context)
	{
		THEME_PATH = FolderMgr.getInstance().RESOURCE_THEME_PATH;
		TEXT_PATH = FolderMgr.getInstance().RESOURCE_TEXT_PATH;
		MUSIC_PATH = FolderMgr.getInstance().RESOURCE_MUSIC_PATH;
		VIDEO_TEXT_PATH = FolderMgr.getInstance().RESOURCE_VIDEO_TEXT_PATH;
		LIGHT_EFFECT_PATH = FolderMgr.getInstance().RESOURCE_LIGHT_EFFECT_PATH;
		LOCK_PATH = FolderMgr.getInstance().LOCK_PATH;
		BANNER_PATH = FolderMgr.getInstance().BANNER_PATH;
		FONT_PATH = FolderMgr.getInstance().RESOURCE_FONT_PATH;
		BUSINESS_PATH = FolderMgr.getInstance().BUSINESS_PATH;
		MY_LOGO_PATH = FolderMgr.getInstance().RESOURCE_MY_LOGO_PATH;
		APP_MARKET_PATH = FolderMgr.getInstance().RESOURCE_APP_MARKET_PATH;
		FILTER_PATH = FolderMgr.getInstance().RESOURCE_FILTER_PATH;
		OTHER_PATH = FolderMgr.getInstance().OTHER_PATH;

		CommonUtils.MakeFolder(TEXT_PATH);
		CommonUtils.MakeFolder(FONT_PATH);
		CommonUtils.MakeFolder(LIGHT_EFFECT_PATH);
		CommonUtils.MakeFolder(THEME_PATH);
		CommonUtils.MakeFolder(LOCK_PATH);
		CommonUtils.MakeFolder(BANNER_PATH);
		CommonUtils.MakeFolder(BUSINESS_PATH);
		CommonUtils.MakeFolder(MY_LOGO_PATH);
		CommonUtils.MakeFolder(APP_MARKET_PATH);
		CommonUtils.MakeFolder(FILTER_PATH);
		CommonUtils.MakeFolder(OTHER_PATH);
		CommonUtils.MakeFolder(VIDEO_TEXT_PATH);
		CommonUtils.MakeFolder(MUSIC_PATH);
	}

	@Override
	protected DownloadTaskThread MakeDownloadTaskThread(Context context, String tempPath, int threadNum, CallbackHandler cb)
	{
		return new MyDownloadTaskThread(context, tempPath, threadNum, cb);
	}

	private static class MyDownloadTaskThread extends DownloadTaskThread
	{
		public MyDownloadTaskThread(Context context, String tempPath, int threadNum, CallbackHandler cb)
		{
			super(context, tempPath, threadNum, cb);
		}

		@Override
		protected ResourceDownloader MakeResourceDownloader(Context context, String tempPath, cn.poco.resource.ResourceDownloader.CallbackHandler cb)
		{
			return new MyResourceDownloader(context, tempPath, cb);
		}
	}

	private static class MyResourceDownloader extends ResourceDownloader
	{
		public MyResourceDownloader(Context context, String tempPath, CallbackHandler cb)
		{
			super(context, tempPath, cb);
		}

		@Override
		protected NetCore2 MakeNetCore(Context context)
		{
			return new MyNetCore(context);
		}
	}
}
