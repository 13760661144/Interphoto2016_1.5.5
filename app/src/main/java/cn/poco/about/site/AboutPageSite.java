package cn.poco.about.site;

import android.content.Context;

import cn.poco.about.AboutPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

public class AboutPageSite extends BaseSite
{
	public AboutPageSite()
	{
		super(SiteID.ABOUT);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new AboutPage(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
