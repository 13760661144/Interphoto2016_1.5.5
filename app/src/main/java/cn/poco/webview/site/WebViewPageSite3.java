package cn.poco.webview.site;

import android.content.Context;

import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.webview.WebViewPage1;

public class WebViewPageSite3 extends WebViewPageSite
{
	@Override
	public IPage MakePage(Context context)
	{
		return new WebViewPage1(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnClose(Context context)
	{
		MyFramework.SITE_ClosePopup(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
