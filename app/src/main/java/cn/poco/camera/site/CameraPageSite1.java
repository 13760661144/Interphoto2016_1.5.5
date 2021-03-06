package cn.poco.camera.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.camera.ImageFile2;
import cn.poco.camera.RotationImg2;
import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.login.site.EditHeadIconImgPageSite1;
import cn.poco.login.userinfo.EditHeadIconImgPage;

/**
 * 用户信息页拍头像
 */
public class CameraPageSite1 extends CameraPageSite {
	@Override
	public void OnTakePicture(Context context,HashMap<String, Object> params) {
		RotationImg2[] imgs = ((ImageFile2)params.get("imgs")).SaveImg2(context);
		HashMap<String,Object> datas = (HashMap<String, Object>)m_inParams.clone();
//		datas.put(EditHeadIconImgPage.KEY_FILTER_VALUE, params.get("color_filter_id"));
		datas.put(EditHeadIconImgPage.KEY_IMG_PATH,imgs);
		datas.put(EditHeadIconImgPage.KEY_MODE, EditHeadIconImgPage.OTHER);
		MyFramework.SITE_Open(context, EditHeadIconImgPageSite1.class, datas, Framework2.ANIM_NONE);
	}

	@Override
	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_NONE);
	}
}