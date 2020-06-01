package cn.poco.beautify.site;

import android.content.Context;

import cn.poco.beautify.animations.TextHelpAnim;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * 文字帮助页
 */
public class TextHelpAnimSite extends BaseSite
{
	public TextHelpAnimSite()
	{
		super(SiteID.TEXT_HELP_ANIM1);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new TextHelpAnim(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
	}
}
