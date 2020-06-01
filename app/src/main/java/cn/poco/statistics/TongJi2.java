package cn.poco.statistics;

import android.app.Activity;
import android.content.Context;

import cn.poco.interphoto2.PocoCamera;
import cn.poco.statisticlibs.StatUtils;
import cn.poco.tianutils.ShareData;

public class TongJi2
{
	/**
	 * 报活
	 */
	public static void IAmLive(Activity activity)
	{
		/*try
		{
			ShareData.InitData(activity);
			MyStatService.PushIAmLiveCount(activity, MyStatService.class, ShareData.m_screenWidth, ShareData.m_screenHeight);
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	/**
	 * 通过XML资源获取统计id
	 *
	 * @param resId 资源id
	 */
	public static void AddCountByRes(final Context context, final int resId)
	{
		/*try
		{
			String value = context.getString(resId);
			if(value != null && value.length() > 0)
			{
				MyStatService.PushOfflineCount2(context, MyStatService.class, value, null, StatUtils.TYPE_OFF_CLICK);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	/**
	 * 直接统计id
	 *
	 * @param id 真实的统计id
	 */
	public static void AddCountById(final String id)
	{
		/*try
		{
			if(PocoCamera.main != null && id != null && id.length() > 0)
			{
				MyStatService.PushOfflineCount2(PocoCamera.main, MyStatService.class, id, null, StatUtils.TYPE_OFF_CLICK);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	/**
	 * 直接统计id
	 *
	 * @param id  真实的统计id
	 * @param res 资源统计id
	 */
	public static void AddCountById(final String id, final String res)
	{
		/*try
		{
			if(PocoCamera.main != null && id != null && id.length() > 0)
			{
				MyStatService.PushOfflineCount2(PocoCamera.main, MyStatService.class, id, res, StatUtils.TYPE_OFF_CLICK);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	public static void AddOnlineClickCount(String res, String event, String page)
	{
		/*try
		{
			if(PocoCamera.main != null)
			{
				MyStatService.PushOnlineClickCount(PocoCamera.main, MyStatService.class, res, event, page);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	public static void AddOnlineSaveCount(String product, String res, String event)
	{
		/*try
		{
			if(PocoCamera.main != null)
			{
				MyStatService.PushOnlineSaveCount(PocoCamera.main, MyStatService.class, product, res, event);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	public static void AddOnlineDeadCount(Activity activity, String page)
	{
		/*try
		{
			if(PocoCamera.main != null)
			{
				ShareData.InitData(activity);
				MyStatService.PushOnlineDeadCount(PocoCamera.main, MyStatService.class, ShareData.m_screenWidth, ShareData.m_screenHeight, page);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	public static void StartPage(final Context context, final String key)
	{
		/*try
		{
			if(key != null && key.length() > 0)
			{
				MyStatService.PushOfflineCount2(context, MyStatService.class, key, null, StatUtils.TYPE_OFF_PAGE, StatUtils.AT_IN_PAGE);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	public static void EndPage(final Context context, final String key)
	{
		/*try
		{
			if(key != null && key.length() > 0)
			{
				MyStatService.PushOfflineCount2(context, MyStatService.class, key, null, StatUtils.TYPE_OFF_PAGE, StatUtils.AT_OUT_PAGE);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	public static void OnResume(final Context context, final String key)
	{
		/*try
		{
			if(key != null && key.length() > 0)
			{
				MyStatService.PushOfflineCount2(context, MyStatService.class, key, null, StatUtils.TYPE_OFF_PAGE, StatUtils.AT_IN_APP);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}

	public static void OnPause(final Context context, final String key)
	{
		/*try
		{
			if(key != null && key.length() > 0)
			{
				MyStatService.PushOfflineCount2(context, MyStatService.class, key, null, StatUtils.TYPE_OFF_PAGE, StatUtils.AT_OUT_APP);
			}
		}
		catch(Throwable e)
		{
			e.printStackTrace();
		}*/
	}
}
