package cn.poco.login.site;

import android.content.Context;

import java.util.HashMap;

import cn.poco.framework.BaseSite;
import cn.poco.framework.IPage;
import cn.poco.framework.MyFramework;
import cn.poco.framework.SiteID;
import cn.poco.framework2.Framework2;
import cn.poco.login.RegisterLoginPage;

/**
 * 从登录页进入注册
 */
public class RegisterLoginPageSite extends BaseSite {

	public RegisterLoginPageSite()
	{
		super(SiteID.REGISTER);
	}

	@Override
	public IPage MakePage(Context context) {
		return new RegisterLoginPage(context, this);
	}

	public void fillRegisterInfo(Context context,HashMap<String,Object> params)
	{
		if(m_inParams.containsKey("img"))
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Open(context, RegisterLoginInfoPageSite.class, params, Framework2.ANIM_NONE);
	}
	public void chooseCountry(Context context)
	{
		HashMap<String, Object> params = new HashMap<>();
		if(m_inParams != null)
		{
			params.put("img", m_inParams.get("img"));
		}
		MyFramework.SITE_Popup(context, ChooseCountryAreaCodePageSite.class, params, Framework2.ANIM_NONE);
	}

	public void onBack(Context context)
	{
		MyFramework.SITE_Back(context, null, Framework2.ANIM_NONE);
	}
}
