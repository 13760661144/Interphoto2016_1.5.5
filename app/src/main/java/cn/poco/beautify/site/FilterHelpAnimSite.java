package cn.poco.beautify.site;

import android.content.Context;

import cn.poco.beautify.animations.FilterHelpAnim;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * 滤镜
 */
public class FilterHelpAnimSite extends BaseSite
{
	public FilterHelpAnimSite()
	{
		super(SiteID.FILTER_HELP_ANIM);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new FilterHelpAnim(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
	}
}
