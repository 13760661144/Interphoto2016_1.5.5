package cn.poco.share.site;

import android.content.Context;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.home.site.HomePageSite;
import cn.poco.share.SharePage;

public class SharePageSite extends BaseSite
{
	public SharePageSite()
	{
		super(SiteID.SHARE);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new SharePage(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OnHome(Context context)
	{
		MyFramework.SITE_Open(context, HomePageSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
