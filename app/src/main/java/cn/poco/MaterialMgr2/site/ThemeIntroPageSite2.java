package cn.poco.MaterialMgr2.site;

import android.content.Context;

import java.util.HashMap;

/**
 * 美化（滤镜、裁剪、文字）点击推荐位
 */
public class ThemeIntroPageSite2 extends ThemeIntroPageSite
{
	@Override
	public void OnResourceUse(HashMap<String, Object> params, Context context)
	{
		OnBack(params,context);
	}
}
