package cn.poco.webview.site;

import android.content.Context;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.home.site.HomePageSite;

public class WebViewPageSite100 extends WebViewPageSite3
{
	@Override
	public void OnBack(Context context)
	{
		MyFramework.SITE_Open(context, true, HomePageSite.class, null, Framework2.ANIM_NONE);
	}

	@Override
	public void OnClose(Context context)
	{
		MyFramework.SITE_Open(context, true, HomePageSite.class, null, Framework2.ANIM_NONE);
	}
}
