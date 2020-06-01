package cn.poco.beautify.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.beautify.page.MasterIntroPage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

public class MasterIntroPageSite extends BaseSite
{	
	public MasterIntroPageSite()
	{
		super(SiteID.MASTER_INTRODUCE);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new MasterIntroPage(context, this);
	}

	public void onBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_ClosePopup(context, params, Framework2.ANIM_TRANSLATION_TOP);
	}

}
