package cn.poco.system;

public class FolderMgr
{
	private static FolderMgr sInstance;

	public synchronized static FolderMgr getInstance()
	{
		if(sInstance == null)
		{
			sInstance = new FolderMgr();
		}
		return sInstance;
	}

	/**
	 * SD card
	 */
	//素材中心
	public final String RESOURCE_TEMP_PATH = SysConfig.GetAppPath() + "/appdata/resource/temp";
	public final String RESOURCE_FRAME_PATH = SysConfig.GetAppPath() + "/appdata/resource/frame";
	public final String RESOURCE_THEME_PATH = SysConfig.GetAppPath() + "/appdata/resource/theme";
	public final String RESOURCE_TEXT_PATH = SysConfig.GetAppPath() + "/appdata/resource/text";
	public final String RESOURCE_MUSIC_PATH = SysConfig.GetAppPath() + "/appdata/resource/music";
	public final String RESOURCE_VIDEO_TEXT_PATH = SysConfig.GetAppPath() + "/appdata/resource/videotext";
	public final String RESOURCE_LIGHT_EFFECT_PATH = SysConfig.GetAppPath() + "/appdata/resource/light_effect";
	public final String RESOURCE_FONT_PATH = SysConfig.GetAppPath() + "/appdata/resource/font";
	public final String RESOURCE_MY_LOGO_PATH = SysConfig.GetAppPath() + "/appdata/resource/my_logo";
	public final String RESOURCE_APP_MARKET_PATH = SysConfig.GetAppPath() + "/appdata/resource/app_market";
	public final String RESOURCE_FILTER_PATH = SysConfig.GetAppPath() + "/appdata/resource/filter";

	//其他资源
	public final String OTHER_PATH = SysConfig.GetAppPath() + "/appdata/other";

	//解锁
	public final String LOCK_PATH = SysConfig.GetAppPath() + "/appdata/lock";

	//banner
	public final String BANNER_PATH = SysConfig.GetAppPath() + "/appdata/banner";

	//business
	public final String BUSINESS_PATH = SysConfig.GetAppPath() + "/appdata/business/.nomedia";

	//运行时图片缓存
	public final String IMAGE_CACHE_PATH = SysConfig.GetAppPath() + "/appdata/rcache";

	/**
	 * data/data
	 * 
	 * cache
	 * files
	 * databases
	 */
	public static final String VIDEO_LOCAL_PATH = "videopulgin";

	/*
	 * 以下是SharedPreferences文件名
	 */
	public static final String SETTING_SP_NAME = "setting_sp"; //设置页面
	public static final String SYSTEM_CONFIG_SP_NAME = "system_config_sp"; //系统配置
	public static final String OTHER_CONFIG_SP_NAME = "other_config_sp"; //其他配置
	public static final String NEW_RES_FLAG_SP_NAME = "new_res_flag_sp"; //各种资源的new标志

	//用户信息
	public final String USER_INFO_TEMP = SysConfig.GetAppPath() + "/appdata/userinfo/temp";
	public final String USER_INFO = SysConfig.GetAppPath() + "/appdata/userinfo";

	public boolean IsCachePath(String path)
	{
		boolean out = false;

		if(path != null)
		{
			out = path.contains(IMAGE_CACHE_PATH);
		}

		return out;
	}
}
