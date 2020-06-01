package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.PhotoPicker.PhotoPickerPage;
import cn.poco.PhotoPicker.site.PhotoPickerPageSite4;
import cn.poco.camera.site.CameraPageSite2;
import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.login.RegisterLoginInfoPage;

/**
登陆->注册 ->
 */
public class RegisterLoginInfoPageSite extends BaseSite
{

	public RegisterLoginInfoPageSite()
	{
		super(SiteID.REGISTER_DETAIL);
	}

	@Override
	public IPage MakePage(Context context)
	{
		return new RegisterLoginInfoPage(context, this);
	}

	public void fillInfoSuccess(Context context)
	{
		HashMap<String,Object> params = new HashMap<>();
//		MyFramework.SITE_BackTo(context, HomePageSite.class, params, Framework.ANIM_NONE);
		MyFramework.SITE_ClosePopup2(context, params,2,  Framework2.ANIM_NONE);
	}

	public void uploadHeadImg(Context context)
	{
		HashMap<String, Object> params = (HashMap<String, Object>)m_inParams.clone();
		params.put("mode", PhotoPickerPage.MODE_SINGLE);
		MyFramework.SITE_Popup(context, PhotoPickerPageSite4.class, params, Framework2.ANIM_NONE);
	}

	public void onBack(Context context)
	{
//		MyFramework.SITE_Back(context , null, MyFramework.ANIM_NONE);
		MyFramework.SITE_ClosePopup2(context, null,2, Framework2.ANIM_NONE);
	}

	public void onCamera(Context context)
	{
		HashMap<String, Object> datas = (HashMap<String, Object>)m_inParams.clone();
		datas.put("isTakeOneThenExits", true);
		datas.put("isHideAlbum", true);
		MyFramework.SITE_Popup(context, CameraPageSite2.class, datas, Framework2.ANIM_NONE);
	}
}
