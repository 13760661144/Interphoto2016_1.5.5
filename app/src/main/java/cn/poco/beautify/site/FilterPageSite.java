package cn.poco.beautify.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.MaterialMgr2.site.ThemePageSite2;
import cn.poco.beautify.page.FilterPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * 滤镜
 */
public class FilterPageSite extends BaseSite
{
	public FilterPageSite()
	{
		super(SiteID.BEAUTY_FILTER);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new FilterPage(context, this);
	}

	public void onBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Back(context, params, Framework2.ANIM_NONE);
	}

	/*public void OnMasterIntroPage(HashMap<String, Object> params)
	{
		MyFramework.SITE_Popup(context, MasterFilterIntroPageSite.class, params, Framework.ANIM_TRANSLATION_TOP);
	}*/

	public void OpenHelpPage(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, BightHelpAnimPageSite.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OpenFilterHelpPage(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, FilterHelpAnimSite.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OnRecommend(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, ThemeIntroPageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnDownloadMore(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, ThemePageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
