package cn.poco.webview.site;

import android.content.Context;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 设置页调用，主要是返回动画
 */
public class WebViewPageSite1 extends WebViewPageSite
{
	@Override
	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
