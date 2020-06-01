package cn.poco.PhotoPicker.site;

/**
 * Created by admin on 2016/10/25.
 */

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.login.site.EditHeadIconImgPageSite;
import cn.poco.login.userinfo.EditHeadIconImgPage;

/**
 * 注册选择头像
 */
public class PhotoPickerPageSite4 extends PhotoPickerPageSite
{
	@Override
	public void OnSelPhoto(Context context, HashMap<String, Object> params)
	{
		HashMap<String, Object> temp = (HashMap<String, Object>)m_inParams.clone();
		params.put(EditHeadIconImgPage.KEY_MODE, EditHeadIconImgPage.REGISTER);
		temp.put(EditHeadIconImgPage.KEY_IMG_PATH, MakeRotationImg((String[])params.get("imgs")));
		MyFramework.SITE_Open(context, EditHeadIconImgPageSite.class, temp, Framework2.ANIM_NONE);
	}
	public void OnBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_NONE);
	}
}
