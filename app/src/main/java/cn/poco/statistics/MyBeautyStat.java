package cn.poco.statistics;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import cn.poco.framework.MyFramework2App;
import cn.poco.setting.SettingInfo;
import cn.poco.setting.SettingInfoMgr;
import cn.poco.statisticlibs.BeautyStat;
import cn.poco.tianutils.CommonUtils;

/**
 * Created by Raining on 2017/9/8.
 * 神策统计
 */

public class MyBeautyStat extends BeautyStat
{
	public synchronized static void checkLogin(Context context)
	{
		final SettingInfo settingInfo = SettingInfoMgr.GetSettingInfo(context);
		if(settingInfo != null)
		{
			String userId = settingInfo.GetPoco2Id(true);
			if(userId != null && userId.length() > 0)
			{
				MyBeautyStat.onLogin(settingInfo.GetPoco2Id(true), settingInfo.GetPoco2Sex(),
									 settingInfo.GetPoco2BirthdayYear(), settingInfo.GetPoco2BirthdayMonth(),
									 settingInfo.GetPoco2BirthdayDay(), settingInfo.GetPoco2Phone(),
									 settingInfo.GetPoco2RegisterTime(),
									 MyFramework2App.getInstance().GetLastRunTime() + "",
									 MyFramework2App.getInstance().GetFirstRunTime());
			}
			else
			{
				MyBeautyStat.onLogout(MyFramework2App.getInstance().GetLastRunTime() + "", MyFramework2App.getInstance().GetFirstRunTime());
			}
		}
	}

