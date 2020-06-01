package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.login.userinfo.EditHeadIconImgPage;

/**
 * 登陆或注册流程上传头像进入
 */
public class EditHeadIconImgPageSite extends BaseSite
{

	public EditHeadIconImgPageSite()
	{
		super(SiteID.CLIPHEAD);
	}
	@Override
	public IPage MakePage(Context context) {
		return new EditHeadIconImgPage(context, this);
	}

	public void upLoadSuccess(Context context,String headUrl)
	{
		HashMap<String, Object> datas = new HashMap<>();
		datas.put(EditHeadIconImgPage.KEY_HEAD_URL, headUrl);
		MyFramework.SITE_ClosePopup(context, datas, Framework2.ANIM_NONE);
	}

	public void onBack(Context context)
	{
		MyFramework.SITE_Back(context,null,Framework2.ANIM_NONE);
	}

}