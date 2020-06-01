package cn.poco.MaterialMgr2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 美化下载更多-素材中心
 */
public class ThemePageSite2 extends ThemePageSite
{
	public void OpenIntroPage(HashMap<String, Object> params, Context context)
	{
		MyFramework.SITE_Popup(context, ThemeIntroPageSite3.class, params, Framework2.ANIM_NONE);
	}
}
