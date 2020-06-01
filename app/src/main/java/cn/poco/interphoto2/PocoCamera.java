package cn.poco.interphoto2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.webkit.WebView;
import android.widget.Toast;

import com.adnonstop.admasterlibs.AdUtils;
import com.baidu.mobstat.StatService;
import com.hmt.analytics.HMTAgent;
import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import java.util.ArrayList;
import java.util.HashMap;

import cn.poco.PhotoPicker.site.PhotoPickerPageSite;
import cn.poco.adMaster.AdMaster;
import cn.poco.adMaster.BootImgRes;
import cn.poco.album2.PhotoStore;
import cn.poco.album2.utils.AlbumUtils;
import cn.poco.beautify.site.BeautifyPageSite100;
import cn.poco.bootimg.site.BootImgPageSite;
import cn.poco.camera.site.CameraPageSite100;
import cn.poco.capture2.site.CapturePageSite101;
import cn.poco.framework.AnimatorHolder;
import cn.poco.framework.BaseFwActivity;
import cn.poco.framework.BaseSite;
import cn.poco.framework.FileCacheMgr;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.MyFramework2App;
import cn.poco.framework2.Framework2;
import cn.poco.home.HomePage;
import cn.poco.home.site.HomePageSite;
import cn.poco.interphoto2.site.activity.MainActivitySite;
import cn.poco.resource.DownloadMgr;
import cn.poco.resource.ResourceMgr;
import cn.poco.setting.LanguagePage;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statisticlibs.BeautyStat;
import cn.poco.statistics.MyBeautyStat;
import cn.poco.statistics.TongJi2;
import cn.poco.system.ConfigIni;
import cn.poco.system.FolderMgr;
import cn.poco.system.SysConfig;
import cn.poco.system.TagMgr;
import cn.poco.tianutils.CommonUtils;
import cn.poco.tianutils.ImageUtils;
import cn.poco.tianutils.ShareData;
import cn.poco.utils.FileUtil;
import cn.poco.utils.PermissionUtils;
import cn.poco.video.helper.controller.MediaController;
import cn.poco.watermarksync.manager.WatermarkSyncManager;
import poco.photedatabaselib2016.PhotoDatabaseHelper;

/**
 * Created by Raining on 2017/11/30.
 */

public class PocoCamera extends BaseFwActivity<MainActivitySite>
{
	protected boolean mQuit = false;
	protected boolean mHasInitStorageData = false;
	protected boolean mHasInitAlumbData = false;
	protected boolean mHasPause = false;

