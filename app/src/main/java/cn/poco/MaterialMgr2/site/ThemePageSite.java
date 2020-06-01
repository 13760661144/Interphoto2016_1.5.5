package cn.poco.MaterialMgr2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.ThemePage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * 素材中心首页
 */
public class ThemePageSite extends BaseSite
{
	public ThemePageSite()
	{
		super(SiteID.THEME_PAGE);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new ThemePage(context, this);
	}

	public void OnBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OpenManagePage(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, ManagePageSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OpenIntroPage(HashMap<String, Object> params, Context context)
	{
		MyFramework.SITE_Popup(context, ThemeIntroPageSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
