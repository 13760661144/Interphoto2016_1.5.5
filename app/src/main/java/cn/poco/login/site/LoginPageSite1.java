package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.MyFramework;
import cn.poco.framework2.Framework2;

//侧栏->设置->登陆
public class LoginPageSite1 extends LoginPageSite
{
	public void onBack(Context context)
	{
//		MyFramework.SITE_BackTo(PocoCamera.main, SettingPageSite.class, null, MyFramework.ANIM_NONE);
		MyFramework.SITE_ClosePopup(context,null, Framework2.ANIM_NONE);
	}

	public void loginSuccess(Context context)
	{
//		MyFramework.SITE_BackTo(context, SettingPageSite.class, null, MyFramework.ANIM_NONE);
		MyFramework.SITE_ClosePopup(context,null, Framework2.ANIM_NONE);
	}

	@Override
	public void LosePsw(Context context,HashMap<String, Object> params) {
		if(m_inParams.containsKey("img"))
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Popup(context, ResetLoginPswPageSite1.class, params, Framework2.ANIM_NONE);
	}
}