	public static void onLogin(String userId, String sex, String year, String month, String day, String phone, String registerTime, String lastRunTime, long firstRunTime)
	{
		BeautyStat.onLogin(userId);
		try
		{
			JSONObject properties = new JSONObject();
			if(userId != null && userId.length() > 0)
			{
				properties.put("userid", userId);
			}
			if(sex != null && sex.length() > 0)
			{
				properties.put("sex", sex);
			}
			if(year != null && month != null && day != null)
			{
				if(month.length() == 1)
				{
					month = "0" + month;
				}
				if(day.length() == 1)
				{
					day = "0" + day;
				}
				properties.put("year", year + "-" + month + "-" + day);
			}
			if(phone != null && phone.length() > 0)
			{
				properties.put("phone", phone);
			}
			if(registerTime != null && registerTime.length() > 0)
			{
				try
				{
					long time = Long.parseLong(registerTime) * 1000;
					Date date = new Date(time);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
					String dateStr = sdf.format(date);
					properties.put("register_time", dateStr);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			if(lastRunTime != null && lastRunTime.length() > 0)
			{
				try
				{
					long time = Long.parseLong(lastRunTime);
					Date date = new Date(time);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
					String dateStr = sdf.format(date);
					properties.put("last_active", dateStr);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			if(firstRunTime > 0)
			{
				try
				{
					Date date = new Date(firstRunTime);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
					String dateStr = sdf.format(date);
					properties.put("$first_visit_time", dateStr);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			String imei = CommonUtils.GetIMEI(MyFramework2App.getInstance().getApplicationContext());
			if(imei != null && imei.length() > 0)
			{
				properties.put("only_key", imei);
			}
			properties.put("brand", Build.BRAND);
			properties.put("model", Build.MODEL);
			SensorsDataAPI.sharedInstance().profileSet(properties);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static void onLogout(String lastRunTime, long firstRunTime)
	{
		BeautyStat.onLogout();
		try
		{
			JSONObject properties = new JSONObject();
			if(lastRunTime != null && lastRunTime.length() > 0)
			{
				try
				{
					long time = Long.parseLong(lastRunTime);
					Date date = new Date(time);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
					String dateStr = sdf.format(date);
					properties.put("last_active", dateStr);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			if(firstRunTime > 0)
			{
				try
				{
					Date date = new Date(firstRunTime);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
					String dateStr = sdf.format(date);
					properties.put("$first_visit_time", dateStr);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
			String imei = CommonUtils.GetIMEI(MyFramework2App.getInstance().getApplicationContext());
			if(imei != null && imei.length() > 0)
			{
				properties.put("only_key", imei);
			}
			properties.put("brand", Build.BRAND);
			properties.put("model", Build.MODEL);
			SensorsDataAPI.sharedInstance().profileSet(properties);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 点击统计
	 *
	 * @param resId 资源id
	 */
	public static void onClickByRes(int resId)
	{
		Context context = MyFramework2App.getInstance().getApplicationContext();
		if(context != null)
		{
			onClick(context.getResources().getString(resId));
		}
	}

	/**
	 * 页面开始
	 *
	 * @param resPageId 页面资源id
	 */
	public static void onPageStartByRes(int resPageId)
	{
		Context context = MyFramework2App.getInstance().getApplicationContext();
		if(context != null)
		{
			onPageStart(context.getResources().getString(resPageId));
		}
	}

	/**
	 * 页面结束
	 *
	 * @param resPageId 页面资源id
	 */
	public static void onPageEndByRes(int resPageId)
	{
		Context context = MyFramework2App.getInstance().getApplicationContext();
		if(context != null)
		{
			onPageEnd(context.getResources().getString(resPageId));
		}
	}

	/**
	 * 获取默认配置
	 */
	public static Config getDefaultConfig(Application app)
	{
		Config config = new Config();
		config.app = app;
		config.serverURL = "http://tj.adnonstop.com:8106/sa?project=yx_project";
		config.configureUrl = "http://tj.adnonstop.com:8106/config/?project=yx_project";
		config.debugMode = SensorsDataAPI.DebugMode.DEBUG_OFF;
		return config;
	}

	public static class MgrInfo
	{
		public String id;
		public int alpha;
	}

	public static class MgrInfo1
	{
		public String type;
		public String material_id;
	}


	public static void onUseClip(String clipRatio, boolean rotate, boolean adjustH, boolean adjustV)
	{
		try
		{
			JSONObject properties = new JSONObject();
			properties.put("pcrop_mode", clipRatio);
			properties.put("pcrop_rotate", rotate);
			properties.put("pcrop_hori", adjustH);
			properties.put("pcrop_vert", adjustV);
			onClick("p_crop", properties);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static void onUseFilter(ArrayList<MgrInfo> adjustInfos, boolean hasCurve, String filterId, int fliterAlpha, int pageResId)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{

				JSONObject properties;
				properties = new JSONObject();
				properties.put("filter_id", filterId);
				properties.put("opaqueness", fliterAlpha);
				if(adjustInfos != null)
				{
					for(MgrInfo info : adjustInfos)
					{
						properties.put(info.id, info.alpha);
					}
				}
				if(hasCurve) properties.put("curve", hasCurve);
				onClick("p_filter", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static void onUseEffect(ArrayList<MgrInfo> adjustInfos, String effectId, int effectAlpha, int pageResId)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{

				JSONObject properties;

				properties = new JSONObject();
				properties.put("lighthit_id", effectId);
				properties.put("opaqueness", effectAlpha);
				if(adjustInfos != null)
				{
					for(MgrInfo info : adjustInfos)
					{
						properties.put(info.id, info.alpha);
					}
				}
				onClick("p_lighthit", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static void onUseText(String textId, int textColor, int textShadow)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{

				JSONObject properties;

				properties = new JSONObject();
				properties.put("watermark_id", textId);
				properties.put("text_color", Integer.toHexString(textColor));
				properties.put("text_shadow", textShadow);
				onClick("p_word", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static void onSaveToMy(String textId)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{

				JSONObject properties;

				properties = new JSONObject();
				properties.put("material_id", textId);
				onClick("save", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public enum PictureSize
	{
		PictureSize16_9竖("16_9_竖"),
		PictureSize16_9横("16_9_横"),
		PictureSize4_3("4_3"),
		PictureSize1_1("1_1"),
		PictureSize235_1("2.35_1");

		private String m_value;

		public String GetValue()
		{
			return m_value;
		}

		PictureSize(String value)
		{
			m_value = value;
		}
	}

	public static void onTakePhoto(String material_id, int alpha, PictureSize size, int pageID)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{

				JSONObject properties;

				properties = new JSONObject();
				properties.put("filter_id", material_id);
				properties.put("opaqueness", alpha);
				properties.put("Frame_size", size.GetValue());
				properties.put("pagetype", context.getResources().getString(pageID));
				onClick("camera", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public static void onVideoFilter(String material_id)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{

				JSONObject properties;

				properties = new JSONObject();
				properties.put("filter_id", material_id);
				onClick("videofilter", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public enum Segment
	{
		Segmentfree("free"),
		Segment1("1"),
		Segment2("2"),
		Segment3("3"),
		Segment4("4"),
		Segment5("5"),
		Segment6("6");

		private String m_value;

		public String GetValue()
		{
			return m_value;
		}

		Segment(String value)
		{
			m_value = value;
		}
	}

	public enum Time
	{
		Time10(10),
		Time30(30),
		Time60(60),
		Time180(180);

		private int m_value;

		public int GetValue()
		{
			return m_value;
		}

		Time(int value)
		{
			m_value = value;
		}
	}

	/**
	 *	镜头录像
	 * @param material_id 素材id
	 * @param alpha    不透明度
	 * @param size    画幅尺寸
	 * @param segment    片段数选择
	 * @param time    时长选择
	 * @param pageID    页面id
	 */
	public static void onTakeVideo(String material_id, int alpha, PictureSize size, Segment segment, Time time, int pageID)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			String tag = GetTag();
			if(context != null)
			{

				JSONObject properties;

				properties = new JSONObject();
				properties.put("filter_id", material_id);
				properties.put("opaqueness", alpha);
				properties.put("Frame_size", size.GetValue());
				properties.put("video_segment", segment.GetValue());
				properties.put("video_duration", time.GetValue());
				onClick("videotape", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public enum ClipMode
	{
		modeFree("free"),
		mode10S("10s");

		private String m_value;

		public String GetValue()
		{
			return m_value;
		}

		ClipMode(String value)
		{
			m_value = value;
		}
	}

	public static void onVideoSetting(PictureSize size, ClipMode mode)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			String tag = GetTag();
			if(context != null)
			{

				JSONObject properties;

				properties = new JSONObject();
				properties.put("Frame_size", size.GetValue());
				properties.put("Clip_mode", mode.GetValue());
				onClick("videosetting", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 点击素材的时候触发，传素材的统计id
	 * @param mgrId
	 */
	public static void onChooseMaterial(String mgrId, int pageResId)
	{
		Context context = MyFramework2App.getInstance().getApplicationContext();
		if(context != null)
		{
			try
			{
				JSONObject properties = new JSONObject();
				properties.put("material_id", mgrId);
				properties.put("pagetype", context.getResources().getString(pageResId));
				onClick("materialchoise", properties);
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 视频保存
	 * @param filter_id    滤镜统计id	缺省值"0000"
	 * @param musicId    音乐统计id	同上
	 * @param silence
	 * @param textId
	 * @param videoNum    视频个数
	 * @param videoDur    视频时长
	 * @param pageId    页面id
	 */
	public static void onVideoSave(String filter_id, String musicId, boolean silence, String textId, int videoNum, long videoDur, int pageId)
	{
		try
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{
				JSONObject properties;

				properties = new JSONObject();
				if(!TextUtils.isEmpty(filter_id)) properties.put("filter_id", filter_id);
				if(!TextUtils.isEmpty(textId)) properties.put("word_id", textId);
				if(!TextUtils.isEmpty(musicId)) properties.put("music_id", musicId);
				properties.put("sound", silence);
				properties.put("video_num", videoNum);
				properties.put("video_duration", videoDur);
				properties.put("pagetype", context.getResources().getString(pageId));
				onClick("videosave", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * 删除素材的时候触发，传素材的统计id
	 * @param mgrId
	 */
	public static void onDeleteMaterial(String mgrId, int pageResId)
	{
		Context context = MyFramework2App.getInstance().getApplicationContext();
		if(context != null)
		{
			try
			{
				JSONObject properties = new JSONObject();
				properties.put("material_id", mgrId);
				properties.put("pagetype", context.getResources().getString(pageResId));
				onClick("delete", properties);
			}
			catch(Throwable e)
			{
				e.printStackTrace();
			}
		}
	}

	public enum WatermarkLogoType
	{
		videologo_0000,
		videologo_0001,
		videologo_0002,
		videologo_0003,
		videologo_0004,
		videologo_0005,
		videologo_0006,
	}

	/**
	 * 保存水印logo
	 *
	 * @param type 主题统计id
	 */
	public static void onSaveWatermarkLogo(WatermarkLogoType type, boolean logoswitch)
	{
		try
		{
			if(type != null)
			{
				JSONObject properties = new JSONObject();
				properties.put("videologo_id", type.toString());
				properties.put("logoswitch", logoswitch);
				onClick("logouse", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}

	public enum BlogType
	{
		朋友圈,
		微信好友,
		微博,
		QQ空间,
		QQ好友,
		短信,
		facebook,
		instagram,
		twitter,
		在一起,
	}

	public static void onShareCompleteByRes(BlogType type, int resPageId)
	{
		onShareCompleteByRes(type, resPageId, null, null, null);
	}

	/**
	 * 分享成功后统计
	 *
	 * @param type      博客类型
	 * @param resPageId 页面资源id
	 */
	public static void onShareCompleteByRes(BlogType type, int resPageId, String filter_id, String music_id, String video_text_id)
	{
		if(type != null)
		{
			Context context = MyFramework2App.getInstance().getApplicationContext();
			if(context != null)
			{
				try
				{
					JSONObject properties;

					properties = new JSONObject();
					properties.put("channel", type.toString());
					properties.put("pagetype", context.getResources().getString(resPageId));
					if(!TextUtils.isEmpty(filter_id)) properties.put("filter_id", filter_id);
					if(!TextUtils.isEmpty(video_text_id))
						properties.put("lighthit_id", video_text_id);
					if(!TextUtils.isEmpty(music_id)) properties.put("music_id", music_id);
					onClick("share", properties);
				}
				catch(Throwable e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private static String IMEI = null;

	private static String GetTag()
	{
		if(IMEI == null)
		{
			IMEI = CommonUtils.GetIMEI(MyFramework2App.getInstance().getApplicationContext());
			if(IMEI == null || IMEI.length() < 4)
			{
				IMEI = UUID.randomUUID().toString();
			}
		}
		return IMEI + System.currentTimeMillis();
	}

	public enum DownloadType
	{
		全部,
		滤镜,
		光效,
		照片水印,
		照片态度,
		视频水印,
		视频创意,
	}

	public enum ButtonType
	{
		朋友圈解锁,
		五星好评解锁,
		立即下载,
		立即使用,
	}

	/**
	 *
	 * @param type	下载类型
	 * @param btnType	按钮类型
	 * @param isRecommend	是否推荐
	 * @param themeId
	 */
	public static void onDownloadRes(DownloadType type, ButtonType btnType, boolean isRecommend, String themeId)
	{
		try
		{
			if(type != null)
			{
				JSONObject properties = new JSONObject();
				if(btnType != null){
					properties.put("buttontype", btnType.toString());
				}
				if(type != null){
					properties.put("m_category", type.toString());
				}
				properties.put("isrecommend", isRecommend ? "1" : "0");
				properties.put("theme_id", themeId);
				onClick("download", properties);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}
	}
}