	@Override
	protected void InitStaticOnce(@Nullable Bundle savedInstanceState)
	{
		super.InitStaticOnce(savedInstanceState);

		ShareData.InitData(this);

		//获取一次user agent
		try
		{
			WebView view = new WebView(this);
			AdUtils.USER_AGENT = view.getSettings().getUserAgentString();
			//System.out.println(sUA);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}

		if(!TagMgr.CheckTag(getApplicationContext(), LanguagePage.CHINA_TAGVALUE))
		{
			LanguagePage.changeLanguage(LanguagePage.CHINA, getResources());
		}
		if(!TagMgr.CheckTag(getApplicationContext(), LanguagePage.ENGLISH_TAGVALUE))
		{
			LanguagePage.changeLanguage(LanguagePage.ENGLISH, getResources());
		}
	}

	/**
	 * 初始化跟sd卡权限有关的数据
	 */
	private void initStorageData()
	{
		if(!mHasInitStorageData)
		{
			mHasInitStorageData = true;
			DownloadMgr.getInstance().initPathData(this);

			//初始化文件管理并清理APP级缓存
			String processName = CommonUtils.GetProcessName(this);
			//System.out.println("processName : " + processName);
			//System.out.println("packageName : " + getPackageName());
			if(processName != null && processName.equals(this.getPackageName()))
			{
				//主进程
				FileCacheMgr.Init(FolderMgr.getInstance().IMAGE_CACHE_PATH, true);

				if(FileUtil.isFileExists(PhotoDatabaseHelper.DB_PATH))
				{
					AlbumUtils.clearHistory(getApplicationContext(), false, null);
				}

				//统计
				try
				{
					//百度统计
					//StatService.setDebugOn(true);
					String channel = ConfigIni.getMiniVer();
					if(channel != null && channel.length() > 0)
					{
						StatService.setAppChannel(this, channel, true);
					}
					else
					{
						StatService.setAppChannel(this, null, false);
					}

					//艾瑞
					HMTAgent.setChannelId(this, channel);
					HMTAgent.Initialize(this);

					//神策
					BeautyStat.Config config = MyBeautyStat.getDefaultConfig(getApplication());
					if(SysConfig.IsDebug())
					{
						config.serverURL = "http://tj.adnonstop.com:8106/sa?project=yx_project_test";
						config.configureUrl = "http://tj.adnonstop.com:8106/config/?project=yx_project_test";
						config.debugMode = SensorsDataAPI.DebugMode.DEBUG_AND_TRACK;
					}
					config.channel = ConfigIni.getMiniVer();
					MyBeautyStat.Init(config);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				FileCacheMgr.Init(FolderMgr.getInstance().IMAGE_CACHE_PATH, false);
			}

			//加载一次素材
			ResourceMgr.PreInit(this.getApplicationContext());
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					ResourceMgr.AsyncInit(PocoCamera.this);
					WatermarkSyncManager.getInstacne(PocoCamera.this).startUpSyncDependOnSituation();
				}
			}).start();
		}
	}

	@Override
	protected void InitData(@Nullable Bundle savedInstanceState)
	{
		super.InitData(savedInstanceState);

		if(mSite == null)
		{
			mSite = new MainActivitySite();
		}
	}

	@Override
	protected void InitFinal(@Nullable Bundle savedInstanceState)
	{
		super.InitFinal(savedInstanceState);

		try
		{
			boolean flag = true;
			if(Build.VERSION.SDK_INT >= 23){
				flag =PermissionUtils.checkStoragePermission(this);
				PermissionUtils.requestInterPermissions(this);
			}

			if(flag)
			{
				initStorageData();
			}

			TongJi2.IAmLive(this);

			flag = true;
			if(Build.VERSION.SDK_INT >= 23){
				flag =PermissionUtils.checkStoragePermission(this);
			}
			if(flag)
			{
				initAlumbData();
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	private void initAlumbData()
	{
		if(!mHasInitAlumbData)
		{
			mHasInitStorageData = true;
			// 读取相册的数据，注意，不能放在Application里面初始化，因为涉及语言问题
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					PhotoStore.getInstance(PocoCamera.this).initFolderInfos(false);
				}
			}).start();
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					MediaController.getInstance(PocoCamera.this).initVideoInfo();
				}
			}).start();
		}
	}

	@Override
	protected void onAppMapGate(Context context, Bundle savedInstanceState, boolean newActivity)
	{
		Intent intent = getIntent();
		if(intent != null)
		{
			String action = intent.getAction();
			if(action != null)
			{
				//android.os.Debug.waitForDebugger();
				if(action.equals(Intent.ACTION_EDIT) || action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_SEND))
				{
					String path = null;
					Uri uri = intent.getData();
					if(uri == null)
					{
						Bundle bundle = intent.getExtras();
						if(bundle != null)
						{
							Object obj = bundle.get(Intent.EXTRA_STREAM);
							if(obj instanceof Uri)
							{
								uri = (Uri)obj;
							}
						}
					}
					if(uri != null)
					{
						if(uri.toString().startsWith("file:"))
						{
							path = uri.getPath();
						}
						else
						{
							Cursor c = context.getContentResolver().query(uri, null, null, null, null);
							if(c != null)
							{
								if(c.moveToFirst())
								{
									int id = c.getColumnIndex(MediaStore.Images.Media.DATA);
									if(id != -1)
									{
										path = c.getString(id);
									}
								}
								c.close();
							}
						}
					}
					if(path != null && ImageUtils.IsImageFile(path))
					{
						//第三方调用,直接进入美化
						HashMap<String, Object> temp = new HashMap<String, Object>();
						temp.put(MyFramework.EXTERNAL_CALL_TYPE, MyFramework.EXTERNAL_CALL_TYPE_EDIT);
						temp.put("imgs", PhotoPickerPageSite.MakeRotationImg(new String[]{path}));
						temp.put("other_call", true);
						SITE_Open(context, true, BeautifyPageSite100.class, temp, Framework2.ANIM_NONE);
						return;
					}
					else
					{
						Toast.makeText(context, "无效的图片文件", Toast.LENGTH_LONG).show();
					}
				}
				else if(action.equals(MediaStore.ACTION_IMAGE_CAPTURE))
				{
					boolean flag = true;
					if(Build.VERSION.SDK_INT >= 23)
					{
						flag = PermissionUtils.checkCameraPermission(context);
					}
					if(flag)
					{
						openImageCapture(context, intent);
					}
					else
					{
						PermissionUtils.requestPermissions(context, Manifest.permission.CAMERA);
					}
					return;
				}
				else if(action.equals(MediaStore.ACTION_VIDEO_CAPTURE))
				{
					boolean flag = true;
					boolean flag1 = true;
					if(Build.VERSION.SDK_INT >= 23)
					{
						flag = PermissionUtils.checkCameraPermission(context);
						flag1 = PermissionUtils.checkAudioPermission(context);
					}
					if(flag && flag1)
					{
						MyBeautyStat.onClickByRes(R.string.首页_打开录像);
						openVideoCapture(context, intent);
					}
					if(!flag)
					{
						PermissionUtils.requestPermissions(context, Manifest.permission.CAMERA);
					}
					if(!flag1)
					{
						PermissionUtils.requestPermissions(context, Manifest.permission.RECORD_AUDIO);
					}
					return;
				}
			}
		}

		if(newActivity)
		{
			ArrayList<BaseSite> arr = mFramework.GetCurrentSiteList();
			if(arr != null && arr.size() > 0)
			{
				mFramework.onCreate(context, savedInstanceState);
			}
			else
			{
				HashMap<String, Object> params = null;
				BootImgRes res = AdMaster.getInstance(MyFramework2App.getInstance().getApplicationContext()).GetOneLocalBootImgRes();
				if(res != null)
				{
					params = new HashMap<String, Object>();
					params.put("img", res);
				}
				SITE_Open(context, true, BootImgPageSite.class, params, Framework2.ANIM_NONE);
			}
		}

		if(intent != null && !newActivity)
		{
			Uri uri = intent.getData();
			if(uri != null && "intercamera".equals(uri.getScheme()) && "goto".equals(uri.getHost()))
			{
				IPage page = GetTopPage();
				if(page instanceof HomePage)
				{
					String str = uri.toString();
					MyFramework.startGoTo(context, str);
				}
				setIntent(null);
			}
		}
	}

	private void openImageCapture(Context context, Intent intent)
	{
		Uri uri = null;
		Bundle bundle = intent.getExtras();
		if(bundle != null)
		{
			uri = bundle.getParcelable(MediaStore.EXTRA_OUTPUT);
		}
		//第三方调用,直接去拍照
		HashMap<String, Object> temp = new HashMap<String, Object>();
		if(uri != null)
		{
			temp.put(MyFramework.EXTERNAL_CALL_IMG_SAVE_URI, uri);
		}
		temp.put("isHideAlbum", true);
		temp.put("isOtherAppCall", true);
		SITE_Open(context, true, CameraPageSite100.class, temp, Framework2.ANIM_NONE);
	}

	private void openVideoCapture(Context context, Intent intent)
	{
		Uri uri = null;
		int mintime = -1;
		int videoQuality = -1;
		Bundle bundle = intent.getExtras();
		if(bundle != null)
		{
			uri = bundle.getParcelable(MediaStore.EXTRA_OUTPUT);
			//android.os.Debug.waitForDebugger();
			mintime = bundle.getInt(MediaStore.EXTRA_DURATION_LIMIT, -1);
			videoQuality = bundle.getInt(MediaStore.EXTRA_VIDEO_QUALITY, -1);
		}
		//第三方调用,直接去录视频
		HashMap<String, Object> temp = new HashMap<>();
		temp.put("isOtherAppCall", true);
		if(uri != null)
		{
			temp.put("saveUri", uri);
		}
		if(mintime >= 0)
		{
			temp.put("minDuration", mintime);
		}
		if(videoQuality >= 0)
		{
			temp.put(MyFramework.EXTERNAL_CALL_VIDEO_QUALITY, videoQuality);
		}
		SITE_Open(context, true, CapturePageSite101.class, temp, Framework2.ANIM_NONE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldW, int oldH)
	{
		super.onSizeChanged(w, h, oldW, oldH);

		ShareData.InitData(PocoCamera.this, true);
	}

	@Override
	protected void onResume()
	{
		if(!TagMgr.CheckTag(getApplicationContext(), LanguagePage.CHINA_TAGVALUE))
		{
			LanguagePage.changeLanguage(LanguagePage.CHINA, getResources());
		}
		if(!TagMgr.CheckTag(getApplicationContext(), LanguagePage.ENGLISH_TAGVALUE))
		{
			LanguagePage.changeLanguage(LanguagePage.ENGLISH, getResources());
		}

		if(Build.VERSION.SDK_INT >= 23 && mHasPause){
			PermissionUtils.requestInterPermissions(this);
		}
		mHasPause = false;

		super.onResume();
	}

	@Override
	protected void onPause()
	{
		SysConfig.Save(this);
		TagMgr.Save(this);
		SettingInfoMgr.Save(this);
		mHasPause = true;

		super.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();

		if(mQuit)
		{
			MyFramework2App.getInstance().quit();
			System.exit(0);
		}
	}

	@Override
	public void SITE_Open(Context context, Class<? extends BaseSite> siteClass, HashMap<String, Object> params, int animType)
	{
		boolean newLink = false;
		//System.out.println("String是Object的父类:" + String.class.isAssignableFrom(Object.class)); //false
		//System.out.println("Object是String的父类:" + Object.class.isAssignableFrom(String.class)); //true
		//System.out.println("Object和Object相同:" + Object.class.isAssignableFrom(Object.class)); //true
		if(HomePageSite.class.isAssignableFrom(siteClass))
		{
			newLink = true;
		}
		super.SITE_Open(context, newLink, siteClass, params, animType);
	}

	@Override
	public void SITE_OpenAndClosePopup(Context context, Class<? extends BaseSite> siteClass, HashMap<String, Object> params, int animType)
	{
		boolean newLink = false;
		if(HomePageSite.class.isAssignableFrom(siteClass))
		{
			newLink = true;
		}
		super.SITE_OpenAndClosePopup(context, newLink, siteClass, params, animType);
	}

	@Override
	public void SITE_BackTo(Context context, Class<? extends BaseSite> siteClass, HashMap<String, Object> params, int animType)
	{
		int currentIndex = mFramework.GetCurrentIndex();
		ArrayList<BaseSite> siteList = mFramework.GetCurrentSiteList();
		if(currentIndex == 0 && (siteList == null || siteList.size() < 2))
		{
			//退出软件
			mQuit = true;
			finish();
		}
		else
		{
			super.SITE_BackTo(context, siteClass, params, animType);
		}
	}

	@Override
	public void SITE_BackTo(Context context, Class<? extends BaseSite> siteClass, HashMap<String, Object> params, AnimatorHolder holder)
	{
		int currentIndex = mFramework.GetCurrentIndex();
		ArrayList<BaseSite> siteList = mFramework.GetCurrentSiteList();
		if(currentIndex == 0 && (siteList == null || siteList.size() < 2))
		{
			//退出软件
			mQuit = true;
			finish();
		}
		else
		{
			super.SITE_BackTo(context, siteClass, params, holder);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if(Build.VERSION.SDK_INT >= 23){
			if(permissions != null && grantResults != null && permissions.length == grantResults.length)
			{
				String str;

				boolean flag1 = false;
				boolean flag2 = false;
				Intent intent = getIntent();
				boolean isCamera = false;
				boolean isVideo = false;
				if(intent != null)
				{
					String action = intent.getAction();
					if(MediaStore.ACTION_IMAGE_CAPTURE.equals(action))
					{
						isCamera = true;
					}else if(MediaStore.ACTION_VIDEO_CAPTURE.equals(action))
					{
						isVideo = true;
					}
				}
				for(int i = 0; i < permissions.length; i ++)
				{
					str = permissions[i];
					if(Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(str))
					{
						if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
						{
							initStorageData();
							initAlumbData();
						}
						else
						{
							Toast.makeText(this, "需要开通存储权限才能使用印象", Toast.LENGTH_SHORT).show();
							finish();
						}
						if(!isCamera && !isVideo)
						{
							break;
						}
					}
					str = permissions[i];
					if(isCamera)
					{
						if(Manifest.permission.CAMERA.equals(str))
						{
							if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
							{
								openImageCapture(this, intent);
							}
							else
							{
								if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity)this, str))
								{
									Toast.makeText(this, "请到应用程序管理里面打开相机权限", Toast.LENGTH_SHORT).show();
								}
								HashMap<String, Object> params = null;
								BootImgRes res = AdMaster.getInstance(MyFramework2App.getInstance().getApplicationContext()).GetOneLocalBootImgRes();
								if(res != null)
								{
									params = new HashMap<String, Object>();
									params.put("img", res);
								}
								SITE_Open(this, true, BootImgPageSite.class, params, Framework2.ANIM_NONE);
							}
							break;
						}
					}
					if(isVideo)
					{
						if(Manifest.permission.CAMERA.equals(str))
						{
							if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
							{
								flag1 = true;
							}
							else
							{
								flag1 = false;
								if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity)this, str))
								{
									Toast.makeText(this, "请到应用程序管理里面打开相机权限", Toast.LENGTH_SHORT).show();
								}
							}
						}

						if(Manifest.permission.RECORD_AUDIO.equals(str))
						{
							if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
							{
								flag2 = true;
							}
							else
							{
								flag2 = false;
								if(!ActivityCompat.shouldShowRequestPermissionRationale((Activity)this, str))
								{
									Toast.makeText(this, "请到应用程序管理里面打开录音权限", Toast.LENGTH_SHORT).show();
								}
							}
						}
						if(flag1 && flag2)
						{
							openVideoCapture(this, intent);
						}
						else
						{
							HashMap<String, Object> params = null;
							BootImgRes res = AdMaster.getInstance(MyFramework2App.getInstance().getApplicationContext()).GetOneLocalBootImgRes();
							if(res != null)
							{
								params = new HashMap<String, Object>();
								params.put("img", res);
							}
							SITE_Open(this, true, BootImgPageSite.class, params, Framework2.ANIM_NONE);
						}
					}

					PermissionUtils.setmOpPermissions(permissions);
				}
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
