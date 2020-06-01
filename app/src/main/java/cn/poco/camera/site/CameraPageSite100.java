package cn.poco.camera.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.album2.site.AlbumSite100;
import cn.poco.beautify.site.BeautifyPageSite100;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.interphoto2.PocoCamera;
import cn.poco.interphoto2.site.activity.MainActivitySite;

/**
 * 第三方调用-拍照
 */
public class CameraPageSite100 extends CameraPageSite {
	@Override
	public void OnTakePicture(Context context, HashMap<String, Object> params) {
		//activity级跳转交给activity的site
		if(context instanceof PocoCamera)
		{
			MainActivitySite site = ((PocoCamera)context).getActivitySite();
			if(site != null)
			{
				site.OnTakePicture(context, m_inParams, params);
			}
		}
	}

	@Override
	public void OnPickPhoto(Context context, HashMap<String, Object> params) {
		if (params == null) {
			params = new HashMap<>();
		}
		MyFramework.CopyExternalCallParams(m_inParams, params);
		MyFramework.SITE_Open(context, AlbumSite100.class, params, Framework2.ANIM_TRANSLATION_LEFT);
	}

	/**
	 * @param params imgs ImageFile2
	 */
	public void openBeautyPage(Context context, HashMap<String, Object> params) {
		MyFramework.CopyExternalCallParams(m_inParams, params);
		params.put("other_call", true);
		MyFramework.SITE_Open(context, BeautifyPageSite100.class, params, Framework2.ANIM_TRANSITION);
	}
}