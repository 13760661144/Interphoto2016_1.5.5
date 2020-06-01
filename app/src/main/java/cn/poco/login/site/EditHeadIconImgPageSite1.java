package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;
import cn.poco.login.userinfo.EditHeadIconImgPage;

/**
 * 个人中心头像进入
 */
public class EditHeadIconImgPageSite1 extends EditHeadIconImgPageSite
{
	@Override
	public void upLoadSuccess(Context context,String headUrl)
	{
		HashMap<String, Object> datas = new HashMap<>();
		datas.put(EditHeadIconImgPage.KEY_HEAD_URL, headUrl);
		MyFramework.SITE_BackTo(context, UserInfoPageSite.class, datas, Framework2.ANIM_NONE);
	}
}