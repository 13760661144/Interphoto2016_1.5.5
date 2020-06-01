package cn.poco.MaterialMgr2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * 美化下载更多-素材中心-素材详情
 */
public class ThemeIntroPageSite3 extends ThemeIntroPageSite
{
	@Override
	public void OnResourceUse(HashMap<String, Object> params, Context context)
	{
		boolean need_refresh = false;
		if(params.containsKey("need_refresh"))
		{
			need_refresh = (Boolean)params.get("need_refresh");
		}
		if(!need_refresh)
		{
			if(m_inParams.containsKey("need_refresh"));
			{
				need_refresh = (Boolean)m_inParams.get("need_refresh");
			}
		}
		params.put("need_refresh", need_refresh);
		MyFramework.SITE_ClosePopup2(context, params, 2, Framework2.ANIM_TRANSLATION_LEFT);
	}
}
