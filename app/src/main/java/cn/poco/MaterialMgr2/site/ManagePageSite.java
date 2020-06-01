package cn.poco.MaterialMgr2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.MaterialMgr2.ManagePage;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;

/**
 * Created by admin on 2016/9/9.
 */
public class ManagePageSite extends BaseSite
{
	public ManagePageSite()
	{
		super(SiteID.MANAGE);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new ManagePage(context, this);
	}

	public void OnBack(HashMap<String, Object> params,Context context)
	{
		MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
