package cn.poco.appmarket.site;

import android.content.Context;

import cn.poco.appmarket.MarketPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

public class MarketPageSite extends BaseSite
{

	public MarketPageSite()
	{
		super(SiteID.APP_MARKET);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new MarketPage(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
	}
}
