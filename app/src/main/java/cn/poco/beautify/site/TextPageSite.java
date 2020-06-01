package cn.poco.beautify.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.site.ThemeIntroPageSite2;
import cn.poco.MaterialMgr2.site.ThemePageSite2;
import cn.poco.album2.site.AlbumSite2;
import cn.poco.beautify.page.TextPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.home.HomePage;
import cn.poco.login.site.LoginPageSite2;
import cn.poco.webview.site.WebViewPageSite3;

public class TextPageSite extends BaseSite
{
	public TextPageSite()
	{
		super(SiteID.BEAUTY_TEXT);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new TextPage(context, this);
	}

	public void OnHeadLink(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, WebViewPageSite3.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OnSelMyLogo(Context context)
	{
		HashMap<String, Object> params = new HashMap<>();
		params.put("single_select", true);
		MyFramework.SITE_Popup(context, AlbumSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void onBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Back(context, params, Framework2.ANIM_NONE);
	}

	//打开动画提示页面
	public void OpenMyHelpPage(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, TextMyHelpAnimSite.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OpenHelpPage(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, TextHelpAnimSite.class, params, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OnRecommend(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, ThemeIntroPageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnDownloadMore(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Popup(context, ThemePageSite2.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnLogin(Context context)
	{
		HashMap<String,Object> params = new HashMap<>();
		params.put("img", HomePage.s_glassPath);
		MyFramework.SITE_Popup(context, LoginPageSite2.class, params, Framework2.ANIM_NONE);
	}
}
