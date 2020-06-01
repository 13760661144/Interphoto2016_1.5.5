package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.PhotoPicker.site.PhotoPickerPageSite5;
import cn.poco.camera.site.CameraPageSite1;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.home.site.HomePageSite;
import cn.poco.login.LoginPage;
import cn.poco.login.LoginPageInfo;
import cn.poco.login.userinfo.UserInfoPage;

/**
 * 侧边栏进入个人中心
 */
public class UserInfoPageSite extends BaseSite
{
	@Override
	public IPage MakePage(Context context)
	{
		return new UserInfoPage(context, this);
	}

	public UserInfoPageSite()
	{
		super(SiteID.USERINFO);
	}

	public void onCamera(Context context,LoginPageInfo info)
	{
		HashMap<String, Object> datas = new HashMap<String, Object>();
        datas.put("isTakeOneThenExits", true);
        datas.put("isHideAlbum", true);
		datas.put(LoginPage.KEY_INFO, info);
		MyFramework.SITE_Popup(context, CameraPageSite1.class, datas, Framework2.ANIM_NONE);
	}

	public void onChooseHeadBmp(Context context,LoginPageInfo info)
	{
		HashMap<String, Object> datas = new HashMap<>();
		datas.put(LoginPage.KEY_INFO, info);
		MyFramework.SITE_Popup(context, PhotoPickerPageSite5.class, datas, Framework2.ANIM_NONE);

	}

	public void onExit(Context context)
	{
		MyFramework.SITE_BackTo(context, HomePageSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void onBack(Context context)
	{
		MyFramework.SITE_BackTo(context, HomePageSite.class, null, Framework2.ANIM_TRANSLATION_LEFT);
	}

	public void toLoginPage(Context context)
	{
		HashMap<String, Object> params = new HashMap<>();
		if(m_inParams != null )
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Open(context,LoginPageSite3.class, params, Framework2.ANIM_NONE);
	}

}
