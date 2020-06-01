package cn.poco.beautify.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.share.site.SharePageSite;

public class BeautifyPageSite extends BaseSite
{
	public static final String DEF_OPEN_PAGE = "def_page";
	public static final String DEF_SEL_URI = "def_uri";
	public BeautifyPageSite()
	{
		super(SiteID.BEAUTY);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new cn.poco.beautify.page.BeautifyPage(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnSave(Context context, HashMap<String, Object> params)
	{
//		MyFramework.SITE_Open(PocoCamera.main, SharePageSite.class, params, Framework.ANIM_TRANSLATION_LEFT);
		MyFramework.SITE_Popup(context, SharePageSite.class, params, Framework2.ANIM_TRANSLATION_TOP);
	}

	/*public void OpenEffectPage(HashMap<String, Object> params)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Popup(PocoCamera.main, LightEffectPageSite.class, params, Framework.ANIM_NONE);
	}

	public void OpenTextPage(HashMap<String, Object> params)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Popup(PocoCamera.main, TextPageSite.class, params, Framework.ANIM_NONE);
	}

	public void OpenFilterPage(HashMap<String, Object> params)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Popup(PocoCamera.main, FilterPageSite.class, params, Framework.ANIM_NONE);
	}

	public void OpenClipPage(HashMap<String, Object> params)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Popup(PocoCamera.main, ClipPageSite.class, params, Framework.ANIM_NONE);
	}*/
}
