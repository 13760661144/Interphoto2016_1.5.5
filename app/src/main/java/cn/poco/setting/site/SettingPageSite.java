package cn.poco.setting.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.about.site.AboutPageSite;
import cn.poco.albumCache.site.AlbumCacheSite;
import cn.poco.camera.site.CameraPageSite;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.home.HomePage;
import cn.poco.login.site.LoginPageSite1;
import cn.poco.setting.SettingPage;
import cn.poco.webview.site.WebViewPageSite3;

public class SettingPageSite extends BaseSite
{

	public SettingPageSite()
	{
		super(SiteID.SETTING);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new SettingPage(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_ClosePopup(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OnFixCamera(Context context)
	{
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("cameraId", 0);
		params.put("patchMode", 1);
		MyFramework.SITE_Popup(context, CameraPageSite.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnAbout(Context context)
	{
		MyFramework.SITE_Popup(context, AboutPageSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnLanguage(Context context)
	{
		MyFramework.SITE_Popup(context,LanguagePageSite.class,null,Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnFeedback(Context context, HashMap<String, Object> params)
	{
		MyFramework.SITE_Popup(context, WebViewPageSite3.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void onAlbumCache(Context context) {
		MyFramework.SITE_Popup(context, AlbumCacheSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void onLogin(Context context)
	{
		HashMap<String,Object> params = new HashMap<>();
		params.put("img", HomePage.s_glassPath);
		MyFramework.SITE_Popup(context, LoginPageSite1.class, params, Framework2.ANIM_NONE);
	}
	public void onSelectWatermark(Context context){
		MyFramework.SITE_Popup(context, SelectWatermarkSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
