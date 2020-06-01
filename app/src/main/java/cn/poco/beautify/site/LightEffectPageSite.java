package cn.poco.beautify.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.MaterialMgr2.site.ThemePageSite2;
import cn.poco.beautify.page.LightEffectPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * 光效
 */
public class LightEffectPageSite extends BaseSite
{
	public LightEffectPageSite()
	{
		super(SiteID.BEAUTY_LIGHTEFFECT);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new LightEffectPage(context, this);
	}


	public void onBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Back(context, params, Framework2.ANIM_NONE);
	}

	//打开动画提示页面
	public void OpenHelpPage(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, LightEffectHelpAnimSite.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	/*public void OnMasterIntroPage(HashMap<String, Object> params)
	{
		MyFramework.SITE_Popup(PocoCamera.main, MasterFilterIntroPageSite.class, params, Framework.ANIM_TRANSLATION_TOP);
	}*/

	public void OnRecommend(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, ThemeIntroPageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnDownloadMore(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, ThemePageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
