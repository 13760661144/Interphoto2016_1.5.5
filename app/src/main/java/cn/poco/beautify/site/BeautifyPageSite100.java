package cn.poco.beautify.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.site.activity.MainActivitySite;

/**
 * 第三方调用 -相机-美化
 */
public class BeautifyPageSite100 extends BeautifyPageSite
{
	@Override
	public void OnSave(Context context, HashMap<String, Object> params)
	{
		//activity级跳转交给activity的site
		if(context instanceof PocoCamera)
		{
			MainActivitySite site = ((PocoCamera)context).getActivitySite();
			if(site != null)
			{
				site.OnSave(context, m_inParams, params);
			}
		}
	}
}