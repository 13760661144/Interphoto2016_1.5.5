package cn.poco.webview.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.album2.site.AlbumSite5;
import cn.poco.camera.site.CameraPageSite3;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.login.site.LoginPageSite4;
import cn.poco.webview.WebViewPage;

public class WebViewPageSite extends BaseSite
{

	public WebViewPageSite()
	{
		super(SiteID.WEBVIEW);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new WebViewPage(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OnClose(Context context)
	{
		MyFramework.SITE_ClosePopup(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
	}

	public void OnLogin(Context context)
	{
		HashMap<String,Object> params = new HashMap<>();
		MyFramework.SITE_Popup(context, LoginPageSite4.class, params, Framework2.ANIM_NONE);
	}

	public void OnSelPhoto(Context context)
	{
		HashMap<String, Object> params = new HashMap<>();
		params.put("single_select", true);
		MyFramework.SITE_Popup(context, AlbumSite5.class, params, Framework2.ANIM_NONE);
	}

	public void OnCamera(Context context)
	{
		HashMap<String,Object> params = new HashMap<>();
		params.put("isHideAlbum", true);
		params.put("isTakeOneThenExits", true);
		MyFramework.SITE_Popup(context, CameraPageSite3.class, params, Framework2.ANIM_NONE);
	}
}
