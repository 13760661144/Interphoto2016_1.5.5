package cn.poco.beautify.site;

import android.content.Context;

import cn.poco.beautify.animations.TextMyHelpAnim;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * 文字-我的帮助页
 */
public class TextMyHelpAnimSite extends BaseSite
{
	public TextMyHelpAnimSite()
	{
		super(SiteID.TEXT_HELP_ANIM);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new TextMyHelpAnim(context, this);
	}

	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_TRANSLATION_BOTTOM);
	}
}
