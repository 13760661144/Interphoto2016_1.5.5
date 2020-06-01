package cn.poco.beautify.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.beautify.page.ClipPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * 裁剪
 */
public class ClipPageSite extends BaseSite
{
	public ClipPageSite()
	{
		super(SiteID.BEAUTY_CLIP);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new ClipPage(context, this);
	}

	public void onBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Back(context, params, Framework2.ANIM_NONE);
	}
}
