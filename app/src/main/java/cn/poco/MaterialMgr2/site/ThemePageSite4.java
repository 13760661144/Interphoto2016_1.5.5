package cn.poco.MaterialMgr2.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

/**
 * Created by: fwc
 * Date: 2017/6/8
 * 视频更多 ->水印-> 素材中心
 */
public class ThemePageSite4 extends ThemePageSite {

	@Override
	public void OnBack(HashMap<String, Object> params, Context context) {
		MyFramework.SITE_Back(context, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void OpenIntroPage(HashMap<String, Object> params, Context context) {
		MyFramework.SITE_Popup(context, ThemeIntroPageSite5.class, params, Framework2.ANIM_NONE);
	}
}
